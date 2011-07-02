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

package unibo.as.cupido.common.structures;

import java.io.Serializable;

/**
 * Used to store generic pair of value
 * 
 * @param <T1>
 *            type of first component of this pair
 * @param <T2>
 *            type of second component of this pair
 */
public class Pair<T1, T2> implements Serializable {

	private static final long serialVersionUID = 1L;

	/** first component of this pair */
	public final T1 first;
	/** second component of this pair */
	public final T2 second;

	/**
	 * Create a new pair with null components
	 */
	public Pair() {
		this.first = null;
		this.second = null;
	}

	/**
	 * Create a new pair with specified components
	 * 
	 * @param first
	 *            first component of this pair
	 * @param second
	 *            second component of this pair
	 */
	public Pair(T1 first, T2 second) {
		this.first = first;
		this.second = second;
	}

	@Override
	public String toString() {
		return "[" + first + ", " + second + "]";
	}
}
