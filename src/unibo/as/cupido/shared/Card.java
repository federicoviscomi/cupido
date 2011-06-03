/**
 * 
 */
package unibo.as.cupido.shared;

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
	 * Cards value range is [1-13]
	 */
	public int value;
}
