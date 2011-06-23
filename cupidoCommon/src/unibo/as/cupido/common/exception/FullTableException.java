package unibo.as.cupido.common.exception;

import java.io.Serializable;

import unibo.as.cupido.common.structures.TableDescriptor;

/**
 * Thrown wheter a table cannot host more players.
 * 
 * @author cane
 * 
 */
public class FullTableException extends Exception implements Serializable {

	public FullTableException(String message) {
		super(message);
	}

	public FullTableException() {
		//
	}

	private static final long serialVersionUID = 1L;

}
