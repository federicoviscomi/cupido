package unibo.as.cupido.shared.cometNotification;

import java.io.Serializable;

public class NewLocalChatMessage implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public NewLocalChatMessage() {
		
	}

	public String user;
	public String message;
}
