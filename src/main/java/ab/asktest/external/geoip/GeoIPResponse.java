package ab.asktest.external.geoip;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * TelizeLookup GEO-IP response
 * 
 * Currently only interested in one field
 * 
 * @author andrewb
 * @version 0.0.1
 * */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeoIPResponse {
/* sample json:
	{
	"longitude":-6.2489,
	"latitude":53.3331,
	"asn":"AS6830",
	"offset":"1",
	"ip":"109.255.172.67",
	"area_code":"0",
	"continent_code":"EU",
	"dma_code":"0",
	"city":"Dublin",
	"timezone":"Europe\/Dublin",
	"region":"Dublin",
	"country_code":"IE",
	"isp":"Liberty Global Operations B.V.",
	"country":"Ireland",
	"country_code3":"IRL",
	"region_code":"07"
	}
*/
	private String country_code;

	public String getCountry_code() {
		return country_code;
	}

	public void setCountry_code(String country_code) {
		this.country_code = country_code;
	}
}
