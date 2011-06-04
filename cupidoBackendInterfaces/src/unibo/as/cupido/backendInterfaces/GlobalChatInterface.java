package unibo.as.cupido.backendInterfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * 
 * 
 * @author cane
 * 
 */
public interface GlobalChatInterface extends Remote {

	/**
	 * 
	 * @return
	 */
	public String[] getLastMessages() throws RemoteException;

	/**
	 * 
	 * @param message
	 */
	public void sendMessage(String message) throws RemoteException;

}
