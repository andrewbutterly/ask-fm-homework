package ab.asktest.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import ab.asktest.controller.obj.JSONQuestion;
import ab.asktest.controller.util.QuestionHelper;
import ab.asktest.lib.error.ApplicationException;

/**
 * QuestionController Servlet for ASK and LIST API calls
 * 
 * Delegates business logic to a local Helper class.
 * 
 * @author andrewb
 * @version 0.0.1
 * */
@RestController
public class QuestionController implements ErrorController {
	
	@Autowired
	private QuestionHelper questionHelper;
	
	public QuestionHelper getQuestionHelper() {
		return questionHelper;
	}
	
	
	/**
	 * ASK Question: POST option
	 * Question passed in via HTTP body as JSON object
	 * 
	 * HTTP status codes used to indicate result
	 * 
	 * @param HttpServletRequest request
	 * @param String JSON bodyContent (optional, but will return HTTP 4** response if not set)
	 * @return JSONQuestion - but will return an empty string either way
	 * */
	@RequestMapping(value = "/ask" , method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes={MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<JSONQuestion> ask(HttpServletRequest request, @RequestBody(required=false) JSONQuestion bodyContent) {
		return askQuestion(request, bodyContent!=null?bodyContent.getQuestion():null);
	}
	/**
	 * ASK Question: GET option
	 * Question passed in via URL value:"/ask/{question}"
	 * 
	 * HTTP status codes used to indicate result
	 * 
	 * Technically we should not allow a GET to change state, but it makes the App much easier to casually use and test.
	 * 
	 * @param HttpServletRequest request
	 * @param String question (optional, but will return 400 response if not set)
	 * @return JSONQuestion - but will return an empty string either way
	 * */
	@RequestMapping(value = "/ask/{question}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONQuestion> askQuestion(HttpServletRequest request, @PathVariable("question") String question) {
		try {
			boolean created = getQuestionHelper().askQuestion(request.getRemoteAddr(), question);
			return new ResponseEntity<JSONQuestion>(created?HttpStatus.OK:HttpStatus.NOT_MODIFIED);
		} catch (Throwable th) {
			return new ResponseEntity<JSONQuestion>(interpretException(th));
		}			
	}	
	
	/**
	 * List all Questions.
	 * Note: Full version should consider pagination by default!
	 * Note: narrowing to GET only
	 * @return List<String> - list of question Strings that will be converted to JSON array
	 * */
	@RequestMapping(value = "/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<JSONQuestion>> listAll(HttpServletRequest request) {
		return list(request, null);
	}
	
	/**
	 * List all questions by country code
	 * Note: Full version should consider pagination by default!
	 * Note: narrowing to GET only
	 * @param String countryCode (optional, if not defined will search all)
	 * @return List<String> - list of question Strings that will be converted to JSON array
	 * */
	@RequestMapping(value = "/list/{countryCode}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<JSONQuestion>> list(HttpServletRequest request, @PathVariable("countryCode") String countryCode) {
		try {
			return new ResponseEntity<List<JSONQuestion>>(
					QuestionHelper.convert(getQuestionHelper().listQuestions(request.getRemoteAddr(), countryCode)), 					
					HttpStatus.OK);			
		} catch (Throwable th) {
			return new ResponseEntity<List<JSONQuestion>>(interpretException(th));
		}
	}	
	
	/*handle unsupported/Bad requests*/
	private static final String ERROR = "/error";
	
	@RequestMapping(value = ERROR, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> badRequest(HttpServletRequest request) {
		getLog().info("Bad request made ["+request.getRequestURL()+"] from "+request.getRemoteAddr());	
		return new ResponseEntity<String>("{\"message\":\"URL or Contents were not valid\"}", HttpStatus.BAD_REQUEST);
	}
	public String getErrorPath() {
		return ERROR;
	}
	
	/****************** Helper Methods *****************/
	
	/**
	 * Map application exceptions to something that can be returned to the API user
	 * @param Throwable th
	 * @return HttpStatus to return
	 * */
	private HttpStatus interpretException(Throwable th){
					
		if(th==null){//Should never happen!
			getLog().error("Null Exception thrown by application ");	
			return HttpStatus.INTERNAL_SERVER_ERROR;
		}
		if(th instanceof ApplicationException){//Can happen
			ApplicationException ex = (ApplicationException)th;
			getLog().warn("ApplicationException thrown by application " + ex.getMessage());
			switch(ex.getError()){
				case BAD_INPUT:
					return HttpStatus.BAD_REQUEST;
				case TOO_MANY_REQUESTS:	
					return HttpStatus.TOO_MANY_REQUESTS;
				default:
					return HttpStatus.INTERNAL_SERVER_ERROR;
			}
		}else{//something *unchecked* was thrown. Should never happen!	
			th.printStackTrace();
			getLog().error("Unchecked exception thrown by application " + th.getMessage());			
			return HttpStatus.INTERNAL_SERVER_ERROR;
		}
	}

	private Logger log;
	private Logger getLog(){
		if(log==null){
			log = LoggerFactory.getLogger(QuestionController.class);	
		}
		return log;
	}

}
