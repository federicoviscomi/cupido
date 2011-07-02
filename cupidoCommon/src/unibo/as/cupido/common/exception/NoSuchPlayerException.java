package unibo.as.cupido.common.exception;

import java.io.Serializable;

/**
 * Used to signal that specified player is missing in a table
 */
public class NoSuchPlayerException extends Exception implements Serializable {

	private static final long serialVersionUID = 1L;

	public NoSuchPlayerException(String playerName) {
		super("no such player named: " + playerName);
	}

	public NoSuchPlayerException(int position) {
		super("there is no player in position: " + position);
	}

	public NoSuchPlayerException() {
		//
	}
}
