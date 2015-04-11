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
import ab.asktest.dao.APIRepository;

/**
 * QuestionControllerTest test class for main API servlet
 *
 * Bit of a mixed bag but covers the basic functionality
 * 
 * Tests:
 * 1: Get full listing of Questions via GET
 * 2: Get listing of Questions filtered by country via GET
 * 3: Make a GET to ASK a question, with no question content
 * 4: Make a POST to ASK a question, with no question content
 * 5: Make a GET to ASK a question, with question content
 * 6: Make a POST to ASK a question, with question content
 * 
 * @author andrewb
 * @version 0.0.1
 * */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebIntegrationTest("server.port:0")
public class QuestionControllerTest {

	@Autowired
	QuestionHelper questionHelper;	
	@Autowired
	private APIRepository apiRepository;

    RestTemplate restTemplate = new TestRestTemplate();
    
    @Value("${local.server.port}")
    int port;
    private static final String LIST_URL = "/list";
    private static final String ASK_URL = "/ask";
    
    public String getFullURL(String path){
    	return "http://localhost:"+port+path;
    }
    
    public static String generateJSON(String message){
    	return "{\"question\":\""+message+"\"}";
    }

    /**
     * simple test, should work OK
     * Note: can fail if general.maxQPerSecondPerCountry is set too low in the properties file !
     * Not a test fail per se if so :)
     * */
    @Test
    public void testGetAsk(){
    	testGetAsk("A Nice question1");          
    }    
    
    @Test
    public void testGetFullList(){
        ResponseEntity<String> result = executeGetCall(getFullURL(LIST_URL));
        assertEquals("QuestionControllerTest: ", HttpStatus.OK, result.getStatusCode());        
    }
    
    @Test
    public void testGetPartialList(){
    	ResponseEntity<String> result = executeGetCall(getFullURL(LIST_URL)+"/lv");
        assertEquals("QuestionControllerTest: ", HttpStatus.OK, result.getStatusCode());        
    }
    
    /**
     * GET http://localhost:8080/ask
     * will return a 404 error
     * only 
     * http://localhost:8080/ask/SOME QUESTION
     * exists
     * */
    @Test
    public void testBadGetAsk(){
    	//no question provided
    	ResponseEntity<String> result = executeGetCall(getFullURL(ASK_URL));
    	assertEquals("QuestionControllerTest: ", HttpStatus.BAD_REQUEST, result.getStatusCode());   
        
    }
    /**
     * POST http://localhost:8080/ask
     * with no body content will return a BAD_REQUEST error
     * */
    @Test
    public void testBadPostAsk(){
    	//no question provided
    	HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);       
        ResponseEntity<String> result = restTemplate.exchange(getFullURL(ASK_URL), HttpMethod.POST, entity, String.class);
            	
    	assertEquals("QuestionControllerTest: ", HttpStatus.BAD_REQUEST, result.getStatusCode());   
        
    }
    /**
     * simple POST test, should work OK
     * */
    @Test
    public void testPostAsk(){
    	HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(generateJSON("A Nice question2"), headers);
        ResponseEntity<String> result = restTemplate.exchange(getFullURL(ASK_URL), HttpMethod.POST, entity, String.class);
            	
    	assertEquals("QuestionControllerTest: ", HttpStatus.OK, result.getStatusCode());       
    }
        
    private void testGetAsk(String question){
    	ResponseEntity<String> result = executeGetCall(getFullURL(ASK_URL)+"/"+question);
    	assertEquals("QuestionControllerTest: ", HttpStatus.OK, result.getStatusCode());          
    }

    /** helpers */
    private ResponseEntity<String> executeGetCall(String uri){    	
   	 HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        return restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
   }
    
	
}
