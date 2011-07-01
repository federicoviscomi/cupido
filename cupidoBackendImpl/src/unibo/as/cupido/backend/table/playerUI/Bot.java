package unibo.as.cupido.backend.table.playerUI;

import java.rmi.RemoteException;

import unibo.as.cupido.common.exception.GameEndedException;
import unibo.as.cupido.common.interfaces.ServletNotificationsInterface;

public interface Bot {

	void addBot(int position) throws RemoteException;

	ServletNotificationsInterface getServletNotificationsInterface()
			throws RemoteException;

	void passCards() throws RemoteException;

	void playNextCard() throws RemoteException, GameEndedException;

}
