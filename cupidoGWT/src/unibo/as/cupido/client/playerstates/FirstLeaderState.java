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
import unibo.as.cupido.common.exception.GameInterruptedException;
import unibo.as.cupido.common.exception.NoSuchTableException;
import unibo.as.cupido.common.structures.Card;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * This class handles the state of the game in which the
 * player has to play the two of clubs.
 */
public class FirstLeaderState implements PlayerState {

	/**
	 * The widget that displays the game.
	 */
	private CardsGameWidget cardsGameWidget;

	/**
	 * This is used to communicate with the servlet using RPC.
	 */
	private CupidoInterfaceAsync cupidoService;

	/**
	 * Specifies whether the UI is frozen (i.e. does no longer react to events) or not.
	 */
	private boolean frozen = false;

	/**
	 * The list of the cards that the current user has in his hand.
	 */
	private List<Card> hand;

	/**
	 * The widget that displays the current message in the top-right corner
	 * of the table.
	 */
	private HTML message;

	/**
	 * This specifies whether or not the <code>PlayedCard</code> event has already been
	 * received.
	 */
	private boolean playedCard = false;

	/**
	 * The manager of game states.
	 */
	private PlayerStateManager stateManager;

	/**
	 * @param cardsGameWidget The widget that displays the game.
	 * @param stateManager The manager of game states.
	 * @param hand The list of the cards that the current user has in his hand.
	 * @param cupidoService This is used to communicate with the servlet using RPC.
	 */
	public FirstLeaderState(CardsGameWidget cardsGameWidget,
			final PlayerStateManager stateManager, List<Card> hand,
			final CupidoInterfaceAsync cupidoService) {

		this.cardsGameWidget = cardsGameWidget;
		this.stateManager = stateManager;
		this.hand = hand;
		this.cupidoService = cupidoService;

		VerticalPanel panel = new VerticalPanel();
		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

		message = new HTML(
				"Sei il primo a giocare; devi giocare il due di fiori");
		message.setWidth("120px");
		message.setWordWrap(true);
		panel.add(message);

		cardsGameWidget.setCornerWidget(panel);
	}

	@Override
	public void activate() {
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

		if (player != 0 || card == null)
			return;
		if (card.suit != Card.Suit.CLUBS || card.value != 2)
			return;

		if (playedCard)
			// The user has already played a card.
			return;

		playedCard = true;

		message.setText("");

		hand.remove(card);

		stateManager.addPlayedCard(0, card);

		cardsGameWidget.playCard(0, card);
		cardsGameWidget.runPendingAnimations(2000,
				new AnimationCompletedListener() {
					@Override
					public void onComplete() {
						stateManager.transitionToWaitingPlayedCard(hand);
					}
				});

		cupidoService.playCard(card, new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				try {
					throw caught;
				} catch (NoSuchTableException e) {
					// The table does not exist anymore, because the creator has
					// left.
					// Do nothing yet, this situation will be handled when the
					// GameEnded
					// notification arrives.
				} catch (GameInterruptedException e) {
					// The table does not exist anymore, because the creator has
					// left.
					// Do nothing yet, this situation will be handled when the
					// GameEnded
					// notification arrives.
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
		if (frozen)
			return false;

		// This notification should never arrive in this state.
		freeze();
		stateManager
				.onFatalException(new Exception(
						"The CardPassed notification was received when the client was in the FirstLeader state"));
		return true;
	}

	@Override
	public boolean handleCardPlayed(Card card, int playerPosition) {
		if (frozen)
			return false;

		if (playedCard)
			// Let the next state handle this.
			return false;

		// This notification should never arrive in this state.
		freeze();
		stateManager
				.onFatalException(new Exception(
						"The CardPlayed notification was received when the client was in the FirstLeader state"));
		return true;
	}

	@Override
	public boolean handleGameEnded(int[] matchPoints, int[] playersTotalPoints) {
		if (frozen)
			return false;

		if (playedCard)
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

		// This notification should never arrive in this state.
		freeze();
		stateManager
				.onFatalException(new Exception(
						"The GameStarted notification was received when the client was in the FirstLeader state"));
		return true;
	}

	@Override
	public void handlePlayerReplaced(String name, int position) {
		if (frozen)
			return;
		// Nothing to do.
	}
}
