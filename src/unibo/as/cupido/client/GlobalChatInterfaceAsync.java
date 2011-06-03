/**
 * 
 */
package unibo.as.cupido.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
<<<<<<< HEAD
 * @author cippy
 * 
=======
 * @author Lorenzo Belli
 *
>>>>>>> 0db9d8bf916eb2508893c5f2eb05944c7b4122b1
 */
public interface GlobalChatInterfaceAsync {

	void viewLastMessages(AsyncCallback<String[]> callback);

	void sendMessage(String message, AsyncCallback<Void> callback);

}
