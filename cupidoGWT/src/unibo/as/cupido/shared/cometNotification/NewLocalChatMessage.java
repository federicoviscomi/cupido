package unibo.as.cupido.shared.cometNotification;

import java.io.Serializable;

public class NewLocalChatMessage implements Serializable {

	private static final long serialVersionUID = 1L;

	public String user;
	public String message;
	
	public NewLocalChatMessage() {
	}

	public NewLocalChatMessage(String user, String message) {
		this.user = user;
		this.message = message;
	}
}
