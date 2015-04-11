package ab.asktest.lib;

import java.security.MessageDigest;

import ab.asktest.lib.error.ApplicationException;

/**
 * Basic hash function for hasing some values stored/searched for in DB
 *
 * Uses a basic Java String HASH function.
 *  
 * Purpose of Hashing here for speed/minimizing collisions, *not security*.
 * A full version would probably use some sort of native hash library? (maybe xxHash?) Might be overkill. 
 * 
 * Hashes of questions are stored with the questions and used for speedier lookup in the db.
 * It still necessitates manual comparison on creation, but should scale out much better.
 * 
 * @author andrewb
 * @version 0.0.1
 * */
public class HashHelper {
	
	public static String hash(String input) throws ApplicationException{
		if(input==null||input.length()<1){
			return null;
		}		
		try {
			//apparently is not thread safe - has to be created each time.
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			digest.update(input.getBytes("UTF-8"));
			return new String(digest.digest(), "UTF-8");
		} catch (Exception e) {
			throw new ApplicationException(ApplicationException.ERROR.ENCODING_EXCEPTION, 
					"HashHelper: error encoding ", e);
		}
		
		
	}
	
}
