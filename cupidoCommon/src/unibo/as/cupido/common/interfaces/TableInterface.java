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
import java.sql.SQLException;

import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.ChatMessage;
import unibo.as.cupido.common.structures.InitialTableStatus;
import unibo.as.cupido.common.structures.ObservedGameStatus;
import unibo.as.cupido.common.exception.DuplicateUserNameException;
import unibo.as.cupido.common.exception.DuplicateViewerException;
import unibo.as.cupido.common.exception.FullTableException;
import unibo.as.cupido.common.exception.GameEndedException;
import unibo.as.cupido.common.exception.GameInterruptedException;
import unibo.as.cupido.common.exception.IllegalMoveException;
import unibo.as.cupido.common.exception.NoSuchLTMException;
import unibo.as.cupido.common.exception.NoSuchPlayerException;
import unibo.as.cupido.common.exception.NoSuchTableException;
import unibo.as.cupido.common.exception.NoSuchUserException;
import unibo.as.cupido.common.exception.NotCreatorException;
import unibo.as.cupido.common.exception.FullPositionException;
import unibo.as.cupido.common.exception.WrongGameStateException;

/**
 * 
 * Used by the Servlet
 * 
 * implemented by the Table
 * 
 * Ther is no polling on the local table chat.
 * 
 * @author cane
 * 
 */
public interface TableInterface extends Remote {
	/**
	 * INIT means that the Table has less then four players(clients or bots)
	 * joined and cards are not dealt yet.
	 * 
	 * PASSING_CARDS means that the Table has exactly four players, cards are
	 * dealt and not all players have passed cards yet.
	 * 
	 * STARTED means that the table has exactly four players and they all have
	 * passed cards.
	 * 
	 * ENDED means that the table has four players and none of them has cards
	 * left to play.
	 * 
	 * INTERRUPTED means that the owner left the table.
	 */
	public static enum GameStatus {
		INIT, PASSING_CARDS, STARTED, ENDED, INTERRUPTED
	}

	/**
	 * TODO ?Use this enum in all method instead of an int?
	 */
	public static enum Positions {
		OWNER, LEFT, RIGHT, UP
	}

	/**
	 * Add a bot in the table. This can be accomplished by
	 * <ul>
	 * <li>the creator of the table only when game status is INIT</li>
	 * <li>the Table itself only when the game status is PASSING_CARDS or
	 * STARTED and a player leaves</li>
	 * </ul>
	 * 
	 * @param userName
	 *            the name of the user who wants to add a bot. Note that only
	 *            the creator of the table can add a bot
	 * @param position
	 *            the absolute position of the bot in the table
	 * @throws FullPositionException
	 *             if the position is occupied by another player
	 * @throws RemoteException
	 * @throws IllegalArgumentException
	 *             <ul>
	 *             <li>if <code>botName</code> is null</li>
	 *             <li>if position is out of range, i.e. not {@link Positions}
	 *             .LEFT, {@link Positions}.UP or {@link Positions}.RIGHT.</li>
	 *             </ul>
	 * 
	 * @throws NotCreatorException
	 *             if this method is called by a player who is not the creator
	 *             of the table.
	 * @throws GameInterruptedException
	 *             if the game status is INTERRUPTED
	 * @return The name of the bot.
	 */
	String addBot(String userName, int position) throws FullPositionException,
			RemoteException, IllegalArgumentException,
			NotCreatorException, GameInterruptedException;

	/**
	 * Called by player <code>userName</code> to join this table. A player can
	 * join a Table only if game status is {@link GameStatus}.INIT.
	 * 
	 * @param userName
	 *            the name of the player who wants to join this table.
	 * @return a portion of the status of the table as described by
	 *         {@link InitialTableStatus}
	 * @throws FullTableException
	 *             if the table already has four player
	 * @throws IllegalArgumentException
	 *             if an argument is null
	 * @throws DuplicateUserNameException
	 *             if a player name <code>userName</code> is already playing or
	 *             viewing the table
	 * @throws NoSuchUserException
	 *             if the database contains no player named
	 *             <code>userName</code>
	 * @throws GameInterruptedException
	 *             if the game status is INTERRUPTED
	 */
	public InitialTableStatus joinTable(String userName,
			ServletNotificationsInterface snf) throws FullTableException,
			RemoteException, IllegalArgumentException,
			DuplicateUserNameException, GameInterruptedException,
			NoSuchUserException;

	/**
	 * Called by a player to leave a table. If a player leaves a table when game
	 * status is PASSING_CARDS or STARTED then a bot takes its place. TODO ? If
	 * the creator leaves the table when game status is INIT then the table is
	 * destoyed.? If the creator leaves the table when game status is not INIT
	 * what happens? Can a bot leave the game?
	 * 
	 * 
	 * @param userName
	 * @throws NoSuchPlayerException
	 *             if player <code>userName</code> is not in the table
	 * @throws GameInterruptedException
	 *             if the game status is INTERRUPTED
	 * @throws GameEndedException 
	 * @throws IllegalArgumentException 
	 */
	void leaveTable(String userName) throws RemoteException,
			NoSuchPlayerException, GameInterruptedException, IllegalArgumentException,
			GameEndedException;

	/**
	 * The user <code>userName</code> passes cards <code>cards</code> to the
	 * player next to him. TODO The next player is the next element in
	 * {@link Positions} or the next player is chosen by the method {@link
	 * PasscardsPolicy.getNext(position)}?
	 * 
	 * @param userName
	 * @param cards
	 *            the cards passed
	 * @throws IllegalArgumentException
	 *             <ul>
	 *             <li>if some argument is <code>null</code></li>
	 *             <li>if <code>cards</code> length is not 3</li>
	 *             <li>if the user <code>userName</code> does not own the cards
	 *             he wants to pass</li>
	 *             <li>if the user <code>userName</code> does not exists</li>
	 *             </ul>
	 * @throws WrongGameStateException
	 *             if the card must not be passed in this state of the game
	 * @throws RemoteException
	 * @throws NoSuchPlayerException
	 * @throws GameInterruptedException
	 *             if the game status is INTERRUPTED
	 */
	void passCards(String userName, Card[] cards)
			throws IllegalArgumentException, 
			RemoteException, NoSuchPlayerException, GameInterruptedException, WrongGameStateException;

	/**
	 * Player <code>platerName</code> plays card <code>card</code>.
	 * 
	 * @throws IllegalArgumentException
	 *             if player does not own the card or card is null
	 * @param userName
	 * @param card
	 *            the card played
	 * @throws IllegalMoveException
	 *             if one of the following rules are not satisfied:
	 *             <ul>
	 *             <li>At first turn the first card played must be two of clubs</li>
	 *             <li>If current player is not the first of current turn he
	 *             must play a card of the same suit of the first card played in
	 *             this turn. If he does not have such a card then he can play
	 *             every card.</li>
	 *             <li>If <code>card</code> suit is heart then at least one of
	 *             the following rule must be satisfied:
	 *             <ul>
	 *             <li>someone played heart before</li>
	 *             <li>current player is not first in turn and he does not own
	 *             cards of the same suit as the first card played in current
	 *             turn</li>
	 *             <li>player owns only hearts card(this could happen only if
	 *             player is first in turn)</li>
	 *             </ul>
	 *             </ul>
	 * @throws IllegalArgumentException
	 *             <ul>
	 *             <li>if an argument is null</li>
	 *             <li>if <code>userName</code> is not playing in this table</li>
	 *             <li>if it's not this player turn</li>
	 *             <li>if this player does not own the card</li>
	 *             </ul>
	 * @throws NoSuchPlayerException
	 * @throws GameInterruptedException
	 *             if the game status is INTERRUPTED
	 * @throws WrongGameStateException 
	 */
	void playCard(String userName, Card card) throws IllegalMoveException,
			RemoteException, IllegalArgumentException, NoSuchPlayerException,
			GameInterruptedException, WrongGameStateException;

	/**
	 * Sends a message to the table chat
	 * 
	 * @param message
	 *            holds name of user and message sent by user
	 * @throws GameInterruptedException 
	 *             if the game status is INTERRUPTED.
	 *             TODO: Remove this in 2.0.
	 * @throws GameEndedException 
	 *             if the game status is ENDED.
	 *             TODO: Remove this in 2.0.
	 */
	void sendMessage(ChatMessage message) throws
			RemoteException, GameInterruptedException, GameEndedException;

	/**
	 * Add a viewer <code>userName</code>to this table. This can be called any
	 * time except when game status is {@link Positions}.ENDED or
	 * {@link Positions}.INTERRUPTED
	 * 
	 * @param userName
	 * @return
	 * @throws GameInterruptedException When the game status is  {@link Positions}.INTERRUPTED
	 * @throws WrongGameStateException When the game status is {@link Positions}.ENDED 
	 */
	public ObservedGameStatus viewTable(String userName,
			ServletNotificationsInterface snf) throws DuplicateViewerException,
			RemoteException,  WrongGameStateException, GameInterruptedException;

}
