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

package unibo.as.cupido.backend.table.bot;

import unibo.as.cupido.common.structures.Card;

public interface BotNotificationInterface {

	/**
	 * End of the game is notified to the servlet. Every players and every
	 * viewers in the table get this notification.
	 * 
	 * @param matchPoints
	 *            points the player has taken during this hand FIXME arguments
	 *            are array not integer
	 * @param playersTotalPoint
	 *            points of the player updated after this match
	 */
	public void notifyGameEnded(int[] matchPoints, int[] playersTotalPoint);

	/**
	 * Start of the game is notified to the servlet. When the game in a table
	 * can start a player in the table gets exactly one of this notification and
	 * from this notification he konws what his cards are.
	 * 
	 * @param cards
	 *            the starting hand of the player
	 */
	public void notifyGameStarted(Card[] cards);

	/**
	 * This notification is sent by the table to the player who receives
	 * <code>cards</code>. In other words when a player A passes cards to a
	 * player B the table component notifies player B and gives him the cards
	 * passed by player A.
	 * 
	 * @param cards
	 *            card passed. Card.lenght must be 3 @
	 */
	public void notifyPassedCards(Card[] cards);

	/**
	 * When a player in a table plays a card, every other players and viewers in
	 * the table get this notification.
	 * 
	 * @param card
	 *            card played
	 * @param playerPosition
	 *            position in the table of the player who played the card. Note
	 *            that position is relative to the player who is notified. More
	 *            precisely the player who joined is <code>position</code>
	 *            positions next to the player who received the notification in
	 *            clockwise order. So minimum position is one and maximum
	 *            position is three. @
	 */
	public void notifyPlayedCard(Card card, int playerPosition);

	/**
	 * When a player joins a table, every other players and viewers get
	 * notified. If joined player is a bot it is added to the table by table
	 * creator. In this case the table creator is not notified of this event
	 * because he already knows.
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
	 *            clockwise order. @
	 */
	public void notifyPlayerJoined(String playerName, boolean isBot, int score,
			int position);

	/**
	 * When a player leaves the table every other players and viewer get
	 * notified.
	 * 
	 * @param playerName
	 *            the name of the player who left the table @
	 */
	public void notifyPlayerLeft(String playerName);

	public void notifyPlayerReplaced(String botName, int relativePosition);

}
