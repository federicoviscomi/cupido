/**
 * 
 */
package unibo.as.cupido.client;

import java.util.Collection;

import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.ChatMessage;
import unibo.as.cupido.backendInterfaces.common.InitialTableStatus;
import unibo.as.cupido.backendInterfaces.common.ObservedGameStatus;
import unibo.as.cupido.backendInterfaces.common.TableInfoForClient;
import unibo.as.cupido.backendInterfaces.exception.DuplicateUserNameException;
import unibo.as.cupido.backendInterfaces.exception.FatalException;
import unibo.as.cupido.backendInterfaces.exception.FullTableException;
import unibo.as.cupido.backendInterfaces.exception.IllegalMoveException;
import unibo.as.cupido.backendInterfaces.exception.MaxNumTableReachedException;
import unibo.as.cupido.backendInterfaces.exception.NoSuchServerException;
import unibo.as.cupido.backendInterfaces.exception.NoSuchTableException;
import unibo.as.cupido.backendInterfaces.exception.NotCreatorException;
import unibo.as.cupido.backendInterfaces.exception.PlayerNotFoundException;
import unibo.as.cupido.backendInterfaces.exception.PositionFullException;
import unibo.as.cupido.backendInterfaces.exception.UserNotAuthenticatedException;

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
	 * @throws UserNotAuthenticatedException
	 * @throws FatalException
	 *             in case of internal serious error while joining the table,
	 *             probably future action will not be performed
	 */
	public InitialTableStatus joinTable(String server, int tableId)
			throws FullTableException, NoSuchTableException,
			DuplicateUserNameException, UserNotAuthenticatedException,
			FatalException;

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
	 * @throws UserNotAuthenticatedException
	 * @throws FatalException
	 */
	void sendLocalChatMessage(String message) throws IllegalArgumentException,
			UserNotAuthenticatedException, FatalException;

	/**
	 * The current player leaves the table
	 * 
	 * @throws UserNotAuthenticatedException
	 * @throws PlayerNotFoundException
	 *             if player is not playing or viewing a game
	 * @throws FatalException
	 */
	void leaveTable() throws UserNotAuthenticatedException,
			PlayerNotFoundException, FatalException;

	/**
	 * 
	 * @param card
	 *            Card to be played
	 * @throws IllegalMoveException
	 *             user can't play that card now or the player is not at the
	 *             table
	 * @throws IllegalArgumentException
	 *             if player does not own the card, or card==null
	 * @throws UserNotAuthenticatedException
	 * @throws FatalException
	 */
	void playCard(Card card) throws IllegalMoveException, FatalException,
			IllegalArgumentException, UserNotAuthenticatedException;

	/**
	 * 
	 * @param cards
	 *            cards.length must be 3
	 * @throws IllegalStateException
	 *             if the cards must not be passed now in the game
	 * @throws IllegalArgumentException
	 *             if cards is not a valid parameter or the user is not playing
	 * @throws UserNotAuthenticatedException
	 * @throws FatalException
	 */
	void passCards(Card[] cards) throws IllegalStateException,
			IllegalArgumentException, UserNotAuthenticatedException,
			FatalException;

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
	 *             if position value is not valid
	 * @throws UserNotAuthenticatedException
	 * @throws FatalException
	 */
	void addBot(int position) throws PositionFullException, FullTableException,
			NotCreatorException, IllegalArgumentException,
			UserNotAuthenticatedException, FatalException;

	/**
	 * 
	 * @return
	 * @throws UserNotAuthenticatedException
	 * @throws FatalException
	 */
	ChatMessage[] viewLastMessages() throws UserNotAuthenticatedException,
			FatalException;

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
	 * Destroy comet and http sessions
	 */
	public void destroySession();
}
