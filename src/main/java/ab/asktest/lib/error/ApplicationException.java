package ab.asktest.lib.error;
/**
 * ApplicationException
 * 
 * Some error types we can encapsulate into one generic and check individually
 * 
 * @author andrewb
 * @version 0.0.1
 * */
public class ApplicationException extends Exception {

	private static final long serialVersionUID = 1L;

	public enum ERROR {
		BAD_INPUT,
		IO_EXCEPTION,
		ENCODING_EXCEPTION,
		TOO_MANY_REQUESTS			
	}
	
	private ERROR error;
	private String msg;
	private Throwable th;
	
	public ApplicationException(ERROR error){
		this(error, null, null);
	}
	public ApplicationException(ERROR error, String msg){
		this(error, msg, null);
	}
	public ApplicationException(ERROR error, String msg, Throwable th){
		super();
		this.error = error;
		this.msg = msg;
		this.th = th;
	}
	public ERROR getError(){
		return error;
	}
	@Override
	public String toString(){
		return getMessage();
	}
	@Override
	public String getLocalizedMessage() {
		return getMessage();
	}
	@Override
	public String getMessage() {
		StringBuilder buf = new StringBuilder();
		
		buf.append(getClass().getName()).append(" ").append(error);
		
		if(msg!=null){
			buf.append(" ").append(msg);
		}
		
		if(th!=null){
			buf.append(" ").append(th.toString());
		}
		
		return buf.toString();
	}	
	/*
	 * The expensive bit of exceptions in Java - filling in the trace all the way up the stack.
	 * Have removed as all of these exception types have custom enum ERROR types which should be handed where they are returned.
	 * */
	@Override
	public synchronized Throwable fillInStackTrace() {
		return this;		
	}
	
}
