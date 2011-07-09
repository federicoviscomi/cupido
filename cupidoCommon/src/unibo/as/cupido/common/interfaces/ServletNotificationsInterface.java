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

package unibo.as.cupido.common.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

import unibo.as.cupido.common.exception.NoSuchPlayerException;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.ChatMessage;

public interface ServletNotificationsInterface extends Remote {

	/**
	 * Every players and every viewers in the table get this notification. The
	 * game could end normally or prematurely. The last happens when player
	 * creator leaves the table before normal end of the game, in this case and
	 * only in this case all arguments are <code>null</code>.
	 * 
	 * @param matchPoints
	 *            score the player has taken during this hand
	 * @param playersTotalPoint
	 *            score of the player updated after this match
	 */
	public void notifyGameEnded(int[] matchPoints, int[] playersTotalPoint)
			throws RemoteException;

	/**
	 * When the game in a table can start every player in the table gets exactly
	 * one of this notification and from this notification he knows what his
	 * cards are.
	 * 
	 * @param cards
	 *            the starting hand of the player
	 */
	public void notifyGameStarted(Card[] cards) throws RemoteException;

	/**
	 * When a user sends a local chat message then the table component notifies
	 * every players and every viewers in the table but not the one who sent the
	 * message.
	 * 
	 * @param message
	 *            contains user name and its message sent to a local table chat
	 * @throws RemoteException
	 *             is message can't be send
	 */
	public void notifyLocalChatMessage(ChatMessage message)
			throws RemoteException;

	/**
	 * This notification is sent by the table to the player who receives
	 * <code>cards</code>. In other words when a player A passes cards to a
	 * player B the table component notifies player B and gives him the cards
	 * passed by player A.
	 * 
	 * @param cards
	 *            card passed. Card.lenght must be 3
	 * @throws RemoteException
	 */
	public void notifyPassedCards(Card[] cards) throws RemoteException;

	/**
	 * When a player in a table plays a card, every other players and viewers in
	 * the table get this notification.
	 * 
	 * @param card
	 *            card played
	 * @param playerPosition
	 *            position of the player who played the card. If who receives
	 *            this notification is a viewer then <code>playerPosition</code>
	 *            is the absolute position in the table of the player who played
	 *            a card and is in range 0-3. Otherwise if who receives this
	 *            notification is a player then <code>playerPosition</code> is
	 *            the position of the player who played relative to the player
	 *            who receives this notification and is in range 0-2.
	 * @throws RemoteException
	 * 
	 */
	public void notifyPlayedCard(Card card, int playerPosition)
			throws RemoteException;

	/**
	 * When a player joins a table, every other players and viewers get
	 * notified. If joined player is a bot it is added to the table by table
	 * creator. In this case the table creator is not notified of this event
	 * because he already knows. This notification is not sent when a bot join
	 * an already started game.
	 * 
	 * @param playerName
	 *            of the joined player
	 * @param isBot
	 *            <code>false<code> if the player is a human player,
	 *            <code>true<code> if he is a bot
	 * @param score
	 *            total player score. This is meaningful only if player is not a
	 *            bot.
	 * @param position
	 *            table position where the player has entered. Note that
	 *            position is relative to the player who is notified. More
	 *            precisely the player who joined is <code>position</code>
	 *            positions next to the player who received the notification in
	 *            clockwise order.
	 * @throws RemoteException
	 * @see notifyPlayerJoined
	 */
	public void notifyPlayerJoined(String playerName, boolean isBot, int score,
			int position) throws RemoteException;

	/**
	 * If a player left the table when game is already started, it's replaced
	 * with a bot called {@link botName} and every other player get this
	 * notification.
	 * 
	 * @param position
	 *            position of the player who has left
	 * @param botName
	 *            name of the joining bot
	 * @throws NoSuchPlayerException
	 */
	public void notifyPlayerReplaced(String botName, int position)
			throws RemoteException;

	/**
	 * When a player leaves the table before the game starts, every other
	 * players and viewer get this notification.
	 * 
	 * @param playerName
	 *            the name of the player who left the table
	 * @throws RemoteException
	 */
	public void notifyPlayerLeft(String playerName) throws RemoteException;
}
