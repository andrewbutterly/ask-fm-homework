package ab.asktest.external.geoip;

import java.net.MalformedURLException;
import java.net.URL;

import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.springframework.util.StringUtils.isEmpty;
import ab.asktest.lib.error.ApplicationException;

/**
 * GEO IP lookup service
 * 
 * Lookup service for External GEO IP tool
 * 
 * @author andrewb
 * @version 0.0.1
 * */
public class GeoIPLookup {

	private String baseURL;
	private RestTemplate restTemplate;
	
	public GeoIPLookup(String baseURL) throws ApplicationException {
		if(isEmpty(baseURL)){			
			throw new ApplicationException(ApplicationException.ERROR.BAD_INPUT, 
					"Bad external URL passed into constructor ["+baseURL+"]");
		}
		try{
			new URL(baseURL);
		}catch(MalformedURLException e){
			throw new ApplicationException(ApplicationException.ERROR.BAD_INPUT, 
					"Bad external URL passed into constructor ["+baseURL+"]", e);
		}
		
		this.baseURL = baseURL;
		this.restTemplate = new RestTemplate();
	}
	public String lookupCountryCode(String externalIP) throws ApplicationException {
		if(isEmpty(externalIP)){
			throw new ApplicationException(ApplicationException.ERROR.BAD_INPUT, 
						"Bad external IP into passed into Telize Lookup ["+externalIP+"]");
		}		
		
		//using standard GET
		GeoIPResponse page = null;
		try{
			/* RestTemplate lib is thread safe - can reuse this <restTemplate> instance and it will 
			 * create a new HTTP conn each time.
			 * 
			 * Note: 'full version' should think about pooling outgoing connections here for 
			 * reuse? Maybe a job-queue based system for questions with a max pool size 
			 * to prevent resource overuse?   
			 * */
			page = restTemplate.getForObject(baseURL+externalIP, GeoIPResponse.class);			
		}catch(RestClientException e){
			throw new ApplicationException(ApplicationException.ERROR.IO_EXCEPTION, 
					e.getMessage(), e);
		}					
		if(page==null){
			throw new ApplicationException(ApplicationException.ERROR.IO_EXCEPTION, 
					"no values returned from the lookup service");			
		}
		return page.getCountry_code();
		
	}
	
}
