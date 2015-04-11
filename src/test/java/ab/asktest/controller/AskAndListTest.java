package ab.asktest.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import ab.asktest.controller.obj.JSONQuestion;

/**
 * Post/Gets a question, and then scans the listings for it
 * 
 * Test:
 * 1: send a random question, then poll the API afterwards to make sure it was created
 * 
 * @author andrewb
 * @version 0.0.1
 * */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebIntegrationTest("server.port:0")
public class AskAndListTest {

	RestTemplate restTemplate = new TestRestTemplate();
	
	@Value("${local.server.port}")
    int port;
    private static final String LIST_URL = "/list";
    private static final String ASK_URL = "/ask";
    
    public String getFullURL(String path){
    	return "http://localhost:"+port+path;
    }	   
    
	/**
     * simple ask a unique question and then poll for the result
     * */    
    @Test
    public void askQuestionAndPollForResult(){
    	
    	String question = System.currentTimeMillis()+" is a nice timely question "+System.nanoTime();
    	
    	ResponseEntity<String> askResult = restTemplate.exchange(getFullURL(ASK_URL)+"/"+question, HttpMethod.GET, null, String.class);    
    	assertEquals("AskAndListTest: ", HttpStatus.OK, askResult.getStatusCode()); 
    	
    			    	   
    	ResponseEntity<JSONQuestion[]> listResult = restTemplate.exchange(getFullURL(LIST_URL), HttpMethod.GET, null, JSONQuestion[].class);    
        assertEquals("AskAndListTest: ", HttpStatus.OK, listResult.getStatusCode()); 
        
        JSONQuestion[] jsonArray = listResult.getBody();
        
        boolean questionWasReturned = false;
        for(JSONQuestion q:jsonArray){
        	if(q!=null && q.getQuestion().equals(question)){
        		questionWasReturned = true;
        	}
        }
        
        assertTrue("AskAndListTest: ", questionWasReturned);                   
    }    
	
}
