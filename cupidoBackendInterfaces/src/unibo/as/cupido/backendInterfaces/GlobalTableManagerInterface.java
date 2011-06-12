package unibo.as.cupido.backendInterfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;
import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.ChatMessage;
import unibo.as.cupido.backendInterfaces.common.Pair;
import unibo.as.cupido.backendInterfaces.exception.AllLTMBusyException;
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

	/**
	 * 
	 * 
	 * 
	 * @author cane
	 * 
	 */
	public class Table {
		public String owner;
		public int freePosition;
		public TableDescriptor tableDescriptor;

		public Table() {
			//
		}

		public Table(String owner, int freePosition, String server, int id) {
			this.owner = owner;
			this.freePosition = freePosition;
			this.tableDescriptor = new TableDescriptor(server, id);
		}

		public Table(String owner, int freePosition,
				TableDescriptor tableDescriptor) {
			this.owner = owner;
			this.freePosition = freePosition;
			this.tableDescriptor = tableDescriptor;
		}

		/**
		 * A Table is uniquely identified by two things: the server it's managed
		 * by, the unique id the Table has within that server. This method is
		 * used by an hashmap in the TableManager
		 */
		@Override
		public int hashCode() {
			return (tableDescriptor.server + Long.toString(tableDescriptor.id))
					.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			return tableDescriptor.equals(((Table) o).tableDescriptor);
		}

		@Override
		public String toString() {
			return "[owner=" + owner + ", free position=" + freePosition
					+ ", server=" + tableDescriptor.server + ", table id="
					+ tableDescriptor.id + "]";
		}
	}

	public class TableDescriptor {
		public int id;

		public String server;

		public TableDescriptor(String server, int id) {
			this.server = server;
			this.id = id;
		}

		@Override
		public boolean equals(Object o) {
			TableDescriptor otd = (TableDescriptor) o;
			return (otd.id == this.id && otd.server.equals(this.server));
		}
	}

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
	 * This method is userd by the Servlet to get a list of all the tables
	 * 
	 * TODO non e' meglio fare restituire direttamente l'interfaccia del tavolo
	 * e non quella dell'LTM?
	 * 
	 * @return
	 * 
	 * @throws RemoteException
	 */
	public Collection<Pair<Table, LocalTableManagerInterface>> getTableList()
			throws RemoteException;

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
	 * Just for test purpose
	 * 
	 * @return
	 * @throws RemoteException
	 */
	public char[] ping() throws RemoteException;

}
