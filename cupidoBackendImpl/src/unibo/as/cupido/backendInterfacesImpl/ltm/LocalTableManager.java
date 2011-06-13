package unibo.as.cupido.backendInterfacesImpl.ltm;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.RejectedExecutionException;

import unibo.as.cupido.backendInterfaces.GlobalTableManagerInterface;
import unibo.as.cupido.backendInterfaces.GlobalTableManagerInterface.Table;
import unibo.as.cupido.backendInterfaces.LocalTableManagerInterface;
import unibo.as.cupido.backendInterfaces.ServletNotificationsInterface;
import unibo.as.cupido.backendInterfaces.TableInterface;
import unibo.as.cupido.backendInterfacesImpl.table.SingleTableManager;

/**
 * 
 * 
 * 
 * @author cane
 * 
 */
public class LocalTableManager implements LocalTableManagerInterface {

	private static final String LOCALTABLEMANAGER_CONFIGURATION_FILE = "localTableManager.config";

	private GlobalTableManagerInterface gtmRemote;

	/**
	 * The maximum number of table a LocalTableManager can handle. This number
	 * is stored in the configuration file
	 * 
	 * localTableManager.config
	 * 
	 * This file has to contain a line in the format
	 * 
	 * MAX_TABLE 1...*
	 */
	private int MAX_TABLE;

	private int nextId = 0;

	private Map<Integer, TableInterface> allTables;

	private String localAddress;

	public LocalTableManager() throws RemoteException {

		// reads configuration file
		try {
			BufferedReader configuration = new BufferedReader(new FileReader(
					LOCALTABLEMANAGER_CONFIGURATION_FILE));
			String nextConfigurationLine;
			String gtmAddress = "localhost";
			while ((nextConfigurationLine = configuration.readLine()) != null) {
				StringTokenizer tokenizer = new StringTokenizer(
						nextConfigurationLine.trim());
				if (tokenizer.hasMoreTokens()) {
					String configurationVariable = tokenizer.nextToken();
					if (configurationVariable.equals("MAX_TABLE")) {
						MAX_TABLE = Integer.parseInt(tokenizer.nextToken());
					} else if (configurationVariable.equals("GTM_ADDRESS")) {
						gtmAddress = tokenizer.nextToken();
					}
				}
			}
			configuration.close();

			Registry remoteServerRegistry = LocateRegistry
					.getRegistry(gtmAddress);
			gtmRemote = (GlobalTableManagerInterface) remoteServerRegistry
					.lookup(GlobalTableManagerInterface.globalTableManagerName);

			gtmRemote.notifyLocalTableManagerStartup(
					(LocalTableManagerInterface) UnicastRemoteObject
							.exportObject(this), MAX_TABLE);

			allTables = new HashMap<Integer, TableInterface>();

			System.out
					.println("Local table manager server started correctly at address "
							+ InetAddress.getLocalHost()
							+ "\nGlobal table manager server address is "
							+ gtmAddress
							+ "\n Current thread is "
							+ Thread.currentThread());
			localAddress = InetAddress.getLocalHost().toString();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			System.err.println("Error:"
					+ GlobalTableManagerInterface.globalTableManagerName
					+ " is not bound to anything in the rmiregistry");
		}
	}

	@Override
	public TableInterface createTable(String owner,
			ServletNotificationsInterface snf) throws RemoteException {
		try {
			System.out.println("Current thread is " + Thread.currentThread());
			Table newTable = new Table(owner, 3, localAddress, nextId);
			TableInterface tableRemote = (TableInterface) UnicastRemoteObject
					.exportObject(new SingleTableManager(snf, newTable,
							gtmRemote));
			allTables.put(nextId, tableRemote);
			nextId++;
			return tableRemote;
		} catch (RejectedExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public TableInterface getTable(int tableId) {
		System.out.println("Current thread is " + Thread.currentThread());
		return allTables.get(tableId);
	}

	@Override
	public void isAlive() throws RemoteException {
		System.out.println("Current thread is " + Thread.currentThread()
				+ " called isAlive()");
	}

	@Override
	public void notifyGTMShutDown() {
		// TODO Auto-generated method stub
		System.out.println("Current thread is " + Thread.currentThread());
	}

	/**
	 * 
	 * When a Table terminates, it calls this method to notify his
	 * LocalTableManager
	 * 
	 * @param tableId
	 *            the id of the table that terminates
	 */
	public void notifyTableDestruction(int tableId) {
		System.out.println("Current thread is " + Thread.currentThread());
		allTables.remove(tableId);
	}

	public void shutDown() {
		try {
			System.out.println("Current thread is " + Thread.currentThread());
			gtmRemote.notifyLocalTableManagerShutdown(this);
		} catch (AccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
