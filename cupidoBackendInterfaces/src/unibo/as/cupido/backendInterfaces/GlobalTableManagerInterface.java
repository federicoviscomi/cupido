package unibo.as.cupido.backendInterfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;
import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.ChatMessage;
import unibo.as.cupido.backendInterfaces.common.Pair;
import unibo.as.cupido.backendInterfaces.common.TableDescriptor;
import unibo.as.cupido.backendInterfaces.common.TableInfoForClient;
import unibo.as.cupido.backendInterfaces.exception.AllLTMBusyException;
import unibo.as.cupido.backendInterfaces.exception.NoSuchLTMException;
import unibo.as.cupido.backendInterfaces.exception.NoSuchLTMInterfaceException;
import unibo.as.cupido.backendInterfaces.exception.NoSuchTableException;

/**
 * 
 * The remote interface that local tables managers and Servlets use to
 * communicate with the global table manager
 * 
 * @author cane
 * 
 */
public interface GlobalTableManagerInterface extends Remote {

	/** global server's name in the RMI registry */
	public static final String globalTableManagerName = "globaltableserver";

	/**
	 * 
	 * This method is used by the Servlet to create a new Table implemented by
	 * the GTM
	 * 
	 * @param owner
	 * @return
	 * @throws RemoteException
	 * @throws AllLTMBusyException
	 */
	public TableInterface createTable(String owner,
			ServletNotificationsInterface snf) throws RemoteException,
			AllLTMBusyException;

	/**
	 * 
	 * This method is userd by the Servlet to get a list of all the tables.
	 * 
	 * @return
	 * 
	 * @throws RemoteException
	 */
	public Collection<TableInfoForClient> getTableList() throws RemoteException;

	/**
	 * This method is used by a local table manager to notify the global table
	 * manager.
	 * 
	 * GTM removes LTM from the set
	 * 
	 * 
	 * 
	 * @param name
	 */
	public void notifyLocalTableManagerShutdown(LocalTableManagerInterface ltm)
			throws RemoteException;

	/**
	 * 
	 * The TableManager component keeps a set of LocalTableManager. When a new
	 * LocalTableManager wants to join this set is sends a notification to the
	 * TableManager
	 * 
	 * @param name
	 * 
	 * @param localTableManagerAddress
	 *            is the remote object of the new LocalTableManager.
	 * @param maxTable
	 *            is the maximum number of Tables this LTM can manage
	 * @return the LTM set
	 * @throws RemoteException
	 */
	public void notifyLocalTableManagerStartup(
			LocalTableManagerInterface localTableManagerInterface, int maxTable)
			throws RemoteException;

	/**
	 * 
	 * The components LocalTableManager uses this method to notify the component
	 * GTM when the Table terminates
	 * 
	 * @param tableDescriptor
	 * @throws RemoteException
	 * @throws NoSuchLTMInterfaceException
	 * 
	 */
	public void notifyTableDestruction(TableDescriptor tableDescriptor,
			LocalTableManagerInterface ltm) throws RemoteException,
			NoSuchLTMInterfaceException;

	/**
	 * called by the LTM on the GTM
	 * 
	 * @param tableDescriptor
	 * @throws RemoteException
	 * @throws NoSuchTableException
	 */
	public void notifyTableJoin(TableDescriptor tableDescriptor)
			throws RemoteException, NoSuchTableException;

	/**
	 * Get the LTM remote object identified by <code>ltmId</code>
	 * 
	 * @param ltmId
	 * @return
	 * @throws RemoteException
	 * @throws NoSuchLTMException
	 *             if <code>ltmId</code> is not found.
	 */
	public LocalTableManagerInterface getLTMInterface(String ltmId)
			throws RemoteException, NoSuchLTMException;

	/**
	 * Just for test purpose
	 * 
	 * @return
	 * @throws RemoteException
	 */
	public char[] ping() throws RemoteException;

}
