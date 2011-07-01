package unibo.as.cupido.backend.table;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ActionQueue extends Thread {

	private Object lock;
	private List<Action> actions;
	boolean exit;

	public ActionQueue() {
		lock = new Object();
		actions = new LinkedList<Action>();
		exit = false;
	}

	public void enqueue(Action action) {
		synchronized (lock) {
			if (exit)
				return;
			actions.add(action);
			lock.notify();
		}
	}

	@Override
	public void run() {
		try {
			while (!exit) {
				List<Action> list;
				synchronized (lock) {
					while (actions.isEmpty() && !exit)
						lock.wait();

					// Make sure the caller has returned.
					// Note that no actions can be added in the meantime.
					Thread.sleep(10);

					list = this.actions;
					this.actions = new ArrayList<Action>();
				}
				for (Action action : list) {
					action.execute();
				}
			}
		} catch (InterruptedException e) {
			//
		}
	}

	public void killConsumer() {
		synchronized (lock) {
			exit = true;
			lock.notify();
		}
	}
}
