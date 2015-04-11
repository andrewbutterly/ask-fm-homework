package ab.asktest.dao;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Timestamp;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ab.asktest.controller.Application;
import ab.asktest.controller.util.QuestionHelper;
import ab.asktest.dao.obj.IPCountryMap;
import ab.asktest.dao.obj.Question;
import ab.asktest.dao.obj.WordBlackList;

/**
 * Unit test for RepoImpl class.
 * 
 * All of the more complicated dbase operations are tested as part of the controller tests. 
 * These are just basic "creation" tests
 * 
 * A few simple queries
 * 
 * @author andrewb
 * @version 0.0.1
 * */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebIntegrationTest
public class APIRepositoryImplTest{
	
	@Autowired
	QuestionHelper questionHelper;	
	@Autowired
	private APIRepository apiRepository;
	
	private JdbcTemplate jdbcTemplate;	
	@Autowired
	public void setDataSource(DataSource dataSource) {
		jdbcTemplate = new JdbcTemplate(dataSource);
		
	}
	
	@Test
	public void testCreateQuestion(){
		
		String randomStr = System.nanoTime()+"";
		
		Question testQ = new Question(0l, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), 0l,  "127.0.0.1", randomStr, randomStr, "XX");//fake country code		
		apiRepository.createQuestion(testQ);
		
		//then query the back end directly
		List<Long> ids = jdbcTemplate.queryForList(
								"select id from Question where question = ? and countryCode = ?", 
								new Object[]{randomStr, "XX"}, 
								Long.class );
		
		assertTrue("APIRepositoryImplTest: ", (ids!=null&&ids.size()==1));
		//purge it
		jdbcTemplate.execute("delete from Question where id = "+ids.get(0));	
	}
	
	@Test
	public void testCreateCountryIpMapping(){
		
		String randomStr = trimToLen(System.nanoTime()+"", 45);
		
		IPCountryMap testIpMap = new IPCountryMap(0l, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), 0l,  randomStr, "ZZ");//fake country code	
		apiRepository.createIPCountryMap(testIpMap);			
		
		//then query the back end directly
		List<Long> ids = jdbcTemplate.queryForList(
								"select id from IPCountryMap where ipAddress = ? and countryCode = ?", 
								new Object[]{randomStr, "ZZ"}, 
								Long.class );
		
		System.out.println(ids.size());
		
		assertTrue("APIRepositoryImplTest: ", (ids!=null&&ids.size()==1));
		//purge it
		jdbcTemplate.execute("delete from IPCountryMap where id = "+ids.get(0));
		
	}
	public static String trimToLen(String input, int maxlen){
		if(input==null||input.length()<1){
			return null;
		}			
		return (input.length()>maxlen?input.substring(0, maxlen):input);
	}
	
	@Test
	public void testReadFromBlackList(){
		
		String randomStr = System.nanoTime()+"";
		
		jdbcTemplate.update("insert into WordBlackList (createdDate, modifiedDate, version, word) values (?, ?, ?, ?)", 
				   new Object[] { new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), 0l, randomStr});
		
		List<WordBlackList> list = apiRepository.getAllWordBlackLists();
		
		boolean found = false;
		long id = 0;
		
		for(WordBlackList l :list){
			if(l.getWord().equals(randomStr)){
				found = true;
				id = l.getId();
			}
		}
		
		if(!found){
			fail("APIRepositoryImplTest: failed creating and finding word blacklist entry");			
		}else{
			//purge it
			jdbcTemplate.execute("delete from WordBlackList where id = "+id);
		}
		
	}
	
	/**
	public List<String> getAllQuestions();	
	public List<String> findQuestionsByCountryCode(String countryCode);
	
	public List<WordBlackList> getAllWordBlackLists();
	public int noOfQuestionsAskedSince(String countryCode, Timestamp since);
	
	public IPCountryMap findCountryForIP(String IP);
	
	public void createIPCountryMap(IPCountryMap map);
	
	public List<Question> findQuestionsByIPHashSince(String ipAddress, String questionHash, Timestamp since);
	public void createQuestion(Question question);
	 * */
	
}
