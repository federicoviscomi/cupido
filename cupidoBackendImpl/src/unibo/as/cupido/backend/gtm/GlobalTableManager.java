package unibo.as.cupido.backend.gtm;

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

import unibo.as.cupido.backend.GlobalChatImpl;
import unibo.as.cupido.backend.table.LTMSwarm;
import unibo.as.cupido.backend.table.LTMSwarm.Triple;
import unibo.as.cupido.common.exception.AllLTMBusyException;
import unibo.as.cupido.common.exception.NoSuchLTMException;
import unibo.as.cupido.common.exception.NoSuchLTMInterfaceException;
import unibo.as.cupido.common.exception.NoSuchTableException;
import unibo.as.cupido.common.interfaces.GlobalChatInterface;
import unibo.as.cupido.common.interfaces.GlobalTableManagerInterface;
import unibo.as.cupido.common.interfaces.LocalTableManagerInterface;
import unibo.as.cupido.common.interfaces.ServletNotificationsInterface;
import unibo.as.cupido.common.interfaces.TableInterface;
import unibo.as.cupido.common.structures.Pair;
import unibo.as.cupido.common.structures.TableDescriptor;
import unibo.as.cupido.common.structures.TableInfoForClient;

/**
 * 
 * GTM(global table manager) has various tasks:
 * <ul>
 * <li>Manage a set of LTM(local table manager). This includes:
 * <ul>
 * <li>
 * adding/removing a LocalTableManages to the set</li>
 * <li>checking for consistency of the set, i.e. polling the LTM in the set
 * </ul>
 * </li>
 * <li>Dispatch the Servlet request to the right LTM i.e.:
 * <ul>
 * <li>choose an alive LTM</li>
 * <li>balance the work load between the LocalTableManagers</li>
 * </ul>
 * </ul>
 * 
 * 
 * @author cane
 * 
 */
public class GlobalTableManager implements GlobalTableManagerInterface {

	public static void main(String args[]) throws RemoteException {
		new GlobalTableManager();
	}

	private AllTables allTables;

	/** manage a swarm of LTM */
	private LTMSwarm ltmSwarm;

	private Registry registry;

	public GlobalTableManager() throws RemoteException {
		try {
			allTables = new AllTables();
			ltmSwarm = new LTMSwarm();
			// registry = LocateRegistry.createRegistry(1099);
			registry = LocateRegistry.getRegistry();
			registry.bind(GlobalTableManagerInterface.globalTableManagerName,
					UnicastRemoteObject.exportObject(this));

			registry.bind(GlobalChatInterface.globalChatName,
					UnicastRemoteObject.exportObject(new GlobalChatImpl()));

			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					try {
						registry.unbind(GlobalTableManagerInterface.globalTableManagerName);
						registry.unbind(GlobalChatInterface.globalChatName);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});

			System.out
					.println("Global table manager server started correctly at address "
							+ InetAddress.getLocalHost());
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
	public TableInterface createTable(String owner,
			ServletNotificationsInterface snf) throws RemoteException,
			AllLTMBusyException {
		try {
			System.out.println("\n"
					+ Thread.currentThread().getStackTrace()[1].getMethodName()
					+ "(" + owner + ", " + snf + ")");
			/* chose an LTM according to some load balancing policy */
			LocalTableManagerInterface chosenLTM = ltmSwarm.chooseLTM();

			/* create table in the chosen local table manager */
			Pair<TableInterface, TableInfoForClient> table = chosenLTM
					.createTable(owner, snf);

			/* store created table */
			allTables.addTable(table.second, chosenLTM);

			return table.first;
		} catch (RemoteException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return null;
	}

	public Triple[] getAllLTM() {
		return ltmSwarm.getAllLTM();
	}

	@Override
	public LocalTableManagerInterface getLTMInterface(String ltmId)
			throws RemoteException, NoSuchLTMException {
		return allTables.getLTMInterface(ltmId);
	}

	@Override
	public Collection<TableInfoForClient> getTableList() throws RemoteException {
		return allTables.getAllTables();
	}

	@Override
	public void notifyLocalTableManagerShutdown(LocalTableManagerInterface ltm) {
		ltmSwarm.remove(ltm);
	}

	@Override
	public void notifyLocalTableManagerStartup(LocalTableManagerInterface ltmi,
			int maxTable) throws RemoteException {
		System.out.println("\n GlobalTableManager."
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + ltmi + ", " + maxTable + ")");
		ltmSwarm.addLTM(ltmi, maxTable);
	}

	@Override
	public void notifyTableDestruction(TableDescriptor tableDescriptor,
			LocalTableManagerInterface ltm) throws RemoteException,
			NoSuchLTMInterfaceException {

		allTables.removeTable(tableDescriptor);
		ltmSwarm.decreaseTableCount(ltm);
	}

	@Override
	public void notifyTableJoin(TableDescriptor tableDescriptor)
			throws RemoteException, NoSuchTableException {
		allTables.decreaseFreePosition(tableDescriptor);
	}

	@Override
	public char[] ping() throws RemoteException {
		try {

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
