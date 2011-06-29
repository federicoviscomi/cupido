package unibo.as.cupido.common.exception;

import java.io.Serializable;

public class NoSuchPlayerException extends Exception implements Serializable {

	private static final long serialVersionUID = 1L;

	public NoSuchPlayerException(String string) {
		super(string);
	}

	public NoSuchPlayerException() {
		// 
	}
}
