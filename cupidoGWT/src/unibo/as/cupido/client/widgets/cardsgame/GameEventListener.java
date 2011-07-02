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

package unibo.as.cupido.client.widgets.cardsgame;

import unibo.as.cupido.common.structures.Card;

/**
 * This is used by <code>CardsGameWidget</code> to notify users of the class
 * about various events.
 */
public interface GameEventListener {
	/**
	 * This is called when an animation finishes.
	 */
	public void onAnimationEnd();

	/**
	 * This is called before starting an animation.
	 */
	public void onAnimationStart();

	/**
	 * This is called when the user clicks on a card, except during animations
	 * and when controls are disabled.
	 * 
	 * @param player
	 *            The player to whom the card belongs
	 * @param card
	 *            The card that was clicked, or <code>null</code> if a covered
	 *            card was clicked.
	 * @param state
	 *            The current state of the card.
	 * @param isRaised
	 *            This is true only if <code>state==HAND</code> and this card is
	 *            currently raised.
	 */
	public void onCardClicked(int player, Card card, CardRole.State state,
			boolean isRaised);

	/**
	 * This is called when the user clicks on the exit button.
	 * 
	 * Note that clicking the button only triggers this method; if the caller
	 * wants to freeze the <code>CardsGameWidget</code>, he must do so
	 * explicitly.
	 */
	public void onExit();
}
