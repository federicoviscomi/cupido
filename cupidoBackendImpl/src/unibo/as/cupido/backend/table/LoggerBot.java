package unibo.as.cupido.backend.table;

import java.rmi.RemoteException;
import java.util.Arrays;

import unibo.as.cupido.backend.table.bot.LocalBotInterface;
import unibo.as.cupido.common.interfaces.ServletNotificationsInterface;
import unibo.as.cupido.common.interfaces.TableInterface;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.ChatMessage;

/**
 * A local bot implementation that does nothing but logs methods calls. This is
 * used only for logging creator operation.
 */
public class LoggerBot implements LocalBotInterface {

	/** this bot name */
	private final String botName;

	/**
	 * Create a new logger bot whit name <tt>name</tt>
	 * 
	 * @param botName
	 *            this bot name
	 */
	public LoggerBot(final String botName) {
		this.botName = botName;
	}

	@Override
	public void activate(TableInterface tableInterface) throws RemoteException {
		System.out.println("" + botName + " activate(" + tableInterface + ")");
	}

	/**
	 * The notification interface for a logger bot
	 */
	public static final class LoggerBotNotificationInterface implements
			ServletNotificationsInterface {

		/** this bot name */
		private final String botName;

		/**
		 * Creates a notification interface for logger bot <tt>botName</tt>
		 * 
		 * @param botName
		 */
		public LoggerBotNotificationInterface(String botName) {
			this.botName = botName;
		}

		@Override
		public void notifyGameEnded(int[] matchPoints, int[] playersTotalPoint)
				throws RemoteException {
			System.out.println("" + botName + " notifyGameEnded("
					+ Arrays.toString(matchPoints) + ", "
					+ Arrays.toString(playersTotalPoint) + ")");
		}

		@Override
		public void notifyGameStarted(Card[] cards) throws RemoteException {
			System.out.println("" + botName + " notifyGameStarted("
					+ Arrays.toString(cards) + ")");
		}

		@Override
		public void notifyLocalChatMessage(ChatMessage message)
				throws RemoteException {
			System.out.println("" + botName + " notifyChatMessage(" + message
					+ ")");
		}

		@Override
		public void notifyPassedCards(Card[] cards) throws RemoteException {
			System.out.println("" + botName + " notifyPassedCards("
					+ Arrays.toString(cards) + ")");
		}

		@Override
		public void notifyPlayedCard(Card card, int playerPosition)
				throws RemoteException {
			System.out.println("" + botName + " notifyPlayedCard(" + card
					+ ", " + playerPosition + ")");
		}

		@Override
		public void notifyPlayerJoined(String playerName, boolean isBot,
				int score, int position) throws RemoteException {
			System.out.println("" + botName + " notifyPlayerJoined("
					+ playerName + ", " + isBot + ", " + score + ")");
		}

		@Override
		public void notifyPlayerLeft(String playerName) throws RemoteException {
			System.out.println("" + botName + " notifyPlayerLefy(" + playerName
					+ ")");
		}

		@Override
		public void notifyPlayerReplaced(String botName, int position)
				throws RemoteException {
			System.out.println("" + botName + " notifyPlayerReplaced("
					+ botName + ", " + position + ")");
		}

	}

	@Override
	public ServletNotificationsInterface getServletNotificationsInterface() {
		return new LoggerBot.LoggerBotNotificationInterface(botName);
	}

	@Override
	public void passCards(Card[] cards) throws RemoteException {
		System.out.println("" + botName + " passCards("
				+ Arrays.toString(cards) + ")");
	}

	@Override
	public void playCard(Card card) {
		System.out.println("" + botName + " playCard(" + card + ")");
	}
}
