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
import java.util.List;

import unibo.as.cupido.client.CupidoInterfaceAsync;
import unibo.as.cupido.client.GWTAnimation;
import unibo.as.cupido.client.widgets.CardsGameWidget;
import unibo.as.cupido.client.widgets.CardsGameWidget.CardRole.State;
import unibo.as.cupido.common.structures.Card;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;

public class CardPassingWaitingState implements PlayerState {

	private CardsGameWidget cardsGameWidget;

	private PlayerStateManager stateManager;

	private List<Card> hand;

	private boolean frozen = false;

	private boolean eventReceived = false;

	private HTML text;

	public CardPassingWaitingState(CardsGameWidget cardsGameWidget,
			final PlayerStateManager stateManager, List<Card> hand,
			final CupidoInterfaceAsync cupidoService) {

		this.cardsGameWidget = cardsGameWidget;
		this.stateManager = stateManager;
		this.hand = hand;
		VerticalPanel panel = new VerticalPanel();
		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

		text = new HTML("Aspetta che gli altri giocatori decidano quali carte passare.");
		text.setWidth("120px");
		text.setWordWrap(true);
		panel.add(text);

		cardsGameWidget.setCornerWidget(panel);
	}

	@Override
	public void activate() {
	}

	@Override
	public void freeze() {
		frozen = false;
	}

	@Override
	public void handleAnimationStart() {
		if (frozen) {
			System.out
					.println("Client: notice: the handleAnimationStart() event was received while frozen, ignoring it.");
			return;
		}
	}

	@Override
	public void handleAnimationEnd() {
		if (frozen) {
			System.out
					.println("Client: notice: the handleAnimationEnd() event was received while frozen, ignoring it.");
			return;
		}
	}

	@Override
	public void handleCardClicked(int player, Card card, State state,
			boolean isRaised) {
		if (frozen) {
			System.out
					.println("Client: notice: the handleCardClicked() event was received while frozen, ignoring it.");
			return;
		}
	}

	@Override
	public boolean handleCardPassed(Card[] passedCards) {
		if (frozen) {
			System.out
					.println("Client: notice: the handleCardPassed() event was received while frozen, deferring it.");
			return false;
		}

		if (eventReceived) {
			// Let the next state handle this.
			// It is an error, but the animation is running and it can't be
			// handled right now.
			return false;
		}

		eventReceived = true;
		
		text.setText("");

		List<Card> cards = new ArrayList<Card>();

		for (Card card : passedCards)
			cards.add(card);

		hand.addAll(cards);

		Collections.sort(cards, CardsGameWidget.getCardComparator());

		for (int i = 0; i < 3; i++)
			cardsGameWidget.revealCoveredCard(0, cards.get(3 - i - 1));

		for (Card card : passedCards)
			cardsGameWidget.pickCard(0, card);

		cardsGameWidget.runPendingAnimations(2000,
				new GWTAnimation.AnimationCompletedListener() {
					@Override
					public void onComplete() {
						boolean found = false;
						for (Card card : hand)
							if (card.suit == Card.Suit.CLUBS && card.value == 2) {
								found = true;
								break;
							}
						if (found)
							stateManager.transitionToFirstLeader(hand);
						else
							stateManager.transitionToWaitingFirstLead(hand);
					}
				});

		return true;
	}

	@Override
	public boolean handleCardPlayed(Card card, int playerPosition) {
		if (frozen) {
			System.out
					.println("Client: notice: the handleCardPassed() event was received while frozen, deferring it.");
			return false;
		}
		// Let the next state handle this.
		return false;
	}

	@Override
	public boolean handleGameEnded(int[] matchPoints, int[] playersTotalPoints) {
		if (frozen) {
			System.out
					.println("Client: notice: the handleGameEnded() event was received while frozen, deferring it.");
			return false;
		}
		if (eventReceived) {
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
		// Let the next state handle this.
		return false;
	}

	@Override
	public void handlePlayerReplaced(String name, int position) {
		if (frozen) {
			System.out
					.println("Client: notice: the handlePlayerReplaced() event was received while frozen, ignoring it.");
			return;
		}
		// Nothing to do.
	}
}
