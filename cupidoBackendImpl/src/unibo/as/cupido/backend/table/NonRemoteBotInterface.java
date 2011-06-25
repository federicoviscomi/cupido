package unibo.as.cupido.backend.table;

import java.rmi.RemoteException;

import unibo.as.cupido.common.interfaces.ServletNotificationsInterface;
import unibo.as.cupido.common.structures.Card;

public interface NonRemoteBotInterface extends ServletNotificationsInterface {

	// not really throw RemoteException
	void passCards(Card[] cards) throws RemoteException;

	// not really throw RemoteException
	void playCard(Card card) throws RemoteException;

}
