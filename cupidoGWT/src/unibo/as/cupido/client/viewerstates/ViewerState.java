package unibo.as.cupido.client.viewerstates;

import unibo.as.cupido.backendInterfaces.common.Card;

public interface ViewerState {

	public void disableControls();
	
	public void handleCardPlayed(Card card, int playerPosition);

	public void handleGameEnded(int[] matchPoints, int[] playersTotalPoints);

	public void handleNewPlayerJoined(String name, boolean isBot, int points,
			int position);

	public void handlePlayerLeft(String player);

}
