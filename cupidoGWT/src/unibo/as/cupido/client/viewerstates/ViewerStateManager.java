package unibo.as.cupido.client.viewerstates;

import java.util.List;

import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.client.CardsGameWidget;

public interface ViewerStateManager {

	public class PlayerInfo {
		/**
		 * This is relevant only when `isBot' is false.
		 */
		String name;
		boolean isBot;
	}

	/**
	 * Returns the leading player for the current trick. A return value of 0
	 * means the bottom player, and other players' indexes follow in clockwise
	 * order.
	 * 
	 * This returns -1 in the initial card-passing states and at the beginning
	 * of the first trick.
	 */
	public int getFirstPlayerInTrick();

	/**
	 * @return The ordered list containing the cards dealt in the current trick.
	 */
	public List<Card> getDealtCards();

	public void addDealtCard(int player, Card card);

	public void goToNextTrick();

	/**
	 * @return The number of remaining tricks, including the current one (if
	 *         any).
	 */
	public int getRemainingTricks();

	/**
	 * Exits from the game.
	 */
	public void exit();

	public List<PlayerInfo> getPlayerInfo();

	CardsGameWidget getWidget();

	public void transitionToEndOfTrick();

	public void transitionToWaitingFirstDeal();

	public void transitionToGameEnded();

	public void transitionToWaitingDeal();
	
	public void disableControls();

	public void handleCardPlayed(Card card, int playerPosition);

	public void handleGameEnded(int[] matchPoints, int[] playersTotalPoints);

	public void handleNewPlayerJoined(String name, boolean isBot, int points,
			int position);

	public void handlePlayerLeft(String player);
}
