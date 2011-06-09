package unibo.as.cupido.client;

import java.util.ArrayList;
import java.util.List;

import unibo.as.cupido.backendInterfaces.common.Card;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.AbsolutePanel;

public class CardsGameWidget extends AbsolutePanel {
	
	private PlayersCards cards;
	
	/**
	 * The positions of the cards (both hands' cards and dealt cards) on the table.
	 */
	private CardPositions cardPositions;
	
	/**
	 * The widgets displaying the cards (both hands' cards and dealt cards) on the table.
	 */
	private CardWidgets cardWidgets;

	/**
	 * The size of the table (width and height) in pixels.
	 */
	private int tableSize;

	/**
	 * This class models the position of a card on the table.
	 * 
	 * @author marco
	 */
	static class Position {
		
		public Position() {
		}
		
		public Position(int x, int y, int z, int rotation) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.rotation = rotation;
		}
		
		/**
		 * The distance between the left margin and the center of the card.
		 */
		public int x;
		
		/**
		 * The distance between the top margin and the center of the card.
		 */
		public int y;
		
		/**
		 * The height of the point. Objects with higher values of z are drawn above
		 * objects with lower values.
		 */
		public int z;
		
		/**
		 * The rotation is measured in degrees. When this is 0, there is no rotation.
		 * The rotation is clockwise, so a card with rotation `90' will have its top
		 * pointed towards the left edge of the table.
		 */
		public int rotation;
	}
	
	static class CardPositions {
		public List<List<Position>> hands;
		public List<List<Position>> dealt;
	}
	
	static class CardWidgets {
		public List<List<CardWidget>> hands;
		public List<List<CardWidget>> dealt;
	}
	
	static class PlayersCards {
		/**
		 * 	The players' hands. hands[0] is the hand of the bottom player, and the other players
		 *  follow in clockwise order.
		 *  
		 *  A `null' means a covered card.
		 */
		List<List<Card>> hands;
		
		/**
		 * 	The cards dealt by each player in the current trick.
		 *  dealt[0] are the cards dealt by the bottom player, and the other players' cards
		 *  follow in clockwise order.
		 *  
		 *  A `null' means a covered card.
		 */
		List<List<Card>> dealt;
	}
	
	/**
	 * 
	 * @param tableSize The width and height of this widget.
	 * @param playersCards
	 *     The players' cards. playersCards[0] are the cards of the bottom player, and the other players
	 *     follow in clockwise order. A `null' element means a covered card.
	 */
	public CardsGameWidget(int tableSize, PlayersCards cards) {
		
		setWidth(tableSize + "px");
		setHeight(tableSize + "px");
		DOM.setStyleAttribute(getElement(), "background", "green");
		
		this.tableSize = tableSize;
		this.cards = cards;
		
		cardPositions = computePositions(cards, tableSize);
		
		cardWidgets = new CardWidgets();
		cardWidgets.hands = new ArrayList<List<CardWidget>>();
		cardWidgets.hands.add(new ArrayList<CardWidget>());
		cardWidgets.hands.add(new ArrayList<CardWidget>());
		cardWidgets.hands.add(new ArrayList<CardWidget>());
		cardWidgets.hands.add(new ArrayList<CardWidget>());
		cardWidgets.dealt = new ArrayList<List<CardWidget>>();
		cardWidgets.dealt.add(new ArrayList<CardWidget>());
		cardWidgets.dealt.add(new ArrayList<CardWidget>());
		cardWidgets.dealt.add(new ArrayList<CardWidget>());
		cardWidgets.dealt.add(new ArrayList<CardWidget>());
		
		for (int player = 0; player < 4; player++) {
			for (int i = 0; i < cards.hands.get(player).size(); i++) {
				CardWidget x = new CardWidget(cards.hands.get(player).get(i));
				cardWidgets.hands.get(player).add(x);
				add(x, 0, 0);
				setCardPosition(x, cardPositions.hands.get(player).get(i));
			}
			for (int i = 0; i < cards.dealt.get(player).size(); i++) {
				CardWidget x = new CardWidget(cards.dealt.get(player).get(i));
				cardWidgets.dealt.get(player).add(x);
				add(x, 0, 0);
				setCardPosition(x, cardPositions.dealt.get(player).get(i));
			}
		}
	}

	private void setCardPosition(CardWidget x, Position position) {
		int rotation = position.rotation;
		x.setRotation(rotation);
		assert rotation % 90 == 0;
		if (rotation % 180 == 0)
			// Vertical card
			setWidgetPosition(x, position.x - CardWidget.cardWidth/2, position.y - CardWidget.cardHeight/2);
		else
			// Horizontal card
			setWidgetPosition(x, position.x - CardWidget.cardHeight/2, position.y - CardWidget.cardWidth/2);
		DOM.setIntStyleAttribute(x.getElement(), "zIndex", position.z);
	}
	
	/**
	 * Computes the positions of a set of cards as if they belong to the bottom player,
	 * centered horizontally and with the center `offset' pixels above the bottom edge.
	 * 
	 * @param cards
	 * @param offset
	 * @return
	 */
	private static List<Position> computePositionsHelper(List<Card> cards, int offset, int tableSize) {
		List<Position> positions = new ArrayList<Position>();
		
		int maxCenterDistance;
		if (cards.size() == 0)
			maxCenterDistance = 0;
		else
			maxCenterDistance = (cards.size() - 1) * CardWidget.borderWidth;
		
		for (int i = 0; i < cards.size(); i++)
			positions.add(new Position(tableSize/2 - maxCenterDistance/2 + i * CardWidget.borderWidth,
					      tableSize - offset, i, 0));
		
		return positions;
	}

	private static CardPositions computePositions(PlayersCards cards, int tableSize) {
		
		final int handsOffset = 70;
		final int dealtOffset = 260;
		
		CardPositions cardPositions = new CardPositions();
		cardPositions.hands = new ArrayList<List<Position>>();
		cardPositions.dealt = new ArrayList<List<Position>>();
		
		for (int player = 0; player < 4; player++) {
			List<Position> handPosition = computePositionsHelper(cards.hands.get(player),
                    handsOffset, tableSize);
			List<Position> dealtPosition = computePositionsHelper(cards.dealt.get(player),
					dealtOffset, tableSize);
			for (int i = 0; i < player; i++) {
				rotatePositions(handPosition, tableSize);
				rotatePositions(dealtPosition, tableSize);
			}
			cardPositions.hands.add(handPosition);
			cardPositions.dealt.add(dealtPosition);
		}
		
		for (int player = 0; player < 4; player++) {
			assert cards.hands.get(player).size() == cardPositions.hands.get(player).size();
			assert cards.dealt.get(player).size() == cardPositions.dealt.get(player).size();
		}
		
		return cardPositions;
	}
	
	/**
	 * Rotates the given list of positions clockwise by 90 degrees around the table center.
	 * @param positions
	 */
	private static void rotatePositions(List<Position> positions, int tableSize) {
		for (Position position : positions) {
			position.rotation = (position.rotation + 90) % 360;
			int x = tableSize - position.y;
			int y = position.x;
			position.x = x;
			position.y = y;
		}
	}

}
