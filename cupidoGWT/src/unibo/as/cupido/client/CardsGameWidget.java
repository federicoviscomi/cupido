package unibo.as.cupido.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.ObservedGameStatus;
import unibo.as.cupido.backendInterfaces.common.PlayerStatus;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.AbsolutePanel;

public class CardsGameWidget extends AbsolutePanel {
	
	/**
	 * The positions of the cards on the table.
	 */
	private Map<CardWidget,Position> cardPositions;
	
	/**
	 * The roles of the cards on the table.
	 */
	private Map<CardWidget,CardRole> cardRoles;
	
	/**
	 * The widgets displaying the cards on the table.
	 * The relative order of values is not meaningful.
	 */
	private List<CardWidget> cardWidgets;
	
	/**
	 * Informations about the players. The first element refers to the bottom player, and
	 * the other elements are sorted clockwise.
	 */
	private List<PlayerData> players;

	/**
	 * The size of the table (width and height) in pixels.
	 */
	private int tableSize;

	/**
	 * This is true when an animation involving the table is running, and so
	 * the table must be insensitive to commands.
	 */
	private boolean runningAnimation = false;

	/**
	 * This class models the position of a card on the table.
	 * 
	 * @author marco
	 */
	private static class Position {
		
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
	
	public static class CardRole {
		@Override
		public int hashCode() {
			assert player >= 0;
			assert player < 4;
			switch (state) {
			case HAND:
				return player;
			case DEALT:
				return player + 4;
			}
			throw new IllegalStateException();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj != null && obj instanceof CardRole) {
				CardRole x = (CardRole) obj;
				return (player == x.player && state == x.state);
			} else
				return false;
		}

		public enum State {
			HAND, DEALT
		}
		
		/**
		 * The state of the card (see the State enum).
		 */
		public State state;
		
		/**
		 * The player to whom the card belongs.
		 */
		public int player;
		
		public CardRole() {
			
		}

		public CardRole(State state, int player) {
			this.state = state;
			this.player = player;
		}
	}
	
	private class PlayerData {
		public String name;
		public boolean isBot;
		public int score;
	}
	
	/**
	 * 
	 * @param tableSize The width and height of this widget.
	 * @param gameStatus The game status, except the cards of the bottom player (if they are shown).
	 * @param bottomPlayerCards The cards of the bottom player. If this is null, the cards are covered,
	 *                          and their number is extracted from gameStatus.
	 */
	public CardsGameWidget(int tableSize, ObservedGameStatus gameStatus, Card[] bottomPlayerCards) {
		
		setWidth(tableSize + "px");
		setHeight(tableSize + "px");
		DOM.setStyleAttribute(getElement(), "background", "green");
		
		this.tableSize = tableSize;
		
		cardWidgets = new ArrayList<CardWidget>();		
		cardRoles = new HashMap<CardWidget,CardRole>();
		
		// Fill cardWidgets and cardRoles, using gameStatus and bottomPlayerCards.
		for (int player = 0; player < 4; player++) {
			PlayerStatus playerStatus = gameStatus.ogs[player];
			
			PlayerData playerData = new PlayerData();
			playerData.isBot = playerStatus.isBot;
			playerData.name = playerStatus.name;
			playerData.score = playerStatus.score;
			
			if (player == 0 && bottomPlayerCards != null) {
				for (Card card: bottomPlayerCards) {
					// Set the correct rotation right away, to avoid loading the not-rotated
					// image first.
					CardWidget cardWidget = new CardWidget(card, 0);
					cardWidgets.add(cardWidget);
					cardRoles.put(cardWidget, new CardRole(CardRole.State.HAND, player));
					add(cardWidget, 0, 0);
				}
			} else {
				for (int i = 0; i < playerStatus.numOfCardsInHand; i++) {
					// Set the correct rotation right away, to avoid loading the not-rotated
					// image first.
					CardWidget cardWidget = new CardWidget(null, 90 * player);
					cardWidgets.add(cardWidget);
					cardRoles.put(cardWidget, new CardRole(CardRole.State.HAND, player));
					add(cardWidget, 0, 0);
				}
			}
			
			if (playerStatus.playedCard != null) {
				// Set the correct rotation right away, to avoid loading the not-rotated
				// image first.
				CardWidget cardWidget = new CardWidget(playerStatus.playedCard, 90 * player);
				cardWidgets.add(cardWidget);
				cardRoles.put(cardWidget, new CardRole(CardRole.State.DEALT, player));
				add(cardWidget, 0, 0);
			}
		}
		
		cardPositions = computePositions(cardWidgets, cardRoles, tableSize);
		
		// Lay out the widgets according to cardPositions.
		for (CardWidget x : cardWidgets)
			setCardPosition(x, cardPositions.get(x));
	}
	
	private Map<CardWidget,CardRole> cloneCardRoles() {
		Map<CardWidget,CardRole> result = new HashMap<CardWidget,CardRole>();
		for (Entry<CardWidget,CardRole> e : cardRoles.entrySet()) {
			// Clone the role. The widget does not need to be cloned.
			CardRole role = new CardRole();
			role.player = e.getValue().player;
			role.state = e.getValue().state;
			result.put(e.getKey(), role);
		}
		return result;
	}
	
	/**
	 * 
	 * @param newCardRoles
	 * @param switchZAt
	 *     Specifies when the z index should be changed. 0.0 means at the beginning of the animation
	 *     and 1.0 means at the end of the animation. This value must not be negative or greater than 1.
	 * @return
	 */
	private GWTAnimation animateMoveTo(final Map<CardWidget,CardRole> newCardRoles, final double switchZAt) {
		assert switchZAt >= 0;
		assert switchZAt <= 1;
		final Map<CardWidget,Position> newCardPositions = computePositions(cardWidgets, newCardRoles, tableSize);
		
		// A card move takes 2000 milliseconds.
		final int duration = 2000;
		GWTAnimation animation = new SimpleAnimation(duration) {
			@Override
			public void onUpdate(double progress) {
				for (CardWidget widget : cardWidgets) {
					Position position = new Position();
					Position startPosition = cardPositions.get(widget);
					Position endPosition = newCardPositions.get(widget);
					// Compute position = startPosition + (endPosition - startPosition) * progress
					assert startPosition.rotation == endPosition.rotation;
					position.rotation = startPosition.rotation;
					if (progress < switchZAt)
						position.z = startPosition.z;
					else
						position.z = endPosition.z;
					position.x = (int) (startPosition.x + (endPosition.x - startPosition.x) * progress);
					position.y = (int) (startPosition.y + (endPosition.y - startPosition.y) * progress);
					setCardPosition(widget, position);
				}
			}
			@Override
			public void onStart() {
				assert !runningAnimation;
				runningAnimation = true;
			}
			@Override
			public void onComplete() {
				assert runningAnimation;
				runningAnimation = false;
				cardRoles = newCardRoles;
				cardPositions = newCardPositions;
			}
		};
		return animation;
	}
	
	/**
	 * The player `player' deals the card `card'.
	 * If the player hasn't got that card in its hand, but it has at least one covered card,
	 * that card is uncovered as the specified card and then dealt.
	 * It is an error if the player hasn't got neither the specified card nor a covered card.
	 */
	public void dealCard(int player, Card card, GWTAnimation.AnimationCompletedListener listener) {
		
		assert card != null;
		
		if (runningAnimation)
			return;
		
		CardWidget widget = null;
		
		// Firstly, search for the specified card.
		
		for (CardWidget candidateWidget : cardWidgets) {
			Card candidateCard = candidateWidget.getCard();
			if (candidateCard != null && card.value == candidateCard.value && card.suit == candidateCard.suit) {
				CardRole role = cardRoles.get(candidateWidget);
				if (role.player == player && role.state == CardRole.State.HAND) {
					// Found a match
					widget = candidateWidget;
					break;
				}
			}
		}
		
		// Otherwise, search for a covered card.
		
		if (widget == null) {
			for (CardWidget candidateWidget : cardWidgets) {
				if (candidateWidget.getCard() == null) {
					CardRole role = cardRoles.get(candidateWidget);
					if (role.player == player && role.state == CardRole.State.HAND) {
						// Found a match. Keep the widget with higher z index.
						if (widget == null || cardPositions.get(widget).z < cardPositions.get(candidateWidget).z)
							widget = candidateWidget;
					}
				}
			}
		
			// If no match has been found, the precondition for this method was violated.
			assert widget != null;
			
			widget.setCard(card);
		}
		
		Map<CardWidget,CardRole> newCardRoles = cloneCardRoles();
		newCardRoles.get(widget).state = CardRole.State.DEALT;
		
		GWTAnimation animation = animateMoveTo(newCardRoles, 1.0);
		animation.run(listener);
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
	 * The z values are *not* computed.
	 * 
	 * @param cards
	 * @param offset
	 * @return
	 */
	private static List<Position> computePositionsHelper(List<CardWidget> cards, CardRole.State state, int tableSize) {
		int offset;
		
		switch (state) {
		case DEALT:
			offset = 260;
			break;
		case HAND:
			offset = 70;
			break;
		default:
			throw new IllegalStateException();
		}
		
		List<Position> positions = new ArrayList<Position>();
		
		int maxCenterDistance;
		if (cards.size() == 0)
			maxCenterDistance = 0;
		else
			maxCenterDistance = (cards.size() - 1) * CardWidget.borderWidth;
		
		for (int i = 0; i < cards.size(); i++)
			positions.add(new Position(tableSize/2 - maxCenterDistance/2 + i * CardWidget.borderWidth,
					      tableSize - offset, 0, 0));
		
		return positions;
	}
	
	private static void sortCardWidgets(List<CardWidget> list) {
		
		Collections.sort(list, new Comparator<CardWidget>() {
			private int compareSuit(Card.Suit x, Card.Suit y) {
				// HEARTS < SPADES < DIAMONDS < CLUBS
				if (x == y)
					return 0;
				if (x == Card.Suit.HEARTS)
					return -1;
				if (y == Card.Suit.HEARTS)
					return 1;
				if (x == Card.Suit.SPADES)
					return -1;
				if (y == Card.Suit.SPADES)
					return 1;
				if (x == Card.Suit.DIAMONDS)
					return -1;
				if (y == Card.Suit.DIAMONDS)
					return 1;
				throw new IllegalStateException();
			}
			
			@Override
			public int compare(CardWidget widget1, CardWidget widget2) {
				
				Card x = widget1.getCard();
				Card y = widget2.getCard();
				
				if (x == null) {
					if (y == null)
						return 0;
					else
						return -1;
				}
				
				if (y == null)
					return 1;
				
				int suitResult = compareSuit(x.suit, y.suit);
				
				if (suitResult != 0)
					return suitResult;
				
				if (x.value == y.value)
					return 0;
				
				// The ace is displayed after the king, instead of before the 2.
				if (x.value == 1)
					return 1;
				
				if (x.value < y.value)
					return -1;
				else
					return 1;
			}
		});
	}

	private static Map<CardWidget,Position> computePositions(List<CardWidget> cards,
			Map<CardWidget,CardRole> cardRoles, int tableSize) {
				
		// 1. Calculate the list of card widgets for each role.
		
		Map<CardRole,List<CardWidget>> widgetByRole = new HashMap<CardRole,List<CardWidget>>();
		
		for (CardWidget cardWidget : cards) {
			CardRole role = cardRoles.get(cardWidget);
			if (!widgetByRole.containsKey(role))
				widgetByRole.put(role, new ArrayList<CardWidget>());
			
			widgetByRole.get(role).add(cardWidget);
		}
		
		// 2. Sort the list of card widget for each role
		
		for (List<CardWidget> x : widgetByRole.values())
			sortCardWidgets(x);
		
		// 3. Calculate the positions (except the z fields).
		
		Map<CardWidget,Position> cardPositions = new HashMap<CardWidget,Position>();
		
		for (CardRole role : widgetByRole.keySet()) {
			List<CardWidget> widgets = widgetByRole.get(role);
			List<Position> positions = computePositionsHelper(widgets, role.state, tableSize);
			for (int i = 0; i < role.player; i++)
				rotatePositions(positions, tableSize);
			assert widgets.size() == positions.size();
			for (int i = 0; i < positions.size(); i++)
				cardPositions.put(widgets.get(i), positions.get(i));
		}
		
		// 4. Calculate the list of card widgets for each player.
		
		List<List<CardWidget>> widgetByPlayer = new ArrayList<List<CardWidget>>();
		for (int player = 0; player < 4; player++)
			widgetByPlayer.add(new ArrayList<CardWidget>());
		
		for (CardWidget cardWidget : cards) {
			CardRole role = cardRoles.get(cardWidget);
			widgetByPlayer.get(role.player).add(cardWidget);
		}
		
		// 5. Sort the list of card widget for each player.
		
		for (List<CardWidget> x : widgetByPlayer)
			sortCardWidgets(x);
		
		// 6. Compute the z values based on widgetByPlayer.
		
		for (int player = 0; player < 4; player++) {
			List<CardWidget> list = widgetByPlayer.get(player);
			for (int i = 0; i < list.size(); i++)
				cardPositions.get(list.get(i)).z = i;
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
