package unibo.as.cupido.backend.table;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ActionQueue extends Thread {

	private Object lock = new Object();
	private final List<Action> actions = new LinkedList<Action>();

	public ActionQueue() {
	}

	private synchronized void consume() throws InterruptedException {
		// Make sure the caller has returned.
		Thread.sleep(10);
		
		System.err.println("ActionQueue: entering consume().");
		for (Action action : actions)
			action.execute();
		actions.clear();
		System.err.println("ActionQueue: exiting consume().");
	}
	
	public void enqueue(Action action) {
		synchronized (lock) {
			actions.add(action);
			lock.notify();
		}
	}

	@Override
	public void run() {
		try {
			synchronized (lock) {
				while (true) {
					lock.wait();
					this.consume();
				}
			}
		} catch (InterruptedException e) {
			//
		}
	}
}
