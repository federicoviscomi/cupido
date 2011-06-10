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
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class CardsGameWidget extends AbsolutePanel {
	
	// The width of the players' labels that contain usernames and scores.
	private static final int playerLabelWidth = 200;
	
	// The height of the players' labels that contain usernames and scores.
	private static final int playerLabelHeight = 20;
	
	/**
	 * The current layout of movable widgets within the table.
	 */
	private TableLayout tableLayout;
	
	/**
	 * The roles of the cards on the table.
	 */
	private Map<CardWidget,CardRole> cardRoles;
	
	/**
	 * The movable widgets within the table.
	 */
	private MovableWidgets movableWidgets;
		
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

	private GameEventListener listener;

	/**
	 * This class models the position of a widget on the table.
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
		 * The distance between the left margin and the center of the widget.
		 */
		public int x;
		
		/**
		 * The distance between the top margin and the center of the widget.
		 */
		public int y;
		
		/**
		 * The height of the widget. Widgets with higher values of z are drawn above
		 * those with lower values.
		 */
		public int z;
		
		/**
		 * The rotation is measured in degrees. When this is 0, there is no rotation.
		 * The rotation is clockwise, so a widget with rotation `90' will have its top
		 * pointed towards the right edge of the table.
		 */
		public int rotation;
	}
	
	private static class TableLayout {
		/**
		 * The positions of the cards on the table.
		 */
		public Map<CardWidget,Position> cards;
		
		/**
		 * The positions of the usernames (and scores) on the table.
		 * The rotation field must always be 0.
		 */
		public List<Position> names;
	}
	
	private static class MovableWidgets {
		/**
		 * The widgets displaying the cards on the table.
		 * The relative order of values is not meaningful.
		 */
		public List<CardWidget> cards;
		
		/**
		 * The labels containing the users' names and scores.
		 * The first element refers to the bottom player, and other elements are
		 * sorted clockwise.
		 */
		public List<Label> playerNames;
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
	
	private static class PlayerData {
		public String name;
		public boolean isBot;
		public int score;
	}
	
	public interface GameEventListener {
		public void onAnimationStart();
		public void onAnimationEnd();
	}
	
	/**
	 * 
	 * @param tableSize The width and height of this widget.
	 * @param gameStatus The game status, except the cards of the bottom player (if they are shown).
	 * @param bottomPlayerCards The cards of the bottom player. If this is null, the cards are covered,
	 *                          and their number is extracted from gameStatus.
     * @param cornerWidget An arbitrary 200x200 pixel widget placed in the bottom-right corner.
	 */
	public CardsGameWidget(int tableSize, ObservedGameStatus gameStatus, Card[] bottomPlayerCards,
                           Widget cornerWidget, GameEventListener listener) {
		
		setWidth(tableSize + "px");
		setHeight(tableSize + "px");
		DOM.setStyleAttribute(getElement(), "background", "green");
		
		this.tableSize = tableSize;
		this.listener = listener;
		
		cornerWidget.setWidth("200px");
		cornerWidget.setHeight("200px");
		add(cornerWidget, tableSize - 200, tableSize - 200);
		
		movableWidgets = new MovableWidgets();
		movableWidgets.cards = new ArrayList<CardWidget>();		
		movableWidgets.playerNames = new ArrayList<Label>();		
		cardRoles = new HashMap<CardWidget,CardRole>();
		
		// Fill movableWidgets.cards and cardRoles, using gameStatus and bottomPlayerCards.
		for (int player = 0; player < 4; player++) {
			PlayerStatus playerStatus = gameStatus.ogs[player];
			
			PlayerData playerData = new PlayerData();
			playerData.isBot = playerStatus.isBot;
			playerData.name = playerStatus.name;
			playerData.score = playerStatus.score;
			
			String s;
			if (playerData.isBot)
				s = "bot";
			else
				s = playerData.name + " (" + playerData.score + ")";
			
			Label playerLabel = new Label(s);
			playerLabel.setWordWrap(false);
			playerLabel.setWidth(playerLabelWidth + "px");
			playerLabel.setHeight(playerLabelHeight + "px");
			DOM.setStyleAttribute(playerLabel.getElement(), "overflow", "hidden");			
			add(playerLabel, 0, 0);
			movableWidgets.playerNames.add(playerLabel);
			
			if (player == 0 && bottomPlayerCards != null) {
				for (Card card: bottomPlayerCards) {
					// Set the correct rotation right away, to avoid loading the not-rotated
					// image first.
					CardWidget cardWidget = new CardWidget(card, 0);
					movableWidgets.cards.add(cardWidget);
					cardRoles.put(cardWidget, new CardRole(CardRole.State.HAND, player));
					add(cardWidget, 0, 0);
				}
			} else {
				for (int i = 0; i < playerStatus.numOfCardsInHand; i++) {
					// Set the correct rotation right away, to avoid loading the not-rotated
					// image first.
					CardWidget cardWidget = new CardWidget(null, 90 * player);
					movableWidgets.cards.add(cardWidget);
					cardRoles.put(cardWidget, new CardRole(CardRole.State.HAND, player));
					add(cardWidget, 0, 0);
				}
			}
			
			if (playerStatus.playedCard != null) {
				// Set the correct rotation right away, to avoid loading the not-rotated
				// image first.
				CardWidget cardWidget = new CardWidget(playerStatus.playedCard, 90 * player);
				movableWidgets.cards.add(cardWidget);
				cardRoles.put(cardWidget, new CardRole(CardRole.State.DEALT, player));
				add(cardWidget, 0, 0);
			}
		}
		
		// Set the horizontal alignment for the player names.
		movableWidgets.playerNames.get(0).setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		movableWidgets.playerNames.get(1).setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		movableWidgets.playerNames.get(2).setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		movableWidgets.playerNames.get(3).setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		
		tableLayout = computePositions(movableWidgets, cardRoles, tableSize);
		
		// Lay out the widgets according to tableLayout.
		for (CardWidget x : movableWidgets.cards)
			setCardPosition(x, tableLayout.cards.get(x));
		
		for (int player = 0; player < 4; player++)
			setLabelPosition(movableWidgets.playerNames.get(player), tableLayout.names.get(player));
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

	private static Position interpolatePosition(Position startPosition, Position endPosition, double progress) {
		Position position = new Position();
		assert startPosition.rotation == endPosition.rotation;
		position.rotation = startPosition.rotation;
		assert startPosition.z == endPosition.z;
		position.z = startPosition.z;
		position.x = (int) (startPosition.x + (endPosition.x - startPosition.x) * progress);
		position.y = (int) (startPosition.y + (endPosition.y - startPosition.y) * progress);
		return position;
	}
	
	/**
	 * 
	 * @param newCardRoles
	 * @param switchZAt
	 *     Specifies when the z index should be changed. 0.0 means at the beginning of the animation
	 *     and 1.0 means at the end of the animation. This value must not be negative or greater than 1.
	 * @return
	 */
	private GWTAnimation animateMoveTo(final Map<CardWidget,CardRole> newCardRoles) {
		final TableLayout newTableLayout = computePositions(movableWidgets, newCardRoles, tableSize);
		
		// A card move takes 2000 milliseconds.
		final int duration = 2000;
		GWTAnimation animation = new SimpleAnimation(duration) {
			@Override
			public void onUpdate(double progress) {
				for (CardWidget widget : movableWidgets.cards) {
					Position position = interpolatePosition(tableLayout.cards.get(widget),
							                                newTableLayout.cards.get(widget),
							                                progress);
					setCardPosition(widget, position);
				}
				for (int player = 0; player < 4; player++) {
					Position position = interpolatePosition(tableLayout.names.get(player),
							                                newTableLayout.names.get(player),
							                                progress);
					setLabelPosition(movableWidgets.playerNames.get(player), position);
				}
			}
			@Override
			public void onStart() {
				assert !runningAnimation;
				runningAnimation = true;
				listener.onAnimationStart();
			}
			@Override
			public void onComplete() {
				assert runningAnimation;
				runningAnimation = false;
				cardRoles = newCardRoles;
				tableLayout = newTableLayout;
				listener.onAnimationEnd();
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
		
		Map<CardWidget,Position> cardPositions = tableLayout.cards;
		
		// Firstly, search for the specified card.
		
		for (CardWidget candidateWidget : movableWidgets.cards) {
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
			for (CardWidget candidateWidget : movableWidgets.cards) {
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
		
		GWTAnimation animation = animateMoveTo(newCardRoles);
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
	
	private void setLabelPosition(Label x, Position position) {
		assert position.rotation == 0;
		setWidgetPosition(x, position.x - playerLabelWidth/2, position.y - playerLabelHeight/2);
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
			offset = 90;
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

	private static TableLayout computePositions(MovableWidgets movableWidgets,
			Map<CardWidget,CardRole> cardRoles, int tableSize) {
				
		// 1. Calculate the list of card widgets for each role.
		
		Map<CardRole,List<CardWidget>> widgetByRole = new HashMap<CardRole,List<CardWidget>>();
		
		for (CardWidget cardWidget : movableWidgets.cards) {
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
		
		for (CardWidget cardWidget : movableWidgets.cards) {
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
		
		// 7. Lay out the players' names.
		
		List<Position> namePositions = new ArrayList<Position>();
		{
			// The z index of the card names.
			final int z = 100;
			
			// Bottom player: fixed position.
			namePositions.add(new Position(tableSize/2, tableSize - 10 - playerLabelHeight/2, z, 0));
			
			// Left player: under its cards.
			Position leftPosition = new Position(10 + playerLabelWidth/2, tableSize/2, 100, 0);
			for (CardWidget widget : movableWidgets.cards) {
				CardRole role = cardRoles.get(widget);
				if (role.player == 1 && role.state == CardRole.State.HAND) {
					// Make sure the name is at least 10px under this card.
					Position cardPosition = cardPositions.get(widget);
					int cardBottomY = cardPosition.y + CardWidget.cardWidth/2;
					leftPosition.y = Math.max(leftPosition.y, cardBottomY + playerLabelHeight/2 + 10);
				}
			}
			namePositions.add(leftPosition);
			
			// Top player: fixed position.
			namePositions.add(new Position(tableSize/2, 10 + playerLabelHeight/2, z, 0));
			
			// Right player: under its cards.
			Position rightPosition = new Position(tableSize - 10 - playerLabelWidth/2, tableSize/2, z, 0);
			for (CardWidget widget : movableWidgets.cards) {
				CardRole role = cardRoles.get(widget);
				if (role.player == 3 && role.state == CardRole.State.HAND) {
					// Make sure the name is at least 10px under this card.
					Position cardPosition = cardPositions.get(widget);
					int cardBottomY = cardPosition.y + CardWidget.cardWidth/2;
					rightPosition.y = Math.max(rightPosition.y, cardBottomY + playerLabelHeight/2 + 10);
				}
			}
			namePositions.add(rightPosition);			
		}
		
		// 8. Construct the result object.
		
		TableLayout result = new TableLayout();
		result.cards = cardPositions;
		result.names = namePositions;
		
		return result;
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
