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

import unibo.as.cupido.backendInterfaces.GlobalTableManagerInterface;
import unibo.as.cupido.backendInterfaces.LocalTableManagerInterface;
import unibo.as.cupido.backendInterfaces.TableInterface;
import unibo.as.cupido.backendInterfaces.common.AllLTMBusyException;
import unibo.as.cupido.backendInterfaces.common.NoSuchTableException;
import unibo.as.cupido.backendInterfaces.common.Pair;
import unibo.as.cupido.backendInterfacesImpl.LTMSwarm.Triple;

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
 * 
 * @author cane
 * 
 */
public class GlobalTableManager implements GlobalTableManagerInterface {

	public static void main(String[] args) {
		new GlobalTableManagerCommandInterpreterUI().execute();
	}

	private AllTables allTables;

	private LTMSwarm ltmSwarm;

	private Registry registry;

	public GlobalTableManager() throws RemoteException {
		try {
			allTables = new AllTables();
			ltmSwarm = new LTMSwarm();
			System.setSecurityManager(new SecurityManager());
			// registry = LocateRegistry.createRegistry(1099);
			registry = LocateRegistry.getRegistry();
			registry.bind(GlobalTableManagerInterface.globalTableManagerName, UnicastRemoteObject.exportObject(this));
			System.out.println("Global table manager server started correctly at address " + InetAddress.getLocalHost()
					+ "\n Current thread is " + Thread.currentThread());
		} catch (RemoteException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AlreadyBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public TableDescriptor createTable(String owner, ServletNotifcationsInterface snf) throws RemoteException,
			AllLTMBusyException {
		try {
			/* chose an LTM according to some load balancing policy */
			LocalTableManagerInterface chosenLTM = ltmSwarm.chooseLTM();

			/* create table in the chosen local table manager */
			TableInterface tableInterface = chosenLTM.createTable(owner, snf);
			Table table = new Table(owner, 3, new TableDescriptor(tableInterface.toString(), 0));

			/* store created table */
			allTables.addTable(table, chosenLTM);
			System.out.println("Current thread is " + Thread.currentThread());
			return table.tableDescriptor;
		} catch (RemoteException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return null;
	}

	public Triple[] getAllLTM() {
		System.out.println("Current thread is " + Thread.currentThread());
		return ltmSwarm.getAllLTM();
	}

	public Collection<Pair<Table, LocalTableManagerInterface>> getTableList() throws RemoteException {
		System.out.println("Current thread is " + Thread.currentThread());
		return allTables.getAllTables();
	}

	@Override
	public void notifyLocalTableManagerShutdown(LocalTableManagerInterface ltm) {
		System.out.println("Current thread is " + Thread.currentThread());
		ltmSwarm.remove(ltm);
	}

	@Override
	public void notifyLocalTableManagerStartup(LocalTableManagerInterface ltmi, int maxTable) throws RemoteException {
		System.out.println("Current thread is " + Thread.currentThread());
		ltmSwarm.addLTM(ltmi, maxTable);
	}

	@Override
	public void notifyTableDestruction(TableDescriptor tableDescriptor, LocalTableManagerInterface ltm)
			throws RemoteException {
		System.out.println("Current thread is " + Thread.currentThread());
		allTables.removeTable(tableDescriptor);
		ltmSwarm.decreaseTableCount(ltm);
	}

	@Override
	public void notifyTableJoin(TableDescriptor tableDescriptor) throws RemoteException, NoSuchTableException {
		System.out.println("Current thread is " + Thread.currentThread());
		allTables.decreaseFreePosition(tableDescriptor);
	}

	@Override
	public char[] ping() throws RemoteException {
		try {
			System.out.println("Current thread is " + Thread.currentThread());
			Thread.sleep(500);
			return "pong".toCharArray();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public void shutDown() {
		try {
			System.out.println("Current thread is " + Thread.currentThread());
			registry.unbind(globalTableManagerName);
			for (LocalTableManagerInterface ltmi : ltmSwarm) {
				ltmi.notifyGTMShutDown();
			}
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
}
