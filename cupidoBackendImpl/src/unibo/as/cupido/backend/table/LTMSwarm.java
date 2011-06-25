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
import java.util.Collections;
import java.util.Iterator;

import unibo.as.cupido.common.exception.AllLTMBusyException;
import unibo.as.cupido.common.exception.NoSuchLTMInterfaceException;
import unibo.as.cupido.common.interfaces.GlobalTableManagerInterface;
import unibo.as.cupido.common.interfaces.LocalTableManagerInterface;

public class LTMSwarm implements Iterable<LocalTableManagerInterface> {

	public class It implements Iterator<LocalTableManagerInterface> {

		private Iterator<Triple> iterator;

		public It() {
			iterator = swarm.iterator();
		}

		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		@Override
		public LocalTableManagerInterface next() {
			return iterator.next().ltmi;
		}

		@Override
		public void remove() {
			iterator.remove();
		}
	}

	private static class LTMPollingThread extends Thread {
		private final LTMSwarm ltmSwarm;

		public LTMPollingThread(LTMSwarm ltmSwarm) {
			this.ltmSwarm = ltmSwarm;
		}

		@Override
		public void run() {
			try {
				while (true) {
					super.sleep(GlobalTableManagerInterface.POLLING_DELAY);
					synchronized (ltmSwarm.swarm) {
						Iterator<Triple> iterator = ltmSwarm.swarm.iterator();
						while (iterator.hasNext()) {
							Triple next = iterator.next();
							try {
								next.ltmi.isAlive();
							} catch (RemoteException e) {
								System.out
										.println("LTM "
												+ next.ltmi
												+ ".isAlive() thrown RemoteException\n Removing it from swarm");
								iterator.remove();
							}
						}
					}
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			}
		}
	}

	/**
	 * a non mental sick implementor should write equals and compareTo methods
	 * consistent to each other
	 */
	public static class Triple implements Comparable<Triple> {
		public static Triple getDefault(LocalTableManagerInterface ltmi) {
			return new Triple(ltmi, 0, 0);
		}

		public LocalTableManagerInterface ltmi;
		public int maximumTable;

		public int tableCount;

		public Triple(LocalTableManagerInterface ltmi, int tableCount,
				int maximumTable) {
			this.ltmi = ltmi;
			this.tableCount = tableCount;
			this.maximumTable = maximumTable;
		}

		@Override
		public int compareTo(Triple o) {
			return (o.tableCount / o.maximumTable)
					- (this.tableCount / this.maximumTable);
		}

		@Override
		public boolean equals(Object o) {
			return this.ltmi.equals(((Triple) o).ltmi);
		}

		@Override
		public String toString() {
			return "[" + ltmi + ", " + tableCount + ", " + maximumTable + "]";
		}
	}

	ArrayList<Triple> swarm;

	public LTMSwarm() {
		swarm = new ArrayList<LTMSwarm.Triple>();
		new LTMPollingThread(this).start();
	}

	public void addLTM(LocalTableManagerInterface ltmi, int maximumTable) {
		synchronized (swarm) {
			Triple triple = new Triple(ltmi, 0, maximumTable);
			// work with equals
			if (swarm.contains(triple)) {
				throw new IllegalArgumentException(
						"duplicate local table manager");
			}
			// works with compareTo
			int index = Collections.binarySearch(swarm, triple);
			if (index < 0) {
				swarm.add(-index - 1, triple);
			}
		}
	}

	/**
	 * Choose the least busy LTM from the swarm and updates its number of table
	 * managed
	 * 
	 * @return
	 */
	public LocalTableManagerInterface chooseLTM() throws AllLTMBusyException {
		synchronized (swarm) {
			if (swarm.size() == 0)
				throw new AllLTMBusyException(
						"There are no LTMs associated with GTM");
			Triple triple = swarm.get(0);
			if (triple.tableCount == triple.maximumTable) {
				throw new AllLTMBusyException();
			}
			triple.tableCount++;
			return triple.ltmi;
		}
	}

	/**
	 * 
	 * 
	 * 
	 * @param ltmi
	 * @throws NoSuchLTMInterfaceException
	 */
	public void decreaseTableCount(LocalTableManagerInterface ltmi)
			throws NoSuchLTMInterfaceException {
		synchronized (swarm) {
			int index = swarm.indexOf(Triple.getDefault(ltmi));
			if (index < 0)
				throw new NoSuchLTMInterfaceException();
			swarm.get(index).tableCount--;
		}
	}

	/**
	 * Just for debug purpose
	 * 
	 * @return
	 */
	public Triple[] getAllLTM() {
		synchronized (swarm) {
			return swarm.toArray(new Triple[swarm.size()]);
		}
	}

	@Override
	public Iterator<LocalTableManagerInterface> iterator() {
		synchronized (swarm) {
			return new It();
		}
	}

	public void remove(LocalTableManagerInterface ltmi) {
		synchronized (swarm) {
			swarm.remove(Triple.getDefault(ltmi));
		}
	}
}
