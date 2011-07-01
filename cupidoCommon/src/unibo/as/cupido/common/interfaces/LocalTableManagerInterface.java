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

	public static final int DEFAULT_MAX_TABLE = 100;
	public static final String DEFAULT_GTM_ADDRESS = "localhost";

	/**
	 * 
	 * This method is used by global table manager component to create a new
	 * table.
	 * 
	 * @param craetor
	 *            the craetor of the table to be created
	 * @param snf
	 *            the notification interface associated with the player who
	 *            wants to create a table
	 * @return a remote interface to the table manager
	 * @throws RemoteException
	 */
	public Pair<TableInterface, TableInfoForClient> createTable(String owner,
			ServletNotificationsInterface snf) throws RemoteException;

	/***
	 * 
	 * The Servlet uses this method to get the remote reference to the component
	 * who manages the table <code>tableId</code>
	 * 
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
	 * Called by the GTM to notify his shutdown
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
	 * Called by an STM when a play terminates
	 * 
	 * @param tableId
	 * @throws RemoteException
	 * @throws NoSuchTableException
	 */
	public void notifyTableDestruction(int tableId) throws RemoteException,
			NoSuchTableException;

}
