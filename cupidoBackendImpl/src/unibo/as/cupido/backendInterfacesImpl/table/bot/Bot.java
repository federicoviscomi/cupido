package unibo.as.cupido.backendInterfacesImpl.table.bot;

import java.rmi.RemoteException;

import unibo.as.cupido.backendInterfaces.ServletNotificationsInterface;

public interface Bot extends ServletNotificationsInterface {

	void playNextCard() throws RemoteException;

	void passCards() throws RemoteException;

}
