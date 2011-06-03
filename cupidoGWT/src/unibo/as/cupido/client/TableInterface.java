/**
 * 
 */
package unibo.as.cupido.client;


import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.IllegalMoveException;
import unibo.as.cupido.backendInterfaces.common.PositionFullException;

import com.google.gwt.user.client.rpc.RemoteService;

/**
 * @author Lorenzo Belli
 * 
 */
public interface TableInterface extends RemoteService {

	/**
	 * Sends a message to the table chat
	 * 
	 * @param message
	 */
	void sendMessage(String message);

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
}
