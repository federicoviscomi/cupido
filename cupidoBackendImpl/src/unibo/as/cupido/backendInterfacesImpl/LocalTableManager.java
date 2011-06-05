package unibo.as.cupido.backendInterfacesImpl;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import unibo.as.cupido.backendInterfaces.LocalTableManagerInterface;
import unibo.as.cupido.backendInterfaces.GlobalTableManagerInterface;
import unibo.as.cupido.backendInterfaces.GlobalTableManagerInterface.ServletNotifcationsInterface;
import unibo.as.cupido.backendInterfaces.GlobalTableManagerInterface.Table;

/**
 * 
 * 
 * 
 * @author cane
 * 
 */
public class LocalTableManager implements LocalTableManagerInterface {

	private static final String LOCALTABLEMANAGER_CONFIGURATION_FILE = "localTableManager.config";

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

	private ThreadPoolExecutor tpe;

	private int tableCount = 0;

	private GlobalTableManagerInterface serverRemote;

	private String serverAddress = GlobalTableManagerInterface.defaultServerAddress;

	private Map<Integer, Table> tableIds;

	private Registry registry;

	private String name;

	private int nextId = 0;

	public LocalTableManager(String serverAddress) {
		if (serverAddress != null)
			this.serverAddress = serverAddress;
		// reads configuration file
		try {
			BufferedReader configuration = new BufferedReader(new FileReader(LOCALTABLEMANAGER_CONFIGURATION_FILE));
			String nextConfigurationLine;
			while ((nextConfigurationLine = configuration.readLine()) != null) {
				StringTokenizer tokenizer = new StringTokenizer(nextConfigurationLine.trim());
				if (tokenizer.hasMoreTokens()) {
					String configurationVariable = tokenizer.nextToken();
					if (configurationVariable.equals("MAX_TABLE")) {
						MAX_TABLE = Integer.parseInt(tokenizer.nextToken());
					}
				}
			}

			Registry remoteServerRegistry = LocateRegistry.getRegistry(this.serverAddress);
			serverRemote = (GlobalTableManagerInterface) remoteServerRegistry
					.lookup(GlobalTableManagerInterface.globalTableManagerName);

			/* non mi convince */
			name = InetAddress.getLocalHost() + "/" + System.currentTimeMillis();

			registry = LocateRegistry.getRegistry();
			registry.bind(name, UnicastRemoteObject.exportObject(this));
			serverRemote.notifyLocalTableManagerSturtup(name);

			/* */
			tpe = new ThreadPoolExecutor(MAX_TABLE / 2, MAX_TABLE, 10, TimeUnit.SECONDS,
					new SynchronousQueue<Runnable>());
			tpe.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());

			tableIds = new HashMap<Integer, Table>();

			// tableThreads = new HashMap<Long, SingleTableThread>(MAX_TABLE);

			System.out.println("Local table manager server started correctly at address " + InetAddress.getLocalHost()
					+ ".\nRMI registry name is " + name + ".\nGlobal table manager server address is "
					+ this.serverAddress);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			System.err.println("Error:" + GlobalTableManagerInterface.globalTableManagerName
					+ " is not bound to anything in the rmiregistry");
		} catch (AlreadyBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public Table createTable(String owner, ServletNotifcationsInterface snf) {
		try {
			Table newTable = new Table(owner, 3, name, nextId++);
			SingleTableThread tableThread = new SingleTableThread(snf, this, newTable);
			tpe.execute(tableThread);
			return newTable;
		} catch (RejectedExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Table getTable(int tableId) {
		return tableIds.get(tableId);
	}

	@Override
	public int getWorkLoad() throws RemoteException {
		return (tableIds.size() / MAX_TABLE) * 100;
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
		tableIds.remove(tableId);
	}

	public void shutDown() {
		try {
			tpe.shutdown();
			serverRemote.notifyLocalTableManagerShutdown(name);
			registry.unbind(name);
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
	public String getAddress() {
		try {
			return "[address=" + InetAddress.getLocalHost() + ", registry name=" + name + "]";
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
