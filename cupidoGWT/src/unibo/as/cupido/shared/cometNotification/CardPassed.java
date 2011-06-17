package unibo.as.cupido.shared.cometNotification;

import java.io.Serializable;

import unibo.as.cupido.backendInterfaces.common.Card;

/*
 * Card passed to you from the player at your right
 */
public class CardPassed implements Serializable {

	private static final long serialVersionUID = 1L;

	/*
	 * Card.lenght is always 3
	 */
	public Card[] cards;
}
