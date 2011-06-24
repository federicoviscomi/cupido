package unibo.as.cupido.client.screens;

import unibo.as.cupido.common.structures.Card;

/**
 * This is meant as a base class for classes that handle messages received
 * through comet. Derived classes must override the relevant methods, without
 * calling the base class methods. This is needed because the base class methods
 * assume the message can't be handled.
 */
public class CometMessageListener {
	public void onPlayerLeft(String player) {
		System.out
				.println("Client: the PlayerLeft comet message can't be handled in the current state, ignoring it.");
	}

	public void onNewPlayerJoined(String name, boolean isBot, int points,
			int position) {
		System.out
				.println("Client: the NewPlayerJoined comet message can't be handled in the current state, ignoring it.");
	}

	public void onGameEnded(int[] matchPoints, int[] playersTotalPoints) {
		System.out
				.println("Client: the GameEnded comet message can't be handled in the current state, ignoring it.");
	}

	public void onCardPlayed(Card card, int playerPosition) {
		System.out
				.println("Client: the CardPlayed comet message can't be handled in the current state, ignoring it.");
	}

	public void onCardPassed(Card[] cards) {
		System.out
				.println("Client: the CardPassed comet message can't be handled in the current state, ignoring it.");
	}

	public void onGameStarted(Card[] myCards) {
		System.out
				.println("Client: the GameStarted comet message can't be handled in the current state, ignoring it.");
	}

	public void onNewLocalChatMessage(String user, String message) {
		System.out
				.println("Client: the NewLocalChatMessage comet message can't be handled in the current state, ignoring it.");
	}
}