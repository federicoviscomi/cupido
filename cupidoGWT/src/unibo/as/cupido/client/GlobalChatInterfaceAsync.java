/**
 * 
 */
package unibo.as.cupido.client;

import unibo.as.cupido.backendInterfaces.common.ChatMessage;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * <<<<<<< HEAD
 * 
 * @author cippy
 * 
 *         =======
 * @author Lorenzo Belli
 * 
 *         >>>>>>> 0db9d8bf916eb2508893c5f2eb05944c7b4122b1
 */
public interface GlobalChatInterfaceAsync {

	void viewLastMessages(AsyncCallback<ChatMessage[]> callback);

	void sendMessage(String message, AsyncCallback<Void> callback);

}
