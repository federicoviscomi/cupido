/**
 * 
 */
package unibo.as.cupido.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Lorenzo Belli
 *
 */
public interface GlobalChatInterfaceAsync {

	void viewLastMessages(AsyncCallback<String[]> callback);

	void sendMessage(String message, AsyncCallback<Void> callback);

}
