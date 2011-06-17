package unibo.as.cupido.backendInterfaces.exception;

public class DuplicateUserNameException extends Exception {
	private static final long serialVersionUID = 1L;
	public DuplicateUserNameException(){
	}
	public DuplicateUserNameException(String userName) {
		super(userName);
	}

}
