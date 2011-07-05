/*  Cupido - An online Hearts game.
 *  Copyright (C) 2011 Lorenzo Belli, Marco Poletti, Federico Viscomi
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package unibo.as.cupido.backend.ltm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
import unibo.as.cupido.common.database.DatabaseManager;
import unibo.as.cupido.common.exception.NoSuchLTMException;
import unibo.as.cupido.common.exception.NoSuchTableException;
import unibo.as.cupido.common.exception.NoSuchUserException;
import unibo.as.cupido.common.interfaces.GlobalTableManagerInterface;
import unibo.as.cupido.common.interfaces.LocalTableManagerInterface;
import unibo.as.cupido.common.interfaces.ServletNotificationsInterface;
import unibo.as.cupido.common.interfaces.TableInterface;
import unibo.as.cupido.common.structures.Pair;
import unibo.as.cupido.common.structures.TableDescriptor;
import unibo.as.cupido.common.structures.TableInfoForClient;

/**
 * A local table manager handles a portion of all the tables of Cupido.
 * <p>
 * 
 * When LTM starts, it reads the configuration file
 * <tt>LOCALTABLEMANAGER_CONFIGURATION_FILE</tt>. This file contains association
 * of variables and values, one association per line. The syntax is <center>
 * VARIABLE_IDENTIFIER VALUE </center>
 * <p>
 * All associations are optional. There are two variable that can be specified
 * in the configuration file:
 * <ul>
 * <li>MAX_TABLE: specifies the maximum number of table that this LTM can
 * handle, default value is
 * <tt>LocalTableManagerInterface.DEFAULT_MAX_TABLE</tt></li>
 * <li>RMI_REGISTRY_ADDRESS: specifies the rmi registry address, default value
 * is <tt>LocalTableManagerInterface.DEFAULT_RMI_REGISTRY_ADDRESS</tt></li>
 * <li>DATABASE_ADDRESS: specifies the database address, default value is
 * <tt>DatabaseInterface.DEFAULT_DATABASE_ADDRESS</tt></li>
 * </ul>
 */
public class LocalTableManager implements LocalTableManagerInterface {

	/** Shuts down the ltm on exit if necessary */
	private static final class ShutdownHook extends Thread {
		/** the ltm to shut down */
		private final LocalTableManager localTableManager;

		/**
		 * Creates a shut down hook
		 * 
		 * @param localTableManager
		 *            the ltm to shut down
		 */
		public ShutdownHook(LocalTableManager localTableManager) {
			this.localTableManager = localTableManager;
		}

		@Override
		public void run() {
			localTableManager.shutDown();
		}
	}

	/** configuration file name */
	private static final String LOCALTABLEMANAGER_CONFIGURATION_FILE = "localTableManager.config";

	public static void main(String[] args) throws NotBoundException,
			RemoteException, UnknownHostException {
		new LocalTableManager();
	}

	private final GlobalTableManagerInterface gtmRemote;
	/** The maximum number of table a LocalTableManager can handle */
	private int MAX_TABLE = LocalTableManagerInterface.DEFAULT_MAX_TABLE;
	/** address of gtm */
	private String rmiRegistryAddress = LocalTableManagerInterface.DEFAULT_RMI_REGISTRY_ADDRESS;
	/** gives a unique identifier for tables */
	private int nextId = 0;
	/** stores association between table identifiers and STM interfaces */
	private final Map<Integer, TableInterface> allTables;
	/** this LTM local host address */
	private final String localAddress;
	/** <tt>false</tt> if GTM or this LTM are down; <tt>true</tt> otherwise */
	private boolean acceptMoreRequest;
	/** calls ltm shutdown method at exit if necessary */
	private ShutdownHook shutdownHook;
	/** database address */
	private String databaseAddress = DatabaseManager.DEFAULT_DATABASE_ADDRESS;

	public LocalTableManager() throws NotBoundException, RemoteException,
			UnknownHostException {
		// reads configuration file
		try {
			BufferedReader configuration = new BufferedReader(new FileReader(
					LOCALTABLEMANAGER_CONFIGURATION_FILE));
			String nextConfigurationLine;
			while ((nextConfigurationLine = configuration.readLine()) != null) {
				StringTokenizer tokenizer = new StringTokenizer(
						nextConfigurationLine.trim());
				if (tokenizer.hasMoreTokens()) {
					String configurationVariable = tokenizer.nextToken();
					if (configurationVariable.equals("MAX_TABLE")) {
						MAX_TABLE = Integer.parseInt(tokenizer.nextToken());
					} else if (configurationVariable
							.equals("RMI_REGISTRY_ADDRESS")) {
						rmiRegistryAddress = tokenizer.nextToken();
					} else if (configurationVariable.equals("DATABASE_ADDRESS")) {
						databaseAddress = tokenizer.nextToken();
					}
				}
			}
			configuration.close();
		} catch (IOException e) {
			System.err.println("cannot read configuration file: "
					+ LOCALTABLEMANAGER_CONFIGURATION_FILE);
			System.exit(-1);
		}

		Registry remoteServerRegistry = LocateRegistry
				.getRegistry(rmiRegistryAddress);
		gtmRemote = (GlobalTableManagerInterface) remoteServerRegistry
				.lookup(GlobalTableManagerInterface.GTM_RMI_NAME);

		gtmRemote.notifyLocalTableManagerStartup(
				(LocalTableManagerInterface) UnicastRemoteObject
						.exportObject(this), MAX_TABLE);

		allTables = new HashMap<Integer, TableInterface>(MAX_TABLE);
		localAddress = InetAddress.getLocalHost().toString();

		acceptMoreRequest = true;
		System.out
				.println("Local table manager server started correctly at address "
						+ InetAddress.getLocalHost());

		final LocalTableManager ltm = this;
		shutdownHook = new ShutdownHook(this);
		Runtime.getRuntime().addShutdownHook(shutdownHook);
	}

	@Override
	public synchronized Pair<TableInterface, TableInfoForClient> createTable(
			String creator, ServletNotificationsInterface snf)
			throws RemoteException {
		if (creator == null || snf == null)
			throw new IllegalArgumentException(creator + " " + snf);
		if (!acceptMoreRequest) {
			throw new RemoteException();
		}
		try {
			TableInfoForClient newTable = new TableInfoForClient(creator, 3,
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
	public synchronized TableInterface getTable(int tableId) {
		return allTables.get(tableId);
	}

	@Override
	public synchronized void isAlive() throws RemoteException {
		if (!acceptMoreRequest) {
			throw new RemoteException();
		}
	}

	@Override
	public synchronized void notifyGTMShutDown() {
		acceptMoreRequest = false;
	}

	@Override
	public synchronized void notifyTableDestruction(int tableId)
			throws NoSuchTableException {
		if (allTables.remove(tableId) == null)
			throw new NoSuchTableException("table identifier: " + tableId);
	}

	/**
	 * Shut down this LTM
	 */
	public synchronized void shutDown() {
		try {
			gtmRemote.notifyLocalTableManagerShutdown(this);
			acceptMoreRequest = false;
		} catch (AccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			Runtime.getRuntime().removeShutdownHook(shutdownHook);
		} catch (IllegalStateException e) {
			//
		}
	}
}
