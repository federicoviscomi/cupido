package unibo.as.cupido.backendInterfacesImpl;

import java.rmi.Remote;
import java.util.Arrays;
import java.util.Collections;

import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.ChatMessage;
import unibo.as.cupido.backendInterfacesImpl.PlayersManager.PlayerInfo;

public class BotNotification implements Remote, BotNotificationInterface {

	private Card[] cards;
	private final PlayerInfo[] playersInfo;
	private final int position;

	public BotNotification(PlayerInfo[] playersInfo, int position) {
		this.playersInfo = playersInfo;
		this.position = position;
	}

	@Override
	public void notifyGameEnded(int[] matchPoints, int[] playersTotalPoint) {
		System.out.println("\n" + Thread.currentThread() + " "
				+ Thread.currentThread().getStackTrace()[1].getMethodName() + "(" + Arrays.toString(matchPoints) + ", "
				+ Arrays.toString(playersTotalPoint) + ")");

	}

	@Override
	public void notifyGameStarted(Card[] cards) {
		this.cards = cards;
		System.out.println("\n" + Thread.currentThread() + " "
				+ Thread.currentThread().getStackTrace()[1].getMethodName() + "(" + Arrays.toString(cards) + ")");
		for (Card card : cards){
			
		}
	}

	@Override
	public void notifyLocalChatMessage(ChatMessage message) {
		System.out.println("\n" + Thread.currentThread() + " "
				+ Thread.currentThread().getStackTrace()[1].getMethodName() + "(" + message + ")");
	}

	@Override
	public void notifyPassedCards(Card[] cards) {
		System.out.println("\n" + Thread.currentThread() + " "
				+ Thread.currentThread().getStackTrace()[1].getMethodName() + "(" + Arrays.toString(cards) + ")");
	}

	@Override
	public void notifyPlayedCard(Card card, int playerPosition) {
		System.out.println("\n" + Thread.currentThread() + " "
				+ Thread.currentThread().getStackTrace()[1].getMethodName() + "(" + card + ", " + playerPosition + ")");
	}

	@Override
	public void notifyPlayerJoined(String name, boolean isBot, int point, int position) {
		System.out.println("\n" + Thread.currentThread() + " "
				+ Thread.currentThread().getStackTrace()[1].getMethodName() + "(" + name + ", " + isBot + "," + point
				+ "," + position + ")");
	}

	@Override
	public void notifyPlayerLeft(String name) {
		System.out.println("\n" + Thread.currentThread() + " "
				+ Thread.currentThread().getStackTrace()[1].getMethodName() + "(" + name + ")");
	}
}
