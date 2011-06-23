package unibo.as.cupido.backend.gtm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EmptyStackException;
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

	public void removeTable(TableDescriptor tableDescriptor) {
		tifc.remove(tableDescriptor);
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

}
