/**
 * 
 */
package unibo.as.cupido.client;

import unibo.as.cupido.backendInterfaces.common.ChatMessage;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Lorenzo Belli
 */
public interface GlobalChatInterfaceAsync {

	void viewLastMessages(AsyncCallback<ChatMessage[]> callback);

	void sendMessage(String message, AsyncCallback<Void> callback);

}
