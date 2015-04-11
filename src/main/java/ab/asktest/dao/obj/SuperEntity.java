package ab.asktest.dao.obj;

import java.sql.Timestamp;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
/**
 * Boilerplate superclass for entity objects
 * 
 * @author andrewb
 * @version 0.0.1
 * */
@MappedSuperclass
public class SuperEntity {

	public SuperEntity(){
		super();
	}
	public SuperEntity(long id, Timestamp createdDate,
			Timestamp modifiedDate, long version) {
		super();
		this.id = id;
		this.createdDate = createdDate;
		this.modifiedDate = modifiedDate;
		this.version = version;
	}

	@Id 
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long id;
	
	@CreatedDate
	private Timestamp createdDate;
	
	@LastModifiedDate
	private Timestamp modifiedDate;
	
	@Version
	private long version;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Timestamp getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Timestamp createdDate) {
		this.createdDate = createdDate;
	}

	public Timestamp getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Timestamp modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}
}
