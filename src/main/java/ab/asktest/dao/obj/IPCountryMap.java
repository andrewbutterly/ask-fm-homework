package ab.asktest.dao.obj;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * IP to Country Map
 * 
 * As remote Web Service for looking up Country based in IP is unreliable, best to have a local version.
 * Code will search local version first, then look remotely if no result found. Local version will be created then. 
 * 
 * Also, the local lookup queries:
 * 1: can be LRU cached in memory (2LC, or Redis/etc)
 * 2: will be faster over time than the remote lookup
 * 3: Will reduce dependence on external services
 * 
 * Assumption: over even short timeframes, many users / unique IPs will ask many questions
 * 
 * @author andrewb
 * @version 0.0.1
 * */
@Entity
/* uniqueConstraints, indexes, etc. defined in schema file*/
@Table(name = "IPCountryMap")
public class IPCountryMap extends SuperEntity{

	/*ipv6 max len is 45 chars*/
	@Column(nullable = false, length=45)
	private String ipAddress;
	
	/*ISO 2-letter country code*/
	@Column(nullable = false, length=2)
	private String countryCode;

	public IPCountryMap(long id, Timestamp createdDate,
			Timestamp modifiedDate, long version,
			String ipAddress, String countryCode) {
		super(id, createdDate, modifiedDate, version);
		this.ipAddress = ipAddress;
		this.countryCode = countryCode;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}
}
