package unibo.as.cupido.backendInterfaces.exception;

public class DuplicateUserNameException extends Exception {

	public DuplicateUserNameException(String userName) {
		super(userName);
	}

}
