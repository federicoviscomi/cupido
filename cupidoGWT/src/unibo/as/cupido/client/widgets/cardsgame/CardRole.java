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

/**
 * This contains some data about a card on the table.
 */
public class CardRole {
	public enum State {
		HAND, PLAYED
	}

	/**
	 * This is valid only when state==HAND. It specifies whether this card is
	 * raised.
	 */
	public boolean isRaised;

	/**
	 * The player to whom the card belongs.
	 */
	public int player;

	/**
	 * The state of the card (see the State enum).
	 */
	public CardRole.State state;

	public CardRole() {
	}

	public CardRole(CardRole.State state, boolean raised, int player) {
		this.state = state;
		this.player = player;
	}

	@Override
	public boolean equals(Object obj) {
		// Note that the `isRaised' field is *not* compared.
		if (obj != null && obj instanceof CardRole) {
			CardRole x = (CardRole) obj;
			return (player == x.player && state == x.state);
		} else
			return false;
	}

	@Override
	public int hashCode() {
		// Note that the `isRaised' field does *not* change the hash code.
		// This is needed to be consistent with equals().
		assert player >= 0;
		assert player < 4;
		switch (state) {
		case HAND:
			return player;
		case PLAYED:
			return player + 4;
		}
		throw new IllegalStateException();
	}
}