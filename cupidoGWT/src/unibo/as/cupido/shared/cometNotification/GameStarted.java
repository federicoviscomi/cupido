package unibo.as.cupido.shared.cometNotification;

import java.io.Serializable;

import unibo.as.cupido.common.structures.Card;

/*
 * this notification isn's sent to viewers
 */
public class GameStarted implements Serializable {

	private static final long serialVersionUID = 1L;

	public Card[] myCards;

}
