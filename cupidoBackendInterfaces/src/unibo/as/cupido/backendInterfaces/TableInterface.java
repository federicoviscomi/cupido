package unibo.as.cupido.backendInterfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

import unibo.as.cupido.backendInterfaces.TableManagerInterface.ServletNotifcationsInterface;
import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.FullTableException;
import unibo.as.cupido.backendInterfaces.common.IllegalMoveException;
import unibo.as.cupido.backendInterfaces.common.InitialTableStatus;
import unibo.as.cupido.backendInterfaces.common.NoSuchTableException;
import unibo.as.cupido.backendInterfaces.common.ObservedGameStatus;
import unibo.as.cupido.backendInterfaces.common.PositionFullException;

/**
 * 
 * 
 * @author cane
 * 
 */
public interface TableInterface extends Remote {

	/**
	 * 
	 * @param userName
	 * @param position
	 * @throws PositionFullException
	 */
	void addBot(String userName, int position) throws PositionFullException, RemoteException;

	/**
	 * 
	 * @param userName
	 * @return
	 * @throws FullTableException
	 * @throws NoSuchTableException
	 */
	public InitialTableStatus joinTable(String userName, ServletNotifcationsInterface snf) throws FullTableException,
			NoSuchTableException, RemoteException;

	/**
	 * 
	 * @param userName
	 */
	void leaveTable(String userName) throws RemoteException;

	/**
	 * 
	 * @param cards
	 *            cards.length must be 3
	 * @throws IllegalMoveException
	 * 
	 */
	void passCards(String userName, Card[] cards) throws IllegalMoveException, RemoteException;

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
	void sendMessage(String userName, String message) throws RemoteException;

	/**
	 * 
	 * @param userName
	 * @return
	 * @throws NoSuchTableException
	 */
	public ObservedGameStatus viewTable(String userName, ServletNotifcationsInterface snf) throws NoSuchTableException,
			RemoteException;

}
