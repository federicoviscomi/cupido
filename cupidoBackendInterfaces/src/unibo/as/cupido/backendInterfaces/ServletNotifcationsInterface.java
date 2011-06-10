package unibo.as.cupido.backendInterfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.ChatMessage;

/**
 * Implemented by the Servlet
 * 
 * @author cane
 * 
 */
public interface ServletNotifcationsInterface extends Remote {

	public void notifyGameEnded(int[] matchPoints, int[] playersTotalPoint) throws RemoteException;

	/**
	 * A che serve l'argomento?
	 * 
	 * @param cards
	 */
	public void notifyGameStarted(Card[] cards) throws RemoteException;

	/**
	 * 
	 * @param userName
	 * @param message
	 */
	public void notifyLocalChatMessage(ChatMessage message) throws RemoteException;

	public void notifyPassedCards(Card[] cards) throws RemoteException;

	public void notifyPlayedCard(Card card, int playerPosition) throws RemoteException;

	/**
	 * Perche' la servlet dovrebbe sapere se il giocatore e' un bot?
	 * 
	 * @param name
	 * @param isBot
	 * @param point
	 * @param position
	 */
	public void notifyPlayerJoined(String name, boolean isBot, int point, int position) throws RemoteException;

	public void notifyPlayerLeft(String name) throws RemoteException;
}
