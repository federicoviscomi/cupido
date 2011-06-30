package unibo.as.cupido.common.exception;

import java.io.Serializable;

public class EmptyPositionException extends Exception implements Serializable {

	private static final long serialVersionUID = 1L;

	public EmptyPositionException(String string) {
		super(string);
	}

	public EmptyPositionException() {
	}
}
