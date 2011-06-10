package unibo.as.cupido.backendInterfacesImpl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import unibo.as.cupido.backendInterfaces.LocalTableManagerInterface;
import unibo.as.cupido.backendInterfaces.GlobalTableManagerInterface.Table;
import unibo.as.cupido.backendInterfaces.GlobalTableManagerInterface.TableDescriptor;
import unibo.as.cupido.backendInterfaces.common.Pair;

public class AllTables {
	Map<TableDescriptor, Pair<Table, LocalTableManagerInterface>> allTables;

	public AllTables() {
		allTables = new HashMap<TableDescriptor, Pair<Table, LocalTableManagerInterface>>();
	}

	public void addTable(Table table, LocalTableManagerInterface chosenLTM) {
		allTables.put(table.tableDescriptor, new Pair(table, chosenLTM));
	}

	public void decreaseFreePosition(TableDescriptor tableDescriptor) {
		allTables.get(tableDescriptor).first.freePosition--;
	}

	public Collection<Pair<Table, LocalTableManagerInterface>> getAllTables() {
		return allTables.values();
	}

	public void removeTable(TableDescriptor tableDescriptor) {
		allTables.remove(tableDescriptor);
	}
}
