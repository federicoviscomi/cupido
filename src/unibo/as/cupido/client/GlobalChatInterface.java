/**
 * 
 */
package unibo.as.cupido.client;

import com.google.gwt.user.client.rpc.RemoteService;

public interface GlobalChatInterface extends RemoteService {
	
	String[] viewLastMessages();

	/*
	 * message doesn't contain username
	 */
	void sendMessage(String message);
}
