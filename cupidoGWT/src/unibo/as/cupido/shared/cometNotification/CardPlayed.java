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

public class CardPlayed implements Serializable {

	private static final long serialVersionUID = 1L;

	public Card card;

	/**
	 * Position of the player who played the card. If you are a viewer then
	 * <code>playerPosition</code> is the absolute position in the table of the
	 * player who played a card and is in range 0-3. Otherwise if you are a
	 * player then <code>playerPosition</code> is the position of the player who
	 * played relative you and is in range 0-2.
	 * 
	 */
	public int playerPosition;

	public CardPlayed() {
	}

	public CardPlayed(Card card, int playerPosition) {
		this.card = card;
		this.playerPosition = playerPosition;
	}
}