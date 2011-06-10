package unibo.as.cupido.backendInterfacesImpl;

import java.util.Iterator;
import java.util.PriorityQueue;

import unibo.as.cupido.backendInterfaces.LocalTableManagerInterface;
import unibo.as.cupido.backendInterfaces.common.AllLTMBusyException;

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

	public static class Triple implements Comparable<Triple> {
		public static Triple getDefault(LocalTableManagerInterface ltmi) {
			return new Triple(ltmi, 0, 0);
		}

		public LocalTableManagerInterface ltmi;
		public int tableCount;

		public int maximumTable;

		public Triple(LocalTableManagerInterface ltmi, int tableCount, int maximumTable) {
			this.ltmi = ltmi;
			this.tableCount = tableCount;
			this.maximumTable = maximumTable;
		}

		@Override
		public int compareTo(Triple o) {
			return (o.tableCount / o.maximumTable) - (this.tableCount / this.maximumTable);
		}

		@Override
		public boolean equals(Object o) {
			Triple t = (Triple) o;
			return this.ltmi.equals(t.ltmi);
		}

		public String toString() {
			return "[" + ltmi + ", " + tableCount + ", " + maximumTable + "]";
		}
	}

	PriorityQueue<Triple> swarm;

	public LTMSwarm() {
		swarm = new PriorityQueue<Triple>();
	}

	public void addLTM(LocalTableManagerInterface ltmi, int maxTable) {
		swarm.add(new Triple(ltmi, 0, maxTable));
	}

	/**
	 * Choose the least busy LTM from the swarm and updates its number of table
	 * managed
	 * 
	 * @return
	 */
	public LocalTableManagerInterface chooseLTM() throws AllLTMBusyException {
		if (swarm.peek().maximumTable == swarm.peek().tableCount)
			throw new AllLTMBusyException();
		Triple chosen = swarm.remove();
		chosen.tableCount++;
		swarm.add(chosen);
		return chosen.ltmi;
	}

	/**
	 * 
	 * L'implementazione di questo metodo fa schifo!
	 * 
	 * @param ltmi
	 */
	public void decreaseTableCount(LocalTableManagerInterface ltmi) {
		Triple toUpdate = null;
		Iterator<Triple> iterator = swarm.iterator();
		while (iterator.hasNext()) {
			Triple next = iterator.next();
			if (next.ltmi.equals(ltmi)) {
				toUpdate = next;
				iterator.remove();
			}
		}
		toUpdate.tableCount--;
		swarm.add(toUpdate);
	}

	/**
	 * Just for debug purpose
	 * 
	 * @return
	 */
	public Triple[] getAllLTM() {
		Triple[] all = new Triple[swarm.size()];
		return swarm.toArray(all);
	}

	@Override
	public Iterator<LocalTableManagerInterface> iterator() {
		return new It();
	}

	public void remove(LocalTableManagerInterface ltmi) {
		swarm.remove(Triple.getDefault(ltmi));
	}
}
