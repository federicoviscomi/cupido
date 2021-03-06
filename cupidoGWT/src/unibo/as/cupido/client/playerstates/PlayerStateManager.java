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
import unibo.as.cupido.shared.cometNotification.CardPassed;
import unibo.as.cupido.shared.cometNotification.CardPlayed;
import unibo.as.cupido.shared.cometNotification.GameEnded;
import unibo.as.cupido.shared.cometNotification.GameStarted;
import unibo.as.cupido.shared.cometNotification.PlayerReplaced;

/**
 * The interface implemented by the manager of the game states used when the
 * current user is a player.
 * 
 * @see ViewerStateManager
 */
public interface PlayerStateManager {

	/**
	 * This class stores the information that the state manager needs about each
	 * player.
	 */
	public class PlayerInfo {
		/**
		 * Specifies whether this player is a bot or a human player.
		 */
		public boolean isBot;

		/**
		 * This is relevant only when <code>isBot</code> is <code>false</code>.
		 */
		public String name;
	}

	/**
	 * Lets the state manager know that the player in the specified position
	 * played the specified card.
	 * 
	 * @param player
	 *            The position of the player that played the card.
	 * @param card
	 *            The card that has been played.
	 */
	public void addPlayedCard(int player, Card card);

	/**
	 * @return <code>true</code> if the hearts have already been broken, false
	 *         otherwise.
	 */
	public boolean areHeartsBroken();

	/**
	 * Exits from the game.
	 */
	public void exit();

	/**
	 * When this is called, the state manager stops responding to events and
	 * disables all user controls, including the <code>CardsGameWidget</code>.
	 */
	public void freeze();

	/**
	 * @return Returns the leading player for the current trick. A return value
	 *         of 0 means the bottom player, and other players' indexes follow
	 *         in clockwise order. Returns -1 in the initial card-passing states
	 *         and at the beginning of the first trick.
	 */
	public int getFirstPlayerInTrick();

	/**
	 * @return The ordered list containing the cards played in the current
	 *         trick.
	 */
	public List<Card> getPlayedCards();

	/**
	 * @return A list in which each element contains information about a
	 *         specific player in the game.
	 */
	public List<PlayerInfo> getPlayerInfo();

	/**
	 * @return The <code>CardsGameWidget</code> that is managed by this class.
	 */
	public CardsGameWidget getWidget();

	/**
	 * Notifies the state manager that the current trick is completed, and
	 * starts a new trick.
	 */
	public void goToNextTrick();

	/**
	 * This is called when a <code>CardPassed</code> notification is received
	 * from the servlet.
	 * 
	 * @param cards
	 *            The cards that were passed to the user.
	 * 
	 * @see CardPassed
	 */
	public void handleCardPassed(Card[] cards);

	/**
	 * This is called when a <code>CardPlayed</code> notification is received
	 * from the servlet.
	 * 
	 * @param card
	 *            The card that has been played.
	 * @param playerPosition
	 *            The position of the player that played this card.
	 * 
	 * @see CardPlayed
	 */
	public void handleCardPlayed(Card card, int playerPosition);

	/**
	 * This is called when a <code>GameEnded</code> notification is received
	 * from the servlet.
	 * 
	 * @param matchPoints
	 *            The score scored by the players during the current game.
	 * @param playersTotalPoints
	 *            The total score of the players, already updated with the
	 *            results of the current game.
	 * 
	 * @see GameEnded
	 */
	public void handleGameEnded(int[] matchPoints, int[] playersTotalPoints);

	/**
	 * This is called when a GameStarted notification is received from the
	 * servlet.
	 * 
	 * @param myCards
	 *            The cards that the player received from the dealer.
	 * 
	 * @see GameStarted
	 */
	public void handleGameStarted(Card[] myCards);

	/**
	 * This is called when a <code>PlayerReplaced</code> notification is
	 * received from the servlet.
	 * 
	 * @param name
	 *            The name of the bot that replaced the player.
	 * @param position
	 *            The position in the table where the player resided.
	 * 
	 * @see PlayerReplaced
	 */
	public void handlePlayerReplaced(String name, int position);

	/**
	 * Reacts to a fatal exception.
	 * 
	 * @param e
	 *            The exception that was caught.
	 */
	public void onFatalException(Throwable e);

	/**
	 * Changes the current state to <code>CardPassingState</code>.
	 * 
	 * @param hand
	 *            The cards in the user's hand.
	 */
	public void transitionToCardPassing(List<Card> hand);

	/**
	 * Changes the current state to <code>CardPassingWaitingState</code>.
	 * 
	 * @param hand
	 *            The cards in the user's hand.
	 */
	public void transitionToCardPassingWaiting(List<Card> hand);

	/**
	 * Changes the current state to <code>EndOfTrickState</code>.
	 * 
	 * @param hand
	 *            The cards in the user's hand.
	 */
	public void transitionToEndOfTrick(List<Card> hand);

	/**
	 * Changes the current state to <code>FirstLeaderState</code>.
	 * 
	 * @param hand
	 *            The cards in the user's hand.
	 */
	public void transitionToFirstLeader(List<Card> hand);

	/**
	 * Changes the current state to <code>GameEndedState</code>.
	 */
	public void transitionToGameEnded();

	/**
	 * Changes the current state to <code>WaitingFirstLeadState</code>.
	 * 
	 * @param hand
	 *            The cards in the user's hand.
	 */
	public void transitionToWaitingFirstLead(List<Card> hand);

	/**
	 * Changes the current state to <code>CardPassingWaitingState</code>.
	 * 
	 * @param hand
	 *            The cards in the user's hand.
	 */
	public void transitionToWaitingPlayedCard(List<Card> hand);

	/**
	 * Changes the current state to <code>YourTurnState</code>.
	 * 
	 * @param hand
	 *            The cards in the user's hand.
	 */
	public void transitionToYourTurn(List<Card> hand);
}
