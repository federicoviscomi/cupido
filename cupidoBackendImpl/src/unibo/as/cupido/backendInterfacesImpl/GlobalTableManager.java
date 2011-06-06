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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import unibo.as.cupido.backendInterfaces.LocalTableManagerInterface;
import unibo.as.cupido.backendInterfaces.GlobalTableManagerInterface;
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
public class GlobalTableManager implements GlobalTableManagerInterface {

	public static void main(String[] args) {
		new GlobalTableManagerCommandInterpreterUI().execute();
	}

	/** Stores association between Table and local server RMI name */
	private Map<Table, String> tables;

	/**
	 * Stores a queue of local tables managers ordered by their last known
	 * workload
	 */
	private PriorityQueue<LocalTableManagerInterface> localTablesManagers;

	private Registry registry;

	public GlobalTableManager() {
		try {
			tables = new HashMap<GlobalTableManagerInterface.Table, String>();
			localTablesManagers = new PriorityQueue<LocalTableManagerInterface>(1000,
					new Comparator<LocalTableManagerInterface>() {
						public int compare(LocalTableManagerInterface a, LocalTableManagerInterface b) {
							int workLoadA;
							try {
								workLoadA = a.getWorkLoad();
							} catch (RemoteException e) {
								workLoadA = 100;
							}
							int workLoadB;
							try {
								workLoadB = b.getWorkLoad();
							} catch (RemoteException e) {
								workLoadB = 100;
							}
							return workLoadB - workLoadA;
						}
					});
			registry = LocateRegistry.getRegistry();
			registry.bind(GlobalTableManagerInterface.globalTableManagerName, UnicastRemoteObject.exportObject(this));
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
		try {
			/*
			 * choose a local table manager according to some load balancing
			 * policy
			 */
			LocalTableManagerInterface choosedTableManager = localTablesManagers.remove();

			/* create table in the choosTableed local table manager */
			Table table = choosedTableManager.createTable(owner, snf);

			tables.put(table, table.tableDescriptor.server);

			/* updates local table manager workload */
			localTablesManagers.add(choosedTableManager);

			return table.tableDescriptor;
		} catch (RemoteException e) {
			//
		}
		return null;
	}

	public LocalTableManagerInterface[] getAllLocalServer() {
		LocalTableManagerInterface[] allLocalServers = new LocalTableManagerInterface[localTablesManagers.size()];
		return localTablesManagers.toArray(allLocalServers);
	}

	public Set<Table> getTableList() throws RemoteException {
		return tables.keySet();
	}

	@Override
	public void notifyLocalTableManagerShutdown(String name) {
		// TODO Auto-generated method stub
		try {
			localTablesManagers.remove((LocalTableManagerInterface) registry.lookup(name));
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

	@Override
	public void notifyLocalTableManagerStartup(String localServerName) throws RemoteException {
		try {
			localTablesManagers.add((LocalTableManagerInterface) registry.lookup(localServerName));
			System.out.println(" local table manager added: " + localServerName);
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void notifyTableDestruction(TableDescriptor tableDescriptor) throws RemoteException {
		tables.remove(tableDescriptor);
	}

	@Override
	public void notifyTableJoin(TableDescriptor tableDescriptor) throws RemoteException, NoSuchTableException {
		if (tables.get(new Table(null, 0, tableDescriptor)) == null) {
			throw new NoSuchTableException();
		}
		for (Table table : tables.keySet()) {
			if (table.tableDescriptor.equals(tableDescriptor)) {
				table.freePosition--;
				return;
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

	@Override
	public void notifyLocalTableManagerStartup(LocalTableManagerInterface ltmi, int maxTable) throws RemoteException {
		// TODO Auto-generated method stub

	}
}
