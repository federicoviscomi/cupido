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

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * This class handles the state of the game in which the player is waiting
 * another player to play a card.
 * 
 * Note that if no card has been played since the game start, the current state
 * is <code>WaitingFirstLeadState</code> instead.
 * 
 * @see WaitingFirstLeadState
 */
public class WaitingPlayedCardState implements PlayerState {

	/**
	 * The widget that displays the game.
	 */
	private CardsGameWidget cardsGameWidget;

	/**
	 * Specifies the position of player that is expected to play.
	 */
	private final int currentPlayer;

	/**
	 * This specifies whether or not the <code>PlayedCard</code> event has
	 * already been received.
	 */
	private boolean eventReceived = false;

	/**
	 * Specifies whether the UI is frozen (i.e. does no longer react to events)
	 * or not.
	 */
	private boolean frozen = false;

	/**
	 * The list of the cards that the current user has in his hand.
	 */
	private List<Card> hand;

	/**
	 * The widget that displays the current message in the top-right corner of
	 * the table.
	 */
	private HTML message;

	/**
	 * The manager of game states.
	 */
	private PlayerStateManager stateManager;

	/**
	 * @param cardsGameWidget
	 *            The widget that displays the game.
	 * @param stateManager
	 *            The manager of game states.
	 * @param hand
	 *            The list of the cards that the current user has in his hand.
	 * @param cupidoService
	 *            This is used to communicate with the servlet using RPC.
	 */
	public WaitingPlayedCardState(CardsGameWidget cardsGameWidget,
			final PlayerStateManager stateManager, List<Card> hand,
			final CupidoInterfaceAsync cupidoService) {

		this.cardsGameWidget = cardsGameWidget;
		this.stateManager = stateManager;
		this.hand = hand;
		VerticalPanel panel = new VerticalPanel();
		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

		currentPlayer = (stateManager.getFirstPlayerInTrick() + stateManager
				.getPlayedCards().size()) % 4;

		message = new HTML();

		recomputeMessage();

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
	}

	@Override
	public boolean handleCardPassed(Card[] cards) {
		if (frozen)
			return false;

		// This notification should never arrive in this state.
		freeze();
		stateManager
				.onFatalException(new Exception(
						"The CardPassed notification was received when the client was in the WaitingPlayedCard state"));
		return true;
	}

	@Override
	public boolean handleCardPlayed(Card card, int playerPosition) {
		if (frozen)
			return false;

		if (eventReceived)
			// The next state will handle this.
			return false;

		eventReceived = true;

		message.setText("");

		// playerPosition was in the [0-2] interval, now it is between 1 and 3.
		++playerPosition;

		stateManager.addPlayedCard(playerPosition, card);

		cardsGameWidget.revealCoveredCard(playerPosition, card);

		cardsGameWidget.playCard(playerPosition, card);
		cardsGameWidget.runPendingAnimations(2000,
				new AnimationCompletedListener() {
					@Override
					public void onComplete() {
						if (stateManager.getPlayedCards().size() == 4)
							stateManager.transitionToEndOfTrick(hand);
						else {
							if (currentPlayer == 3)
								stateManager.transitionToYourTurn(hand);
							else
								stateManager
										.transitionToWaitingPlayedCard(hand);
						}
					}
				});
		return true;
	}

	@Override
	public boolean handleGameEnded(int[] matchPoints, int[] playersTotalPoints) {
		if (frozen)
			return false;

		if (eventReceived)
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
						"The GameStarted notification was received when the client was in the WaitingPlayedCard state"));
		return true;
	}

	@Override
	public void handlePlayerReplaced(String name, int position) {
		if (frozen)
			return;

		if (eventReceived)
			// Nothing to do.
			return;
		if (currentPlayer == position)
			recomputeMessage();
	}

	/**
	 * This helper method is used to compute or recompute the displayed message.
	 */
	private void recomputeMessage() {
		PlayerStateManager.PlayerInfo playerInfo = stateManager.getPlayerInfo()
				.get(currentPlayer);

		assert currentPlayer != 0;

		SafeHtmlBuilder safeHtmlBuilder = new SafeHtmlBuilder();
		safeHtmlBuilder.appendHtmlConstant("Attendi che ");
		safeHtmlBuilder.appendEscaped(playerInfo.name);
		safeHtmlBuilder.appendHtmlConstant(" giochi.");
		message = new HTML(safeHtmlBuilder.toSafeHtml().asString());
	}
}
