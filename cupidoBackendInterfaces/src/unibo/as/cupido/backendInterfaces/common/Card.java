/**
 * 
 */
package unibo.as.cupido.backendInterfaces.common;

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

	@Override
	public String toString() {
		return String.format("[%8.8s %2.2s]", suit, value);
	}
}
