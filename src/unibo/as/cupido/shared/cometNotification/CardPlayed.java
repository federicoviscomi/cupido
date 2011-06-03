package unibo.as.cupido.shared.cometNotification;

import java.io.Serializable;

import unibo.as.cupido.shared.Card;

public class CardPlayed implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	Card card;
	String playerName;

}
