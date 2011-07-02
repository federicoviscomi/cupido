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

package unibo.as.cupido.shared.cometNotification;

import java.io.Serializable;

import unibo.as.cupido.common.structures.Card;

/**
 * This class is used for comet notifications.
 * 
 * A CardPlayed notification is sent when another user (or a bot)
 * that is at the same table as the current user plays a card.
 */
public class CardPlayed implements Serializable {

	/***/
	private static final long serialVersionUID = 1L;

	/**
	 * The card that was played.
	 */
	public Card card;

	/**
	 * The position of the player who played the card.
	 * 
	 * If the current user is a viewer then <code>playerPosition</code> is
	 * the absolute position in the table of the player who played a card
	 * and is in [0-3] range.
	 * 
	 * Otherwise (if the current user is a player), <code>playerPosition</code>
	 * is the relative position of the player who played the card,
	 * and is in the [0-2] range.
	 */
	public int playerPosition;

	/**
	 * The default constructor.
	 */
	public CardPlayed() {
	}

	/**
	 * A constructor that initializes all fields with the provided values.
	 * 
	 * @param card The card that was played.
	 * @param playerPosition The position of the player who played the card.
	 */
	public CardPlayed(Card card, int playerPosition) {
		this.card = card;
		this.playerPosition = playerPosition;
	}
}