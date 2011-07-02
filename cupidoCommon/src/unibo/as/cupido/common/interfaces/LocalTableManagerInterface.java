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

import unibo.as.cupido.common.structures.Pair;
import unibo.as.cupido.common.structures.TableInfoForClient;
import unibo.as.cupido.common.exception.NoSuchTableException;

/**
 * 
 */
public interface LocalTableManagerInterface extends Remote {

	/** default maximum number of table that this LTM can handle */
	public static final int DEFAULT_MAX_TABLE = 100;
	/** default address of rmi registry */
	public static final String DEFAULT_RMI_REGISTRY_ADDRESS = "localhost";

	/**
	 * 
	 * This method is used by GTM to create a new table.
	 * 
	 * @param creator
	 *            the player who is wants to create a table
	 * @param snf
	 *            the notification interface associated with the player who
	 *            wants to create a table
	 * @return a remote interface to the table manager who handles newly created
	 *         table
	 * @throws RemoteException
	 */
	public Pair<TableInterface, TableInfoForClient> createTable(String owner,
			ServletNotificationsInterface snf) throws RemoteException;

	/***
	 * The Servlet uses this method to get the remote reference to the component
	 * who manages the table <code>tableId</code>
	 * 
	 * @return a remote reference to the component who manages the table
	 *         <code>tableId</code>
	 * @throws NoSuchTableException
	 *             if tableId is invalid
	 * @throws RemoteException
	 *             in case of internal error
	 */
	public TableInterface getTable(int tableId) throws RemoteException,
			NoSuchTableException;

	/**
	 * Called by the GTM to notify it is shutting down
	 * 
	 * @throws RemoteException
	 */
	public void notifyGTMShutDown() throws RemoteException;

	/**
	 * Called by the GTM to see if this LTM is still alive.
	 * 
	 * @throws RemoteException
	 *             if this LTM is not alive anymore.
	 */
	public void isAlive() throws RemoteException;

	/**
	 * Called by an STM when a game terminates.
	 * 
	 * @param tableId
	 *            identifier of table to be destroyed
	 * @throws RemoteException
	 * @throws NoSuchTableException
	 *             if this cannot find specified table
	 */
	public void notifyTableDestruction(int tableId) throws RemoteException,
			NoSuchTableException;

}
