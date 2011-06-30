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

/**
 * 
 */
package unibo.as.cupido.common.structures;

import java.io.Serializable;

/**
 * @author Lorenzo Belli
 * 
 */
public class Card implements Serializable {

	private static final long serialVersionUID = 1L;

	public enum Suit {
		DIAMONDS, SPADES, HEARTS, CLUBS
	}

	public Suit suit;
	/*
	 * 
	 * Cards value range is [1-13]
	 */
	public int value;

	public Card(int value, Suit suit) {
		this.value = value;
		this.suit = suit;
	}

	public Card() {
		//
	}

	@Override
	public String toString() {
		return "[" + suit + " " + value + "]";
	}
	
	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (!(other instanceof Card))
			return false;
		Card otherCard = (Card) other;
		return this.value == otherCard.value && this.suit == otherCard.suit;
	}
	
	public Card clone() {
		return new Card(value, suit);
	}
}
