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
 * An istance of this identifies a table in Cupido.
 */
public class TableDescriptor implements Serializable {
	private static final long serialVersionUID = 5666914154950723508L;

	/** unique identifier of this table in his ltm */
	public int id;

	/** ltm who manages this table */
	public String ltmId;

	/**
	 * GWT needs this constructor.
	 */
	public TableDescriptor() {
		//
	}

	/**
	 * Create a new <code>TableDescriptor</code> with specified arguments
	 * 
	 * @param ltmId
	 *            identifier of ltm who manages this table
	 * @param id
	 *            identifier of this table in his ltm
	 */
	public TableDescriptor(String ltmId, int id) {
		this.ltmId = ltmId;
		this.id = id;
	}

	@Override
	public boolean equals(Object o) {
		TableDescriptor otd = (TableDescriptor) o;
		return (otd.id == this.id && otd.ltmId.equals(this.ltmId));
	}

	@Override
	public int hashCode() {
		return (id + ltmId).hashCode();
	}
}
