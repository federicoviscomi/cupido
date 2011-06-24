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
import unibo.as.cupido.common.exception.FullTableException;
import unibo.as.cupido.common.exception.IllegalMoveException;
import unibo.as.cupido.common.exception.MaxNumTableReachedException;
import unibo.as.cupido.common.exception.NoSuchServerException;
import unibo.as.cupido.common.exception.NoSuchTableException;
import unibo.as.cupido.common.exception.NotCreatorException;
import unibo.as.cupido.common.exception.PositionFullException;
import unibo.as.cupido.common.exception.UserNotAuthenticatedException;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.ChatMessage;
import unibo.as.cupido.common.structures.InitialTableStatus;
import unibo.as.cupido.common.structures.ObservedGameStatus;
import unibo.as.cupido.common.structures.RankingEntry;
import unibo.as.cupido.common.structures.TableInfoForClient;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("cupido")
public interface CupidoInterface extends RemoteService {

	/**
	 * This method must be called by the client before it can open the
	 * connection itself and start receiving Comet notifications.
	 */
	public void openCometConnection();

	public boolean login(String username, String password)
			throws FatalException;

	/**
	 * 
	 * @param username
	 * 
	 * @param password
	 * @throws DuplicateUserNameException
	 *             If this username was already used.
	 * @throws FatalException
	 */
	public void registerUser(String username, String password)
			throws DuplicateUserNameException, FatalException;

	/**
	 * 
	 * @param username
	 * @return
	 * @throws IllegalArgumentException
	 *             If the parameter is null.
	 * @throws FatalException
	 */
	public boolean isUserRegistered(String username)
			throws IllegalArgumentException, FatalException;

	public void logout();

	/**
	 * 
	 * @return
	 * @throws UserNotAuthenticatedException
	 * @throws FatalException
	 */
	public Collection<TableInfoForClient> getTableList()
			throws UserNotAuthenticatedException, FatalException;

	/**
	 * 
	 * @return InitialTableStatus with InitialTableStatus.playerPoints[0] is the
	 *         only meaningful attributes in this object all other attributes of
	 *         InitialTableStatus are meaningless and must be ignored
	 * @throws MaxNumTableReachedException
	 *             if a table can't be created now (you can try again later)
	 * @throws UserNotAuthenticatedException
	 * @throws FatalException
	 */
	public InitialTableStatus createTable() throws MaxNumTableReachedException,
			UserNotAuthenticatedException, FatalException;

	/**
	 * join a match not yet started
	 * 
	 * @param server
	 * @param tableId
	 * @return InitialTableStatus state of the match not yet started
	 * @throws FullTableException
	 *             if game already have 4 players
	 * @throws NoSuchTableException
	 *             if table is no more available, but user can join other table
	 * @throws DuplicateUserNameException
	 *             if player is already playing or viewing the selected table
	 * @throws NoSuchServerException
	 *             if server does not exist
	 * @throws UserNotAuthenticatedException
	 * @throws FatalException
	 *             in case of internal serious error while joining the table,
	 *             probably future action will not be performed
	 */
	public InitialTableStatus joinTable(String server, int tableId)
			throws FullTableException, NoSuchTableException,
			DuplicateUserNameException, NoSuchServerException,
			UserNotAuthenticatedException, FatalException;

	/**
	 * 
	 * @param server
	 * @param tableId
	 * @return
	 * @throws NoSuchTableException
	 *             if tableId is invalid or table no longer exists
	 * @throws NoSuchServerException
	 *             if server is invalid
	 * @throws UserNotAuthenticatedException
	 * @throws FatalException
	 */
	public ObservedGameStatus viewTable(String server, int tableId)
			throws NoSuchTableException, NoSuchServerException,
			UserNotAuthenticatedException, FatalException;

	/**
	 * Sends a message to the table chat
	 * 
	 * @param message
	 * @throws IllegalArgumentException
	 *             if message has bad format
	 * @throws NoSuchTableException
	 *             if player is not playing or viewing a game
	 * @throws UserNotAuthenticatedException
	 * @throws FatalException
	 */
	void sendLocalChatMessage(String message) throws IllegalArgumentException,
			NoSuchTableException, UserNotAuthenticatedException, FatalException;

	/**
	 * The current player leaves the table
	 * 
	 * @throws UserNotAuthenticatedException
	 * @throws NoSuchTableException
	 *             if player is not playing or viewing a game
	 * @throws FatalException
	 */
	void leaveTable() throws UserNotAuthenticatedException,
			NoSuchTableException, FatalException;

	/**
	 * 
	 * @param card
	 *            Card to be played
	 * @throws IllegalMoveException
	 *             user can't play that card now
	 * @throws NoSuchTableException
	 *             if user is not at the table
	 * @throws IllegalArgumentException
	 *             if player does not own the card, or card==null
	 * @throws UserNotAuthenticatedException
	 * @throws FatalException
	 */
	void playCard(Card card) throws IllegalMoveException, FatalException,
			NoSuchTableException, IllegalArgumentException,
			UserNotAuthenticatedException;

	/**
	 * 
	 * @param cards
	 *            cards.length must be 3
	 * @throws IllegalStateException
	 *             if the cards must not be passed now in the game
	 * @throws IllegalArgumentException
	 *             if cards is not a valid parameter or the player does not own
	 *             those cards
	 * @throws NoSuchTableException
	 *             if user is not at the table
	 * @throws UserNotAuthenticatedException
	 * @throws FatalException
	 */
	void passCards(Card[] cards) throws IllegalStateException,
			IllegalArgumentException, NoSuchTableException,
			UserNotAuthenticatedException, FatalException;

	/**
	 * Add an automatic playing machine to the table
	 * 
	 * @param position
	 *            valid range is [1-3]
	 * @throws PositionFullException
	 *             if at the table in position (position) there is a human
	 *             player
	 * @throws FullTableException
	 *             if the table already has four player
	 * @throws NotCreatorException
	 *             if the user is not the table creator
	 * @throws IllegalArgumentException
	 *             if position value is out of valid range
	 * @throws NoSuchTableException
	 *             if user is not at the table
	 * @throws UserNotAuthenticatedException
	 * @throws FatalException
	 * @return The name of the bot.
	 */
	public String addBot(int position) throws PositionFullException,
			FullTableException, NotCreatorException, IllegalArgumentException,
			NoSuchTableException, UserNotAuthenticatedException, FatalException;

	/**
	 * 
	 * @return
	 * @throws UserNotAuthenticatedException
	 * @throws FatalException
	 */
	public ChatMessage[] viewLastMessages()
			throws UserNotAuthenticatedException, FatalException;

	/**
	 * Sends a message to the table chat
	 * 
	 * @param message
	 * @throws IllegalArgumentException
	 *             if message has bad format
	 * @throws UserNotAuthenticatedException
	 * @throws FatalException
	 */
	void sendGlobalChatMessage(String message) throws IllegalArgumentException,
			UserNotAuthenticatedException, FatalException;

	/**
	 * 
	 * @return player name, rank and points
	 * @throws UserNotAuthenticatedException
	 * @throws FatalException
	 */
	public RankingEntry getMyRank() throws UserNotAuthenticatedException,
			FatalException;

	/**
	 * 
	 * @return RankingEntry of 10 top players in global ranking list
	 * @throws UserNotAuthenticatedException
	 * @throws FatalException
	 */
	public ArrayList<RankingEntry> getTopRank()
			throws UserNotAuthenticatedException, FatalException;

	/**
	 * 
	 * @return one chunk the global rank list which contains from four position
	 *         before user to five position after the user.
	 * @throws UserNotAuthenticatedException
	 * @throws FatalException
	 *             ;
	 */
	public ArrayList<RankingEntry> getLocalRank()
			throws UserNotAuthenticatedException, FatalException;

	/**
	 * Destroy comet and http sessions
	 */
	public void destroySession();
}
