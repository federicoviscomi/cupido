/**
 * 
 */
package unibo.as.cupido.client;

import unibo.as.cupido.client.CupidoInterface.TableData;
import unibo.as.cupido.shared.InitialTableStatus;
import unibo.as.cupido.shared.ObservedGameStatus;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Lorenzo Belli
 * 
 */
public interface CupidoInterfaceAsync {

	void logout(AsyncCallback<Void> callback);

	void isUserRegistered(String username, AsyncCallback<Boolean> callback);

	void registerUser(String username, String password,
			AsyncCallback<Boolean> callback);

	void login(String username, String password, AsyncCallback<Boolean> callback);

	void getTableList(AsyncCallback<TableData[]> callback);

	void createTable(AsyncCallback<InitialTableStatus> callback);

	void joinTable(String server, int tableId,
			AsyncCallback<InitialTableStatus> callback);

	void viewTable(String server, int tableId,
			AsyncCallback<ObservedGameStatus> callback);

	void leaveTable(AsyncCallback<Void> callback);

}
