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

/**
 * 
 */
package unibo.as.cupido.client;

import java.util.ArrayList;
import java.util.Collection;

import unibo.as.cupido.common.exception.DuplicateUserNameException;
import unibo.as.cupido.common.exception.FatalException;
import unibo.as.cupido.common.exception.FullPositionException;
import unibo.as.cupido.common.exception.FullTableException;
import unibo.as.cupido.common.exception.GameEndedException;
import unibo.as.cupido.common.exception.GameInterruptedException;
import unibo.as.cupido.common.exception.IllegalMoveException;
import unibo.as.cupido.common.exception.MaxNumTableReachedException;
import unibo.as.cupido.common.exception.NoSuchServerException;
import unibo.as.cupido.common.exception.NoSuchTableException;
import unibo.as.cupido.common.exception.NotCreatorException;
import unibo.as.cupido.common.exception.UserNotAuthenticatedException;
import unibo.as.cupido.common.exception.WrongGameStateException;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.ChatMessage;
import unibo.as.cupido.common.structures.InitialTableStatus;
import unibo.as.cupido.common.structures.ObservedGameStatus;
import unibo.as.cupido.common.structures.RankingEntry;
import unibo.as.cupido.common.structures.TableInfoForClient;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * This interface contains the methods that servlets expose to their clients,
 * through GWT-specific RPC.
 */
@RemoteServiceRelativePath("cupido")
public interface CupidoInterface extends RemoteService {

	/**
	 * Add an automatic playing machine to the table.
	 * 
	 * @param position
	 *            The valid range is [1-3].
	 * @throws FullPositionException
	 *             If the specified position is already occupied by either a
	 *             player or a bot.
	 * @throws NotCreatorException
	 *             If the user is not the table creator.
	 * @throws IllegalArgumentException
	 *             If the specified position is out of range.
	 * @throws NoSuchTableException
	 *             If the user is not at a table.
	 * @throws UserNotAuthenticatedException
	 * @throws FatalException
	 * @return The name of the bot.
	 * @throws GameInterruptedException
	 */
	public String addBot(int position) throws FullPositionException,
			NotCreatorException, IllegalArgumentException,
			NoSuchTableException, UserNotAuthenticatedException,
			FatalException, GameInterruptedException;

	/**
	 * Creates a new table.
	 * 
	 * @return InitialTableStatus.playerPoints[0] is the only meaningful field
	 *         in the return value, other values aren't because this table has
	 *         no players or bots yet.
	 * @throws MaxNumTableReachedException
	 *             If a table can't be created right now, because the maximum
	 *             number of tables has been reached.
	 * @throws UserNotAuthenticatedException
	 * @throws FatalException
	 *             When a fatal error occurs, or if the user is already viewing
	 *             or playing at a table.
	 */
	public InitialTableStatus createTable() throws MaxNumTableReachedException,
			UserNotAuthenticatedException, FatalException;

	/**
	 * Destroys the comet and http sessions.
	 */
	public void destroySession();

	/**
	 * @return A chunk of the global ranking list which contains the entries
	 *         starting three positions before the user's entry and ending three
	 *         positions after that.
	 * @throws UserNotAuthenticatedException
	 * @throws FatalException
	 */
	public ArrayList<RankingEntry> getLocalRank()
			throws UserNotAuthenticatedException, FatalException;

	/**
	 * @return The player's name, rank and points.
	 * @throws UserNotAuthenticatedException
	 * @throws FatalException
	 */
	public RankingEntry getMyRank() throws UserNotAuthenticatedException,
			FatalException;

	/**
	 * @return The list of tables that can be joined and/or viewed by users.
	 * @throws UserNotAuthenticatedException
	 * @throws FatalException
	 */
	public Collection<TableInfoForClient> getTableList()
			throws UserNotAuthenticatedException, FatalException;

	/**
	 * @return RankingEntry of the top 10 players in the global ranking.
	 * @throws UserNotAuthenticatedException
	 * @throws FatalException
	 */
	public ArrayList<RankingEntry> getTopRank()
			throws UserNotAuthenticatedException, FatalException;

	/**
	 * Checks if the specified username is already registered or not.
	 * 
	 * @return true if the username is already used. false otherwise.
	 * @throws IllegalArgumentException
	 *             If the parameter is null.
	 * @throws FatalException
	 */
	public boolean isUserRegistered(String username)
			throws IllegalArgumentException, FatalException;

	/**
	 * Joins the table identified by server and tableId.
	 * 
	 * @return InitialTableStatus state of the match not yet started
	 * @throws FullTableException
	 *             If the specified table has no free seats.
	 * @throws NoSuchTableException
	 *             If the parameters don't specify an existing table. This can
	 *             happen when the user joins a table that has been destroyed in
	 *             the meantime.
	 * @throws DuplicateUserNameException
	 *             If the current user is already playing or viewing the
	 *             specified table.
	 * @throws NoSuchServerException
	 *             If the specified server does not exist.
	 * @throws UserNotAuthenticatedException
	 * @throws FatalException
	 *             When a serious internal error occurs.
	 * @throws GameInterruptedException
	 */
	public InitialTableStatus joinTable(String server, int tableId)
			throws FullTableException, NoSuchTableException,
			DuplicateUserNameException, NoSuchServerException,
			UserNotAuthenticatedException, FatalException,
			GameInterruptedException;

	/**
	 * The current user leaves the table.
	 * 
	 * @throws UserNotAuthenticatedException
	 * @throws NoSuchTableException
	 *             If the current user is neither playing nor viewing a game.
	 * @throws FatalException
	 * @throws GameEndedException
	 * @throws GameInterruptedException
	 * @throws IllegalArgumentException
	 */
	public void leaveTable() throws UserNotAuthenticatedException,
			NoSuchTableException, FatalException, IllegalArgumentException,
			GameInterruptedException, GameEndedException;

	/**
	 * Attempts to login, with the specified username and password.
	 * 
	 * @return This returns true if the login is successful, false otherwise.
	 */
	public boolean login(String username, String password)
			throws FatalException;

	/**
	 * Logs out the current user.
	 */
	public void logout();

	/**
	 * This method must be called by the client before it can open the
	 * connection itself and start receiving Comet notifications.
	 */
	public void openCometConnection();

	/**
	 * 
	 * @param cards
	 *            cards.length must be 3
	 * @throws IllegalStateException
	 *             If the user was not expected to pass cards now.
	 * @throws IllegalArgumentException
	 *             If the specified parameter is not valid or the player does
	 *             not own those cards.
	 * @throws NoSuchTableException
	 *             If the current user is not at a table.
	 * @throws UserNotAuthenticatedException
	 * @throws FatalException
	 * @throws WrongGameStateException
	 * @throws GameInterruptedException
	 */
	public void passCards(Card[] cards) throws IllegalStateException,
			IllegalArgumentException, NoSuchTableException,
			UserNotAuthenticatedException, FatalException,
			GameInterruptedException, WrongGameStateException;

	/**
	 * The current user plays the specified card.
	 * 
	 * @param card
	 *            The card that has to be played.
	 * @throws IllegalMoveException
	 *             If the user can't play that card now.
	 * @throws NoSuchTableException
	 *             If user is not at a table.
	 * @throws IllegalArgumentException
	 *             If the current user does not own the specified card, or if
	 *             card==null.
	 * @throws UserNotAuthenticatedException
	 * @throws FatalException
	 * @throws WrongGameStateException
	 * @throws GameInterruptedException
	 */
	public void playCard(Card card) throws IllegalMoveException,
			FatalException, NoSuchTableException, IllegalArgumentException,
			UserNotAuthenticatedException, GameInterruptedException,
			WrongGameStateException;

	/**
	 * Adds a new user with the specified username and password.
	 * 
	 * @throws DuplicateUserNameException
	 *             If this username was already used.
	 * @throws FatalException
	 */
	public void registerUser(String username, String password)
			throws DuplicateUserNameException, FatalException;

	/**
	 * Sends a message to the global chat.
	 * 
	 * @param message
	 *            The message sent by the user.
	 * @throws IllegalArgumentException
	 *             If the specified message is invalid. TODO: explain what this
	 *             means.
	 * @throws UserNotAuthenticatedException
	 * @throws FatalException
	 */
	public void sendGlobalChatMessage(String message)
			throws IllegalArgumentException, UserNotAuthenticatedException,
			FatalException;

	/**
	 * Sends a message to the table chat.
	 * 
	 * @param message
	 *            The message sent by the user.
	 * @throws IllegalArgumentException
	 *             if message has bad format TODO: explain what this means.
	 * @throws NoSuchTableException
	 *             If the current user is neither playing nor viewing a game.
	 * @throws UserNotAuthenticatedException
	 * @throws FatalException
	 * @throws GameEndedException
	 * @throws GameInterruptedException
	 */
	public void sendLocalChatMessage(String message)
			throws IllegalArgumentException, NoSuchTableException,
			UserNotAuthenticatedException, FatalException,
			GameInterruptedException, GameEndedException;

	/**
	 * Gets the last messages posted to the global chat.
	 * 
	 * @throws UserNotAuthenticatedException
	 * @throws FatalException
	 */
	public ChatMessage[] viewLastMessages()
			throws UserNotAuthenticatedException, FatalException;

	/**
	 * Joins (as a viewer) the table identified by server and tableId.
	 * 
	 * The actual game may or may not already be started.
	 * 
	 * @return The current status of the game.
	 * @throws NoSuchTableException
	 *             If tableId is invalid or the specified table no longer
	 *             exists.
	 * @throws NoSuchServerException
	 *             If the specified server does not exist.
	 * @throws UserNotAuthenticatedException
	 * @throws FatalException
	 *             When a fatal error occurs, or if the user is already viewing
	 *             or playing at a table.
	 * @throws GameInterruptedException
	 * @throws WrongGameStateException
	 */
	public ObservedGameStatus viewTable(String server, int tableId)
			throws NoSuchTableException, NoSuchServerException,
			UserNotAuthenticatedException, FatalException,
			WrongGameStateException, GameInterruptedException;
}
