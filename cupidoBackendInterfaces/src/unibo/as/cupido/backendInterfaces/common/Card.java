/**
 * 
 */
package unibo.as.cupido.backendInterfaces.common;

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
		String stringSuit = "";
		switch (suit) {
		case CLUBS:
			stringSuit = "CLUBS";
			break;
		case DIAMONDS:
			stringSuit = "DIAMONDS";
			break;
		case HEARTS:
			stringSuit = "HEARTS";
			break;
		case SPADES:
			stringSuit = "SPADES";
			break;
		}
		return "[" + stringSuit + " " + value + "]";
	}
}
