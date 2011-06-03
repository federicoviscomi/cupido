package unibo.as.cupido.backendInterfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import unibo.as.cupido.backendInterfaces.common.Card;

public interface TableManagerInterface extends Remote {

	public class TableDescriptor {
		int id;
		String server;
	}

	public class Table {
		String owner;
		int freePosition;
		TableDescriptor tableDescriptor;
	}

	/**
	 * 
	 * Gets a list of all the tables
	 * 
	 * @return
	 * @throws RemoteException
	 */
	public Table[] getTableList() throws RemoteException;

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
	 * The components Table uses this method to notify the component
	 * TableManagerInterface when the Table terminates
	 * 
	 * @param tableDescriptor
	 * @throws RemoteException
	 */
	public void notifyTableDestruction(TableDescriptor tableDescriptor)
			throws RemoteException;

	/**
	 * 
	 * @param tableDescriptor
	 * @throws RemoteException
	 */
	public void notifyTableJoin(TableDescriptor tableDescriptor)
			throws RemoteException;

	public interface ServletNotifcationsInterface {

		/**
		 * 
		 * @param userName
		 * @param message
		 */
		public void notifyLocalChatMessage(String userName, String message);

		public void notifyPlayedCard(Card card, int playerPosition);

		public void notifyPlassedCards(Card[] cards);

		public void notifyGameEnded(int[] matchPoints, int[] playersTotalPoint);

		public void notifyGameStarted(Card[] cards);

		public void notifyPlayerJoined(String name, boolean isBot, int point,
				int position);

		public void notifyPlayerLeft(String name);
	}

}
