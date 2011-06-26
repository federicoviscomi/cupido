/*  Cupido - An online Hearts game.
 *  Copyright (C) 2011 Lorenzo Belli, Marco Poletti, Federico Viscomi
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package unibo.as.cupido.client.widgets;

import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.Card.Suit;

import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.user.client.ui.Image;

public class CardWidget extends Image {

	private Card card;
	private int rotation;

	/**
	 * The width of cards (before rotation).
	 */
	public static final int cardWidth = 72;

	/**
	 * The height of cards (before rotation).
	 */
	public static final int cardHeight = 96;

	/**
	 * The width of the cards' border (before rotation).
	 */
	public static final int borderWidth = 13;

	/**
	 * Constructs a CardWidget that displays the back of a card.
	 */
	public CardWidget() {
		super(constructCardName(null, 0));
		preventDrag();
		card = null;
		rotation = 0;
	}

	/**
	 * Constructs a CardWidget that displays the back of a card.
	 * 
	 * @param rotation
	 *            This is the rotation of the card, in degrees.
	 */
	public CardWidget(int rotation) {
		super(constructCardName(null, rotation));
		preventDrag();
		card = null;
		this.rotation = rotation;
	}

	/**
	 * Constructs a CardWidget that displays the specified card.
	 * 
	 * @param rotation
	 *            This is the rotation of the card, in degrees.
	 */
	public CardWidget(Card card, int rotation) {
		super(constructCardName(card, rotation));
		preventDrag();
		this.card = card;
		this.rotation = rotation;
	}

	/**
	 * Constructs a CardWidget that displays the specified card.
	 */
	public CardWidget(Card card) {
		super(constructCardName(card, 0));
		preventDrag();
		this.card = card;
		this.rotation = 0;
	}

	public Card getCard() {
		return card;
	}

	public void setCard(Card newCard) {
		if (card == null && newCard == null)
			return;
		if (card != null && newCard != null && card.suit == newCard.suit
				&& card.value == newCard.value)
			return;
		this.card = newCard;
		setUrl(constructCardName(card, rotation));
	}

	public void setRotation(int newRotation) {
		if (rotation == newRotation)
			return;
		this.rotation = newRotation;
		setUrl(constructCardName(card, rotation));
	}

	private void preventDrag() {
		addMouseDownHandler(new MouseDownHandler() {
			@Override
			public void onMouseDown(MouseDownEvent event) {
				event.preventDefault();
			}
		});
	}

	private static String constructCardName(Card card, int rotation) {
		final String path_prefix = "classic_cards/";
		rotation = rotation % 360;
		assert rotation % 90 == 0;
		String result = path_prefix + "rotated_" + rotation + "/";
		if (card == null)
			return result + "back_blue.png";
		Suit suit = card.suit;
		int value = card.value;
		assert value >= 1;
		assert value <= 13;
		result += value + "_";
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
		return result + ".png";
	}
}
