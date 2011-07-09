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

/**
 * Used to signal that specified viewer is missing from a table
 */
public class NoSuchViewerException extends Exception {

	/**
	 * Constructs a <code>NoSuchViewerException</code> with the specified detail
	 * message.
	 * 
	 * @param string
	 *            the detail message.
	 */
	public NoSuchViewerException(String viewerName) {
		super(viewerName);
	}

	/**
	 * Constructs a <code>NoSuchViewerException</code> with no detail message.
	 */
	public NoSuchViewerException() {
		//
	}

	private static final long serialVersionUID = 1L;
}
