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

package unibo.as.cupido.client.viewerstates;

import unibo.as.cupido.client.widgets.CardsGameWidget;
import unibo.as.cupido.common.structures.Card;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * This class handles the state of the game in which the 
 * game has completed (without being interrupted).
 */
public class GameEndedState implements ViewerState {

	/**
	 * The widget that displays the game.
	 */
	private CardsGameWidget cardsGameWidget;

	/**
	 * This specifies whether or not the <code>GameEnded</code> event has already been
	 * received.
	 */
	private boolean eventReceived = false;

	/**
	 * Specifies whether the UI is frozen (i.e. does no longer react to events) or not.
	 */
	private boolean frozen = false;

	/**
	 * The manager of game states.
	 */
	private ViewerStateManager stateManager;

	/**
	 * @param cardsGameWidget The widget that displays the game.
	 * @param stateManager The manager of game states.
	 */
	public GameEndedState(CardsGameWidget cardsGameWidget,
			final ViewerStateManager stateManager) {

		this.cardsGameWidget = cardsGameWidget;
		this.stateManager = stateManager;

		VerticalPanel panel = new VerticalPanel();
		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

		final HTML message = new HTML("La partita &egrave; finita");
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
	public boolean handleCardPlayed(Card card, int playerPosition) {
		if (frozen)
			return false;

		// This notification should never arrive in this state.
		freeze();
		stateManager
				.onFatalException(new Exception(
						"The CardPlayed notification was received when the client was in the GameEnded state"));
		return true;
	}

	@Override
	public boolean handleGameEnded(int[] matchPoints, int[] playersTotalPoints) {
		if (frozen)
			return false;

		if (eventReceived) {
			stateManager
					.onFatalException(new Exception(
							"Two GameEnded notifications were received while the client was in the GameEnded state."));
			return true;
		}

		eventReceived = true;

		cardsGameWidget.displayScores(matchPoints, playersTotalPoints);
		return true;
	}

	@Override
	public void handlePlayerReplaced(String name, int position) {
		if (frozen)
			return;
		// Nothing to do.
	}
}
