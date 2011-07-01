package unibo.as.cupido.backend.table;

import java.rmi.RemoteException;
import java.util.Arrays;

import unibo.as.cupido.common.interfaces.ServletNotificationsInterface;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.ChatMessage;

/**
 * A servlet notification interface that just logs operations.
 */
public class LoggerServletNotificationInterface implements
		ServletNotificationsInterface {

	/** this servlet name */
	private final String name;

	/**
	 * Creates a notification interface for logger bot <tt>name</tt>
	 * 
	 * @param name
	 */
	public LoggerServletNotificationInterface(String name) {
		this.name = name;
	}

	@Override
	public void notifyGameEnded(int[] matchPoints, int[] playersTotalPoint)
			throws RemoteException {
		System.out.println("" + name + " notifyGameEnded("
				+ Arrays.toString(matchPoints) + ", "
				+ Arrays.toString(playersTotalPoint) + ")");
	}

	@Override
	public void notifyGameStarted(Card[] cards) throws RemoteException {
		System.out.println("" + name + " notifyGameStarted("
				+ Arrays.toString(cards) + ")");
	}

	@Override
	public void notifyLocalChatMessage(ChatMessage message)
			throws RemoteException {
		System.out.println("" + name + " notifyChatMessage(" + message + ")");
	}

	@Override
	public void notifyPassedCards(Card[] cards) throws RemoteException {
		System.out.println("" + name + " notifyPassedCards("
				+ Arrays.toString(cards) + ")");
	}

	@Override
	public void notifyPlayedCard(Card card, int playerPosition)
			throws RemoteException {
		System.out.println("" + name + " notifyPlayedCard(" + card + ", "
				+ playerPosition + ")");
	}

	@Override
	public void notifyPlayerJoined(String playerName, boolean isBot, int score,
			int position) throws RemoteException {
		System.out.println("" + name + " notifyPlayerJoined(" + playerName
				+ ", " + isBot + ", " + score + ")");
	}

	@Override
	public void notifyPlayerLeft(String playerName) throws RemoteException {
		System.out.println("" + name + " notifyPlayerLefy(" + playerName + ")");
	}

	@Override
	public void notifyPlayerReplaced(String name, int position)
			throws RemoteException {
		System.out.println("" + name + " notifyPlayerReplaced(" + name + ", "
				+ position + ")");
	}

}
