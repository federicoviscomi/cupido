package unibo.as.cupido.backendInterfaces.common;

import java.io.Serializable;

public class PlayerStatus implements Serializable {
	public String name;

	/*
	 * total player points
	 */
	public int score;

	/*
	 * Card == null means player has not played a card
	 */
	public Card playedCard;

	public int numOfCardsInHand;

	public boolean isBot;

	public PlayerStatus() {
	}

	public PlayerStatus(String name, int point, Card playedCard, int numOfCardsInHand, boolean isBot) {
		this.name = name;
		this.score = point;
		this.playedCard = playedCard;
		this.numOfCardsInHand = numOfCardsInHand;
		this.isBot = isBot;
	}
}
