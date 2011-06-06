package unibo.as.cupido.client;

import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.Card.Suit;

import com.google.gwt.user.client.ui.Image;

public class CardWidget extends Image {
	
	private Card card;

	// Constructs a CardWidget that displays the back of a card.
	public CardWidget() {
		super("card_back.png");
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
		if (card == null)
			return "back.png";
		Suit suit = card.suit;
		int value = card.value;
		String result = "";
		assert value >= 1;
		assert value <= 13;
		if (value < 10)
			result = "0" + value;
		else {
			switch (value) {
			case 10:
				result = "10";
				break;
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
		result += "_of_";
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
		result += ".png";
		return result;
	}
}
