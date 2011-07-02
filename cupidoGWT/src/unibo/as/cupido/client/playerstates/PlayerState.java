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

import unibo.as.cupido.client.viewerstates.ViewerState;
import unibo.as.cupido.client.widgets.cardsgame.CardRole;
import unibo.as.cupido.common.structures.Card;

/**
 * The interface implemented by all game states in which the current
 * user is a player.
 *
 * @see ViewerState
 */
public interface PlayerState {

	/**
	 * Activates the state. This is needed, because a state can't start an
	 * animation in the constructor because it is not registered as the event
	 * listener yet, so it would miss some animation-related events.
	 */
	public void activate();

	/**
	 * When this is called, the state stops responding to events
	 * and disables all user controls, except the <code>CardsGameWidget</code>,
	 * that is not affected by this call.
	 */
	public void freeze();

	/**
	 * This is called when an animation finishes.
	 */
	public void handleAnimationEnd();

	/**
	 * This is called before starting an animation.
	 */
	public void handleAnimationStart();

	/**
	 * This is called when the user clicks on a card, except during animations
	 * and when the <code>CardsGameWidget</code>'s controls are disabled.
	 * 
	 * @param player The player to whom the card belongs
	 * @param card The card that was clicked, or <code>null</code> if a covered card was
	 *            clicked.
	 * @param state The state of the card that was clicked.
	 * @param isRaised This is <code>true</code> only if <code>state==HAND</code> and this card
	 *  is currently raised.
	 */
	public void handleCardClicked(int player, Card card, CardRole.State state,
			boolean isRaised);

	/**
	 * Handles a <code>CardPassed</code> notification received from the servlet.
	 * 
	 * @param cards The cards that were passed to the user.
	 * 
	 * @return <code>false</code> if this event can't be handled right now, but it can be
	 *         handled in a later state. It will be notified again at each state
	 *         transition, until it is handled.
	 */
	public boolean handleCardPassed(Card[] cards);

	/**
	 * Handles a <code>CardPlayed</code> notification received from the servlet.
	 * 
	 * @param card The card that has been played.
	 * @param playerPosition The position of the player that played this card.
	 * 
	 * @return <code>false</code> if this event can't be handled right now, but it can be
	 *         handled in a later state. It will be notified again at each state
	 *         transition, until it is handled.
	 */
	public boolean handleCardPlayed(Card card, int playerPosition);

	/**
	 * Handles a <code>GameEnded</code> notification received from the servlet.
	 * 
	 * @param matchPoints The score scored by the players during the current game.
	 * @param playersTotalPoints The total score of the players, already updated
	 *                           with the results of the current game.
	 * 
	 * @return <code>false</code> if this event can't be handled right now, but it can be
	 *         handled in a later state. It will be notified again at each state
	 *         transition, until it is handled.
	 */
	public boolean handleGameEnded(int[] matchPoints, int[] playersTotalPoints);

	/**
	 * Handles a <code>GameStarted</code> notification received from the servlet.
	 * 
	 * @param myCards The cards that the player received from the dealer.
	 * 
	 * @return <code>false</code> if this event can't be handled right now, but it can be
	 *         handled in a later state. It will be notified again at each state
	 *         transition, until it is handled.
	 */
	public boolean handleGameStarted(Card[] myCards);

	/**
	 * Handles a <code>PlayerReplaced</code> notification received from the servlet.
	 * 
	 * @param name The name of the bot that replaced the player.
	 * @param position The position in the table where the player resided.
	 */
	public void handlePlayerReplaced(String name, int position);
}
