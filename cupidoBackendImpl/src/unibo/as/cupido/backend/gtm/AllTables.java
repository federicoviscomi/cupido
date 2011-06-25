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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import unibo.as.cupido.common.exception.EmptyTableException;
import unibo.as.cupido.common.exception.FullTableException;
import unibo.as.cupido.common.interfaces.LocalTableManagerInterface;
import unibo.as.cupido.common.structures.TableDescriptor;
import unibo.as.cupido.common.structures.TableInfoForClient;

public class AllTables {

	Map<TableDescriptor, TableInfoForClient> tifc = new HashMap<TableDescriptor, TableInfoForClient>();
	Map<String, LocalTableManagerInterface> ltmMap = new HashMap<String, LocalTableManagerInterface>();

	public AllTables() {
		//
	}

	public void addTable(TableInfoForClient table,
			LocalTableManagerInterface chosenLTM) {
		tifc.put(table.tableDescriptor, table);
		ltmMap.put(table.tableDescriptor.ltmId, chosenLTM);
	}

	public void decreaseFreePosition(TableDescriptor tableDescriptor)
			throws FullTableException {
		TableInfoForClient tableInfoForClient = tifc.get(tableDescriptor);
		if (tableInfoForClient.freePosition == 0) {
			throw new FullTableException(tableDescriptor.toString());
		} else {
			tableInfoForClient.freePosition--;
		}
	}

	public Collection<TableInfoForClient> getAllTables() {
		ArrayList<TableInfoForClient> values = new ArrayList<TableInfoForClient>();
		values.addAll(tifc.values());
		return values;
		// return tifc.values();
	}

	public LocalTableManagerInterface getLTMInterface(String ltmId) {
		return ltmMap.get(ltmId);
	}

	public void increaseFreePosition(TableDescriptor tableDescriptor)
			throws EmptyTableException {
		TableInfoForClient tableInfoForClient = tifc.get(tableDescriptor);
		if (tableInfoForClient.freePosition == 3) {
			throw new EmptyTableException(tableDescriptor.toString());
		} else {
			tableInfoForClient.freePosition++;
		}
	}

	public void removeTable(TableDescriptor tableDescriptor) {
		tifc.remove(tableDescriptor);
	}

}
