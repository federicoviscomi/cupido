package unibo.as.cupido.shared.cometNotification;

import java.io.Serializable;

import unibo.as.cupido.shared.Card;

public class CardPlayed implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	Card card;
	String playerName;
	/*
	 * if you are playing
	 * playerPosition = 0 is the player are you
	 * 
	 * if you are viewing the match
	 * playerPosition = 0 is the player is the owner
	 *  
	 * the others are clockwise
	 */
	int playerPosition;

}