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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
	 * The layout that the widgets on the table had the last time
	 * runPendingAnimations() was called, or the initial layout if
	 * runPendingAnimations() has never been called.
	 */
	private TableLayout previousTableLayout;

	/**
	 * The current roles of the cards on the table.
	 */
	private Map<CardWidget, CardRole> cardRoles;

	/**
	 * This is `true' when there are some animations pending, that can be
	 * executed with runPendingAnimations(). This is reset to `false' when such
	 * animations complete.
	 */
	boolean someAnimationsPending = false;

	/**
	 * The movable widgets within the table.
	 */
	private MovableWidgets movableWidgets;

	/**
	 * Informations about the players. The first element refers to the bottom
	 * player, and the other elements are sorted clockwise.
	 */
	private List<PlayerData> players;

	/**
	 * The size of the table (width and height) in pixels.
	 */
	private int tableSize;

	/**
	 * This is true when an animation involving the table is running, and so the
	 * table must be insensitive to commands.
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
		 * The height of the widget. Widgets with higher values of z are drawn
		 * above those with lower values.
		 */
		public int z;

		/**
		 * The rotation is measured in degrees. When this is 0, there is no
		 * rotation. The rotation is clockwise, so a widget with rotation `90'
		 * will have its top pointed towards the right edge of the table.
		 */
		public int rotation;
	}

	private static class TableLayout {
		/**
		 * The positions of the cards on the table.
		 */
		public Map<CardWidget, Position> cards;

		/**
		 * The positions of the usernames (and scores) on the table. The
		 * rotation field must always be 0.
		 */
		public List<Position> names;
	}

	private static class MovableWidgets {
		/**
		 * The widgets displaying the cards on the table. The relative order of
		 * values is not meaningful.
		 */
		public List<CardWidget> cards;

		/**
		 * The labels containing the users' names and scores. The first element
		 * refers to the bottom player, and other elements are sorted clockwise.
		 */
		public List<Label> playerNames;
	}

	public static class CardRole {
		@Override
		public int hashCode() {
			// Note that the `isRaised' field does *not* change the hash code.
			// This is needed to be consistent with equals().
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
			// Note that the `isRaised' field is *not* compared.
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
		 * This is valid only when state==HAND. It specifies whether this card
		 * is raised.
		 */
		public boolean isRaised;

		/**
		 * The player to whom the card belongs.
		 */
		public int player;

		public CardRole() {

		}

		public CardRole(State state, boolean raised, int player) {
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
		/**
		 * This is called before starting an animation.
		 */
		public void onAnimationStart();

		/**
		 * This is called when an animation finishes.
		 */
		public void onAnimationEnd();

		/**
		 * This is called when the user clicks on a card.
		 * 
		 * @player: the player to whom the card belongs
		 * @card: the card that was clicked, or `null' if a covered card was
		 *        clicked.
		 * @isRaised: this is true only if state==HAND and this card is
		 *            currently raised.
		 */
		public void onCardClicked(int player, Card card, CardRole.State state,
				boolean isRaised);
	}

	/**
	 * 
	 * @param tableSize
	 *            The width and height of this widget.
	 * @param gameStatus
	 *            The game status, except the cards of the bottom player (if
	 *            they are shown).
	 * @param bottomPlayerCards
	 *            The cards of the bottom player. If this is null, the cards are
	 *            covered, and their number is extracted from gameStatus.
	 * @param cornerWidget
	 *            An arbitrary 200x200 pixel widget placed in the bottom-right
	 *            corner.
	 */
	public CardsGameWidget(int tableSize, ObservedGameStatus gameStatus,
			Card[] bottomPlayerCards, Widget cornerWidget,
			GameEventListener listener) {

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
		cardRoles = new HashMap<CardWidget, CardRole>();

		// Fill movableWidgets.cards and cardRoles, using gameStatus and
		// bottomPlayerCards.
		for (int player = 0; player < 4; player++) {
			PlayerStatus playerStatus = gameStatus.playerStatus[player];

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
			DOM.setStyleAttribute(playerLabel.getElement(), "overflow",
					"hidden");
			add(playerLabel, 0, 0);
			movableWidgets.playerNames.add(playerLabel);

			if (player == 0 && bottomPlayerCards != null) {
				for (Card card : bottomPlayerCards) {
					// Set the correct rotation right away, to avoid loading the
					// not-rotated
					// image first.
					CardWidget cardWidget = new CardWidget(card, 0);
					movableWidgets.cards.add(cardWidget);
					cardRoles.put(cardWidget, new CardRole(CardRole.State.HAND,
							false, player));
					add(cardWidget, 0, 0);
				}
			} else {
				for (int i = 0; i < playerStatus.numOfCardsInHand; i++) {
					// Set the correct rotation right away, to avoid loading the
					// not-rotated
					// image first.
					CardWidget cardWidget = new CardWidget(null, 90 * player);
					movableWidgets.cards.add(cardWidget);
					cardRoles.put(cardWidget, new CardRole(CardRole.State.HAND,
							false, player));
					add(cardWidget, 0, 0);
				}
			}

			if (playerStatus.playedCard != null) {
				// Set the correct rotation right away, to avoid loading the
				// not-rotated
				// image first.
				CardWidget cardWidget = new CardWidget(playerStatus.playedCard,
						90 * player);
				movableWidgets.cards.add(cardWidget);
				cardRoles.put(cardWidget, new CardRole(CardRole.State.DEALT,
						false, player));
				add(cardWidget, 0, 0);
			}
		}

		// Set the horizontal alignment for the player names.
		movableWidgets.playerNames.get(0).setHorizontalAlignment(
				HasHorizontalAlignment.ALIGN_CENTER);
		movableWidgets.playerNames.get(1).setHorizontalAlignment(
				HasHorizontalAlignment.ALIGN_LEFT);
		movableWidgets.playerNames.get(2).setHorizontalAlignment(
				HasHorizontalAlignment.ALIGN_CENTER);
		movableWidgets.playerNames.get(3).setHorizontalAlignment(
				HasHorizontalAlignment.ALIGN_RIGHT);

		// Bind the card widgets to the listener.
		{
			final CardsGameWidget cardsGameWidget = this;
			for (final CardWidget cardWidget : movableWidgets.cards)
				cardWidget.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						if (runningAnimation)
							// Clicking a card during an animation does nothing.
							return;

						CardRole role = cardsGameWidget.cardRoles
								.get(cardWidget);
						cardsGameWidget.listener
								.onCardClicked(role.player,
										cardWidget.getCard(), role.state,
										role.isRaised);
					}
				});
		}

		TableLayout tableLayout = computePositions(movableWidgets, cardRoles,
				tableSize);

		// Lay out the widgets according to tableLayout.
		for (CardWidget x : movableWidgets.cards)
			setCardPosition(x, tableLayout.cards.get(x));

		for (int player = 0; player < 4; player++)
			setLabelPosition(movableWidgets.playerNames.get(player),
					tableLayout.names.get(player));

		previousTableLayout = tableLayout;
	}

	private static Map<CardWidget, CardRole> cloneCardRoles(
			Map<CardWidget, CardRole> cardRoles) {
		Map<CardWidget, CardRole> result = new HashMap<CardWidget, CardRole>();
		for (Entry<CardWidget, CardRole> e : cardRoles.entrySet()) {
			// Clone the role. The widget does not need to be cloned.
			CardRole role = new CardRole();
			role.player = e.getValue().player;
			role.state = e.getValue().state;
			role.isRaised = e.getValue().isRaised;
			result.put(e.getKey(), role);
		}
		return result;
	}

	private static Position interpolatePosition(Position startPosition,
			Position endPosition, double progress) {
		Position position = new Position();
		assert startPosition.rotation == endPosition.rotation;
		position.rotation = startPosition.rotation;
		assert startPosition.z == endPosition.z;
		position.z = startPosition.z;
		position.x = (int) (startPosition.x + (endPosition.x - startPosition.x)
				* progress);
		position.y = (int) (startPosition.y + (endPosition.y - startPosition.y)
				* progress);
		return position;
	}

	/**
	 * Starts an animation that moves the widgets from the layout computed from
	 * previousCardRoles to the layout computed from cardRoles. Calls
	 * GameEventListener.onAnimationStart() and
	 * GameEventListener.onAnimationEnd().
	 * animationCompletedListener.onComplete() is called after the animation
	 * completes, but before calling GameEventListener.onAnimationEnd().
	 * 
	 * @param duration
	 *            The duration of the animation, in milliseconds.
	 */
	public void runPendingAnimations(int duration,
			GWTAnimation.AnimationCompletedListener animationCompletedListener) {

		assert !runningAnimation;

		final TableLayout tableLayout = computePositions(movableWidgets,
				cardRoles, tableSize);

		runningAnimation = true;
		listener.onAnimationStart();

		GWTAnimation animation = new SimpleAnimation(duration) {
			@Override
			public void onUpdate(double progress) {
				for (CardWidget widget : movableWidgets.cards) {
					Position position = interpolatePosition(
							previousTableLayout.cards.get(widget),
							tableLayout.cards.get(widget), progress);
					setCardPosition(widget, position);
				}
				for (int player = 0; player < 4; player++) {
					Position position = interpolatePosition(
							previousTableLayout.names.get(player),
							tableLayout.names.get(player), progress);
					setLabelPosition(movableWidgets.playerNames.get(player),
							position);
				}
			}

			@Override
			public void onComplete() {
				super.onComplete();
				assert runningAnimation;
				runningAnimation = false;
				previousTableLayout = tableLayout;
				someAnimationsPending = false;
				listener.onAnimationEnd();
			}
		};
		animation.run(animationCompletedListener);
	}

	/**
	 * Reveal a covered card of the specified player as `card'. The card must
	 * not be raised.
	 * 
	 * NOTE: There must be no animations pending when this method is called.
	 */
	public void revealCoveredCard(int player, Card card) {

		assert !runningAnimation;
		assert !someAnimationsPending;

		// Otherwise, search for a covered card.

		CardWidget widget = null;

		// No animations are pending, so the current table layout is the one
		// already calculated into previousTableLayout.
		Map<CardWidget, Position> cardPositions = previousTableLayout.cards;

		for (CardWidget candidateWidget : movableWidgets.cards) {
			if (candidateWidget.getCard() == null) {
				CardRole role = cardRoles.get(candidateWidget);
				if (role.player == player && role.state == CardRole.State.HAND
						&& role.isRaised == false) {
					// Found a match. Keep the widget with higher z index.
					if (widget == null
							|| cardPositions.get(widget).z < cardPositions
									.get(candidateWidget).z)
						widget = candidateWidget;
				}
			}
		}

		// If no match has been found, the precondition for this method was
		// violated.
		assert widget != null;

		widget.setCard(card);

		// `previousTableLayout' must be updated because the z indexes may have
		// changed
		// by uncovering the card.
		previousTableLayout = computePositions(movableWidgets, cardRoles,
				tableSize);
	}

	/**
	 * The player `player' deals the card `card'. The card must be an uncovered
	 * card in the specified player's hand.
	 * 
	 * The corresponding animation will be executed at the next call to
	 * runPendingAnimations().
	 */
	public void dealCard(int player, Card card) {

		assert card != null;
		assert !runningAnimation;

		CardWidget widget = null;

		// Search for the specified card.

		for (CardWidget candidateWidget : movableWidgets.cards) {
			Card candidateCard = candidateWidget.getCard();
			if (candidateCard != null && card.value == candidateCard.value
					&& card.suit == candidateCard.suit) {
				CardRole role = cardRoles.get(candidateWidget);
				if (role.player == player && role.state == CardRole.State.HAND) {
					// Found a match
					widget = candidateWidget;
					break;
				}
			}
		}

		assert widget != null;

		cardRoles.get(widget).state = CardRole.State.DEALT;
		cardRoles.get(widget).isRaised = false;

		// Note: this may already be `true'.
		someAnimationsPending = true;
	}

	/**
	 * The player `player' raises the card `card'. The card must be in the
	 * specified player's hand, and must not be covered.
	 * 
	 * The corresponding animation will be executed at the next call to
	 * runPendingAnimations().
	 */
	public void raiseCard(int player, Card card) {

		assert card != null;
		assert !runningAnimation;

		CardWidget widget = null;

		// Search for the specified card.

		for (CardWidget candidateWidget : movableWidgets.cards) {
			Card candidateCard = candidateWidget.getCard();
			if (candidateCard != null && card.value == candidateCard.value
					&& card.suit == candidateCard.suit) {
				CardRole role = cardRoles.get(candidateWidget);
				if (role.player == player && role.state == CardRole.State.HAND
						&& !role.isRaised) {
					// Found a match
					widget = candidateWidget;
					break;
				}
			}
		}

		assert widget != null;

		cardRoles.get(widget).isRaised = true;

		// Note: this may already be `true'.
		someAnimationsPending = true;
	}

	/**
	 * The player `player' lowers the previously-raised the card `card'. The
	 * card must be in the specified player's hand.
	 * 
	 * The corresponding animation will be executed at the next call to
	 * runPendingAnimations().
	 */
	public void lowerRaisedCard(int player, Card card) {

		assert card != null;
		assert !runningAnimation;

		CardWidget widget = null;

		// Search for the specified card.

		for (CardWidget candidateWidget : movableWidgets.cards) {
			Card candidateCard = candidateWidget.getCard();
			if (candidateCard != null && card.value == candidateCard.value
					&& card.suit == candidateCard.suit) {
				CardRole role = cardRoles.get(candidateWidget);
				if (role.player == player && role.state == CardRole.State.HAND
						&& role.isRaised) {
					// Found a match
					widget = candidateWidget;
					break;
				}
			}
		}

		assert widget != null;

		cardRoles.get(widget).isRaised = false;

		// Note: this may already be `true'.
		someAnimationsPending = true;
	}

	private void setCardPosition(CardWidget x, Position position) {
		int rotation = position.rotation;
		x.setRotation(rotation);
		assert rotation % 90 == 0;
		if (rotation % 180 == 0)
			// Vertical card
			setWidgetPosition(x, position.x - CardWidget.cardWidth / 2,
					position.y - CardWidget.cardHeight / 2);
		else
			// Horizontal card
			setWidgetPosition(x, position.x - CardWidget.cardHeight / 2,
					position.y - CardWidget.cardWidth / 2);
		DOM.setIntStyleAttribute(x.getElement(), "zIndex", position.z);
	}

	private void setLabelPosition(Label x, Position position) {
		assert position.rotation == 0;
		setWidgetPosition(x, position.x - playerLabelWidth / 2, position.y
				- playerLabelHeight / 2);
		DOM.setIntStyleAttribute(x.getElement(), "zIndex", position.z);
	}

	/**
	 * Computes the positions of a set of cards as if they belong to the bottom
	 * player, centered horizontally and with the center `offset' pixels above
	 * the bottom edge. The z values are *not* computed.
	 * 
	 * @param cards
	 * @param offset
	 * @return
	 */
	private static List<Position> computePositionsHelper(
			List<CardWidget> cards, CardRole.State state, int tableSize) {
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
			positions.add(new Position(tableSize / 2 - maxCenterDistance / 2
					+ i * CardWidget.borderWidth, tableSize - offset, 0, 0));

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

				if (y.value == 1)
					return -1;

				if (x.value < y.value)
					return -1;
				else
					return 1;
			}
		});
	}

	private static TableLayout computePositions(MovableWidgets movableWidgets,
			Map<CardWidget, CardRole> cardRoles, int tableSize) {

		// 1. Calculate the list of card widgets for each role.

		Map<CardRole, List<CardWidget>> widgetByRole = new HashMap<CardRole, List<CardWidget>>();

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

		Map<CardWidget, Position> cardPositions = new HashMap<CardWidget, Position>();

		for (CardRole role : widgetByRole.keySet()) {
			List<CardWidget> widgets = widgetByRole.get(role);
			List<Position> positions = computePositionsHelper(widgets,
					role.state, tableSize);
			if (role.state == CardRole.State.HAND) {
				for (int i = 0; i < positions.size(); i++)
					if (cardRoles.get(widgets.get(i)).isRaised) {
						// Raise the card by 15 pixels.
						positions.get(i).y -= 15;
					}
			}
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
			namePositions.add(new Position(tableSize / 2, tableSize - 10
					- playerLabelHeight / 2, z, 0));

			// Left player: under its cards.
			Position leftPosition = new Position(10 + playerLabelWidth / 2,
					tableSize / 2, 100, 0);
			for (CardWidget widget : movableWidgets.cards) {
				CardRole role = cardRoles.get(widget);
				if (role.player == 1 && role.state == CardRole.State.HAND) {
					// Make sure the name is at least 10px under this card.
					Position cardPosition = cardPositions.get(widget);
					int cardBottomY = cardPosition.y + CardWidget.cardWidth / 2;
					leftPosition.y = Math.max(leftPosition.y, cardBottomY
							+ playerLabelHeight / 2 + 10);
				}
			}
			namePositions.add(leftPosition);

			// Top player: fixed position.
			namePositions.add(new Position(tableSize / 2,
					10 + playerLabelHeight / 2, z, 0));

			// Right player: under its cards.
			Position rightPosition = new Position(tableSize - 10
					- playerLabelWidth / 2, tableSize / 2, z, 0);
			for (CardWidget widget : movableWidgets.cards) {
				CardRole role = cardRoles.get(widget);
				if (role.player == 3 && role.state == CardRole.State.HAND) {
					// Make sure the name is at least 10px under this card.
					Position cardPosition = cardPositions.get(widget);
					int cardBottomY = cardPosition.y + CardWidget.cardWidth / 2;
					rightPosition.y = Math.max(rightPosition.y, cardBottomY
							+ playerLabelHeight / 2 + 10);
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
	 * Rotates the given list of positions clockwise by 90 degrees around the
	 * table center.
	 * 
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
