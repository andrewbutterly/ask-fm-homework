package ab.asktest.lib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Basic unit test
 * 
 * @author andrewb
 * @version 0.0.1
 * */
public class BasicInputSanitiserTest {

	@Test
	public void testBasicInputSanitiser() {
	
		String input = "1234567890-=!\"£$%^&*()_+\\qwertyuiop[]}{;'#:@~,./<>?asdfghjklzxcvbnmQWERTYUIOPLKJHGFDSAZXCVBNM";
					
		assertNotNull("BasicInputSanitiserTest: ", BasicInputSanitiser.cleanInput(input));
		
		input = "1234567890qwertyuioplkjjhgfdsazxcvbnmMNBVCXZASDFGHJKLPOIUYTREWQ";
		assertEquals("BasicInputSanitiserTest: ", 10, BasicInputSanitiser.cleanInput(input, 10).length());
		assertEquals("BasicInputSanitiserTest: ", input.length(), BasicInputSanitiser.cleanInput(input, Integer.MAX_VALUE).length());
		
		assertNull("BasicInputSanitiserTest: ", BasicInputSanitiser.cleanInput(null));
		assertNull("BasicInputSanitiserTest: ", BasicInputSanitiser.cleanInput(""));
		
		assertEquals("BasicInputSanitiserTest: ", "1", BasicInputSanitiser.cleanInput(" 1 "));
		
		assertEquals("BasicInputSanitiserTest: ", "tag", BasicInputSanitiser.cleanInput("<tag>tag"));
		
	}
}
