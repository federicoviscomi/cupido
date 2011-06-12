package unibo.as.cupido.backendInterfaces.exception;

import java.io.Serializable;

/**
 * A server malfunction that cannot be handled by the client, for example communication errors
 * in the server-side LAN.
 */
public class FatalException extends Exception implements Serializable {

	private static final long serialVersionUID = 1L;

	public FatalException() {
	}
	
	public FatalException(String message) {
		super(message);
	}
}
