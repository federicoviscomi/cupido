package unibo.as.cupido.backend.table;

import java.rmi.RemoteException;

import unibo.as.cupido.common.interfaces.ServletNotificationsInterface;
import unibo.as.cupido.common.interfaces.TableInterface;
import unibo.as.cupido.common.structures.Card;

public interface NonRemoteBotInterface extends ServletNotificationsInterface {

	// not really throws RemoteException
	void passCards(Card[] cards) throws RemoteException;

	// not really throws RemoteException
	void playCard(Card card) throws RemoteException;

	// not really throws RemoteException
	void activate(TableInterface tableInterface) throws RemoteException;
}
