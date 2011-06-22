package unibo.as.cupido.backend.ltm;

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
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import unibo.as.cupido.backend.table.SingleTableManager;
import unibo.as.cupido.common.exception.NoSuchLTMException;
import unibo.as.cupido.common.exception.NoSuchUserException;
import unibo.as.cupido.common.interfaces.GlobalTableManagerInterface;
import unibo.as.cupido.common.interfaces.LocalTableManagerInterface;
import unibo.as.cupido.common.interfaces.ServletNotificationsInterface;
import unibo.as.cupido.common.interfaces.TableInterface;
import unibo.as.cupido.common.structures.Pair;
import unibo.as.cupido.common.structures.TableDescriptor;
import unibo.as.cupido.common.structures.TableInfoForClient;

/**
 * 
 * 
 * 
 * @author cane
 * 
 */
public class LocalTableManager implements LocalTableManagerInterface {

	private static final String LOCALTABLEMANAGER_CONFIGURATION_FILE = "localTableManager.config";

	public static void main(String[] args) {
		try {
			new LocalTableManager();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

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

	// TODO can use HashSet<TableInterface> and table id is hashCode?
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

			allTables = new HashMap<Integer, TableInterface>(MAX_TABLE);
			localAddress = InetAddress.getLocalHost().toString();

			System.out
					.println("Local table manager server started correctly at address "
							+ InetAddress.getLocalHost());

			final LocalTableManager ltm = this;
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					try {
						gtmRemote.notifyLocalTableManagerShutdown(ltm);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});

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
	public Pair<TableInterface, TableInfoForClient> createTable(String owner,
			ServletNotificationsInterface snf) throws RemoteException {
		if (owner == null || snf == null)
			throw new IllegalArgumentException(owner + " " + snf);
		try {
			TableInfoForClient newTable = new TableInfoForClient(owner, 3,
					new TableDescriptor(localAddress + this.toString(), nextId));
			TableInterface tableRemote = (TableInterface) UnicastRemoteObject
					.exportObject(new SingleTableManager(snf, newTable,
							gtmRemote));
			allTables.put(nextId, tableRemote);
			nextId++;
			return new Pair<TableInterface, TableInfoForClient>(tableRemote,
					newTable);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchUserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchLTMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public TableInterface getTable(int tableId) {
		return allTables.get(tableId);
	}

	@Override
	public void isAlive() throws RemoteException {
		//
	}

	@Override
	public void notifyGTMShutDown() {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyTableDestruction(int tableId) {
		allTables.remove(tableId);
	}

	public void shutDown() {
		try {
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
