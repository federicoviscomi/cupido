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

package unibo.as.cupido.backend.table;

import java.rmi.RemoteException;

/**
 * This is a helper class that can be used instead of the Action
 * interface, when the action involves RMI calls.
 * 
 * Using this class, no try-catch block is needed around the RMI call.
 */
public abstract class RemoteAction implements Action {

	@Override
	public void execute() {
		try {
			onExecute();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Derived classes must implement this instead of execute(),
	 * thus simplifying the code.
	 * 
	 * @throws RemoteException
	 *      If there is an error during communications.
	 */
	abstract public void onExecute() throws RemoteException;
}
