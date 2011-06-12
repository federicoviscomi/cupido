package unibo.as.cupido.backendInterfaces.exception;

public class NoSuchUserException extends Exception {

	public NoSuchUserException(String userName) {
		super(userName);
	}

}
