package unibo.as.cupido.client;

import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.Card.Suit;

import com.google.gwt.user.client.ui.Image;

public class CardWidget extends Image {
	
	private Card card;

	// Constructs a CardWidget that displays the back of a card.
	public CardWidget() {
		super(constructCardName(null));
		card = null;
	}
	
	public CardWidget(Card card) {
		super(constructCardName(card));
		this.card = card;
	}
	
	public Card getCard() {
		return card;
	}
	
	private static String constructCardName(Card card) {
		final String path_prefix = "classic_cards/";
		if (card == null)
			return path_prefix + "back_blue.png";
		Suit suit = card.suit;
		int value = card.value;
		String result = "";
		assert value >= 1;
		assert value <= 13;
		if (value <= 10)
			result = "" + value;
		else {
			switch (value) {
			case 11:
				result = "jack";
				break;
			case 12:
				result = "queen";
				break;
			case 13:
				result = "king";
				break;
			}
		}
		result += "_";
		switch (suit) {
		case CLUBS:
			result += "clubs";
			break;
		case DIAMONDS:
			result += "diamonds";
			break;
		case HEARTS:
			result += "hearts";
			break;
		case SPADES:
			result += "spades";
			break;
		}
		result = path_prefix + result + ".png";
		return result;
	}
}
