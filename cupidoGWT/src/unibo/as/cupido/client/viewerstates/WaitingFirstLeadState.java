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
import unibo.as.cupido.client.widgets.cardsgame.AnimationCompletedListener;
import unibo.as.cupido.common.structures.Card;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;

public class WaitingFirstLeadState implements ViewerState {

	private CardsGameWidget cardsGameWidget;
	private boolean eventReceived = false;

	private boolean frozen = false;
	private HTML message;

	private ViewerStateManager stateManager;

	public WaitingFirstLeadState(CardsGameWidget cardsGameWidget,
			final ViewerStateManager stateManager) {

		this.cardsGameWidget = cardsGameWidget;
		this.stateManager = stateManager;

		VerticalPanel panel = new VerticalPanel();
		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

		message = new HTML("Attendi l'inizio del gioco.");
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

		if (eventReceived)
			// Let the next state handle this.
			return false;

		eventReceived = true;

		message.setHTML("");

		stateManager.addPlayedCard(playerPosition, card);

		cardsGameWidget.revealCoveredCard(playerPosition, card);
		cardsGameWidget.playCard(playerPosition, card);
		cardsGameWidget.runPendingAnimations(2000,
				new AnimationCompletedListener() {
					@Override
					public void onComplete() {
						stateManager.transitionToWaitingPlayedCard();
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
	public void handlePlayerReplaced(String name, int position) {
		if (frozen)
			return;
		// Nothing to do.
	}
}
