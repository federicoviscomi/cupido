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
 * A <code>CardPassed</code> notification is sent when the user
 * is playing a game and receives some cards from another player.
 */
public class CardPassed implements Serializable {

	/***/
	private static final long serialVersionUID = 1L;

	/**
	 * An array containing the three cards that have been passed
	 * to the current user.
	 */
	public Card[] cards;
	
	/**
	 * The default constructor.
	 */
	public CardPassed() {
	}
	
	/**
	 * A constructor that initializes the field with the specified value.
	 * 
	 * @param cards The desired value for the corresponding field.
	 */
	public CardPassed(Card[] cards) {
		this.cards = cards;
	}
}
