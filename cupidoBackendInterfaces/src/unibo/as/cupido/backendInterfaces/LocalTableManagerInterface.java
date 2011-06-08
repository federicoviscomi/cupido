package unibo.as.cupido.backendInterfaces;


import java.rmi.Remote;
import java.rmi.RemoteException;

import unibo.as.cupido.backendInterfaces.GlobalTableManagerInterface.ServletNotifcationsInterface;

/**
 * 
 * @author cane
 * 
 */
public interface LocalTableManagerInterface extends Remote {

	/**
	 * 
	 * This method is used by global table manager component to create a new
	 * table
	 * 
	 * @param owner
	 * @return 
	 */
	public TableInterface createTable(String owner, ServletNotifcationsInterface snf) throws RemoteException;


	/***
	 * 
	 * The Servlet uses this method to get the remote reference to the component
	 * who manages the table tableId
	 * 
	 * 
	 * @return
	 */
	public TableInterface getTable(int tableId) throws RemoteException;

	public void notifyGTMShutDown() throws RemoteException;

}
