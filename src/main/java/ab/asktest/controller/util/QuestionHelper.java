package ab.asktest.controller.util;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import ab.asktest.controller.obj.JSONQuestion;
import ab.asktest.dao.APIRepository;
import ab.asktest.dao.obj.IPCountryMap;
import ab.asktest.dao.obj.Question;
import ab.asktest.external.geoip.GeoIPLookup;
import ab.asktest.lib.BlackListWordHelper;
import ab.asktest.lib.HashHelper;
import ab.asktest.lib.QuestionMutexHelper;
import ab.asktest.lib.error.ApplicationException;
import static ab.asktest.lib.BasicInputSanitiser.cleanInput;
import static org.springframework.util.StringUtils.isEmpty;

/**
 * A helper class to encapsulate the business logic in one place
 * 
 * @author andrewb
 * @version 0.0.1
 * */
public class QuestionHelper {

	public String defaultCountry;
	public int maxQPerSecondPerCountry;
	public String geoIPURL;
	//set per backing store type
	public int maxQuestionLength;	
	//allow reposting of same question from same IP within 30 seconds
	public boolean allowQuestionReposting;
	
	private static final QuestionMutexHelper muxtexHelper = new QuestionMutexHelper();
	
	@Autowired
	private APIRepository apiRepository;
	
	private Logger log;
	
	public QuestionHelper(String defaultCountry, int maxQPerSecondPerCountry,
			String geoIPURL, int maxQuestionLength, boolean allowQuestionReposting) {
		super();
		this.defaultCountry = (defaultCountry==null?defaultCountry:defaultCountry.toLowerCase());
		this.maxQPerSecondPerCountry = maxQPerSecondPerCountry;
		this.geoIPURL = (geoIPURL==null?geoIPURL:geoIPURL.trim());
		this.maxQuestionLength = maxQuestionLength;
		this.allowQuestionReposting = allowQuestionReposting;
	}
	public void setApiRepository(APIRepository apiRepository) {
		this.apiRepository = apiRepository;
	}
	public void setGeoIPLookup(GeoIPLookup geoIPLookup) {
		this.geoIPLookup = geoIPLookup;
	}

	private GeoIPLookup geoIPLookup;	
	private GeoIPLookup getGeoIP() throws ApplicationException{
		if(geoIPLookup==null){
			geoIPLookup = new GeoIPLookup(geoIPURL);
		}	
		return geoIPLookup;
	}
	private Logger getLog(){
		if(log==null){
			log = LoggerFactory.getLogger(QuestionHelper.class);	
		}
		return log;
	}
	
	private static BlackListWordHelper blHelper;
	private BlackListWordHelper getBLHelper(){
		if(blHelper==null){
			blHelper = new BlackListWordHelper(apiRepository.getAllWordBlackLists());
		}
		return blHelper;
	}
	
	/**
	 * Return all questions, or questions filtered on countryCode
	 * Will return nothing for an invalid (but *non-null*) country code
	 * 
	 * Note: Future versions of the app would:
	 * 1: scale by choosing a different data source based on country code
	 * 2: force pagination to some default
	 * 3: further limit results by applying date based limiting 
	 * 	 - i.e. only return items from last X hours, or return most recent Y questions, etc.
	 * 
	 * @param String ipAddress
	 * @param String countryCode
	 * @return List<String> List of questions
	 * */
	public List<String> listQuestions(String ipAddress, String countryCode){
		
		String input = cleanInput(countryCode, Question.COUNTRY_CODE_MAX_LEN);				
		getLog().debug("listQuestions: incoming query. countryCode: "+(input)+" from "+ipAddress);
		
		if(input==null){
			return apiRepository.getAllQuestions();
		}else{
			//will return nothing for invalid (but *non-null*) country codes.
			return apiRepository.findQuestionsByCountryCode(input.toLowerCase());
		}		
	}
	/**
	 * Create a new question
	 * 
	 * @param String ipAddress
	 * @param String question 
	 * @throws ApplicationException on bad inputs (BAD_INPUT), or duplicate questions from a user (TOO_MANY_REQUESTS)
	 * */
	public boolean askQuestion(String ipAddress, String question) throws ApplicationException{
		if(isEmpty(ipAddress)||isEmpty(question)){
			throw new ApplicationException(ApplicationException.ERROR.BAD_INPUT, "Bad Request: missing inputs");
		}			
		
		String input = cleanInput(question);								
		if (StringUtils.isEmpty(input)) {
			// return error input
			getLog().info("askQuestion: incoming question. empty question provided from "+ipAddress);
			throw new ApplicationException(ApplicationException.ERROR.BAD_INPUT, "Bad Request: invalid question provided");
		}
		if(input.length()>maxQuestionLength){
			// return error input
			getLog().info("askQuestion: incoming question. question provided was too large. size: "+(input.length())+" from "+ipAddress);			
			throw new ApplicationException(ApplicationException.ERROR.BAD_INPUT, "Bad Request: question length was too long");				
		}			
		
		getLog().debug("askQuestion: incoming question. size: "+(input.length())+" from "+ipAddress);
		
		/**
		 * Business Rules:
		 * 1: Does this question have bad words in it?
		 * 2: Get the country for this IP
		 * 3: Have too many questions been asked from this country before?
		 * 		NOTE: due to the default logic of using "lv" as the country, if the GEO-IP service is down and the local DB/cache empty, 
		 * 			  we can only create N-1 questions per second
		 *  
		 * 4: Has this question been posted from this IP address before?
		 * 		Is this a duplicate question? This is extra to original requirements - but is a good safety check.
		 * 		Users need to wait 30 seconds before re-asking the same question from the same IP address.
		 * 		This seems fair but requirement for change would have to be verified with the designer!
		 * 		*** OPTIONAL by CONFIGURATION in application.properties ***
		 * */
		
		//blacklisted word test
		if( getBLHelper().questionContainsBlackListedWord(input) ){
			throw new ApplicationException(ApplicationException.ERROR.BAD_INPUT, 
					"Bad Request: question contained blacklisted word");			
		}
		
		//get the country for this IP
		String countryCode = getCountryForIP(ipAddress);
		
		long now = System.currentTimeMillis();
				
		/**
		 * test count of Q asked in the last second for this country 
		 * requirement: reject if > N questions / second are asked from a single country'*/
		Timestamp oneSecondAgo = new Timestamp(System.currentTimeMillis()-1000);
		/*
		 * Two checks done here: 
		 * 1: check against the in-memory mutex Helper,
		 * 2: check against the database records 
		 * reason is that this instance of the application might not be the only one running!
		 * */
		if(!muxtexHelper.allowedAskQuestion(countryCode, QuestionMutexHelper.getMSinSec(oneSecondAgo.getTime()), maxQPerSecondPerCountry)){							
			throw new ApplicationException(ApplicationException.ERROR.TOO_MANY_REQUESTS, 
					"Bad Request: Request count exceeded max ["+countryCode+"]");			
		}			
		int createdSince = apiRepository.noOfQuestionsAskedSince(countryCode, oneSecondAgo); 
		if(createdSince>=maxQPerSecondPerCountry){										
			throw new ApplicationException(ApplicationException.ERROR.TOO_MANY_REQUESTS, 
					"Bad Request: Request count exceeded max ["+countryCode+"]");
		}
		
		String questionHash = HashHelper.hash(input);

		//Are we allowing re-posting of the same questions within 30 seconds? 
		if(!allowQuestionReposting){
			//has this been asked before by this IP address?
			Timestamp thirtySecondsAgo = new Timestamp(now-(30*1000));
			if(questionAskedAlready(input, questionHash, ipAddress, thirtySecondsAgo)){
				return false;
			}
		}
		
		Timestamp nowTime = new Timestamp(now);		 
		apiRepository.createQuestion(new Question(0l, nowTime, nowTime, 0l, ipAddress, input, questionHash, countryCode.toLowerCase()));

		return true;
	}
	/************************* Helper methods ************************/
	public static List<JSONQuestion> convert(List<String> questions){
		if(questions==null||questions.isEmpty()){
			return null;
		}
		ArrayList<JSONQuestion> results = new ArrayList<>(questions.size());
		for(String question:questions){
			results.add(new JSONQuestion(question));
		}
		return results;
	}
	
	
	private boolean questionAskedAlready(String question, String questionHash, String ipAddress, Timestamp since){
		
		List<Question> collisions = apiRepository.findQuestionsByIPHashSince(ipAddress, questionHash, since);
		if(collisions==null||collisions.isEmpty()){//we are good
			return false;
		}
		/* Have to do direct string comparisons. 
		 * This is slow, but the use of the IP and hash in the search should have speeded this up by 
		 * cutting out a lot of potential matches - once the dbase and posting rate goes above a certain size.
		 * 
		 * NAT means that we can't trust that one IP == one user.
		 * A future version would try to use something better than the IP address to narrow to an individual user - per browser cookies probably?
		 * */
		for(Question collision:collisions){
			if(question.equals(collision.getQuestion())){
				return true;
			}
		}
		return false;
	}
	
	private String getCountryForIP(String ipAddress) throws ApplicationException {
		IPCountryMap ip = apiRepository.findCountryForIP(ipAddress);
		if(ip!=null){//had it in the local DB / 2LC cache (if enabled)
			return ip.getCountryCode();			
		}
		try {
			//save a trip when testing - GeoIP will fail these anyway
			if("127.0.0.1".equals(ipAddress)||"localhost".equals(ipAddress)){
				return defaultCountry;
			}
			String countryCode = getGeoIP().lookupCountryCode(ipAddress);
			if(countryCode==null){//should not happen - some failure of the call?
				getLog().error("getCountryForIP: GEO-IP service returned an empty value for "+ipAddress);
				return defaultCountry;
			}
			Timestamp now = new Timestamp(System.currentTimeMillis());			
			//save this result for future use...
			apiRepository.createIPCountryMap(new IPCountryMap(0l, now, now, 0l, ipAddress, countryCode.toLowerCase()));
			return countryCode;
		} catch (ApplicationException e) {
			if(ApplicationException.ERROR.BAD_INPUT.equals(e.getError())){
				//should not happen!
				getLog().error("getCountryForIP: BAD initialization inputs provided to GEO-IP code "+ipAddress);
				throw e;
			}
			getLog().error("getCountryForIP: GEO-IP service threw an error "+e.getMessage());
			//otherwise do nothing. Some other intermittent error that can be logged 
			return defaultCountry;
		}
		
		
	}
	
}
