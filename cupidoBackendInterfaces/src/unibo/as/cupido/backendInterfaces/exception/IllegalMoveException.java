package unibo.as.cupido.backendInterfaces.exception;

import java.io.Serializable;

/**
 * 
 * This exception is thrown when the player play an illegal move
 * 
 * @author cane
 * 
 */
public class IllegalMoveException extends Exception implements Serializable {

	private static final long serialVersionUID = 1L;

	public IllegalMoveException() {

	}

	public IllegalMoveException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

}
