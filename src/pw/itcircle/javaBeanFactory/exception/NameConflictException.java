package pw.itcircle.javaBeanFactory.exception;

public class NameConflictException extends Exception 
{
	private static final long serialVersionUID = 1077169782920833927L;

	public NameConflictException() {
        super();
    }
    
    public NameConflictException(String msg) {
        super(msg);
    }
    
    public NameConflictException(String msg, Throwable cause) {
        super(msg, cause);
    }
    
    public NameConflictException(Throwable cause) {
        super(cause);
    }
}
