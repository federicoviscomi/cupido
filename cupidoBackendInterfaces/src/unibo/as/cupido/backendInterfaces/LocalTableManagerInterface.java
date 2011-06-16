package unibo.as.cupido.backendInterfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

import unibo.as.cupido.backendInterfaces.common.Pair;
import unibo.as.cupido.backendInterfaces.common.TableInfoForClient;
import unibo.as.cupido.backendInterfaces.exception.NoSuchTableException;

/**
 * 
 * @author cane
 * 
 */
public interface LocalTableManagerInterface extends Remote {

	/**
	 * 
	 * This method is used by global table manager component to create a new
	 * table.
	 * 
	 * @param owner
	 *            the owner of the table to be created
	 * @param snf
	 *            the notification interface associated with the player who
	 *            wants to create a table
	 * @return a remote interface to the table manager
	 * @throws RemoteException
	 */
	public Pair<TableInterface, TableInfoForClient> createTable(String owner,
			ServletNotificationsInterface snf) throws RemoteException;

	/***
	 * 
	 * The Servlet uses this method to get the remote reference to the component
	 * who manages the table <code>tableId</code>
	 * 
	 * 
	 * @return a remote reference to the component who manages the table
	 *         <code>tableId</code>
	 * @throws NoSuchTableException
	 *             if tableId is invalid
	 * @throws RemoteException
	 *             in case of internal error
	 */
	public TableInterface getTable(int tableId) throws RemoteException,
			NoSuchTableException;

	/**
	 * Called by the GTM to notify his shutdown
	 * 
	 * @throws RemoteException
	 */
	public void notifyGTMShutDown() throws RemoteException;

	/**
	 * Called by the GTM to see if this LTM is still alive.
	 * 
	 * @throws RemoteException
	 *             if this LTM is not alive anymore.
	 */
	public void isAlive() throws RemoteException;

}
