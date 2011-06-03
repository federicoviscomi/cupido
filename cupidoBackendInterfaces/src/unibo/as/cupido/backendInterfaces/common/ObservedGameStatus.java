package unibo.as.cupido.backendInterfaces.common;

/**
 * 
 * Contains the information that an observer needs when he joins a table
 * 
 * @author cane
 * 
 */
public class ObservedGameStatus {

	class PlayerStatus {
		String name;

		/*
		 * total player points
		 */
		int point;

		/*
		 * Card == null means player has not played a card
		 */
		Card playedCard;

		int numOfCardsInHand;

		boolean isBot;
	}

	/*
	 * players are ordered clockwise
	 */
	PlayerStatus[] players;

}
