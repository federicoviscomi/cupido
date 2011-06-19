package unibo.as.cupido.common.exception;

import java.io.Serializable;

public class MaxNumTableReachedException extends Exception implements
		Serializable {
	private static final long serialVersionUID = 1L;

	public MaxNumTableReachedException() {
	}

	public MaxNumTableReachedException(String message) {
		super(message);
	}
}
