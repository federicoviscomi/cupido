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

import unibo.as.cupido.client.CardsGameWidget;
import unibo.as.cupido.client.GWTAnimation;
import unibo.as.cupido.common.structures.Card;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;

public class EndOfTrickState implements ViewerState {

	private PushButton exitButton;
	private CardsGameWidget cardsGameWidget;
	private ViewerStateManager stateManager;

	private boolean frozen = false;

	public EndOfTrickState(CardsGameWidget cardsGameWidget,
			final ViewerStateManager stateManager) {

		this.cardsGameWidget = cardsGameWidget;
		this.stateManager = stateManager;

		VerticalPanel panel = new VerticalPanel();
		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

		final HTML text = new HTML("");
		text.setWidth("120px");
		text.setWordWrap(true);
		panel.add(text);

		exitButton = new PushButton("Esci");
		exitButton.setEnabled(false);
		exitButton.setWidth("80px");
		exitButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				stateManager.exit();
			}
		});
		panel.add(exitButton);

		cardsGameWidget.setCornerWidget(panel);
	}

	@Override
	public void activate() {
		stateManager.goToNextTrick();

		final int player = stateManager.getFirstPlayerInTrick();

		cardsGameWidget.animateTrickTaking(player, 1500, 2000,
				new GWTAnimation.AnimationCompletedListener() {

					@Override
					public void onComplete() {
						if (stateManager.getRemainingTricks() == 0)
							stateManager.transitionToGameEnded();
						else
							stateManager.transitionToWaitingPlayedCard();
					}
				});
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
	public boolean handleCardPlayed(Card card, int playerPosition) {
		if (frozen) {
			System.out
					.println("Client: notice: the CardPlayed event was received while frozen, deferring it.");
			return false;
		}
		// Let the next state handle this.
		return false;
	}

	@Override
	public boolean handleGameEnded(int[] matchPoints, int[] playersTotalPoints) {
		if (frozen) {
			System.out
					.println("Client: notice: the GameEnded event was received while frozen, deferring it.");
			return false;
		}

		// Let the next state handle this.
		return false;
	}

	@Override
	public void handlePlayerLeft(int player) {
		if (frozen) {
			System.out
					.println("Client: notice: the PlayerLeft event was received while frozen, ignoring it.");
			return;
		}
		// Nothing to do.
	}
}
