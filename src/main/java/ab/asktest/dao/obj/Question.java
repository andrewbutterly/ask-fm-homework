package ab.asktest.dao.obj;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * Question
 * 
 * Models a question in a persistent data store.
 * 
 * Also stores a hash of the Question text for faster lookup.
 * The Hash is the Question text in lowercase and hashed using SHA-256. 
 * There will be collisions but that is fine - just using the hash search as an alternative to String search of a LOB field.
 * ASSUMPTION: Sorting through multiple hash collision entries can be done in code and still stay cheaper than the larger DB search.  
 * 
 * @author andrewb
 * @version 0.0.1
 * */
@Entity
/* uniqueConstraints, indexes, etc. defined in schema file*/
@Table(name = "Question")
public class Question extends SuperEntity{

	public static final int COUNTRY_CODE_MAX_LEN = 2;
	
	/*ipv6 max len is 45 chars*/
	@Column(nullable = false, length=45)
	private String ipAddress;
	
	/*Modeling as a LOB, should give a large enough size. 
	   It will be DB dependent though so will need to be tested.
	   So Data store choice will dictate max question size here - max Q len should be config file based? - TODO*/
	@Lob
	@Column(nullable = false)
	private String question;
	
	/* MAX hash string size will also be dependent on the *average* question 
	 * str length. how long is long enough to prevent most collisions but  
	 * short enough to still be quick for searching in our DB? TODO Needs testing
	 * */
	@Column(nullable = false, length=500)
	private String questionHash;	
	
	/*ISO 2-letter country code*/
	@Column(nullable = false, length=COUNTRY_CODE_MAX_LEN)
	private String countryCode;

	public Question(long id, Timestamp createdDate,
			Timestamp modifiedDate, long version,
			String ipAddress, String question, String questionHash,
			String countryCode) {
		super(id, createdDate, modifiedDate, version);
		this.ipAddress = ipAddress;
		this.question = question;
		this.questionHash = questionHash;
		this.countryCode = countryCode;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getQuestionHash() {
		return questionHash;
	}

	public void setQuestionHash(String questionHash) {
		this.questionHash = questionHash;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}			
}
