package unibo.as.cupido.backendInterfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.ChatMessage;

/**
 * Implemented by the Servlet This interface contains method to notify the
 * servlet of some events
 * 
 * Note: servlet in not notified when a player enter the table to observe the
 * match
 */
public interface ServletNotificationsInterface extends Remote {

	/**
	 * End of the game is notified to the servlet.
	 * 
	 * @param matchPoints
	 *            points the player has taken during this hand
	 * @param playersTotalPoint
	 *            points of the player updated after this match
	 */
	public void notifyGameEnded(int[] matchPoints, int[] playersTotalPoint)
			throws RemoteException;

	/**
	 * Start of the game is notified to the servlet
	 * 
	 * @param cards
	 *            the starting hand of the player
	 */
	public void notifyGameStarted(Card[] cards) throws RemoteException;

	/**
	 * A message sent in local chat is notify to the servlet
	 * 
	 * @param message
	 * @throws RemoteException
	 *             is message can't be send
	 */
	public void notifyLocalChatMessage(ChatMessage message)
			throws RemoteException;

	/**
	 * Notify that a player has passed 3 card to the player connected with the
	 * current servlet
	 * 
	 * @param cards
	 *            card passed. Card.lenght must be 3
	 * @throws RemoteException
	 */
	public void notifyPassedCards(Card[] cards) throws RemoteException;

	/**
	 * Notify that a player has played a card
	 * 
	 * @param card
	 *            card played
	 * @param playerPosition
	 *            table position of the player which has played the card
	 * @throws RemoteException
	 */
	public void notifyPlayedCard(Card card, int playerPosition)
			throws RemoteException;

	/**
	 * Servlet is notified when a player enter the table
	 * 
	 * @param name
	 *            of the joined player
	 * @param isBot
	 *            false if the player is a human player, true otherwise
	 * @param point
	 *            points of the player, if isBot==true, point is meaningless.
	 *            ?points is always zero when a players joins a table? then why
	 *            we need this paramenter?
	 * @param position
	 *            table position where the player has entered
	 */
	public void notifyPlayerJoined(String name, boolean isBot, int point,
			int position) throws RemoteException;

	/**
	 * Servlet is notified when a player leaves the table.
	 * 
	 * @param name
	 * @throws RemoteException
	 */
	public void notifyPlayerLeft(String name) throws RemoteException;
}
