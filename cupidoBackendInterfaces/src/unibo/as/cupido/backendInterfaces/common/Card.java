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
	 * Cards value range is [2-14]
	 * 14 e' l'asso
	 * 
	 */
	public int value;
}
