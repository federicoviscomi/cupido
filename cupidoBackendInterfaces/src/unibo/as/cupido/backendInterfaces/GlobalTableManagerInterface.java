package unibo.as.cupido.backendInterfaces;

import java.net.InetAddress;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

import unibo.as.cupido.backendInterfaces.common.Card;

/**
 * 
 * The remote interface that local tables managers and Servlets use to
 * communicate with the global table manager
 * 
 * 
 * @author cane
 * 
 */
public interface GlobalTableManagerInterface extends Remote {

	public interface ServletNotifcationsInterface {

		public void notifyGameEnded(int[] matchPoints, int[] playersTotalPoint);

		public void notifyGameStarted(Card[] cards);

		/**
		 * 
		 * @param userName
		 * @param message
		 */
		public void notifyLocalChatMessage(String userName, String message);

		public void notifyPlassedCards(Card[] cards);

		public void notifyPlayedCard(Card card, int playerPosition);

		public void notifyPlayerJoined(String name, boolean isBot, int point, int position);

		public void notifyPlayerLeft(String name);
	}

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

		public Table(String owner, int freePosition, TableDescriptor tableDescriptor) {
			this.owner = owner;
			this.freePosition = freePosition;
			this.tableDescriptor = tableDescriptor;
		}

		public Table(String owner, int freePosition, String server, int id) {
			this.owner = owner;
			this.freePosition = freePosition;
			this.tableDescriptor = new TableDescriptor(server, id);
		}

		@Override
		/**
		 * A Table is uniquely identified by two things: the server it's managed by, the unique id the Table has within that server. 
		 * This method is used by an hashmap in the TableManager
		 */
		public int hashCode() {
			return (tableDescriptor.server + Long.toString(tableDescriptor.id)).hashCode();
		}

		@Override
		public String toString() {
			return "[owner=" + owner + ", free position=" + freePosition + ", server=" + tableDescriptor.server
					+ ", table id=" + tableDescriptor.id + "]";
		}
	}

	public class TableDescriptor {
		public TableDescriptor(String server, int id) {
			this.server = server;
			this.id = id;
		}

		public int id;
		public String server;
	}

	/** global server's name in the RMI registry */
	public static final String globalTableManagerName = "globaltableserver";

	/** l'indirizzo di default del server */
	public static final String defaultServerAddress = "localhost";

	/**
	 * 
	 * This method is used by the Servlet to create a new Table
	 * 
	 * @param owner
	 * @return
	 * @throws RemoteException
	 */
	public TableDescriptor createTable(String owner, ServletNotifcationsInterface snf) throws RemoteException;

	/**
	 * 
	 * This method is userd by the Servlet to get a list of all the tables
	 * 
	 * @return
	 * @throws RemoteException
	 */
	public Set<Table> getTableList() throws RemoteException;

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
	 * @return
	 * 
	 * @throws RemoteException
	 */
	public void notifyLocalTableManagerSturtup(String name) throws RemoteException;

	/**
	 * 
	 * The components LocalTableManager uses this method to notify the component
	 * TableManagerInterface when the Table terminates
	 * 
	 * @param tableDescriptor
	 * @throws RemoteException
	 */
	public void notifyTableDestruction(TableDescriptor tableDescriptor) throws RemoteException;

	/**
	 * 
	 * @param tableDescriptor
	 * @throws RemoteException
	 */
	public void notifyTableJoin(TableDescriptor tableDescriptor) throws RemoteException;

	/**
	 * This method is used by a local table manager to notify the global table
	 * manager.
	 * 
	 * Posso eliminarlo?
	 * 
	 * @param name
	 */
	public void notifyLocalTableManagerShutdown(String name) throws RemoteException;
}
