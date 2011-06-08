package unibo.as.cupido.backendInterfaces;

import java.rmi.Remote;

import java.rmi.RemoteException;

import unibo.as.cupido.backendInterfaces.GlobalTableManagerInterface.ServletNotifcationsInterface;
import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.ChatMessage;
import unibo.as.cupido.backendInterfaces.common.FullTableException;
import unibo.as.cupido.backendInterfaces.common.IllegalMoveException;
import unibo.as.cupido.backendInterfaces.common.InitialTableStatus;
import unibo.as.cupido.backendInterfaces.common.NoSuchTableException;
import unibo.as.cupido.backendInterfaces.common.ObservedGameStatus;
import unibo.as.cupido.backendInterfaces.common.PlayerNotFoundException;
import unibo.as.cupido.backendInterfaces.common.PositionFullException;

/**
 * 
 * Used by the Servlet
 * 
 * implemented by the Table
 * 
 * Ther is no polling on the local table chat.
 * 
 * @author cane
 * 
 */
public interface TableInterface extends Remote {

	/**
	 * 
	 * the Table has to notify the GTM
	 * 
	 * 
	 * @param botName
	 * @param position
	 * @throws PositionFullException
	 * @throws FullTableException
	 * @throws IllegalArgumentException
	 *             if botName is null; if position is not 1, 2 or 3;
	 */
	void addBot(String botName, int position) throws PositionFullException, RemoteException, IllegalArgumentException,
			FullTableException;

	/**
	 * 
	 * 
	 * @param userName
	 * @return
	 * @throws FullTableException
	 * @throws NoSuchTableException
	 */
	public InitialTableStatus joinTable(String playerName, ServletNotifcationsInterface snf) throws FullTableException,
			NoSuchTableException, RemoteException, IllegalArgumentException;

	/**
	 * Can a bot leave the table? Cosa succede se un utente o un bot lasciano il
	 * tavolo mentre si stanno scambiando le carte? e mentre la partita e'
	 * iniziata?
	 * 
	 * @param userName
	 * @throws PlayerNotFoundException
	 */
	void leaveTable(String userName) throws RemoteException, PlayerNotFoundException;

	/**
	 * The user <code>userName</code> passes cards <code>cards</code> to the
	 * player next to him.
	 * 
	 * @param cards
	 *            the cards passed
	 * @throws IllegalArgumentException
	 *             <ul>
	 *             <li>if some argument is <code>null</code></li>
	 *             <li>if <code>cards</code> length is not 3</li>
	 *             <li>if the user <code>userName</code> does not own the cards
	 *             he wants to pass</li>
	 *             <li>if the user <code>userName</code> does not exists</li>
	 *             </ul>
	 * @throws RemoteException
	 */
	void passCards(String userName, Card[] cards) throws IllegalArgumentException, RemoteException;

	/**
	 * 
	 * @param userName
	 * @param card
	 * @throws IllegalMoveException
	 */
	void playCard(String userName, Card card) throws IllegalMoveException, RemoteException;

	/**
	 * Sends a message to the table chat
	 * 
	 * @param message
	 */
	void sendMessage(ChatMessage message) throws RemoteException;

	/**
	 * 
	 * 
	 * 
	 * @param userName
	 * @return
	 * @throws NoSuchTableException
	 */
	public ObservedGameStatus viewTable(String userName, ServletNotifcationsInterface snf) throws NoSuchTableException,
			RemoteException;

}
