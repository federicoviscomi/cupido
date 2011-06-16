package unibo.as.cupido.backendInterfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;

import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.ChatMessage;
import unibo.as.cupido.backendInterfaces.common.InitialTableStatus;
import unibo.as.cupido.backendInterfaces.common.ObservedGameStatus;
import unibo.as.cupido.backendInterfaces.exception.DuplicateUserNameException;
import unibo.as.cupido.backendInterfaces.exception.FullTableException;
import unibo.as.cupido.backendInterfaces.exception.IllegalMoveException;
import unibo.as.cupido.backendInterfaces.exception.NoSuchTableException;
import unibo.as.cupido.backendInterfaces.exception.NoSuchUserException;
import unibo.as.cupido.backendInterfaces.exception.NotCreatorException;
import unibo.as.cupido.backendInterfaces.exception.PlayerNotFoundException;
import unibo.as.cupido.backendInterfaces.exception.PositionFullException;

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
	 */
	public static enum GameStatus {
		INIT, PASSING_CARDS, STARTED, ENDED
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
	 * @throws PositionFullException
	 *             if the position is occupied by another player
	 * @throws RemoteException
	 * @throws IllegalArgumentException
	 *             <ul>
	 *             <li>if <code>botName</code> is null</li>
	 *             <li>if position is out of range, i.e. not {@link Positions}
	 *             .LEFT, {@link Positions}.UP or {@link Positions}.RIGHT.</li>
	 *             </ul>
	 * 
	 * @throws FullTableException
	 *             if the table already has four player
	 * @throws NotCreatorException
	 *             if this method is called by a player who is not the creator
	 *             of the table. TODO e' necessaria questa eccezione?
	 * @throws IllegalStateException
	 *             if game status is ENDED
	 */
	void addBot(String userName, int position) throws PositionFullException,
			RemoteException, IllegalArgumentException, FullTableException,
			NotCreatorException, IllegalStateException;

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
	 * @throws NoSuchTableException
	 *             TODO what's the meaning of this?
	 * @throws IllegalStateException
	 *             if game status is not {@link GameStatus}.INIT
	 * @throws IllegalArgumentException
	 *             if an argument is null
	 * @throws DuplicateUserNameException
	 *             if a player name <code>userName</code> is already playing or
	 *             viewing the table
	 * @throws NoSuchUserException
	 *             if the database contains no player named
	 *             <code>userName</code>
	 * @throws SQLException
	 */
	public InitialTableStatus joinTable(String userName,
			ServletNotificationsInterface snf) throws FullTableException,
			NoSuchTableException, RemoteException, IllegalArgumentException,
			IllegalStateException, DuplicateUserNameException, SQLException,
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
	 * @throws PlayerNotFoundException
	 *             if player <code>userName</code> is not in the table
	 */
	void leaveTable(String userName) throws RemoteException,
			PlayerNotFoundException;

	/**
	 * The user <code>userName</code> passes cards <code>cards</code> to the
	 * player next to him. TODO The next player is the next element in
	 * {@link Positions} or the next player is chosen by the method {@link
	 * PasscardsPolicy.getNext(position)}?
	 * 
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
	 * @throws RemoteException
	 */
	void passCards(String userName, Card[] cards)
			throws IllegalArgumentException, RemoteException;

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
	 */
	void playCard(String userName, Card card) throws IllegalMoveException,
			RemoteException, IllegalArgumentException;

	/**
	 * Sends a message to the table chat
	 * 
	 * @param message
	 *            holds name of user and message sent by user
	 */
	void sendMessage(ChatMessage message) throws RemoteException;

	/**
	 * Add a viewer <code>userName</code>to this table. This can be called any
	 * time except when game status is {@link Positions}.ENDED
	 * 
	 * 
	 * @param userName
	 * @return
	 * @throws NoSuchTableException
	 */
	public ObservedGameStatus viewTable(String userName,
			ServletNotificationsInterface snf) throws NoSuchTableException,
			RemoteException;

}
