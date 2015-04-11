package ab.asktest.lib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.security.MessageDigest;

import org.junit.Test;

/**
 * Basic unit test
 * 
 * @author andrewb
 * @version 0.0.1
 * */
public class HashHelperTest {

	@Test
	public void testHashHelper() {
			
		String input = "1234567890-=!\"£$%^&*()_+\\qwertyuiop[]}{;'#:@~,./<>?asdfghjklzxcvbnmQWERTYUIOPLKJHGFDSAZXCVBNM";
			
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			digest.update(input.getBytes("UTF-8"));
			String hashed = new String(digest.digest(), "UTF-8");
					
			assertEquals("HashHelperTest: ", hashed, HashHelper.hash(input));
		} catch (Exception e) {
			fail();
		}			
	}
}
