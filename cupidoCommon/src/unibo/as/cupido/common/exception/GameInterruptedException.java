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

import unibo.as.cupido.common.interfaces.TableInterface.GameStatus;

/**
 * Used to signal that some operation on a table cannot be executed after game
 * has been interrupted, i.e game status is {@link GameStatus#INTERRUPTED}.
 */
public class GameInterruptedException extends Exception implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a <code>GameInterruptedException</code> with no detail message.
	 */
	public GameInterruptedException() {
		//
	}

}
