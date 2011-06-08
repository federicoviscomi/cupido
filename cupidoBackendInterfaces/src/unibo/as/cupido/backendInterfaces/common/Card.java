/**
 * 
 */
package unibo.as.cupido.backendInterfaces.common;

import unibo.as.cupido.backendInterfaces.common.Card.Suit;

/**
 * @author Lorenzo Belli
 * 
 */
public class Card {

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
		return String.format("[%8.8s %2.2s]", suit, value);
	}
}
