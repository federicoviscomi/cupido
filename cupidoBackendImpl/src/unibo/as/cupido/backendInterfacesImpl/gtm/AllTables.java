package unibo.as.cupido.backendInterfacesImpl.gtm;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import unibo.as.cupido.backendInterfaces.LocalTableManagerInterface;
import unibo.as.cupido.backendInterfaces.common.TableDescriptor;
import unibo.as.cupido.backendInterfaces.common.TableInfoForClient;

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

	public void decreaseFreePosition(TableDescriptor tableDescriptor) {
		tifc.get(tableDescriptor).freePosition--;
	}

	public Collection<TableInfoForClient> getAllTables() {
		return tifc.values();
	}

	public LocalTableManagerInterface getLTMInterface(String ltmId) {
		return ltmMap.get(ltmId);
	}

	public void removeTable(TableDescriptor tableDescriptor) {
		tifc.remove(tableDescriptor);
	}

}
