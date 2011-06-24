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

import unibo.as.cupido.common.structures.Card;

public interface ViewerState {

	/**
	 * Activates the state. This is needed, because a state can't start an
	 * animation in the constructor because it is not registered as the event
	 * listener yet, so it would miss some animation-related events.
	 */
	public void activate();

	public void freeze();

	/**
	 * This is called before starting an animation.
	 */
	public void handleAnimationStart();

	/**
	 * This is called when an animation finishes.
	 */
	public void handleAnimationEnd();

	/**
	 * Returns false if this event can't be handled right now, but it can be
	 * handled in a later state. It will be notified again at each state
	 * transition, until it is handled.
	 */
	public boolean handleCardPlayed(Card card, int playerPosition);

	/**
	 * Returns false if this event can't be handled right now, but it can be
	 * handled in a later state. It will be notified again at each state
	 * transition, until it is handled.
	 */
	public boolean handleGameEnded(int[] matchPoints, int[] playersTotalPoints);
	
	public void handlePlayerLeft(int player);

}
