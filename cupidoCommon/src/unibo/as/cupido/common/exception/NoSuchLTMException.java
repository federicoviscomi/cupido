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
 * Used to signal that specified LTM cannot be found
 */
public class NoSuchLTMException extends Exception implements Serializable {

	/**
	 * Constructs a <code>NoSuchLTMException</code> with the specified detail
	 * message.
	 * 
	 * @param string
	 *            the detail message.
	 */
	public NoSuchLTMException(String ltmId) {
		super(ltmId);
	}

	/**
	 * Constructs a <code>NoSuchLTMException</code> with no detail message.
	 */
	public NoSuchLTMException() {
		//
	}

	private static final long serialVersionUID = 1L;

}
