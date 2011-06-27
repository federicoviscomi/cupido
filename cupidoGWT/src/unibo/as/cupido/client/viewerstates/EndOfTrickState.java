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

import com.google.gwt.user.client.ui.SimplePanel;

public class EndOfTrickState implements ViewerState {

	private CardsGameWidget cardsGameWidget;
	private ViewerStateManager stateManager;

	private boolean frozen = false;

	public EndOfTrickState(CardsGameWidget cardsGameWidget,
			final ViewerStateManager stateManager) {

		this.cardsGameWidget = cardsGameWidget;
		this.stateManager = stateManager;

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
						if (stateManager.getRemainingTricks() == 0)
							stateManager.transitionToGameEnded();
						else
							stateManager.transitionToWaitingPlayedCard();
					}
				});
	}

	@Override
	public void freeze() {
		frozen = true;
	}

	@Override
	public void handleAnimationStart() {
		if (frozen)
			return;
	}

	@Override
	public void handleAnimationEnd() {
		if (frozen)
			return;
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
