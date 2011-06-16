package unibo.as.cupido.backendInterfaces.common;

import java.io.Serializable;

public class ChatMessage implements Serializable {

	private static final long serialVersionUID = 1L;

	public String userName;

	public String message;

	public ChatMessage(String userName, String message) {
		this.userName = userName;
		this.message = message;
	}

	public ChatMessage() {
		//
	}

	@Override
	public String toString() {
		return userName + ": " + message;
	}
}
