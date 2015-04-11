package ab.asktest.external.geoip;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import ab.asktest.lib.error.ApplicationException;

/**
 * Test for the GeoIPLookup service
 * 
 * Tests against the live service, and has a hard coded IP, so 
 * perhaps too brittle?
 * 
 * @author andrewb
 * @version 0.0.1
 * */
public class GeoIPLookupTest {

	static String SERVICE_URL = "http://www.telize.com/geoip/";
	
	@Test
	public void testGeoIP(){
		
		try{
			new GeoIPLookup(null);
			fail();
		}catch(ApplicationException e){
			if(!ApplicationException.ERROR.BAD_INPUT.equals(e.getError())){
				fail();
			}
		}
		
		try{
			new GeoIPLookup("some non url");
			fail();
		}catch(ApplicationException e){
			if(!ApplicationException.ERROR.BAD_INPUT.equals(e.getError())){
				fail();
			}
		}
		
		try{
			GeoIPLookup lookup = new GeoIPLookup(SERVICE_URL);
			lookup.lookupCountryCode("");
			//The service should throw an error here - this is an invalid address
			fail();
		}catch(ApplicationException e){
			if(!ApplicationException.ERROR.BAD_INPUT.equals(e.getError())){
				fail();
			}
		}
		
		try{
			GeoIPLookup lookup = new GeoIPLookup(SERVICE_URL);
			lookup.lookupCountryCode("127.0.0.1");
			//The service should throw an error here - this is an invalid address
			fail();
		}catch(ApplicationException e){
			if(!ApplicationException.ERROR.IO_EXCEPTION.equals(e.getError())){
				fail();
			}
		}
		
		try{
			GeoIPLookup lookup = new GeoIPLookup(SERVICE_URL);
			lookup.lookupCountryCode("some garbage");
			//The service should throw an error here - this is an invalid address
			fail();
		}catch(ApplicationException e){
			if(!ApplicationException.ERROR.IO_EXCEPTION.equals(e.getError())){
				fail();
			}
		}
		
		try{
			GeoIPLookup lookup = new GeoIPLookup(SERVICE_URL);
			String countryCode = lookup.lookupCountryCode("109.255.172.67");
			assertTrue("GeoIPLookupTest: ", (countryCode!=null&&countryCode.length()==2));			
			assertEquals("GeoIPLookupTest: ", "ie", countryCode.toLowerCase());			
		}catch(ApplicationException e){
			fail();
		}
	}
}
