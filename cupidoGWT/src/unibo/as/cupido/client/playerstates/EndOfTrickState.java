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

import unibo.as.cupido.client.CupidoInterfaceAsync;
import unibo.as.cupido.client.widgets.CardsGameWidget;
import unibo.as.cupido.client.widgets.cardsgame.AnimationCompletedListener;
import unibo.as.cupido.client.widgets.cardsgame.CardRole;
import unibo.as.cupido.common.structures.Card;

import com.google.gwt.user.client.ui.SimplePanel;

public class EndOfTrickState implements PlayerState {

	private CardsGameWidget cardsGameWidget;
	private boolean frozen = false;

	private List<Card> hand;

	private PlayerStateManager stateManager;

	public EndOfTrickState(CardsGameWidget cardsGameWidget,
			final PlayerStateManager stateManager, final List<Card> hand,
			final CupidoInterfaceAsync cupidoService) {

		this.cardsGameWidget = cardsGameWidget;
		this.stateManager = stateManager;
		this.hand = hand;

		cardsGameWidget.setCornerWidget(new SimplePanel());
	}

	@Override
	public void activate() {

		stateManager.goToNextTrick();

		final int player = stateManager.getFirstPlayerInTrick();

		cardsGameWidget.animateTrickTaking(player, 1500, 2000,
				new AnimationCompletedListener() {

					@Override
					public void onComplete() {
						if (frozen)
							return;

						if (hand.size() != 0) {
							if (player == 0)
								stateManager.transitionToYourTurn(hand);
							else
								stateManager
										.transitionToWaitingPlayedCard(hand);
						} else
							stateManager.transitionToGameEnded();
					}
				});
	}

	@Override
	public void freeze() {
		frozen = true;
	}

	@Override
	public void handleAnimationEnd() {
		if (frozen)
			return;
	}

	@Override
	public void handleAnimationStart() {
		if (frozen)
			return;
	}

	@Override
	public void handleCardClicked(int player, Card card, CardRole.State state,
			boolean isRaised) {
		if (frozen)
			return;
	}

	@Override
	public boolean handleCardPassed(Card[] cards) {
		if (frozen)
			return false;

		// This notification should never arrive in this state.
		freeze();
		stateManager
				.onFatalException(new Exception(
						"The CardPassed notification was received when the client was in the EndOfTrick state"));
		return true;
	}

	@Override
	public boolean handleCardPlayed(Card card, int playerPosition) {
		if (frozen) {
			System.out
					.println("Client: notice: the handleCardPlayed() event was received while frozen, deferring it.");
			return false;
		}
		// Let the next state handle this.
		return false;
	}

	@Override
	public boolean handleGameEnded(int[] matchPoints, int[] playersTotalPoints) {
		if (frozen)
			return false;

		// Let the next state handle this.
		return false;
	}

	@Override
	public boolean handleGameStarted(Card[] myCards) {
		if (frozen)
			return false;

		// This notification should never arrive in this state.
		freeze();
		stateManager
				.onFatalException(new Exception(
						"The GameStarted notification was received when the client was in the EndOfTrick state"));
		return true;
	}

	@Override
	public void handlePlayerReplaced(String name, int position) {
		if (frozen)
			return;
		// Nothing to do.
	}
}
