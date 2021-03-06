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

package unibo.as.cupido.backend.gtm;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collection;

import unibo.as.cupido.backend.gtm.LTMSwarm.Triple;
import unibo.as.cupido.common.exception.AllLTMBusyException;
import unibo.as.cupido.common.exception.EmptyTableException;
import unibo.as.cupido.common.exception.FullTableException;
import unibo.as.cupido.common.exception.NoSuchLTMException;
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
 * Implements the global table manager server. This has various feature:
 * <ul>
 * <li>Manage a set of local table manager(LTM). This includes:
 * <ul>
 * <li>
 * adding an LTM to the set</li>
 * <li>
 * removing an LTM from the set</li>
 * <li>checking for consistency of the set, i.e. polling the LTM in the set
 * </ul>
 * </li>
 * <li>Dispatch the Servlet request to the right LTM i.e.:
 * <ul>
 * <li>choose an alive LTM</li>
 * <li>balance the work load between the LTM</li>
 * </ul>
 * </ul>
 * 
 */
public class GlobalTableManager implements GlobalTableManagerInterface {

	/** calls the gtm shutdown method on exit if necessary */
	private static final class ShutdownHook extends Thread {
		/** the gtm to shut down */
		private final GlobalTableManager gmt;

		/**
		 * Create a new shutdown hook
		 * 
		 * @param gmt
		 *            the gtm to shut down
		 */
		public ShutdownHook(GlobalTableManager gmt) {
			this.gmt = gmt;
		}

		@Override
		public void run() {
			gmt.shutDown();
		}
	}

	public static void main(String args[]) throws RemoteException,
			UnknownHostException, AlreadyBoundException {
		new GlobalTableManager();
	}

	/** stores tables information */
	private AllTables allTables;

	/** manage a swarm of LTM */
	private final LTMSwarm ltmSwarm;

	/** rmi registry */
	private final Registry registry;

	/** this thread does some clean up before the gtm is shut down */
	private final Thread shutdownHook;

	/**
	 * Creates a <tt>GlobalTableManager</tt> and tries to bind it in rmi
	 * registry with name <tt>GlobalTableManagerInterface.GTM_RMI_NAME</tt>.
	 * Creates also the global chat and tries to bind it in rmi registry with
	 * name <tt>GlobalChatInterface.GLOBAL_CHAT_RMI_NAME</tt>
	 * 
	 * @throws RemoteException
	 * @throws UnknownHostException
	 * @throws AlreadyBoundException
	 */
	public GlobalTableManager() throws RemoteException, UnknownHostException,
			AlreadyBoundException {
		allTables = new AllTables();
		ltmSwarm = new LTMSwarm();
		registry = LocateRegistry.getRegistry();

		registry.bind(GlobalTableManagerInterface.GTM_RMI_NAME,
				UnicastRemoteObject.exportObject(this));

		shutdownHook = new ShutdownHook(this);
		Runtime.getRuntime().addShutdownHook(shutdownHook);

		System.out
				.println("Global table manager server started correctly at address "
						+ InetAddress.getLocalHost());
	}

	@Override
	public synchronized TableInterface createTable(String creator,
			ServletNotificationsInterface snf) throws RemoteException,
			AllLTMBusyException {
		System.out.println("\n"
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + creator + ", " + snf + ")");
		/* chose an LTM according to some load balancing policy */
		LocalTableManagerInterface chosenLTM = ltmSwarm.chooseLTM();

		/* create table in the chosen local table manager */
		Pair<TableInterface, TableInfoForClient> table = chosenLTM.createTable(
				creator, snf);

		/* store created table */
		allTables.addTable(table.second, chosenLTM);

		return table.first;
	}

	/**
	 * Return all LTMs managed by this GTM. Just for test/debug purpose.
	 * 
	 * @return all LTMs managed by this GTM.
	 */
	public synchronized Triple[] getAllLTM() {
		return ltmSwarm.getAllLTM();
	}

	@Override
	public synchronized LocalTableManagerInterface getLTMInterface(String ltmId)
			throws RemoteException, NoSuchLTMException {
		LocalTableManagerInterface ltmInterface = allTables
				.getLTMInterface(ltmId);
		if (ltmInterface == null)
			throw new NoSuchLTMException(ltmId);
		return ltmInterface;
	}

	@Override
	public synchronized Collection<TableInfoForClient> getTableList()
			throws RemoteException {
		return allTables.getTableList();
	}

	@Override
	public synchronized void notifyLocalTableManagerShutdown(
			LocalTableManagerInterface ltm) {
		ltmSwarm.remove(ltm);
	}

	@Override
	public synchronized void notifyLocalTableManagerStartup(
			LocalTableManagerInterface ltmi, int maxTable)
			throws RemoteException {
		System.out.println("\n GlobalTableManager."
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + ltmi + ", " + maxTable + ")");
		ltmSwarm.addLTM(ltmi, maxTable);
	}

	@Override
	public synchronized void notifyTableDestruction(
			TableDescriptor tableDescriptor, LocalTableManagerInterface ltm)
			throws RemoteException, NoSuchLTMException, NoSuchTableException {
		System.out.println("gtm. destroying table " + tableDescriptor);
		allTables.removeTable(tableDescriptor);
		ltmSwarm.decreaseTableCount(ltm);
	}

	@Override
	public synchronized void notifyTableJoin(TableDescriptor tableDescriptor)
			throws RemoteException, NoSuchTableException, FullTableException {
		System.out.println("gtm. a player joined " + tableDescriptor);
		allTables.decreaseFreePosition(tableDescriptor);
	}

	@Override
	public synchronized void notifyTableLeft(TableDescriptor tableDescriptor)
			throws RemoteException, NoSuchTableException, EmptyTableException {
		System.out.println("gtm. a player left " + tableDescriptor);
		allTables.increaseFreePosition(tableDescriptor);
	}

	/**
	 * Shut the GTM down.
	 */
	public void shutDown() {
		try {
			registry.unbind(GlobalTableManagerInterface.GTM_RMI_NAME);
		} catch (Exception e) {
			//
		}
		try {
			registry.unbind(GlobalChatInterface.GLOBAL_CHAT_RMI_NAME);
		} catch (Exception e) {
			//
		}
		ltmSwarm.shutdown();
		try {
			Runtime.getRuntime().removeShutdownHook(shutdownHook);
		} catch (IllegalStateException e) {
			//
		}
	}
}
