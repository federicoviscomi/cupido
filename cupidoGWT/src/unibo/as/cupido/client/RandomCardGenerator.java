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

package unibo.as.cupido.client;

import unibo.as.cupido.common.structures.Card;

import com.google.gwt.user.client.Random;

/**
 * FIXME: Remove this class when the servlet is ready.
 * 
 * @author marco
 */
public class RandomCardGenerator {

	static public Card generateCard() {
		Card card = new Card();
		card.value = Random.nextInt(13) + 1;
		int n = Random.nextInt(4);
		switch (n) {
		case 0:
			card.suit = Card.Suit.SPADES;
			break;
		case 1:
			card.suit = Card.Suit.DIAMONDS;
			break;
		case 2:
			card.suit = Card.Suit.CLUBS;
			break;
		case 3:
			card.suit = Card.Suit.HEARTS;
			break;
		}
		return card;
	}
}
