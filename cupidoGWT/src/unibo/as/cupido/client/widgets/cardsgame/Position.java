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

package unibo.as.cupido.client.widgets.cardsgame;

/**
 * This class models the position of a widget on the table.
 */
public class Position {

	/**
	 * The rotation is measured in degrees. When this is <code>0</code>, there is no
	 * rotation. The rotation is clockwise, so a widget with rotation <code>90</code> will
	 * have its top pointed towards the right edge of the table.
	 */
	public int rotation;

	/**
	 * The distance between the left margin and the center of the widget.
	 */
	public int x;

	/**
	 * The distance between the top margin and the center of the widget.
	 */
	public int y;

	/**
	 * The height of the widget.
	 * 
	 * Widgets with higher values of <code>z</code> are drawn above
	 * those with lower values.
	 */
	public int z;

	/**
	 * The default constructor.
	 */
	public Position() {
	}

	/**
	 * Initializes all the fields with the specified values.
	 * 
	 * @param x The desired distance between the left margin and the center of the widget.
	 * @param y The desired distance between the top margin and the center of the widget.
	 * @param z The desired height for the widget. Widgets with higher values of <code>z</code> are drawn
	 *          above those with lower values.
	 * @param rotation The desired rotation, measured in degrees.
	 */
	public Position(int x, int y, int z, int rotation) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.rotation = rotation;
	}
}