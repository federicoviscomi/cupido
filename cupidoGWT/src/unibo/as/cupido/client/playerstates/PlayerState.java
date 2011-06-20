package unibo.as.cupido.client.playerstates;

import unibo.as.cupido.backendInterfaces.common.Card;

public interface PlayerState {

	public void disableControls();
	
	public void handleCardPassed(Card[] cards);

	public void handleCardPlayed(Card card, int playerPosition);

	public void handleGameEnded(int[] matchPoints, int[] playersTotalPoints);

	public void handleGameStarted(Card[] myCards);

	public void handlePlayerLeft(String player);
}
