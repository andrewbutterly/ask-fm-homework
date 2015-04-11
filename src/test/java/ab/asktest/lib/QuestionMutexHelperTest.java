package ab.asktest.lib;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;
/**
 * Basic unit test
 * 
 * @author andrewb
 * @version 0.0.1
 * */
public class QuestionMutexHelperTest {

	//only allow 1 action per second for this one country
	String country = "lv";
	int maxQCountAllowed = 1;
	
	static final QuestionMutexHelper muxtexHelper = new QuestionMutexHelper();
	
	@Test
	public void testMultiThreadedCall() {
					
		int threadCount = 50;
		ExecutorService executor = Executors.newFixedThreadPool(threadCount);    	
		ArrayList<ThreadCodeTester> threads = new ArrayList<>();
		
		long currentSecond = QuestionMutexHelper.getMSinSec(System.currentTimeMillis());
		
		for (int i=0;i<threadCount;i++) {
			threads.add(new ThreadCodeTester(i, currentSecond));
		}
		for(ThreadCodeTester thread:threads){
			executor.execute(thread);
		}
		//give them a few seconds to finish their work 
		//or else the threads might be killed without finishing
    	try{
    		Thread.sleep(3000);
    	}catch(InterruptedException e){
    		//do nothing
    	}
    	executor.shutdown();
    	while (!executor.isTerminated()) {}//ugly but its a test method :)
		
    	int failedCount = 0;
    	int successCount = 0;
    	for (ThreadCodeTester thread:threads){
    		if(thread.isSuccess()){
    			successCount++;
    		}else{
    			failedCount++;
    		}    		
    	}

    	if(successCount!=1 || failedCount!=(threadCount-1)){//should not have happened!
    		fail("QuestionMutexHelperTest: threaded ,utex test failed "
    				+ "(failedCount:"+failedCount+", successCount: "+successCount+", threadCount: "+threadCount+")");
    	}   
		
	}
	class ThreadCodeTester implements Runnable{
		
		int threadCount;
		boolean success;
		long currentSecond;

		public ThreadCodeTester(int threadCount, long currentSecond){
			this.threadCount = threadCount;
			this.currentSecond = currentSecond;
		}
		public boolean isSuccess(){
			return success;
		}
		public String printResult(){
			return threadCount+"="+success;
		}
		@Override
		public void run() {			
			success = muxtexHelper.allowedAskQuestion(country, currentSecond, maxQCountAllowed);
		}
		
	}
	
}
