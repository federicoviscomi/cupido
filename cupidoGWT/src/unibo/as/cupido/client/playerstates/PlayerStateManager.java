/*  Cupido - An online Hearts game.
 *  Copyright (C) 2011 Lorenzo Belli, Marco Poletti, Federico Viscomi
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package unibo.as.cupido.client.playerstates;

import java.util.List;

import unibo.as.cupido.client.viewerstates.ViewerStateManager;
import unibo.as.cupido.client.widgets.CardsGameWidget;
import unibo.as.cupido.common.structures.Card;

/**
 * The interface implemented by the manager of the game states used
 * when the current user is a player.
 * 
 * @see ViewerStateManager
 */
public interface PlayerStateManager {

	public class PlayerInfo {
		public boolean isBot;
		/**
		 * This is relevant only when `isBot' is false.
		 */
		public String name;
	}

	public void addPlayedCard(int player, Card card);

	public boolean areHeartsBroken();

	/**
	 * Exits from the game.
	 */
	public void exit();

	/**
	 * When this is called, the state manager stops responding to events
	 * and disables all user controls, including the CardsGameWidget.
	 */
	public void freeze();

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
	 * @return The ordered list containing the cards played in the current
	 *         trick.
	 */
	public List<Card> getPlayedCards();

	public List<PlayerInfo> getPlayerInfo();

	public CardsGameWidget getWidget();

	public void goToNextTrick();

	public void handleCardPassed(Card[] cards);

	public void handleCardPlayed(Card card, int playerPosition);

	public void handleGameEnded(int[] matchPoints, int[] playersTotalPoints);

	public void handleGameStarted(Card[] myCards);

	public void handlePlayerReplaced(String name, int position);

	/**
	 * Reacts to a fatal exception.
	 * 
	 * @param e The exception that was caught.
	 */
	public void onFatalException(Throwable e);

	/**
	 * Changes the current state to CardPassingState.
	 * 
	 * @param hand The cards in the user's hand.
	 */
	public void transitionToCardPassing(List<Card> hand);

	/**
	 * Changes the current state to CardPassingWaitingState.
	 * 
	 * @param hand The cards in the user's hand.
	 */
	public void transitionToCardPassingWaiting(List<Card> hand);

	/**
	 * Changes the current state to EndOfTrickState.
	 * 
	 * @param hand The cards in the user's hand.
	 */
	public void transitionToEndOfTrick(List<Card> hand);

	/**
	 * Changes the current state to FirstLeaderState.
	 * 
	 * @param hand The cards in the user's hand.
	 */
	public void transitionToFirstLeader(List<Card> hand);

	/**
	 * Changes the current state to GameEndedState.
	 */
	public void transitionToGameEnded();

	/**
	 * Changes the current state to WaitingFirstLeadState.
	 * 
	 * @param hand The cards in the user's hand.
	 */
	public void transitionToWaitingFirstLead(List<Card> hand);

	/**
	 * Changes the current state to CardPassingWaitingState.
	 * 
	 * @param hand The cards in the user's hand.
	 */
	public void transitionToWaitingPlayedCard(List<Card> hand);

	/**
	 * Changes the current state to YourTurnState.
	 * 
	 * @param hand The cards in the user's hand.
	 */
	public void transitionToYourTurn(List<Card> hand);
}
