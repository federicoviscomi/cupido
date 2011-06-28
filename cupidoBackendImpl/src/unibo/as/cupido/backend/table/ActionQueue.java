package unibo.as.cupido.backend.table;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ActionQueue extends Thread {

	private Object lock = new Object();
	private final List<Action> actions = new LinkedList<Action>();

	public ActionQueue() {
	}

	private void consume() throws InterruptedException {
		synchronized (lock) {
			// Make sure the caller has returned.
			// Note that no actions can be added in the meantime.
			Thread.sleep(10);
			
			List<Action> actions = this.actions;
			actions = new ArrayList<Action>();
			System.err.println("ActionQueue: entering consume().");
			for (Action action : actions)
				action.execute();
			System.err.println("ActionQueue: exiting consume().");
		}
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
