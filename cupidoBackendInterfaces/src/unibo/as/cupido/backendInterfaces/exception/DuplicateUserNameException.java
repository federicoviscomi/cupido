package unibo.as.cupido.backendInterfaces.exception;

public class DuplicateUserNameException extends Exception {

	public DuplicateUserNameException(){
	}
	public DuplicateUserNameException(String userName) {
		super(userName);
	}

}
