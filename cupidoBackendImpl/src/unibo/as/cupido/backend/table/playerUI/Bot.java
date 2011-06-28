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

package unibo.as.cupido.backend.table.playerUI;

import java.rmi.Remote;
import java.rmi.RemoteException;

import unibo.as.cupido.common.interfaces.ServletNotificationsInterface;

public interface Bot extends Remote {

	void addBot(int i) throws RemoteException;

	void passCards() throws RemoteException;

	void playNextCard() throws RemoteException, GameEndedException;

	ServletNotificationsInterface getServletNotificationsInterface() throws RemoteException;
}
