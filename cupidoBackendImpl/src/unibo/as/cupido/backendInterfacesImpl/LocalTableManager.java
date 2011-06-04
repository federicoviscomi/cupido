package unibo.as.cupido.backendInterfacesImpl;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.StringTokenizer;
import java.util.concurrent.ThreadPoolExecutor;

import unibo.as.cupido.backendInterfaces.LocalTableManagerInterface;
import unibo.as.cupido.backendInterfaces.TableManagerInterface;
import unibo.as.cupido.backendInterfaces.TableManagerInterface.ServletNotifcationsInterface;
import unibo.as.cupido.backendInterfaces.TableManagerInterface.Table;

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

	private InetAddress myAddress;

	private TableManagerInterface serverRemote;

	private String serverAddress = TableManagerInterface.defaultServerAddress;

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
			myAddress = InetAddress.getLocalHost();
			Registry remoteServerRegistry = LocateRegistry.getRegistry(this.serverAddress);
			serverRemote = (TableManagerInterface) remoteServerRegistry
					.lookup(TableManagerInterface.globalTableManagerName);

			serverRemote.notifyLocalTableManagerSturtup(myAddress);
			System.out.println("Local table manager server started correctly at address " + myAddress
					+ ".\nGlobal table manager server address is " + this.serverAddress);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			System.err.println("Error:" + TableManagerInterface.globalTableManagerName
					+ " is not bound to anything in the rmiregistry");
		}
	}

	@Override
	public int createTable(String owner, ServletNotifcationsInterface snf) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Table getTable(int tableId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getWorkLoad() throws RemoteException {
		return (tableCount / MAX_TABLE) * 100;
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
		throw new UnsupportedOperationException();
	}

	public void shutDown() {
		// TODO Auto-generated method stub

	}

	@Override
	public InetAddress getAddress() {
		return myAddress;
	}

}
