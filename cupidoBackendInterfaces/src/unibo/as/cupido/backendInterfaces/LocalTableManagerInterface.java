package unibo.as.cupido.backendInterfaces;

import java.net.InetAddress;
import java.rmi.Remote;
import java.rmi.RemoteException;

import unibo.as.cupido.backendInterfaces.TableManagerInterface.ServletNotifcationsInterface;
import unibo.as.cupido.backendInterfaces.TableManagerInterface.Table;

/**
 * 
 * @author cane
 * 
 */
public interface LocalTableManagerInterface extends Remote {

	/**
	 * 
	 * @param owner
	 * @return the table id
	 */
	public int createTable(String owner, ServletNotifcationsInterface snf) throws RemoteException;

	/**
	 * 
	 * Returns the description of the table with id tableId
	 * 
	 * @param tableId
	 * 
	 * @return
	 */
	public Table getTable(int tableId) throws RemoteException;

	/**
	 * The component TableManager uses this method in order to:
	 * <ul>
	 * <li>know if a LocalTableManager component is alive</li>
	 * <li>ask a LocalTableManager how much of its resources it is using</li>
	 * </ul>
	 * 
	 * 
	 * @return is the LocalTableManager component is alive returns the
	 *         percentage of its resources it is using
	 * @throws RemoteException
	 *             if the LocalTableManger is not alive anymore
	 * 
	 */
	public double getWorkLoad() throws RemoteException;

	public InetAddress getAddress() throws RemoteException;;
}
