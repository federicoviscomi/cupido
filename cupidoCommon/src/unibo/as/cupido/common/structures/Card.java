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
}
