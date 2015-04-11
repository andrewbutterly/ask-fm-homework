package ab.asktest.controller.obj;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Very simple class for encapsulating a question in JSON
 * 
 * Note: a full version would have more fields and utility
 * 
 * @author andrewb
 * @version 0.0.1
 * */
@JsonIgnoreProperties(ignoreUnknown = true)
public class JSONQuestion {

	public String question;

	public JSONQuestion(){
		super();
	}
	public JSONQuestion(String question){
		super();
		this.question = question;
	}
	
	public String getQuestion() {
		return question;
	}
	public void setQuestion(String question) {
		this.question = question;
	}
}
