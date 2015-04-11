package ab.asktest.lib;

import java.util.Hashtable;

/**
 * QuestionMutexHelper.
 * 
 * The transactional concurrency offered by the databases is not good enough for the project requirement:
 * 
 * > Service must perform question validation according to the following rules and reject question if: 
 * >> N questions / second are asked from a single country (essentially we want to limit number of questions coming from a country in a given timeframe)
 * 
 * At testing phase with a multi-threaded test, more than N threads were managing to get through <em>at the same time</em> to write messages to the database.
 * The requirement above does not allow this - only N test threads per second should though to create the database entry.   
 * 
 * This Helper class introduces an in memory, per country mutex to prevent this. 
 * It means that the application as a whole will slow down slightly, and that this object must be kept in memory,
 * but the requirement will be satisfied. 
 * 
 * @author andrewb
 * @version 0.0.1
 * */
public class QuestionMutexHelper {

	public static Hashtable<String, CountryLock> countryMap;
	
	public QuestionMutexHelper(){
		countryMap = new Hashtable<>(); 
	}
	public static long getMSinSec(long currentTimeinMS){
		return currentTimeinMS/1000;
	}
	/**
	 * Is the user allowed ask this question from this country?
	 * 
	 * Note: still uses optimistic concurrency - does not lock the CountryLock object
	 * before allowing initial read. Will have to test to see how it performs...
	 * 
	 * @param String country
	 * @param long currentSecond
	 * @param int maxQCountAllowed
	 * @return boolean -allowed or not
	 * */
	public boolean allowedAskQuestion(String country, long currentSecond, int maxQCountAllowed){
			CountryLock lock = countryMap.get(country);
			if(lock==null){
				synchronized(countryMap){
					lock = new CountryLock(currentSecond, 1);
					//HashTable has a synchronized lock on the put method
					countryMap.put(country, lock);								
					return true;
				}
			}
			synchronized(lock){
				if(lock.getCurrentSecond()==currentSecond){//still in the current second
					boolean allowed = (lock.getCount()<maxQCountAllowed);				
					if(allowed){
						lock.increment();
					}					
					return allowed;
				}
				//time has moved on - can remove the "old" entry 
				lock.setCurrentSecond(currentSecond);
				lock.setCount(1);
				return true;
			}
	}
	
	class CountryLock{
		
		long currentSecond;//seconds since epoch
		int count;
		
		public CountryLock(long currentSecond, int count){
			this.currentSecond = currentSecond;
			this.count = count;
		}
		public long getCurrentSecond() {
			return currentSecond;
		}
		public void setCurrentSecond(long currentSecond) {
			this.currentSecond = currentSecond;
		}
		public long getCount() {
			return count;
		}
		public void setCount(int count) {
			this.count = count;
		}
		public void increment(){
			count++;
		}
	}
}
