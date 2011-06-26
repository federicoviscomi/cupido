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
import java.util.ArrayList;
import java.util.Iterator;

import unibo.as.cupido.common.exception.NoSuchPlayerException;

public class RemovalThread extends Thread {

	private final SingleTableManager singleTableManager;
	private final ArrayList<Integer> removal;

	public RemovalThread(SingleTableManager singleTableManager) {
		this.singleTableManager = singleTableManager;
		removal = new ArrayList<Integer>();
	}

	public void addRemoval(int position) {
		synchronized (removal) {
			removal.add(position);
		}
	}

	public void remove() {
		synchronized (removal) {
			if (removal.size() > 0)
				removal.notify();
		}
	}

	@Override
	public void run() {
		try {
			while (true) {
				synchronized (removal) {
					while (removal.size() == 0) {
						removal.wait();
					}
					Iterator<Integer> iterator = removal.iterator();
					while (iterator.hasNext()) {
						singleTableManager.leaveTable(iterator.next());
						iterator.remove();
					}
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPlayerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
