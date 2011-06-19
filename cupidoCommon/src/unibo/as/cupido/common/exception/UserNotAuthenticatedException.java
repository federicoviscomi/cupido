package unibo.as.cupido.common.exception;

import java.io.Serializable;

public class UserNotAuthenticatedException extends Exception implements
		Serializable {

	public UserNotAuthenticatedException(){
	}

	public UserNotAuthenticatedException(String m){
		super(m);
	}

	private static final long serialVersionUID = 1L;
}
