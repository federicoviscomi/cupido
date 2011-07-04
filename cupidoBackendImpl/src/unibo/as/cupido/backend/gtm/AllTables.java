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

package unibo.as.cupido.backend.gtm;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import unibo.as.cupido.common.exception.EmptyTableException;
import unibo.as.cupido.common.exception.FullTableException;
import unibo.as.cupido.common.exception.NoSuchTableException;
import unibo.as.cupido.common.interfaces.LocalTableManagerInterface;
import unibo.as.cupido.common.structures.TableDescriptor;
import unibo.as.cupido.common.structures.TableInfoForClient;

/**
 * Manages table information for a GTM.
 */
public class AllTables extends AbstractCollection<TableInfoForClient> implements
		Serializable {

	/** auto generated serial version uid */
	private static final long serialVersionUID = 3079835750202226475L;
	/** stores association between table descriptors and table infos */
	private transient final Map<TableDescriptor, TableInfoForClient> tifc = new HashMap<TableDescriptor, TableInfoForClient>();
	/** stores association between ltm names and ltm interfaces */
	private transient final Map<String, LocalTableManagerInterface> ltmMap = new HashMap<String, LocalTableManagerInterface>();

	/** This is the only serialized field of this class. */
	ArrayList<TableInfoForClient> getTableListReturnedObject = null;

	/**
	 * Adds a table.
	 * 
	 * @param table
	 *            the table to add.
	 * @param chosenLTM
	 *            the ltm who handles <tt>table</tt>
	 */
	public void addTable(TableInfoForClient table,
			LocalTableManagerInterface chosenLTM) {
		tifc.put(table.tableDescriptor, table);
		ltmMap.put(table.tableDescriptor.ltmId, chosenLTM);
	}

	/**
	 * Decrease the free position count of table identified by
	 * <tt>tableDescriptor</tt>. Decreasing free position count is done when a
	 * player or a bot joins the table before the game starts.
	 * 
	 * @param tableDescriptor
	 *            the identifier of the table.
	 * @throws FullTableException
	 *             if the table is full, i.e. there are four players.
	 * @throws NoSuchTableException
	 *             if there is no such table identified by
	 *             <tt>tableDescriptor</tt>
	 */
	public void decreaseFreePosition(TableDescriptor tableDescriptor)
			throws FullTableException, NoSuchTableException {
		TableInfoForClient tableInfoForClient = tifc.get(tableDescriptor);
		if (tableInfoForClient == null)
			throw new NoSuchTableException(tableDescriptor.toString());
		if (tableInfoForClient.freePosition == 0) {
			throw new FullTableException(tableDescriptor.toString());
		} else {
			tableInfoForClient.freePosition--;
		}
	}

	/**
	 * Gets the ltm interface identified by <tt>ltmId</tt>
	 * 
	 * @param ltmId
	 *            identifier of the ltm inferface to get
	 * @return the ltm interface identified by <tt>ltmId</tt>
	 */
	public LocalTableManagerInterface getLTMInterface(String ltmId) {
		return ltmMap.get(ltmId);
	}

	/**
	 * Increase the free position count of table identified by
	 * <tt>tableDescriptor</tt>. Increasing free position count is done when a
	 * player or a bot leaves the table before the game starts.
	 * 
	 * @param tableDescriptor
	 *            the identifier of the table
	 * @throws EmptyTableException
	 *             if the table is empty
	 * @throws NoSuchTableException
	 *             if there is no such table identified by
	 *             <tt>tableDescriptor</tt>
	 */
	public void increaseFreePosition(TableDescriptor tableDescriptor)
			throws EmptyTableException, NoSuchTableException {
		TableInfoForClient tableInfoForClient = tifc.get(tableDescriptor);
		if (tifc == null)
			throw new NoSuchTableException(tableDescriptor.toString());
		if (tableInfoForClient.freePosition == 3) {
			throw new EmptyTableException(tableDescriptor.toString());
		} else {
			tableInfoForClient.freePosition++;
		}
	}

	/**
	 * Removes table identified by <tt>tableDescriptor</tt>.
	 * 
	 * @param tableDescriptor
	 *            identifier of the table to be removed
	 * @throws NoSuchTableException
	 *             if there is no such table identified by
	 *             <tt>tableDescriptor</tt>
	 */
	public void removeTable(TableDescriptor tableDescriptor)
			throws NoSuchTableException {
		if (tifc.remove(tableDescriptor) == null)
			throw new NoSuchTableException(tableDescriptor.toString());
	}

	/**
	 * Save a portion of the state of this <tt>AllTables</tt> instance to a
	 * stream (that is, serialize it). Serialize only tables information needed
	 * by a client issuing a {@link GlobalTableManager#getTableList()}, i.e.
	 * {@link #getTableListReturnedObject}. This is done in attempt to send the
	 * least amount of data to the client.
	 * 
	 * @serialData The number of tables in this instance and every tables
	 *             information.
	 */
	private void writeObject(java.io.ObjectOutputStream s)
			throws java.io.IOException {
		System.out.println("writing tables");
		/* write out tables number */
		s.writeInt(tifc.size());

		/* write out all tables informations */
		for (TableInfoForClient tableInfo : tifc.values()) {
			s.writeObject(tableInfo);
		}
	}

	/**
	 * Reconstitute a part of the <tt>AllTables</tt> instance from a stream
	 * (that is, deserialize it). Specificly deserialization is done only for
	 * {@link #getTableListReturnedObject}.
	 */
	private void readObject(java.io.ObjectInputStream s)
			throws java.io.IOException, ClassNotFoundException {
		System.out.println("reading tables");
		int tableNumber = s.readInt();
		getTableListReturnedObject = new ArrayList<TableInfoForClient>(
				tableNumber);
		/* Read in all tables informations */
		for (int i = 0; i < tableNumber; i++) {
			getTableListReturnedObject.add((TableInfoForClient) s.readObject());
		}
	}

	@Override
	public Iterator<TableInfoForClient> iterator() {
		return getTableListReturnedObject.iterator();
	}

	@Override
	public int size() {
		return getTableListReturnedObject.size();
	}

}
