package unibo.as.cupido.backendInterfaces.common;

/**
 * 
 * Contains the information that an observer needs when he joins a table
 * 
 * @author cane
 * 
 */
public class ObservedGameStatus {

	public ObservedGameStatus(PlayerStatus[] players) {
		this.players = players;
	}

	public static class PlayerStatus {
		public PlayerStatus(String name, int point, Card playedCard, int numOfCardsInHand, boolean isBot) {
			this.name = name;
			this.point = point;
			this.playedCard = playedCard;
			this.numOfCardsInHand = numOfCardsInHand;
			this.isBot = isBot;
		}

		public String name;

		/*
		 * total player points
		 */
		public int point;

		/*
		 * Card == null means player has not played a card
		 */
		public Card playedCard;

		public int numOfCardsInHand;

		public boolean isBot;
	}

	/*
	 * players are ordered clockwise
	 */
	public PlayerStatus[] players;

}
