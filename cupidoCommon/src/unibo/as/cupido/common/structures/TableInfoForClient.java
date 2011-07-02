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

import unibo.as.cupido.common.interfaces.GlobalTableManagerInterface;

/**
 * Contains informations about a table. This is used by
 * {@link GlobalTableManagerInterface#getTableList()}
 */
public class TableInfoForClient implements Serializable {
	private static final long serialVersionUID = -3002842357423083821L;
	/** name of table creator */
	public final String creator;
	/** number of free position in this table */
	public int freePosition;
	/** identifier of the table */
	public final TableDescriptor tableDescriptor;

	public TableInfoForClient() {
		creator = null;
		tableDescriptor = null;
	}

	public TableInfoForClient(String owner, int freePosition,
			TableDescriptor tableDescriptor) {
		if (owner == null || tableDescriptor == null || freePosition < 0
				|| freePosition > 4)
			throw new IllegalArgumentException();
		this.creator = owner;
		this.freePosition = freePosition;
		this.tableDescriptor = tableDescriptor;
	}

	/**
	 * A Table is uniquely identified by two things: the LTM it's managed by,
	 * the unique id the Table has within that LTM. This method is used by an
	 * hash map in LTM
	 */
	@Override
	public int hashCode() {
		return tableDescriptor.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return tableDescriptor.equals(((TableInfoForClient) o).tableDescriptor);
	}

	@Override
	public String toString() {
		return "[creator=" + creator + ", free position=" + freePosition
				+ ", server=" + tableDescriptor.ltmId + ", table id="
				+ tableDescriptor.id + "]";
	}
}
