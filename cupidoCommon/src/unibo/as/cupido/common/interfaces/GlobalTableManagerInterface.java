package unibo.as.cupido.common.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;

import unibo.as.cupido.common.structures.TableDescriptor;
import unibo.as.cupido.common.structures.TableInfoForClient;
import unibo.as.cupido.common.exception.AllLTMBusyException;
import unibo.as.cupido.common.exception.EmptyTableException;
import unibo.as.cupido.common.exception.FullTableException;
import unibo.as.cupido.common.exception.NoSuchLTMException;
import unibo.as.cupido.common.exception.NoSuchLTMInterfaceException;
import unibo.as.cupido.common.exception.NoSuchTableException;

/**
 * The remote interface that local tables manager(LTM), single table
 * manager(STM) and Servlets use to communicate with the global table
 * manager(GTM).
 * 
 * @author cane
 * 
 */
public interface GlobalTableManagerInterface extends Remote {

	/** GTM name in the RMI registry */
	public static final String globalTableManagerName = "globaltableserver";

	/** delay of milliseconds that takes between each polling */
	public static final long POLLING_DELAY = (long) 1e5;

	/**
	 * This method is used by the Servlet to create a new Table
	 * 
	 * @param creator
	 * @param snf
	 * @return
	 * @throws RemoteException
	 * @throws AllLTMBusyException
	 */
	public TableInterface createTable(String creator,
			ServletNotificationsInterface snf) throws RemoteException,
			AllLTMBusyException;

	/**
	 * This method is used by the Servlet to get a list of all the tables
	 * managed by GTM.
	 * 
	 * @return
	 * @throws RemoteException
	 */
	public Collection<TableInfoForClient> getTableList() throws RemoteException;

	/**
	 * When an LTM shuts down, it calls this method and GTM removes it from his
	 * LTM set. Note that this method is not strictly necessary because GTM poll
	 * LTMs every POLLING_DELAY milliseconds.
	 * 
	 * @param ltm
	 * @throws RemoteException
	 */
	public void notifyLocalTableManagerShutdown(LocalTableManagerInterface ltm)
			throws RemoteException;

	/**
	 * The GTM component keeps a set of LTM. When a new LTM wants to join this
	 * set, it sends a notification to the GTM
	 * 
	 * @param localTableManagerInterface
	 * @param maxTable
	 * @throws RemoteException
	 */
	public void notifyLocalTableManagerStartup(
			LocalTableManagerInterface localTableManagerInterface, int maxTable)
			throws RemoteException;

	/**
	 * The components STM uses this method to notify the component GTM when the
	 * game on table <code>tableDescriptor</code> ends.
	 * 
	 * @param tableDescriptor
	 * @param ltm
	 * @throws RemoteException
	 * @throws NoSuchLTMInterfaceException
	 */
	public void notifyTableDestruction(TableDescriptor tableDescriptor,
			LocalTableManagerInterface ltm) throws RemoteException,
			NoSuchLTMInterfaceException;

	/**
	 * The components STM uses this method to notify the component GTM when a
	 * player joins table <code>tableDescriptor</code>.
	 * 
	 * @param tableDescriptor
	 * @throws RemoteException
	 * @throws NoSuchTableException
	 * @throws FullTableException
	 */
	public void notifyTableJoin(TableDescriptor tableDescriptor)
			throws RemoteException, NoSuchTableException, FullTableException;

	/**
	 * Called by the STM on the GTM if a player leaves table
	 * <code>tableDescriptor</code> before the game start. This method is not
	 * called after the game starts because in that case a bot automatically
	 * replaces the leaving player. The player who leaves is not table creator
	 * because in that case the table is to be destroyed.
	 * 
	 * @param tableDescriptor
	 * @throws RemoteException
	 * @throws NoSuchTableException
	 * @throws EmptyTableException
	 * @throws unibo.as.cupido.common.exception.EmptyTableException
	 */
	public void notifyTableLeft(TableDescriptor tableDescriptor)
			throws RemoteException, NoSuchTableException, EmptyTableException;

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

}
