package unibo.as.cupido.shared.cometNotification;

import java.io.Serializable;

import unibo.as.cupido.common.structures.Card;

public class CardPlayed implements Serializable {

	private static final long serialVersionUID = 1L;

	public Card card;

	/**
	 * Position of the player who played the card. If you are a viewer then
	 * <code>playerPosition</code> is the absolute position in the table of the
	 * player who played a card and is in range 0-3. Otherwise if you are a
	 * player then <code>playerPosition</code> is the position of the player who
	 * played relative you and is in range 0-2.
	 * 
	 */
	public int playerPosition;

	public CardPlayed() {
	}

	public CardPlayed(Card card, int playerPosition) {
		this.card = card;
		this.playerPosition = playerPosition;
	}
}