package unibo.as.cupido.backendInterfaces.common;

import java.io.Serializable;

public class ChatMessage implements Serializable {

	private static final long serialVersionUID = 1L;

	public String userName;

	public String message;

	@Override
	public String toString() {
		return userName + ": " + message;
	}
}
