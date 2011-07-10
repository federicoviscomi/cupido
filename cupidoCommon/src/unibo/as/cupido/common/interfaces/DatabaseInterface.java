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

import java.sql.SQLException;
import java.util.ArrayList;

import unibo.as.cupido.common.structures.RankingEntry;
import unibo.as.cupido.common.exception.DuplicateUserNameException;
import unibo.as.cupido.common.exception.NoSuchUserException;

/**
 * Cupido database has only the following table:
 * 
 * <code>
	CREATE TABLE `cupido`.`User` (
  		`name` VARCHAR(16)  NOT NULL,
  		`password` CHAR(8) UNICODE NOT NULL,
  		`score` INTEGER NOT NULL DEFAULT 0,
  		PRIMARY KEY (`name`),
  		INDEX `scoreIndex`(`score`, `name`)
	)
	ENGINE = MyISAM;</code>
 */
public interface DatabaseInterface {

	/** default database name */
	public static final String database = "cupido";
	/** default database address */
	public static final String DEFAULT_DATABASE_ADDRESS = "localhost";

	/**
	 * Add a new user with name <code>userName</code>, password
	 * <code>password</code> and score zero.
	 * 
	 * @param userName
	 *            must be long 1 to 8 character
	 * @param password
	 *            must be long 3 to 8 character
	 * @throws DuplicateUserNameException
	 *             if a user named <code>userName</code> already exists in the
	 *             database
	 * @throws IllegalArgumentException
	 *             if:
	 *             <ul>
	 *             <li>any of the arguments are <code>null</code></li>
	 *             <li>any of the arguments contain a character which is not a
	 *             letter(upper or lower case) or a number or the underscore
	 *             character</li>
	 *             <li><tt>password</tt> is not from three to eigth characters
	 *             long</li>
	 *             <li><tt>username</tt> is not from one to sixteen characters
	 *             long</li>
	 *             </ul>
	 */
	public void addNewUser(String userName, String password)
			throws SQLException, DuplicateUserNameException,
			IllegalArgumentException;

	/**
	 * Check if the user can be logged in with the provided {@link password}.
	 * 
	 * @param userName
	 * @param password
	 * @return <code>true</code> if <code>userName</code> is in the database and
	 *         his password is <code>password</code>; otherwise return false.
	 * @throws SQLException
	 *             in case of communication problem with database
	 * @throws IllegalArgumentException
	 *             if argument is <code>null</code>
	 * @throws NoSuchUserException
	 *             if <code>userName</code> is not in database
	 */
	public boolean login(String userName, String password) throws SQLException,
			IllegalArgumentException, NoSuchUserException;

	/**
	 * Return true if userName is in the database
	 * 
	 * @param userName
	 * @return
	 * @throws SQLException
	 */
	public boolean contains(String userName) throws SQLException;

	/**
	 * Update score of user <code>userName</code>
	 * 
	 * @param userName
	 * @param score
	 *            the new score of user <code>userName</code>
	 * @throws SQLException
	 * @throws IllegalArgumentException
	 *             if argument is <code>null</code>
	 * @throws NoSuchUserException
	 *             if <code>userName</code> is not in database
	 */
	public void updateScore(String userName, int score) throws SQLException,
			IllegalArgumentException, NoSuchUserException;

	/**
	 * Retreive at most the first top <code>size</code> positions in the global
	 * rank.
	 * 
	 * @param
	 * @return
	 * @throws SQLException
	 * @throws IllegalArgumentException
	 *             if <code>size</code> is not positive
	 */
	public ArrayList<RankingEntry> getTopRank(int size) throws SQLException,
			IllegalArgumentException;

	/** Number of entries returned from {@link DatabseInterface#getLocalRank()}. */
	public final int LOCAL_RANK_ENTRIES_NUM = 7;

	/**
	 * Returns {@link LOCAL_RANK_ENTRIES_NUM} from the global rank list
	 * containing in the middle <code>userName</code>.
	 * 
	 * @param userName
	 *            the user who wants the rank
	 * @return a list of size {@link LOCAL_RANK_ENTRIES_NUM} with user
	 *         {@link userName} in the middle.
	 * @throws SQLException
	 *             in case of database error
	 * @throws IllegalArgumentException
	 *             if argument is <code>null</code>
	 * @throws NoSuchUserException
	 *             if <code>userName</code> is not in database
	 */
	public ArrayList<RankingEntry> getLocalRank(String userName)
			throws SQLException, IllegalArgumentException, NoSuchUserException;

	/**
	 * Get player position in the global rank.
	 * 
	 * @param userName
	 * @return rank of {@link userName}.
	 * @throws SQLException
	 * @throws IllegalArgumentException
	 *             if argument is <code>null</code>
	 * @throws NoSuchUserException
	 *             if <code>userName</code> is not in database
	 */
	public RankingEntry getUserRank(String userName) throws SQLException,
			IllegalArgumentException, NoSuchUserException;

	/**
	 * Get the player total score.
	 * 
	 * @param userName
	 * @return player score.
	 * @throws SQLException
	 * @throws IllegalArgumentException
	 *             if argument is <code>null</code>
	 * @throws NoSuchUserException
	 *             if <code>userName</code> is not in database
	 */
	public int getPlayerScore(String userName) throws SQLException,
			IllegalArgumentException, NoSuchUserException;
}
