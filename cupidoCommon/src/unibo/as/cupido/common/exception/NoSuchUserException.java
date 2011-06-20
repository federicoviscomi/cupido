package unibo.as.cupido.common.exception;

public class NoSuchUserException extends Exception {
	private static final long serialVersionUID = 1L;
	public NoSuchUserException(){
	}
	public NoSuchUserException(String userName) {
		super(userName);
	}

}
