package unibo.as.cupido.client;

import com.google.gwt.user.client.Random;

import unibo.as.cupido.backendInterfaces.common.Card;

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
