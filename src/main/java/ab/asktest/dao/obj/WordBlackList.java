package ab.asktest.dao.obj;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
/**
 * Blacklisted words
 * 
 * Using the same hash technique (for faster looking) as with the Question Entity
 * 
 * Have not used the notion of words belonging to a specific language/locale for now.
 * Bad language is universal :) 
 * 
 * @author andrewb
 * @version 0.0.1
 * */
@Entity
@Table(name = "WordBlackList")
public class WordBlackList extends SuperEntity {

	public WordBlackList(long id, 
			Timestamp createdDate, Timestamp modifiedDate,
			long version,
			String word) {
		super(id, createdDate, modifiedDate, version);
		this.word = word;
	}

	//reasonable upper limit for bad-word size???
	@Column(nullable = false, length=300)
	private String word;
	
	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}
}
