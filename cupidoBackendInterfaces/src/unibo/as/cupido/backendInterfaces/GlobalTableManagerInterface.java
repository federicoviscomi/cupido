package unibo.as.cupido.backendInterfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.RemoteStub;
import java.util.Collection;
import java.util.Set;

import unibo.as.cupido.backendInterfaces.GlobalTableManagerInterface.TableDescriptor;
import unibo.as.cupido.backendInterfaces.common.AllLTMBusyException;
import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.ChatMessage;
import unibo.as.cupido.backendInterfaces.common.NoSuchTableException;
import unibo.as.cupido.backendInterfaces.common.Pair;

/**
 * 
 * The remote interface that local tables managers and Servlets use to
 * communicate with the global table manager
 * 
 * GTM has to poll each LTM to see if it is alive
 * 
 * @author cane
 * 
 */
public interface GlobalTableManagerInterface extends Remote {

	/**
	 * Implemented by the Servlet
	 * 
	 * @author cane
	 * 
	 */
	public  interface ServletNotifcationsInterface extends Remote {

		public void notifyGameEnded(int[] matchPoints, int[] playersTotalPoint);

		/**
		 * A che serve l'argomento?
		 * 
		 * @param cards
		 */
		public void notifyGameStarted(Card[] cards);

		/**
		 * 
		 * @param userName
		 * @param message
		 */
		public void notifyLocalChatMessage(ChatMessage message);

		public void notifyPlassedCards(Card[] cards);

		public void notifyPlayedCard(Card card, int playerPosition);

		/**
		 * Perche' la servlet dovrebbe sapere se il giocatore e' un bot? 
		 * 
		 * @param name
		 * @param isBot
		 * @param point
		 * @param position
		 */
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

		public Table(String owner, int freePosition, String server, int id) {
			this.owner = owner;
			this.freePosition = freePosition;
			this.tableDescriptor = new TableDescriptor(server, id);
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
			return (tableDescriptor.server + Long.toString(tableDescriptor.id)).hashCode();
		}

		@Override
		public String toString() {
			return "[owner=" + owner + ", free position=" + freePosition + ", server=" + tableDescriptor.server
					+ ", table id=" + tableDescriptor.id + "]";
		}
	}

	public class TableDescriptor {
		public int id;

		public String server;

		public TableDescriptor(String server, int id) {
			this.server = server;
			this.id = id;
		}
	}

	/** global server's name in the RMI registry */
	public static final String globalTableManagerName = "globaltableserver";

	/** l'indirizzo di default del server */
	// public static final String defaultServerAddress = "localhost";

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
	public TableDescriptor createTable(String owner, ServletNotifcationsInterface snf) throws RemoteException,
			AllLTMBusyException;

	/**
	 * 
	 * This method is userd by the Servlet to get a list of all the tables
	 * 
	 * 
	 * @return
	 * @throws RemoteException
	 */
	public Collection<Pair<Table, LocalTableManagerInterface>> getTableList() throws RemoteException;

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
	public void notifyLocalTableManagerShutdown(LocalTableManagerInterface ltm) throws RemoteException;

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
	public void notifyLocalTableManagerStartup(LocalTableManagerInterface localTableManagerInterface, int maxTable)
			throws RemoteException;

	/**
	 * 
	 * The components LocalTableManager uses this method to notify the component
	 * GTM when the Table terminates
	 * 
	 * @param tableDescriptor
	 * @throws RemoteException
	 */
	public void notifyTableDestruction(TableDescriptor tableDescriptor, LocalTableManagerInterface ltm)
			throws RemoteException;

	/**
	 * called by the LTM on the GTM
	 * 
	 * @param tableDescriptor
	 * @throws RemoteException
	 * @throws NoSuchTableException
	 */
	public void notifyTableJoin(TableDescriptor tableDescriptor) throws RemoteException, NoSuchTableException;

	/**
	 * Just for test purpose
	 * 
	 * @return
	 * @throws RemoteException
	 */
	public char[] ping() throws RemoteException;

}
