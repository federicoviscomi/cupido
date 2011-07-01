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

/**
 * Manages a swarm of LTM.
 */
public class LTMSwarm {

	/**
	 * This thread polls the ltm swarm. After this thread finish one polling, it
	 * waits at lest {@link GlobalTableManagerInterface#POLLING_DELAY}
	 * milliseconds before doing another polling. If an LTM throws
	 * RemoteException it is removed from the swarm.
	 */
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
				//
			}
		}
	}

	/**
	 * A <tt>Triple</tt> stores:
	 * <ul>
	 * <li>an LTM interface</li>
	 * <li>the maximum number of table the LTM can handle</li>
	 * <li>the current number of alive tables in the LTM</li>
	 * </ul>
	 * <p>
	 * The {@link Triple#compareTo(Triple)} methods is such that
	 * <tt>Triple t1</tt> is less than <tt>Triple t2</tt> if workload of t1 is
	 * less then workload of t2. In other words the natural ordering of this
	 * class is given by workload. The workload of an LTM is defined as the
	 * current number of alive tables in the LTM over the maximum number of
	 * table the LTM can handle.
	 * <p>
	 * The method {@link Triple#equals(Object)} considers equals to triple if
	 * their field {@link Triple#ltmi} are equals.
	 * <p>
	 * Note that {@link Triple#compareTo(Triple)} and
	 * {@link Triple#equals(Object)} are not consistent to each other, i.e. it
	 * could happen that <tt>((x.compareTo(y)==0) != (x.equals(y))</tt>. This is
	 * intended but has to be specified as wrote on
	 * {@link Comparable#compareTo(Object)}.
	 * 
	 * @see Comparable
	 */
	public static class Triple implements Comparable<Triple> {
		public static Triple getDefault(LocalTableManagerInterface ltmi) {
			return new Triple(ltmi, 0, 0);
		}

		/** the LTM interface of this triple */
		public LocalTableManagerInterface ltmi;
		/** the maximum number of tables that <tt>ltmi</tt> can handle */
		public int maximumTable;
		/** the number of tables that <tt>ltmi</tt> is currently handling */
		public int tableCount;

		/**
		 * Create a new triple with specified LTM interfaces, current table
		 * count and maximum table numbers.
		 * 
		 * @param ltmi
		 *            the LTM interface
		 * @param tableCount
		 *            current number of table that <tt>ltmt</tt> is handling
		 * @param maximumTable
		 *            maximum number of table that <tt>ltmt</tt> can handle
		 */
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
		public int hashCode() {
			return this.ltmi.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return this.ltmi.equals(((Triple) obj).ltmi);
		}

		@Override
		public String toString() {
			return "[" + ltmi + ", " + tableCount + ", " + maximumTable + "]";
		}
	}

	/** a sequence of <tt>Triple</tt> kept sorted low workload first */
	private final ArrayList<Triple> swarm;
	/** thread that polls ltms */
	private final LTMPollingThread ltmPollingThread;

	public LTMSwarm() {
		swarm = new ArrayList<LTMSwarm.Triple>();
		ltmPollingThread = new LTMPollingThread(this);
		ltmPollingThread.start();
	}

	/**
	 * Add specified LTM in the swarm with given maximum table number and zero
	 * current tables handled.
	 * 
	 * @param ltmi
	 *            the LTM interface
	 * @param maximumTable
	 *            maximum number of tables that ltmi can handle
	 * @throws IllegalArgumentException
	 *             if there already is a <tt>Triple</tt> in the swarm equals to
	 *             <tt>ltmi</tt>
	 */
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
			} else {
				swarm.add(index, triple);
			}
		}
	}

	/**
	 * Choose one of the least busy LTM from the swarm and updates its number of
	 * table managed
	 * 
	 * @return one of the least busy LTM from the swarm
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
				throw new NoSuchLTMInterfaceException(ltmi.toString());
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

	public void remove(LocalTableManagerInterface ltmi) {
		synchronized (swarm) {
			swarm.remove(Triple.getDefault(ltmi));
		}
	}

	public void shutdown() {
		synchronized (swarm) {
			ltmPollingThread.interrupt();
			for (Triple swarmEntry : swarm) {
				try {
					swarmEntry.ltmi.notifyGTMShutDown();
				} catch (RemoteException e) {
					//
				}
			}
		}
	}
}
