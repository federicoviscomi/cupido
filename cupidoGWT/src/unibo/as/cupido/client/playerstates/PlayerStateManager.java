package unibo.as.cupido.client.playerstates;

import java.util.List;

import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.client.CardsGameWidget;

public interface PlayerStateManager {
	
	public class PlayerInfo {
		/**
		 * This is relevant only when `isBot' is false.
		 */
		String name;
		boolean isBot;
	}

	/**
	 * Returns the leading player for the current trick.
	 * A return value of 0 means the bottom player, and other players' indexes
	 * follow in clockwise order.
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
	
	public boolean areHeartsBroken();
	
	/**
	 * Exits from the game.
	 */
	public void exit();
	
	public List<PlayerInfo> getPlayerInfo();
	
	public void transitionToCardPassingAsPlayer(List<Card> hand);
	public void transitionToCardPassingWaitingAsPlayer(List<Card> hand);
	public void transitionToEndOfTrickAsPlayer(List<Card> hand);
	public void transitionToFirstDealer(List<Card> hand);
	public void transitionToWaitingDealAsPlayer(List<Card> hand);
	public void transitionToWaitingFirstDealAsPlayer(List<Card> hand);
	public void transitionToYourTurn(List<Card> hand);
	public void transitionToGameEndedAsPlayer();

	CardsGameWidget getWidget();
}
