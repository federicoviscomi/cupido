package unibo.as.cupido.client;

import unibo.as.cupido.backendInterfaces.common.Card;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface TableInterfaceAsync {

	void sendMessage(String message, AsyncCallback<Void> callback);

	void leaveTable(AsyncCallback<Void> callback);

	void playCard(Card card, AsyncCallback<Void> callback);

	void passCards(Card[] cards, AsyncCallback<Void> callback);

	void addBot(int position, AsyncCallback<Void> callback);

}
