package ab.asktest.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import ab.asktest.controller.util.QuestionHelper;
import ab.asktest.dao.APIRepository;
import ab.asktest.dao.obj.WordBlackList;

/**
 * QuestionControllerEdgeCasesTest - another test class for main API servlet
 *
 * Tests some more complicated cases:
 * 1: Question text "too large" - larger than general.maxQuestionLength in the properties file
 * 2: Question containing blacklisted words
 * 3: Posting from same country more than ${maxQPerSecondPerCountry} times per second.
 * 
 * @author andrewb
 * @version 0.0.1
 * */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebIntegrationTest("server.port:0")
public class QuestionControllerEdgeCasesTest {

	@Autowired
	QuestionHelper questionHelper;	
	@Autowired
	private APIRepository apiRepository;
	
	RestTemplate restTemplate = new TestRestTemplate();
	
	@Value("${local.server.port}")
    int port;
    private static final String ASK_URL = "/ask";
    
    public String getFullURL(String path){
    	return "http://localhost:"+port+path;
    }
	
	/**
     * testing asking a question that is bigger than the max size provided by the 
     * application properties general.maxQuestionLength parameter
     * */
    @Test
    public void testBadPostAskQuestionTooBig(){
    	int maxLen = questionHelper.maxQuestionLength+1;
    	StringBuilder buf = new StringBuilder(maxLen);
    	for(int i=0;i<maxLen;i++){
    		buf.append("a");//not the most original question :)
    	}
    	
    	HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(QuestionControllerTest.generateJSON(buf.toString()), headers);
        ResponseEntity<String> result = restTemplate.exchange(getFullURL(ASK_URL), HttpMethod.POST, entity, String.class);            
    	assertEquals("QuestionControllerEdgeCasesTest: ", HttpStatus.BAD_REQUEST, result.getStatusCode());
    	
    }
    /**
     * Test: testing strings with blacklisted words taken from the data.sql data store pre-population file
     * Note: brittle test? requires test content in the test db to run!
     * */
    @Test
    public void testBadPostAskQuestionBlackListedWords(){
    	    
    	List<WordBlackList> allBlackListWords = apiRepository.getAllWordBlackLists();

    	for(WordBlackList blackListWord:allBlackListWords){
    		
    		String question = "This is a message about "+blackListWord.getWord();
    		
    		HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<String>(QuestionControllerTest.generateJSON(question), headers);
            ResponseEntity<String> result = restTemplate.exchange(getFullURL(ASK_URL), HttpMethod.POST, entity, String.class);            
        	assertEquals("QuestionControllerEdgeCasesTest: ", HttpStatus.BAD_REQUEST, result.getStatusCode());
    		
    	}
    	
    }
	
	/**
     * testing the throttling of questions per second in the database
     * */ 
    @Test
    public void testMaxQuestionsPerSecondPerCountryPostAsk(){
    	String dupe = "A Nice question4";
    	    	    
    	//set MAX Questions per second, per country to a nice low level 
    	//to make testing it easy here 
    	questionHelper.maxQPerSecondPerCountry = 1;    
    	/*and wait for twice that for the last tests in this file to not count towards this one*/
    	try{
    		Thread.sleep(2000);
    	}catch(InterruptedException e){
    		//do nothing
    	}
    	
    	int threadCount = 100;
    	ArrayList<QuestionAsk> threads = new ArrayList<>(threadCount);    	    	
    	ExecutorService executor = Executors.newFixedThreadPool(threadCount);    	
    	for (int i=0;i<threadCount;i++) {
    		threads.add(new QuestionAsk(getFullURL(ASK_URL), QuestionControllerTest.generateJSON(dupe)));    		    		
    	}
    	for (QuestionAsk thread:threads) {
    		executor.execute(thread); 
    	}
		//give them a few seconds to finish their work 
		//or else the threads might be killed without finishing
    	try{
    		Thread.sleep(3000);
    	}catch(InterruptedException e){
    		//do nothing
    	}
    	executor.shutdown();
    	while (!executor.isTerminated()) {}//ugly but its a test method :)
    	
    	/**
    	 * Test Expectation: all but one will have failed with HTTP HttpStatus.TOO_MANY_REQUESTS
    	 */
    	int failedCount = 0;
    	int successCount = 0;
    	for (QuestionAsk thread:threads) {
    		if(HttpStatus.TOO_MANY_REQUESTS.equals(thread.result.getStatusCode())){
    			failedCount++;
    		}else if(HttpStatus.OK.equals(thread.result.getStatusCode())){
    			successCount++;
    		}    		
    	}
    	
    	if(successCount!=1 || failedCount!=(threadCount-1)){
    		fail("QuestionControllerEdgeCasesTest: threaded test with MAX posting rate failed "
    				+ "(failedCount:"+failedCount+", successCount: "+successCount+", threadCount: "+threadCount+")");
    	}    	
    	
    }
    class QuestionAsk implements Runnable{
    	ResponseEntity<String> result;
    	String url;
    	String JSON;
    	
    	public QuestionAsk(String url, String JSON){
    		this.url = url;
    		this.JSON = JSON;
    	}    	
		@Override
		public void run() {
			HttpHeaders headers = new HttpHeaders();
	        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
	        headers.setContentType(MediaType.APPLICATION_JSON);
	        HttpEntity<String> entity = new HttpEntity<String>(JSON, headers);
	        result =  restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
		}    	
    }
	
}
