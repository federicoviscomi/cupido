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

package unibo.as.cupido.client;

import unibo.as.cupido.common.structures.Card;

/**
 * This is meant as a base class for classes that handle messages received
 * through comet. Derived classes must override the relevant methods, without
 * calling the base class methods. This is needed because the base class methods
 * assume the message can't be handled.
 */
public class CometMessageListener {
	public void onCardPassed(Card[] cards) {
		System.out
				.println("Client: the CardPassed comet message can't be handled in the current state, ignoring it.");
	}

	public void onCardPlayed(Card card, int playerPosition) {
		System.out
				.println("Client: the CardPlayed comet message can't be handled in the current state, ignoring it.");
	}

	public void onGameEnded(int[] matchPoints, int[] playersTotalPoints) {
		System.out
				.println("Client: the GameEnded comet message can't be handled in the current state, ignoring it.");
	}

	public void onGameStarted(Card[] myCards) {
		System.out
				.println("Client: the GameStarted comet message can't be handled in the current state, ignoring it.");
	}

	public void onNewLocalChatMessage(String user, String message) {
		System.out
				.println("Client: the NewLocalChatMessage comet message can't be handled in the current state, ignoring it.");
	}

	public void onNewPlayerJoined(String name, boolean isBot, int points,
			int position) {
		System.out
				.println("Client: the NewPlayerJoined comet message can't be handled in the current state, ignoring it.");
	}

	public void onPlayerLeft(String player) {
		System.out
				.println("Client: the PlayerLeft comet message can't be handled in the current state, ignoring it.");
	}

	public void onPlayerReplaced(String name, int position) {
		System.out
				.println("Client: the PlayerReplaced comet message can't be handled in the current state, ignoring it.");
	}
}