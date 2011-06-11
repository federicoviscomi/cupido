package unibo.as.cupido.backendInterfacesImpl;

import java.util.Arrays;

import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.ChatMessage;

public class DummyLoggerBotNotification implements BotNotificationInterface {

	private final String botName;

	public DummyLoggerBotNotification(String botName) {
		this.botName = botName;
	}

	@Override
	public void notifyGameEnded(int[] matchPoints, int[] playersTotalPoint) {
		System.out.println("\n" + botName + " " + Thread.currentThread().getStackTrace()[1].getMethodName() + "("
				+ Arrays.toString(matchPoints) + ", " + Arrays.toString(playersTotalPoint) + ")");
	}

	@Override
	public void notifyGameStarted(Card[] cards) {
		System.out.println("\n" + botName + " " + Thread.currentThread().getStackTrace()[1].getMethodName() + "("
				+ Arrays.toString(cards) + ")");
	}

	@Override
	public void notifyLocalChatMessage(ChatMessage message) {
		System.out.println("\n" + botName + " " + Thread.currentThread().getStackTrace()[1].getMethodName() + "("
				+ message + ")");
	}

	@Override
	public void notifyPassedCards(Card[] cards) {
		System.out.println("\n" + botName + " " + Thread.currentThread().getStackTrace()[1].getMethodName() + "("
				+ Arrays.toString(cards) + ")");
	}

	@Override
	public void notifyPlayedCard(Card card, int playerPosition) {
		System.out.println("\n" + botName + " " + Thread.currentThread().getStackTrace()[1].getMethodName() + "("
				+ card + ", " + playerPosition + ")");
	}

	@Override
	public void notifyPlayerJoined(String name, boolean isBot, int point, int position) {
		System.out.println("\n" + botName + " " + Thread.currentThread().getStackTrace()[1].getMethodName() + "("
				+ name + ", " + isBot + "," + point + "," + position + ")");
	}

	@Override
	public void notifyPlayerLeft(String name) {
		System.out.println("\n" + botName + " " + Thread.currentThread().getStackTrace()[1].getMethodName() + "("
				+ name + ")");
	}
}
