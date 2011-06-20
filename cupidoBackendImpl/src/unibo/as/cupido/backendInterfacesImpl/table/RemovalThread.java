package unibo.as.cupido.backendInterfacesImpl.table;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import unibo.as.cupido.common.exception.PlayerNotFoundException;

public class RemovalThread extends Thread {

	private final SingleTableManager singleTableManager;
	private final ArrayList<Integer> removal;
	private final Semaphore lock;

	public RemovalThread(SingleTableManager singleTableManager) {
		this.singleTableManager = singleTableManager;
		removal = new ArrayList<Integer>();
		lock = new Semaphore(0);
	}

	public synchronized void addRemoval(int i) {
		removal.add(i);
	}

	public synchronized void remove() {
		lock.release();
	}

	@Override
	public synchronized void run() {
		try {
			while (true) {
				lock.acquire();
				for (int i : removal)
					singleTableManager.leaveTable(i);
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
