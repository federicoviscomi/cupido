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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import unibo.as.cupido.client.GWTAnimation;
import unibo.as.cupido.client.SimpleAnimation;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.ObservedGameStatus;
import unibo.as.cupido.common.structures.PlayerStatus;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class CardsGameWidget extends AbsolutePanel {

	// The width of the players' labels that contain usernames and scores.
	private static final int playerLabelWidth = 200;

	// The height of the players' labels that contain usernames and scores.
	private static final int playerLabelHeight = 20;

	// The distance between the center of the bottom player's played cards and
	// the bottom of the screen.
	private static final int playedCardsOffset = 260;

	// The distance between the center of the bottom player's hand and the
	// bottom of the screen.
	private static final int handCardsOffset = 90;

	// This is added to every z-index to allow "positive" and "negative"
	// z-indexes.
	private static final int defaultZIndex = 50;

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
	private boolean someAnimationsPending = false;

	/**
	 * The movable widgets within the table.
	 */
	private MovableWidgets movableWidgets;

	/**
	 * Informations about the players. The first element refers to the bottom
	 * player, and the other elements are sorted clockwise.
	 */
	private List<PlayerData> players = new ArrayList<PlayerData>();

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

	private Widget cornerWidget = null;

	private boolean frozen = false;

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
			case PLAYED:
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
			HAND, PLAYED
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
		 * This is called when the user clicks on a card, except during
		 * animations and when controls are disabled.
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

		setCornerWidget(cornerWidget);

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
			players.add(playerData);

			Label playerLabel = new Label();
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
				cardRoles.put(cardWidget, new CardRole(CardRole.State.PLAYED,
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

						if (frozen)
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

		updateLabels();
	}

	private void updateLabels() {
		for (int i = 0; i < 4; i++) {
			PlayerData playerInfo = players.get(i);
			String s;
			if (playerInfo.isBot)
				s = playerInfo.name;
			else
				s = playerInfo.name + " (" + playerInfo.score + ")";
			movableWidgets.playerNames.get(i).setText(s);
		}
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

		if (frozen) {
			System.out
					.println("Client: notice: runPendingAnimations() was called while frozen, ignoring it.");
			return;
		}

		TableLayout tableLayout = computePositions(movableWidgets, cardRoles,
				tableSize);

		animateLayoutChange(duration, tableLayout, animationCompletedListener);
	}

	private void animateLayoutChange(int duration,
			final TableLayout targetTableLayout,
			GWTAnimation.AnimationCompletedListener animationCompletedListener) {

		assert !runningAnimation;

		runningAnimation = true;
		listener.onAnimationStart();

		GWTAnimation animation = new SimpleAnimation(duration) {
			@Override
			public void onUpdate(double progress) {
				for (CardWidget widget : movableWidgets.cards) {
					Position position = interpolatePosition(
							previousTableLayout.cards.get(widget),
							targetTableLayout.cards.get(widget), progress);
					setCardPosition(widget, position);
				}
				for (int player = 0; player < 4; player++) {
					Position position = interpolatePosition(
							previousTableLayout.names.get(player),
							targetTableLayout.names.get(player), progress);
					setLabelPosition(movableWidgets.playerNames.get(player),
							position);
				}
			}

			@Override
			public void onComplete() {
				super.onComplete();
				assert runningAnimation;
				runningAnimation = false;
				previousTableLayout = targetTableLayout;
				someAnimationsPending = false;
				listener.onAnimationEnd();
			}
		};
		animation.run(animationCompletedListener);
	}

	/**
	 * Reveal a covered card of the specified player as `card'. The card must
	 * not be raised. If the player has multiple covered cards, the one with
	 * higher z index is chosen.
	 * 
	 * The card is *not* moved to the correct position. Instead, the caller must
	 * ensure that it will be at the right position even when uncovered.
	 * 
	 * NOTE: There must be no animations pending when this method is called.
	 */
	public void revealCoveredCard(int player, Card card) {

		if (frozen) {
			System.out
					.println("Client: notice: revealCoveredCard() was called while frozen, ignoring it.");
			return;
		}

		assert !runningAnimation;
		assert !someAnimationsPending;

		CardWidget widget = null;

		// No animations are pending, so the current table layout is the one
		// already calculated into previousTableLayout.
		Map<CardWidget, Position> cardPositions = previousTableLayout.cards;

		for (CardWidget candidateWidget : movableWidgets.cards) {
			if (candidateWidget.getCard() == null) {
				CardRole role = cardRoles.get(candidateWidget);
				if (role.player == player && role.isRaised == false) {
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
	 * Cover the card `card' of the specified player. The card must not be
	 * raised.
	 * 
	 * The card is *not* moved to the correct position. Instead, the caller must
	 * ensure that it will be at the right position even when covered.
	 * 
	 * NOTE: There must be no animations pending when this method is called.
	 */
	public void coverCard(int player, Card card) {

		if (frozen) {
			System.out
					.println("Client: notice: coverCard() was called while frozen, ignoring it.");
			return;
		}

		assert !runningAnimation;
		assert !someAnimationsPending;
		assert card != null;

		CardWidget widget = null;

		// No animations are pending, so the current table layout is the one
		// already calculated into previousTableLayout.
		Map<CardWidget, Position> cardPositions = previousTableLayout.cards;

		for (CardWidget candidateWidget : movableWidgets.cards) {
			if (card.equals(candidateWidget.getCard())) {
				CardRole role = cardRoles.get(candidateWidget);
				if (role.player == player && role.isRaised == false) {
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

		widget.setCard(null);

		// `previousTableLayout' must be updated because the z indexes may have
		// changed by covering the card.
		previousTableLayout = computePositions(movableWidgets, cardRoles,
				tableSize);
	}

	/**
	 * The player `player' plays the card `card'. The card must be an uncovered
	 * card in the specified player's hand.
	 * 
	 * The corresponding animation will be executed at the next call to
	 * runPendingAnimations().
	 */
	public void playCard(int player, Card card) {

		if (frozen) {
			System.out
					.println("Client: notice: playCard() was called while frozen, ignoring it.");
			return;
		}

		assert card != null;
		assert !runningAnimation;

		CardWidget widget = null;

		// Search for the specified card.

		for (CardWidget candidateWidget : movableWidgets.cards) {
			Card candidateCard = candidateWidget.getCard();
			if (card.equals(candidateCard)) {
				CardRole role = cardRoles.get(candidateWidget);
				if (role.player == player && role.state == CardRole.State.HAND) {
					// Found a match
					widget = candidateWidget;
					break;
				}
			}
		}

		assert widget != null;

		cardRoles.get(widget).state = CardRole.State.PLAYED;
		cardRoles.get(widget).isRaised = false;

		// Note: this may already be `true'.
		someAnimationsPending = true;
	}

	/**
	 * The player `player' picks up the card `card' that was previously in the
	 * PLAYED state in front of him. The card must not be covered.
	 * 
	 * The corresponding animation will be executed at the next call to
	 * runPendingAnimations().
	 */
	public void pickCard(int player, Card card) {

		if (frozen) {
			System.out
					.println("Client: notice: pickCard() was called while frozen, ignoring it.");
			return;
		}

		assert card != null;
		assert !runningAnimation;

		CardWidget widget = null;

		// Search for the specified card.

		for (CardWidget candidateWidget : movableWidgets.cards) {
			Card candidateCard = candidateWidget.getCard();
			if (card.equals(candidateCard)) {
				CardRole role = cardRoles.get(candidateWidget);
				if (role.player == player
						&& role.state == CardRole.State.PLAYED) {
					// Found a match
					widget = candidateWidget;
					break;
				}
			}
		}

		assert widget != null;

		cardRoles.get(widget).state = CardRole.State.HAND;
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

		if (frozen) {
			System.out
					.println("Client: notice: raiseCard() was called while frozen, ignoring it.");
			return;
		}

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

		if (frozen) {
			System.out
					.println("Client: notice: lowerRaisedCard() was called while frozen, ignoring it.");
			return;
		}

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

	/**
	 * Runs an animation in which the played cards move towards the specified
	 * player, until they go off screen. After the animation, but before
	 * triggering the listener, such cards are removed.
	 * 
	 * Note: there must be no pending animation when calling this method. Note:
	 * the initial waiting and the following card move are considered two
	 * different animations. So the GameEventListener receives the
	 * onAnimationEnd() and onAnimationStart() notifications between the two
	 * animations.
	 */
	public void animateTrickTaking(
			final int player,
			int waitTime,
			final int animationTime,
			final GWTAnimation.AnimationCompletedListener animationCompletedListener) {

		if (frozen) {
			System.out
					.println("Client: notice: animateTrickTaking() was called while frozen, ignoring it.");
			return;
		}

		assert !runningAnimation;
		assert !someAnimationsPending;

		listener.onAnimationStart();

		Timer timer = new Timer() {
			@Override
			public void run() {
				TableLayout tableLayout = computePositions(movableWidgets,
						cardRoles, tableSize);

				// Now tableLayout is equivalent to previousTableLayout.

				for (int i = 0; i < 4 - player; i++)
					rotateTableLayout(tableLayout, tableSize);

				// Now the layout has been rotated so that the cards must move
				// towards
				// the bottom player.

				// Move the played cards off-screen.

				final List<CardWidget> playedCards = new ArrayList<CardWidget>();

				for (Entry<CardWidget, Position> e : tableLayout.cards
						.entrySet()) {
					if (cardRoles.get(e.getKey()).state == CardRole.State.PLAYED) {
						playedCards.add(e.getKey());
						e.getValue().y += (tableSize + CardWidget.cardHeight
								/ 2 - playedCardsOffset);
					}
				}

				// Rotate the layout back to the original orientation.

				for (int i = 0; i < player; i++)
					rotateTableLayout(tableLayout, tableSize);

				// Let the played cards slide below hands' cards.
				for (CardWidget widget : playedCards) {
					tableLayout.cards.get(widget).z -= defaultZIndex / 2;
					previousTableLayout.cards.get(widget).z -= defaultZIndex / 2;
				}

				// Call this here, to minimize the delay before the succeeding
				// onAnimationStart().
				listener.onAnimationEnd();

				// Actually run the animation.
				animateLayoutChange(animationTime, tableLayout,
						new GWTAnimation.AnimationCompletedListener() {
							@Override
							public void onComplete() {
								// Remove the widgets for the off-screen cards.
								for (CardWidget widget : playedCards) {
									movableWidgets.cards.remove(widget);
									previousTableLayout.cards.remove(widget);
									cardRoles.remove(widget);
									// Remove the widget from the panel.
									remove(widget);
								}

								// Recompute the layout, as the z indexes may
								// have changed.
								previousTableLayout = computePositions(
										movableWidgets, cardRoles, tableSize);

								animationCompletedListener.onComplete();
							}
						});
			}
		};

		timer.schedule(waitTime);
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
		case PLAYED:
			offset = playedCardsOffset;
			break;
		case HAND:
			offset = handCardsOffset;
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
					+ i * CardWidget.borderWidth, tableSize - offset,
					defaultZIndex, 0));

		return positions;
	}

	/**
	 * Gets a comparator that uses the same ordering that is used for displaying
	 * cards.
	 * 
	 * This is public to help callers to satisfy the preconditions for
	 * revealCoveredCard().
	 */
	public static Comparator<Card> getCardComparator() {
		return new Comparator<Card>() {
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
			public int compare(Card x, Card y) {

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
		};
	}

	public static void sortCardWidgets(List<CardWidget> list) {
		final Comparator<Card> cardComparator = getCardComparator();
		Collections.sort(list, new Comparator<CardWidget>() {
			@Override
			public int compare(CardWidget x, CardWidget y) {
				return cardComparator.compare(x.getCard(), y.getCard());
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
				cardPositions.get(list.get(i)).z = i + defaultZIndex;
		}

		// 7. Lay out the players' names.

		List<Position> namePositions = new ArrayList<Position>();
		{
			// The z index of the players' names.
			// Note that this is not 0 + defaultZIndex, just 0.
			final int z = 0;

			// Bottom player: fixed position.
			namePositions.add(new Position(tableSize / 2, tableSize - 10
					- playerLabelHeight / 2, z, 0));

			// Left player: under its cards.
			Position leftPosition = new Position(10 + playerLabelWidth / 2,
					tableSize / 2, z, 0);
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

	private static void rotateTableLayout(TableLayout layout, int tableSize) {
		for (Position position : layout.cards.values()) {
			position.rotation = (position.rotation + 90) % 360;
			int x = tableSize - position.y;
			int y = position.x;
			position.x = x;
			position.y = y;
		}
		rotatePositions(layout.names, tableSize);
	}

	public void setCornerWidget(Widget cornerWidget) {

		if (frozen) {
			System.out
					.println("Client: notice: setCornerWidget() was called while frozen, ignoring it.");
			return;
		}

		if (this.cornerWidget != null)
			remove(this.cornerWidget);

		this.cornerWidget = cornerWidget;
		cornerWidget.setWidth("200px");
		cornerWidget.setHeight("200px");
		add(cornerWidget, tableSize - 200, tableSize - 200);
	}

	/**
	 * Displays the points and the new users' scores.
	 * 
	 * Both parameters are arrays of size 4, containing information regarding
	 * each player. The first elements contain information about the bottom
	 * player, and other elements contain information about the other players,
	 * in clockwise order.
	 * 
	 * The totalScore array contains unspecified values for bots.
	 */
	public void displayScores(int[] matchPoints, int[] totalScore) {
		assert matchPoints.length == 4;
		assert totalScore.length == 4;

		final int gridHeight = 200;
		final int gridWidth = 400;

		Grid grid = new Grid(5, 3);
		grid.setCellSpacing(0);
		grid.setBorderWidth(1);
		grid.setWidth(gridWidth + "px");
		grid.setHeight(gridHeight + "px");
		add(grid, tableSize / 2 - gridWidth / 2, tableSize / 2 - gridHeight / 2);
		DOM.setStyleAttribute(grid.getElement(), "background", "white");
		DOM.setStyleAttribute(grid.getElement(), "borderLeftStyle", "solid");
		DOM.setStyleAttribute(grid.getElement(), "borderRightStyle", "solid");
		DOM.setStyleAttribute(grid.getElement(), "borderBottomStyle", "solid");
		DOM.setStyleAttribute(grid.getElement(), "borderTopStyle", "solid");
		DOM.setStyleAttribute(grid.getElement(), "borderLeftWidth", "1px");
		DOM.setStyleAttribute(grid.getElement(), "borderRightWidth", "1px");
		DOM.setStyleAttribute(grid.getElement(), "borderBottomWidth", "1px");
		DOM.setStyleAttribute(grid.getElement(), "borderTopWidth", "1px");

		{
			HTML userLabel = new HTML("<b>Utente</b>");
			userLabel.setWidth("128px");
			userLabel
					.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
			grid.setWidget(0, 1, userLabel);
			HTML matchPointsLabel = new HTML(
					"<b>Punteggio<br />della partita</b>");
			matchPointsLabel.setWidth("118px");
			matchPointsLabel
					.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
			grid.setWidget(0, 1, matchPointsLabel);
			HTML totalScoreLabel = new HTML(
					"<b>Nuovo punteggio<br/>globale</b>");
			totalScoreLabel.setWidth("148px");
			totalScoreLabel
					.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
			grid.setWidget(0, 2, totalScoreLabel);
		}

		for (int i = 0; i < 4; i++) {
			{
				SafeHtmlBuilder builder = new SafeHtmlBuilder();
				builder.appendHtmlConstant("<b>");
				builder.appendEscaped(players.get(i).name);
				builder.appendHtmlConstant("</b>");
				HTML userLabel = new HTML(builder.toSafeHtml());
				userLabel.setWidth("128px");
				userLabel
						.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
				grid.setWidget(i + 1, 0, userLabel);
			}

			{
				HTML matchPointsLabel = new HTML("" + matchPoints[i]);
				matchPointsLabel.setWidth("118px");
				matchPointsLabel
						.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
				grid.setWidget(i + 1, 1, matchPointsLabel);
			}

			if (!players.get(i).isBot) {
				SafeHtmlBuilder builder = new SafeHtmlBuilder();
				builder.append(totalScore[i]);
				builder.appendEscaped(" (");
				int change = totalScore[i] - players.get(i).score;
				if (change > 0)
					builder.append('+');
				builder.append(change);
				builder.appendEscaped(")");
				HTML totalScoreLabel = new HTML(builder.toSafeHtml());
				totalScoreLabel.setWidth("148px");
				totalScoreLabel
						.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
				grid.setWidget(i + 1, 2, totalScoreLabel);
			}
		}

		for (int i = 0; i < 4; i++) {
			players.get(i).score = totalScore[i];
		}

		updateLabels();
	}

	public void freeze() {
		frozen = true;
	}

	/**
	 * @param position The position where the bot should be inserted.
	 *                 0 means at the bottom, and other positions follow in clockwise order.
	 * @param name The name of the bot.
	 */
	public void setBot(int position, String name) {
		players.get(position).isBot = true;
		players.get(position).name = name;
		updateLabels();
	}
}
