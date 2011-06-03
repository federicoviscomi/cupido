package unibo.as.cupido.shared;

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
		
	}

	/*
	 *players are ordered clockwise 
	 */
	PlayerStatus[] players;

}
