package ab.asktest.dao;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.List;

import javax.sql.DataSource;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import ab.asktest.dao.obj.IPCountryMap;
import ab.asktest.dao.obj.Question;
import ab.asktest.dao.obj.WordBlackList;
/**
 * Concrete class of the Persistent store
 * 
 * Note - the "question" queries here will not scale as the data set grows! 
 * They will return too much data and slow down the service. 
 * This is a business requirements change though so have left as is for now. 
 * 
 * A "full version" would also probably use extensive partition of DB data, 
 * and per service instance caching based on some useful partition point 
 *  (e.g. diff instances of this executing service for different countries)  
 * 
 * Also a "full version" would have:
 * more fine grained transaction management (not leave it up to Spring to handle) 
 * Prepared statements/dbase side functions of some kind? for faster querying
 * 
 * @author andrewb
 * @version 0.0.1
 * */
@Repository
@org.springframework.context.annotation.Configuration
@EnableTransactionManagement
public class APIRepositoryImpl implements APIRepository{

	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	public void setDataSource(DataSource dataSource) {
		jdbcTemplate = new JdbcTemplate(dataSource);
		
	}
	/* Question Queries - not concerned about ordering of results for now. Will return in inserted order.
	 * later versions should partition data/offer search based on createdDate / modifiedDate as well as countryCode.
	 * also no auto-pagination, which will cause issues when this data set grows */
	@Transactional
	public List<String> getAllQuestions() {
		try{
			return jdbcTemplate.query("select question from Question", (ResultSet rs, int rowNum) -> rs.getString(1) );
		}catch(EmptyResultDataAccessException e){//this is ok
			return null;
		}
	}
	
	@Transactional
	public List<String> findQuestionsByCountryCode(String countryCode) {
		try{
			return jdbcTemplate.queryForList("select question from Question where countryCode = ?", new Object[] { countryCode }, String.class); 
		}catch(EmptyResultDataAccessException e){//this is ok
			return null;
		}
	}

	@Transactional
	public List<WordBlackList> getAllWordBlackLists() {
		try{
			return jdbcTemplate.query("select id, createdDate, modifiedDate, version, word from WordBlackList", 
						(ResultSet rs, int rowNum) -> new WordBlackList(
									rs.getLong("id"),
									rs.getTimestamp("createdDate"), rs.getTimestamp("modifiedDate"),
									rs.getLong("version"), 
									rs.getString("word")) ); 
		}catch(EmptyResultDataAccessException e){//this is ok
			return null;
		}
	}
	@Transactional(value=TxType.REQUIRES_NEW)//has to run outside current transaction
	public int noOfQuestionsAskedSince(String countryCode, Timestamp since) {		
		return jdbcTemplate.queryForObject("select count(*) from Question where countryCode = ? and createdDate >= ?", 
				new Object[] { countryCode, since }, Integer.class); 		
	}

	@Cacheable("ipCountryCache")
	@Transactional
	public IPCountryMap findCountryForIP(String IP) {
		try{
			return jdbcTemplate.queryForObject(
					"select id, createdDate, modifiedDate, version, ipAddress, countryCode from IPCountryMap where ipAddress = ?", 
					new Object[] { IP }, new BeanPropertyRowMapper<IPCountryMap>(IPCountryMap.class));
		}catch(EmptyResultDataAccessException e){//this is ok
			return null;
		}
	}

	@Transactional
	public void createIPCountryMap(IPCountryMap map) {
		jdbcTemplate.update("insert into IPCountryMap (createdDate, modifiedDate, version, ipAddress, countryCode) values (?,?,?,?,?)", 
						   new Object[] { map.getCreatedDate(), map.getModifiedDate(), map.getVersion(), map.getIpAddress(), map.getCountryCode()});		
	}

	@Transactional
	public List<Question> findQuestionsByIPHashSince(String ipAddress, String questionHash, Timestamp since) {
		try{
			return jdbcTemplate.query("select id, createdDate, modifiedDate, version, ipAddress, question, questionHash, countryCode from Question where ipAddress = ? and questionHash = ? and createdDate >= ?", 
					new Object[] { ipAddress, questionHash, since }, 							
					(ResultSet rs, int rowNum) -> new Question(
							rs.getLong("id"),
							rs.getTimestamp("createdDate"), rs.getTimestamp("modifiedDate"),
							rs.getLong("version"), 
							rs.getString("ipAddress"),
							rs.getString("question"),
							rs.getString("questionHash"),
							rs.getString("countryCode")
							) 
					);
		}catch(EmptyResultDataAccessException e){//this is ok
			return null;
		}
	}
	
	@Transactional(value=TxType.REQUIRES_NEW)//has to run outside current transaction
	public void createQuestion(Question question) {
		jdbcTemplate.update("insert into Question (createdDate, modifiedDate, version, ipAddress, question, questionHash, countryCode) values (?,?,?,?,?,?,?)", 
				   new Object[] { question.getCreatedDate(), question.getModifiedDate(), 
									question.getVersion(), question.getIpAddress(), question.getQuestion(), 
									question.getQuestionHash(), question.getCountryCode()});
		
	}


}
