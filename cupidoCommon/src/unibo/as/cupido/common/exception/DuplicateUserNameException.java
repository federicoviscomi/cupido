package unibo.as.cupido.common.exception;

public class DuplicateUserNameException extends Exception {
	private static final long serialVersionUID = 1L;
	public DuplicateUserNameException(){
	}
	public DuplicateUserNameException(String userName) {
		super(userName);
	}

}
