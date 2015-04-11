package ab.asktest.controller;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

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

/**
 * Makes another ASK test
 * 
 * Test:
 * 1: Sends the same question twice, a few seconds apart
 * 
 * @author andrewb
 * @version 0.0.1
 * */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebIntegrationTest("server.port:0")
public class DuplicateAskTest {

	@Autowired
	QuestionHelper questionHelper;	
	
	RestTemplate restTemplate = new TestRestTemplate();
	
	@Value("${local.server.port}")
    int port;
    private static final String ASK_URL = "/ask";
    
    public String getFullURL(String path){
    	return "http://localhost:"+port+path;
    }	
	
	/**
     * simple "duplicate post within 30 seconds" test, should fail
     * */    
    @Test
    public void testDuplicateGetAsk(){
    	String dupe = "A Nice question3";
    	
    	ResponseEntity<String> result = executeGetCall(getFullURL(ASK_URL)+"/"+dupe);
    	assertEquals("DuplicateAskTest: ", HttpStatus.OK, result.getStatusCode()); 

    	//now repost the same message again with allowQuestionReposting turned off
    	questionHelper.allowQuestionReposting=false;    	
    	result = executeGetCall(getFullURL(ASK_URL)+"/"+dupe);    	
    	assertEquals("DuplicateAskTest: ", HttpStatus.NOT_MODIFIED, result.getStatusCode());    	    	
    }
    
    
    /** helpers */
    private ResponseEntity<String> executeGetCall(String uri){    	
   	 HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        return restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
   }
}
