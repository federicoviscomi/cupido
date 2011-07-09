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

package unibo.as.cupido.common.exception;

import java.io.Serializable;

/**
 * Used when:
 * <ul>
 * <li>trying to add a new user in the database but the database already
 * contains a user with same name</li>
 * <li>trying to add a new player in a table but the table already contains a
 * player with same name</li>
 * </ul>
 */
public class DuplicateUserNameException extends Exception implements
		Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a <code>DuplicateUserNameException</code> with the specified
	 * detail message.
	 * 
	 * @param string
	 *            the detail message.
	 */
	public DuplicateUserNameException(String userName) {
		super(userName);
	}

	/**
	 * Constructs a <code>DuplicateUserNameException</code> with no detail
	 * message.
	 */
	public DuplicateUserNameException() {
		//
	}
}
