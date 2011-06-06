/**
 * 
 */
package unibo.as.cupido.client;

import unibo.as.cupido.backendInterfaces.common.ChatMessage;

import com.google.gwt.user.client.rpc.RemoteService;


public interface GlobalChatInterface extends RemoteService {

	ChatMessage[] viewLastMessages();

	/*
	 * message doesn't contain username
	 */
	void sendMessage(String message);
}
