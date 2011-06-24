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

package unibo.as.cupido.client.playerstates;

import java.util.List;

import unibo.as.cupido.client.CardsGameWidget;
import unibo.as.cupido.client.CardsGameWidget.CardRole.State;
import unibo.as.cupido.client.CupidoInterfaceAsync;
import unibo.as.cupido.client.GWTAnimation;
import unibo.as.cupido.common.exception.NoSuchTableException;
import unibo.as.cupido.common.structures.Card;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;

public class YourTurnState implements PlayerState {

	private PushButton exitButton;
	private PlayerStateManager stateManager;
	private CardsGameWidget cardsGameWidget;

	private boolean playedCard = false;
	private List<Card> hand;

	private boolean frozen = false;
	private CupidoInterfaceAsync cupidoService;

	public YourTurnState(CardsGameWidget cardsGameWidget,
			final PlayerStateManager stateManager, List<Card> hand,
			final CupidoInterfaceAsync cupidoService) {

		this.cardsGameWidget = cardsGameWidget;
		this.stateManager = stateManager;
		this.hand = hand;
		this.cupidoService = cupidoService;

		VerticalPanel panel = new VerticalPanel();
		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

		final HTML text = new HTML("Tocca a te giocare.");
		text.setWidth("120px");
		text.setWordWrap(true);
		panel.add(text);

		exitButton = new PushButton("Esci");
		exitButton.setWidth("80px");
		exitButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				freeze();
				cupidoService.leaveTable(new AsyncCallback<Void>() {
					@Override
					public void onFailure(Throwable caught) {
						try {
							throw caught;
						} catch (NoSuchTableException e) {
							// The table has been destroyed in the meantime,
							// nothing to do.
						} catch (Throwable e) {
							stateManager.onFatalException(e);
						}
					}

					@Override
					public void onSuccess(Void result) {
						stateManager.exit();
					}
				});
			}
		});
		panel.add(exitButton);

		cardsGameWidget.setCornerWidget(panel);
	}

	@Override
	public void activate() {
	}

	private static boolean canPlayCard(Card card, List<Card> playedCards,
			List<Card> hand, boolean areHeartsBroken) {

		if (playedCards.size() == 0) {
			// This is the first card in the trick.

			// If this was the first trick in the game, the active state
			// would be FirstLeader.
			assert hand.size() < 13;

			if (areHeartsBroken)
				// Any card will is ok.
				return true;

			// The player must not play hearts, if possible.
			boolean hasOnlyHearts = true;
			
			for (Card x : hand)
				if (x.suit != Card.Suit.HEARTS) {
					hasOnlyHearts = false;
					break;
				}
			
			if (hasOnlyHearts)
				return true;
			else
				return card.suit != Card.Suit.HEARTS;
		}

		// This is not the first card in a trick, so the player must play a card
		// with the
		// same suit, if possible.

		Card.Suit suit = playedCards.get(0).suit;

		boolean hasSameSuit = false;

		for (Card handCard : hand)
			if (handCard.suit == suit) {
				hasSameSuit = true;
				break;
			}

		if (hasSameSuit)
			return card.suit == suit;

		// The player has no card with that suit, so he is allowed to play any
		// card.

		return true;
	}

	@Override
	public void freeze() {
		exitButton.setEnabled(false);
		frozen = true;
	}

	@Override
	public void handleAnimationStart() {
		if (frozen) {
			System.out
					.println("Client: notice: the handleAnimationStart() event was received while frozen, ignoring it.");
			return;
		}
		exitButton.setEnabled(false);
	}

	@Override
	public void handleAnimationEnd() {
		if (frozen) {
			System.out
					.println("Client: notice: the handleAnimationEnd() event was received while frozen, ignoring it.");
			return;
		}
		exitButton.setEnabled(true);
	}

	@Override
	public void handleCardClicked(int player, Card card, State state,
			boolean isRaised) {
		if (frozen) {
			System.out
					.println("Client: notice: the handleCardClicked() event was received while frozen, ignoring it.");
			return;
		}

		if (playedCard)
			return;
		if (player != 0)
			return;

		if (!canPlayCard(card, stateManager.getPlayedCards(), hand,
				stateManager.areHeartsBroken()))
			return;

		playedCard = true;

		hand.remove(card);

		stateManager.addPlayedCard(player, card);

		cardsGameWidget.playCard(player, card);
		cardsGameWidget.runPendingAnimations(2000,
				new GWTAnimation.AnimationCompletedListener() {
					@Override
					public void onComplete() {

						if (frozen) {
							System.out
									.println("Client: notice: the onComplete() event was received while frozen, ignoring it.");
							return;
						}

						if (stateManager.getPlayedCards().size() == 4)
							stateManager.transitionToEndOfTrick(hand);
						else
							stateManager.transitionToWaitingPlayedCard(hand);
					}
				});

		cupidoService.playCard(card, new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				try {
					throw caught;
				} catch (NoSuchTableException e) {
					// The owner has left the table, so the game was
					// interrupted.
					// Nothing to do.
				} catch (Throwable e) {
					stateManager.onFatalException(e);
				}
			}

			@Override
			public void onSuccess(Void result) {
			}
		});
	}

	@Override
	public boolean handleCardPassed(Card[] cards) {
		if (frozen) {
			System.out
					.println("Client: notice: the handleCardPassed() event was received while frozen, deferring it.");
			return false;
		}
		// This notification should never arrive in this state. 
		freeze();
		stateManager.onFatalException(new Exception("The CardPassed notification was received when the client was in the YourTurn state"));
		return true;
	}

	@Override
	public boolean handleCardPlayed(Card card, int playerPosition) {
		if (frozen) {
			System.out
					.println("Client: notice: the handleCardPlayed() event was received while frozen, deferring it.");
			return false;
		}
		if (playedCard) {
			// The animation for the card playing is still in progress, let
			// the next state handle this.
			return false;
		} else {
			// This notification should never arrive in this state. 
			freeze();
			stateManager.onFatalException(new Exception("The CardPlayed notification was received when the client was in the YourTurn state"));
			return true;
		}
	}

	@Override
	public boolean handleGameEnded(int[] matchPoints, int[] playersTotalPoints) {
		if (frozen) {
			System.out
					.println("Client: notice: the handleGameEnded() event was received while frozen, deferring it.");
			return false;
		}
		if (playedCard) {
			// Let the next state handle this.
			return false;
		}
		stateManager.exit();
		Window.alert("Il creatore del tavolo \350 uscito dalla partita, quindi la partita \350 stata interrotta.");
		return true;
	}

	@Override
	public boolean handleGameStarted(Card[] myCards) {
		if (frozen) {
			System.out
					.println("Client: notice: the handleGameStarted() event was received while frozen, deferring it.");
			return false;
		}
		// This notification should never arrive in this state. 
		freeze();
		stateManager.onFatalException(new Exception("The GameStarted notification was received when the client was in the YourTurn state"));
		return true;
	}

	@Override
	public void handlePlayerLeft(int player) {
		if (frozen) {
			System.out
					.println("Client: notice: the handlePlayerLeft() event was received while frozen, ignoring it.");
			return;
		}
		// Nothing to do.
	}
}
