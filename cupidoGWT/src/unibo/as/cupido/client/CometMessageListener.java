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
import unibo.as.cupido.shared.cometNotification.CardPassed;
import unibo.as.cupido.shared.cometNotification.CardPlayed;
import unibo.as.cupido.shared.cometNotification.GameEnded;
import unibo.as.cupido.shared.cometNotification.GameStarted;
import unibo.as.cupido.shared.cometNotification.NewLocalChatMessage;
import unibo.as.cupido.shared.cometNotification.NewPlayerJoined;
import unibo.as.cupido.shared.cometNotification.PlayerLeft;
import unibo.as.cupido.shared.cometNotification.PlayerReplaced;

/**
 * This is meant as a base class for classes that handle messages received
 * through comet.
 * 
 * Derived classes must override the relevant methods, without
 * calling the base class' methods. This is needed because the
 * base class methods assume that the notification can't be handled.
 */
public class CometMessageListener {
	/**
	 * This is called when a PassedCards notification is received
	 * from the servlet.
	 * 
	 * @param cards The cards that were passed to the user.
	 * 
	 * @see CardPassed
	 */
	public void onCardPassed(Card[] cards) {
		System.out
				.println("Client: the CardPassed comet message can't be handled in the current state, ignoring it.");
	}

	/**
	 * This is called when a CardPlayed notification is received
	 * from the servlet.
	 * 
	 * @param card The card that has been played.
	 * @param playerPosition The position of the player that played this card.
	 * 
	 * @see CardPlayed
	 */
	public void onCardPlayed(Card card, int playerPosition) {
		System.out
				.println("Client: the CardPlayed comet message can't be handled in the current state, ignoring it.");
	}

	/**
	 * This is called when a GameEnded notification is received
	 * from the servlet.
	 * 
	 * @param matchPoints The score scored by the players during the current game.
	 * @param playersTotalPoints The total score of the players, already updated
	 *                           with the results of the current game.
	 * 
	 * @see GameEnded
	 */
	public void onGameEnded(int[] matchPoints, int[] playersTotalPoints) {
		System.out
				.println("Client: the GameEnded comet message can't be handled in the current state, ignoring it.");
	}

	/**
	 * This is called when a GameStarted notification is received
	 * from the servlet.
	 * 
	 * @param myCards The cards that the player received from the dealer.
	 * 
	 * @see GameStarted
	 */
	public void onGameStarted(Card[] myCards) {
		System.out
				.println("Client: the GameStarted comet message can't be handled in the current state, ignoring it.");
	}

	/**
	 * This is called when a NewLocalChatMessage notification is received
	 * from the servlet.
	 * 
	 * @param user The user that sent the specified message.
	 * @param message The actual message.
	 * 
	 * @see NewLocalChatMessage
	 */
	public void onNewLocalChatMessage(String user, String message) {
		System.out
				.println("Client: the NewLocalChatMessage comet message can't be handled in the current state, ignoring it.");
	}

	/**
	 * This is called when a NewPlayerJoined notification is received
	 * from the servlet.
	 * 
	 * @param name The name of the player who joined the game.
	 * @param isBot Specifies whether the player is a user or a bot.
	 * @param score The (global) score of the player.
	 * @param position The position of the player in the table.
	 * 
	 * @see NewPlayerJoined
	 */
	public void onNewPlayerJoined(String name, boolean isBot, int points,
			int position) {
		System.out
				.println("Client: the NewPlayerJoined comet message can't be handled in the current state, ignoring it.");
	}

	/**
	 * This is called when a Playerleft notification is received
	 * from the servlet.
	 * 
	 * @param player The player that left the game.
	 * 
	 * @see PlayerLeft
	 */
	public void onPlayerLeft(String player) {
		System.out
				.println("Client: the PlayerLeft comet message can't be handled in the current state, ignoring it.");
	}

	/**
	 * This is called when a PlayerReplaced notification is received
	 * from the servlet.
	 * 
	 * @param name The name of the bot that replaced the player.
	 * @param position The position in the table where the player resided.
	 * 
	 * @see PlayerReplaced
	 */
	public void onPlayerReplaced(String name, int position) {
		System.out
				.println("Client: the PlayerReplaced comet message can't be handled in the current state, ignoring it.");
	}
}