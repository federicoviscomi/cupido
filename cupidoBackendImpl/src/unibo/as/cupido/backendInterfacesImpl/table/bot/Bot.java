package unibo.as.cupido.backendInterfacesImpl.table.bot;

import java.rmi.Remote;
import java.rmi.RemoteException;

import unibo.as.cupido.backendInterfaces.ServletNotificationsInterface;

public interface Bot extends ServletNotificationsInterface, Remote {

	void addBot(int i) throws RemoteException;

	void createTable() throws RemoteException;

	void passCards() throws RemoteException;

	void playNextCard() throws RemoteException;

}