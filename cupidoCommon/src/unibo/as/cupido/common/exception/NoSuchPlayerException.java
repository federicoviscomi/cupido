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
 * Used to signal that specified player is missing in a table
 */
public class NoSuchPlayerException extends Exception implements Serializable {

	private static final long serialVersionUID = 1L;

	public NoSuchPlayerException(String playerName) {
		super("no such player named: " + playerName);
	}

	public NoSuchPlayerException(int position) {
		super("there is no player in position: " + position);
	}

	public NoSuchPlayerException() {
		//
	}
}
