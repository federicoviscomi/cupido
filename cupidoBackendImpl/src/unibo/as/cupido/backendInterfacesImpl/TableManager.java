package unibo.as.cupido.backendInterfacesImpl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import unibo.as.cupido.backendInterfaces.LocalTableManagerInterface;
import unibo.as.cupido.backendInterfaces.TableManagerInterface;
import unibo.as.cupido.backendInterfaces.common.NoSuchTableException;

/**
 * 
 * TableManager has various tasks:
 * <ul>
 * <li>manage a set of LocalTableManager. This includes adding/removing a
 * LocalTableManages to the set and checking for consistency of the set</li>
 * <li>dispatch the Servlet request to the right LocalTableManager i.e. choose
 * an alive LocalTableManager and balance the work load between the
 * LocalTableManagers</li>
 * </ul>
 * 
 * @author cane
 * 
 */
public class TableManager implements TableManagerInterface {

	public static void main(String[] args) {
		new GlobalTableManagerCommandInterpreterUI().execute();
	}

	private Map<Table, LocalTableManagerInterface> tables;

	/**
	 * Stores a set of local tables managers and theyr last known workload
	 * 
	 */
	private Map<InetAddress, Double> localTablesManagers;

	private Registry registry;

	public TableManager() {
		try {
			tables = new HashMap<TableManagerInterface.Table, LocalTableManagerInterface>();
			localTablesManagers = new HashMap<InetAddress, Double>();
			registry = LocateRegistry.getRegistry();
			registry.bind(TableManagerInterface.globalTableManagerName, UnicastRemoteObject.exportObject(this));
			System.out
					.println("Global table manager server started correctly at address " + InetAddress.getLocalHost());
		} catch (RemoteException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (AlreadyBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public TableDescriptor createTable(String owner, ServletNotifcationsInterface snf) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<Table> getTableList() throws RemoteException {
		return tables.keySet();
	}

	@Override
	public void notifyLocalTableManagerSturtup(InetAddress localTableManager) throws RemoteException {
		localTablesManagers.put(localTableManager, new Double("0"));
	}

	@Override
	public void notifyTableDestruction(TableDescriptor tableDescriptor) throws RemoteException {
		tables.remove(tableDescriptor);
	}

	@Override
	public void notifyTableJoin(TableDescriptor tableDescriptor) throws RemoteException {

		LocalTableManagerInterface localTableManagerInterface = tables.get(new Table(null, 0, tableDescriptor));
		if (localTableManagerInterface == null) {
			throw new NoSuchTableException();
		}
		for (Table table : tables.keySet()) {
			if (table.tableDescriptor.equals(tableDescriptor)) {
				table.freePosition--;
			}
		}
	}

	public void shutDown() {
		try {
			registry.unbind(globalTableManagerName);
		} catch (AccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Set<InetAddress> getAllLocalServer() {
		// TODO Auto-generated method stub
		return localTablesManagers.keySet();
	}
}
