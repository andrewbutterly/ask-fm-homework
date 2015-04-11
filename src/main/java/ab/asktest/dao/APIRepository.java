package ab.asktest.dao;

import java.sql.Timestamp;
import java.util.List;

import ab.asktest.dao.obj.IPCountryMap;
import ab.asktest.dao.obj.Question;
import ab.asktest.dao.obj.WordBlackList;

/**
 * Abstraction of the Persistent store
 * 
 * @author andrewb
 * @version 0.0.1
 * */
public interface APIRepository {

	//business methods
	public List<String> getAllQuestions();	
	public List<String> findQuestionsByCountryCode(String countryCode);
	
	public List<WordBlackList> getAllWordBlackLists();
	public int noOfQuestionsAskedSince(String countryCode, Timestamp since);
	
	public IPCountryMap findCountryForIP(String IP);
	
	public void createIPCountryMap(IPCountryMap map);
	
	public List<Question> findQuestionsByIPHashSince(String ipAddress, String questionHash, Timestamp since);
	public void createQuestion(Question question);
	
}
