/**
 * 
 */
package unibo.as.cupido.client;

import java.io.Serializable;

import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.ChatMessage;
import unibo.as.cupido.backendInterfaces.common.InitialTableStatus;
import unibo.as.cupido.backendInterfaces.common.ObservedGameStatus;
import unibo.as.cupido.backendInterfaces.exception.FullTableException;
import unibo.as.cupido.backendInterfaces.exception.IllegalMoveException;
import unibo.as.cupido.backendInterfaces.exception.NoSuchTableException;
import unibo.as.cupido.backendInterfaces.exception.PositionFullException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * @author Lorenzo Belli
 * 
 */
@RemoteServiceRelativePath("cupido")
public interface CupidoInterface extends RemoteService {

	/**
	 * This method must be called by the client before it can open the connection itself
	 * and start receiving Comet notifications.
	 */
	public void openCometConnection();

	public boolean login(String username, String password);

	/**
	 * 
	 * @param username
	 * 
	 * @param password
	 * @return
	 */
	public boolean registerUser(String username, String password);

	public boolean isUserRegistered(String username);

	public void logout();

	/*
	 * Identify a single table
	 */
	public class TableData implements Serializable {

		private static final long serialVersionUID = 1L;
		public String owner;
		public int vacantSeats;
		public String server;
		public int tableId;
	}

	/*
	 * Return all the tables
	 */
	public TableData[] getTableList();

	public InitialTableStatus createTable();

	public InitialTableStatus joinTable(String server, int tableId)
			throws FullTableException, NoSuchTableException;

	public ObservedGameStatus viewTable(String server, int tableId)
			throws NoSuchTableException;

	/**
	 * Sends a message to the table chat
	 * 
	 * @param message
	 */
	void sendLocalChatMessage(String message);

	/**
	 * The current player leaves the table
	 */
	void leaveTable();

	void playCard(Card card) throws IllegalMoveException;

	/**
	 * 
	 * @param cards
	 *            cards.length must be 3
	 * @throws IllegalMoveException
	 * 
	 */
	void passCards(Card[] cards) throws IllegalMoveException;

	void addBot(int position) throws PositionFullException;
	
	ChatMessage[] viewLastMessages();

	/**
	 * Sends a message to the table chat
	 * 
	 * @param message
	 */
	void sendGlobalChatMessage(String message);
}
