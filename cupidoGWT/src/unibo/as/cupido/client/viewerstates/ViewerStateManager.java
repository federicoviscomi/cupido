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

import java.util.List;

import unibo.as.cupido.client.CardsGameWidget;
import unibo.as.cupido.common.structures.Card;

public interface ViewerStateManager {

	public class PlayerInfo {
		/**
		 * This is relevant only when `isBot' is false.
		 */
		String name;
		boolean isBot;
	}

	/**
	 * Returns the leading player for the current trick. A return value of 0
	 * means the bottom player, and other players' indexes follow in clockwise
	 * order.
	 * 
	 * This returns -1 in the initial card-passing states and at the beginning
	 * of the first trick.
	 */
	public int getFirstPlayerInTrick();

	/**
	 * @return The ordered list containing the cards played in the current trick.
	 */
	public List<Card> getPlayedCards();

	public void addPlayedCard(int player, Card card);

	public void goToNextTrick();

	/**
	 * @return The number of remaining tricks, including the current one (if
	 *         any).
	 */
	public int getRemainingTricks();

	/**
	 * Exits from the game.
	 */
	public void exit();

	public List<PlayerInfo> getPlayerInfo();

	CardsGameWidget getWidget();

	public void transitionToEndOfTrick();

	public void transitionToWaitingFirstLead();

	public void transitionToGameEnded();

	public void transitionToWaitingPlayedCard();

	public void freeze();

	public void handleCardPlayed(Card card, int playerPosition);

	public void handleGameEnded(int[] matchPoints, int[] playersTotalPoints);

	public void handlePlayerLeft(String player);
}
