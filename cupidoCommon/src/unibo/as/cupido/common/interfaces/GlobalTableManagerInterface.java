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

package unibo.as.cupido.common.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;

import unibo.as.cupido.common.structures.TableDescriptor;
import unibo.as.cupido.common.structures.TableInfoForClient;
import unibo.as.cupido.common.exception.AllLTMBusyException;
import unibo.as.cupido.common.exception.EmptyTableException;
import unibo.as.cupido.common.exception.FullTableException;
import unibo.as.cupido.common.exception.NoSuchLTMException;
import unibo.as.cupido.common.exception.NoSuchTableException;

/**
 * The remote interface that local tables manager(LTM), single table
 * manager(STM) and Servlets use to communicate with the global table
 * manager(GTM).
 * 
 */
public interface GlobalTableManagerInterface extends Remote {

	/** GTM name in the RMI registry */
	public static final String GTM_RMI_NAME = "globaltableserver";

	/** delay of milliseconds that takes between each polling */
	public static final long POLLING_DELAY = (long) 1e5;

	/** number of tables returned by {@link #getTableList()} */
	public static final int MAX_TABLE_LIST_SIZE = 30;

	/**
	 * This method is used by the Servlet to create a new Table
	 * 
	 * @param creator
	 *            name of player who creates a table
	 * @param snf
	 *            notification interface of player who creates a table
	 * @return a remote reference to the STM who handles the newly created table
	 * @throws RemoteException
	 * @throws AllLTMBusyException
	 *             if there is no LTM that can handle more tables or there are
	 *             no LTM at all.
	 */
	public TableInterface createTable(String creator,
			ServletNotificationsInterface snf) throws RemoteException,
			AllLTMBusyException;

	/**
	 * Return a chunck of tables managed by this GTM. The chunck contains
	 * {@link #MAX_TABLE_LIST_SIZE} tables and is chosen at random.
	 * 
	 * @return a chunck of tables managed by this GTM.
	 * @throws RemoteException
	 */
	public Collection<TableInfoForClient> getTableList() throws RemoteException;

	/**
	 * When an LTM shuts down, it calls this method and GTM removes it from his
	 * LTM set. Note that this method is not strictly necessary because GTM poll
	 * LTMs every POLLING_DELAY milliseconds.
	 * 
	 * @param ltm
	 *            the LTM who has shut down
	 * @throws RemoteException
	 */
	public void notifyLocalTableManagerShutdown(LocalTableManagerInterface ltm)
			throws RemoteException;

	/**
	 * The GTM component keeps a set of LTM. When a new LTM wants to join this
	 * set, it calls this on the GTM.
	 * 
	 * @param localTableManagerInterface
	 *            the LTM who starts
	 * @param maxTable
	 *            maximum number of table that specified LTM can handle
	 * @throws RemoteException
	 */
	public void notifyLocalTableManagerStartup(
			LocalTableManagerInterface localTableManagerInterface, int maxTable)
			throws RemoteException;

	/**
	 * STM uses this method to notify GTM when the game on table
	 * <tt>tableDescriptor</tt> ends.
	 * 
	 * @param tableDescriptor
	 *            identifier of table to be destroyed
	 * @param ltm
	 *            identifier of LTM who managed the table to be destroyed
	 * @throws RemoteException
	 * @throws NoSuchLTMException
	 *             if this cannot find specified LTM
	 * @throws NoSuchTableException
	 *             if this cannot find specified table
	 */
	public void notifyTableDestruction(TableDescriptor tableDescriptor,
			LocalTableManagerInterface ltm) throws RemoteException,
			NoSuchLTMException, NoSuchTableException;

	/**
	 * STM uses this method to notify GTM when a player joins table identified
	 * by <tt>tableDescriptor</tt>.
	 * 
	 * @param tableDescriptor
	 *            identifier of table where a player joined
	 * @throws RemoteException
	 * @throws NoSuchTableException
	 *             if this cannot find specified table
	 * @throws FullTableException
	 *             if the table already contains four players
	 */
	public void notifyTableJoin(TableDescriptor tableDescriptor)
			throws RemoteException, NoSuchTableException, FullTableException;

	/**
	 * STM calls this if a player leaves table identified by
	 * <tt>tableDescriptor</tt> before the game start. This method is not called
	 * after the game starts because in that case a bot automatically replaces
	 * the leaving player. The player who leaves is not table creator because in
	 * that case the table is to be destroyed.
	 * 
	 * @param tableDescriptor
	 *            identifier of table from which a player left
	 * @throws RemoteException
	 * @throws NoSuchTableException
	 *             if this cannot find specified table
	 * @throws EmptyTableException
	 *             if specified table is empty
	 * 
	 */
	public void notifyTableLeft(TableDescriptor tableDescriptor)
			throws RemoteException, NoSuchTableException, EmptyTableException;

	/**
	 * Get the LTM remote object identified by <tt>ltmId</tt>
	 * 
	 * @param ltmId
	 *            identifier of an LTM
	 * @return the LTM remote object identified by <tt>ltmId</tt>
	 * @throws RemoteException
	 * @throws NoSuchLTMException
	 *             if this cannot find <tt>ltmId</tt>
	 */
	public LocalTableManagerInterface getLTMInterface(String ltmId)
			throws RemoteException, NoSuchLTMException;

}
