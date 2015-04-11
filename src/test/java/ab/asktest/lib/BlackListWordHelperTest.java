package ab.asktest.lib;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Stream;

import org.junit.Test;

import ab.asktest.dao.obj.WordBlackList;

/**
 * Unit test for blacklisted words helper class
 * 
 * @author andrewb
 * @version 0.0.1
 * */
public class BlackListWordHelperTest {
			
	@Test
	public void testBlackListWordHelper() {
		
		ArrayList<WordBlackList> badWords = new ArrayList<>();
				
		/**
		 * The test list is stolen off the web somewhere. Not a comprehensive list but good for basic testing
		 * */		
		try(Stream<String> lines = Files.lines( Paths.get(getClass().getResource("testCurseWordsList.txt").toURI()) )){									
			lines.filter(line -> line!=null).forEach(line -> badWords.add(new WordBlackList(0l, null, null, 0l, line.trim())));		
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		assertTrue("BlackListWordHelperTest: ", badWords.size()==450);//450 is size of current test file. brittle?
		
		BlackListWordHelper helper = new BlackListWordHelper(badWords);
		
		StringBuilder fullQuestion = new StringBuilder();
		
		//contains word 
		//Build sentence containing + word word interlaced with non printables & surrounded with garbage
		for(WordBlackList badWord:badWords){			
			assertTrue("BlackListWordHelperTest: ", helper.wordIsBlackListed(badWord.getWord()));
			
			fullQuestion.append(badWord.getWord()+" "+padWithGarbage(badWord.getWord()));
		}
		
		//this full line contains blacklisted words
		assertTrue("BlackListWordHelperTest: ", helper.questionContainsBlackListedWord(fullQuestion.toString()));
		
	}
	private static Random rand = new Random();
	private static String garbage = "1234567890-=+_!\"£$%^&*()qwertyuiop[]}{POIUYTREWQasdfghjkl;'#~@:LKJHGFDSAzxcvbnm,./?><MNBVCXZ ";
	private static String[] nonPrintables = {"\n", "\r", "\t"};
	private String padWithGarbage(String word){
		
		StringBuilder padded = new StringBuilder();
		int len = rand.nextInt(10);
		for(int i=0;i<len;i++){
			padded.append(	garbage.substring(0, garbage.length())	);
		}
		
		for(int i=0;i<word.length();i++){
			padded.append( nonPrintables[rand.nextInt(nonPrintables.length)] );
			
			padded.append( word.substring(i, i+1) );
		}
		
		len = rand.nextInt(10);
		for(int i=0;i<len;i++){
			padded.append(	garbage.substring(0, garbage.length())	);
		}
		
		return padded.toString();
	}
	
}
;