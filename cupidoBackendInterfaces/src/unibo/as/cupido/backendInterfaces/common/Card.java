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

	public enum Suit {
		DIAMONDS, SPADES, HEARTS, CLUBS
	}

	public Suit suit;
	/*
	 * Cards value range is [1-13]
	 */
	public int value;
}
