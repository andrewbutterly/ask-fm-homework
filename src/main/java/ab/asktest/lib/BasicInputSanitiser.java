package ab.asktest.lib;

/**
 * Quick and dirty Input Sanitiser to address basic input - HTML/JS/XSS issues, etc.
 * 
 * A real version would be more clever.
 * 
 * In a real Question asking tool, we would also probably allow a limited set of HTML - emoticons, EM, etc...
 * and no JS, etc. All we're doing here is stripping all HTML. 
 * 
 * This tool would also form the first line of spambot defense - if a question is being input, is it garbage or a real sentence?
 * Not an easy question!
 * 
 * @author andrewb
 * @version 0.0.1
 * */
public class BasicInputSanitiser {

	public static String cleanInput(String input){
		if(input==null||input.length()<1){
			return null;
		}			
		input = input.replaceAll("\\<[^>]*>","");
		return input.trim();
	} 
	public static String cleanInput(String input, int maxlen){
		if(input==null||input.length()<1){
			return null;
		}			
		input = cleanInput(input);
		return (input.length()>maxlen?input.substring(0, maxlen):input);
	}
	
}
