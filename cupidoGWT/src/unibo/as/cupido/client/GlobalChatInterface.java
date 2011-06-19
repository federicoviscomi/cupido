/**
 * 
 */
package unibo.as.cupido.client;

import unibo.as.cupido.common.structures.ChatMessage;

import com.google.gwt.user.client.rpc.RemoteService;

public interface GlobalChatInterface extends RemoteService {

	ChatMessage[] viewLastMessages();

	/*
	 * message doesn't contain username
	 */
	void sendMessage(String message);
}
