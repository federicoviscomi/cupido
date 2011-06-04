package unibo.as.cupido.backendInterfaces;

import java.net.InetAddress;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

import unibo.as.cupido.backendInterfaces.common.Card;

/**
 * 
 * The remote interface that local tables managers and Servlets uses to
 * communicate with the global table manager
 * 
 * 
 * @author cane
 * 
 */
public interface TableManagerInterface extends Remote {

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

		@Override
		/**
		 * A Table is uniquely identified by two things: the server it's managed by, the unique id the Table has within that server. 
		 * This method is used by an hashmap in the TableManager
		 */
		public int hashCode() {
			return (tableDescriptor.server.getCanonicalHostName() + Integer.toString(tableDescriptor.id)).hashCode();
		}
	}

	public class TableDescriptor {
		int id;
		InetAddress server;
	}

	/** global server's name in the RMI registry */
	public static final String globalTableManagerName = "globaltableserver";

	/** l'indirizzo di default del server */
	public static final String defaultServerAddress = "localhost";

	/**
	 * 
	 * 
	 * @param owner
	 * @return
	 * @throws RemoteException
	 */
	public TableDescriptor createTable(String owner, ServletNotifcationsInterface snf) throws RemoteException;

	/**
	 * 
	 * Gets a list of all the tables
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
	 * @param localTableManagerAddress
	 *            is the address of the new LocalTableManager. ?is this argument
	 *            unecessary beacuse the TableManager knows it via RMI?
	 * 
	 * @throws RemoteException
	 */
	public void notifyLocalTableManagerSturtup(InetAddress localTableManagerAddress) throws RemoteException;

	/**
	 * 
	 * The components Table uses this method to notify the component
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

}
