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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import unibo.as.cupido.client.CupidoInterfaceAsync;
import unibo.as.cupido.client.widgets.CardsGameWidget;
import unibo.as.cupido.client.widgets.cardsgame.AnimationCompletedListener;
import unibo.as.cupido.client.widgets.cardsgame.CardRole;
import unibo.as.cupido.common.exception.GameInterruptedException;
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

public class CardPassingState implements PlayerState {

	private CardsGameWidget cardsGameWidget;
	private PlayerStateManager stateManager;
	private CupidoInterfaceAsync cupidoService;

	private boolean frozen = false;
	/**
	 * Whether the user has already confirmed to pass the selected cards.
	 */
	private boolean confirmed = false;

	private List<Card> hand;
	private List<Card> raisedCards = new ArrayList<Card>();

	private HTML message;
	private PushButton okButton;

	public CardPassingState(final CardsGameWidget cardsGameWidget,
			final PlayerStateManager stateManager, final List<Card> hand,
			final CupidoInterfaceAsync cupidoService) {

		this.cardsGameWidget = cardsGameWidget;
		this.stateManager = stateManager;
		this.cupidoService = cupidoService;
		this.hand = hand;

		VerticalPanel cornerWidget = new VerticalPanel();
		cornerWidget.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		cornerWidget
				.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

		message = new HTML("Seleziona le tre carte da passare.");
		message.setWidth("120px");
		message.setWordWrap(true);
		cornerWidget.add(message);

		okButton = new PushButton("OK");
		okButton.setEnabled(false);
		okButton.setWidth("80px");
		okButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				handleConfirmSelectedCards();
			}
		});
		cornerWidget.add(okButton);

		cardsGameWidget.setCornerWidget(cornerWidget);
	}

	private void handleConfirmSelectedCards() {
		assert raisedCards.size() == 3;
		assert !confirmed;

		message.setText("");
		// Note that the button will no longer be *visible*,
		// it will not only be disabled.
		okButton.setVisible(false);

		confirmed = true;

		Card[] cards = raisedCards.toArray(new Card[0]);

		cupidoService.passCards(cards, new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				try {
					throw caught;
				} catch (NoSuchTableException e) {
					// The owner has left the table, so the game was
					// interrupted.
				} catch (GameInterruptedException e) {
					// The owner has left the table, so the game was
					// interrupted.
				} catch (Throwable e) {
					stateManager.onFatalException(e);
				}
			}

			@Override
			public void onSuccess(Void result) {
			}
		});

		for (Card card : raisedCards)
			cardsGameWidget.playCard(0, card);

		cardsGameWidget.runPendingAnimations(1500,
				new AnimationCompletedListener() {
					@Override
					public void onComplete() {

						if (frozen)
							return;

						List<Card> sortedList = new ArrayList<Card>();
						for (Card card : raisedCards)
							sortedList.add(card);

						final Comparator<Card> cardComparator = CardsGameWidget
								.getCardComparator();
						Collections.sort(sortedList, cardComparator);

						// Cover the cards in reverse order, to
						// respect the precondition
						// for CardsGameWidget.coverCard().
						for (int i = 0; i < 3; i++)
							cardsGameWidget.coverCard(0,
									sortedList.get(3 - i - 1));

						for (Card card : raisedCards) {
							boolean removedSomething = hand
									.remove(card);
							assert removedSomething;
						}

						stateManager
								.transitionToCardPassingWaiting(hand);
					}
				});
	}

	@Override
	public void activate() {
	}

	@Override
	public void freeze() {
		okButton.setEnabled(false);
		frozen = true;
	}

	@Override
	public void handleAnimationStart() {
		if (frozen)
			return;
		okButton.setEnabled(false);
	}

	@Override
	public void handleAnimationEnd() {
		if (frozen)
			return;
		okButton.setEnabled(!confirmed && raisedCards.size() == 3);
	}

	@Override
	public void handleCardClicked(int player, Card card, CardRole.State state,
			boolean isRaised) {
		if (frozen)
			return;
		
		if (state == CardRole.State.PLAYED)
			return;
		if (player != 0 || card == null)
			return;

		if (isRaised) {
			boolean found = raisedCards.remove(card);
			assert found;
			cardsGameWidget.lowerRaisedCard(0, card);
		} else {
			if (raisedCards.size() == 3)
				// Never raise more than 3 cards at once.
				return;
			raisedCards.add(card);
			cardsGameWidget.raiseCard(0, card);
		}

		cardsGameWidget.runPendingAnimations(200,
				new AnimationCompletedListener() {
					@Override
					public void onComplete() {
					}
				});
	}

	@Override
	public boolean handleCardPassed(Card[] cards) {
		if (frozen)
			return false;
		
		// Let the next state handle this.
		return false;
	}

	@Override
	public boolean handleCardPlayed(Card card, int playerPosition) {
		if (frozen)
			return false;
		
		// Let the next state handle this.
		return false;
	}

	@Override
	public boolean handleGameEnded(int[] matchPoints, int[] playersTotalPoints) {
		if (frozen)
			return false;
		
		if (confirmed)
			// Let the next state handle this.
			return false;
		
		stateManager.exit();
		Window.alert("Il creatore del tavolo \350 uscito dalla partita, quindi la partita \350 stata interrotta.");
		return true;
	}

	@Override
	public boolean handleGameStarted(Card[] myCards) {
		if (frozen)
			return false;
		
		// Let the next state handle this.
		return false;
	}

	@Override
	public void handlePlayerReplaced(String name, int position) {
		if (frozen)
			return;
		// Nothing to do.
	}
}
