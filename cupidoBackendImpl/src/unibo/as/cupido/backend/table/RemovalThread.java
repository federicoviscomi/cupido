package unibo.as.cupido.backend.table;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;

import unibo.as.cupido.common.exception.PlayerNotFoundException;

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
		} catch (PlayerNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
