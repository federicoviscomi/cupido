package unibo.as.cupido.client;

import java.util.Collection;

import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.ChatMessage;
import unibo.as.cupido.backendInterfaces.common.InitialTableStatus;
import unibo.as.cupido.backendInterfaces.common.ObservedGameStatus;
import unibo.as.cupido.backendInterfaces.common.TableInfoForClient;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface CupidoInterfaceAsync {

	void logout(AsyncCallback<Void> callback);

	void isUserRegistered(String username, AsyncCallback<Boolean> callback);

	void registerUser(String username, String password,
			AsyncCallback<Void> callback);

	void login(String username, String password, AsyncCallback<Boolean> callback);

	void getTableList(AsyncCallback<Collection<TableInfoForClient>> callback);

	void createTable(AsyncCallback<InitialTableStatus> callback);

	void joinTable(String server, int tableId,
			AsyncCallback<InitialTableStatus> callback);

	void viewTable(String server, int tableId,
			AsyncCallback<ObservedGameStatus> callback);

	void openCometConnection(AsyncCallback<Void> callback);

	void leaveTable(AsyncCallback<Void> callback);

	void playCard(Card card, AsyncCallback<Void> callback);

	void passCards(Card[] cards, AsyncCallback<Void> callback);

	void addBot(int position, AsyncCallback<Void> callback);

	void viewLastMessages(AsyncCallback<ChatMessage[]> callback);

	void sendGlobalChatMessage(String message, AsyncCallback<Void> callback);

	void sendLocalChatMessage(String message, AsyncCallback<Void> callback);

	void destroySession(AsyncCallback<Void> callback);
}