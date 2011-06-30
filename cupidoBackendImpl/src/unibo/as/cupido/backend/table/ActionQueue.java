package unibo.as.cupido.backend.table;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ActionQueue extends Thread {

	private Object lock = new Object();
	private List<Action> actions = new LinkedList<Action>();

	public ActionQueue() {
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
			while (true) {
				List<Action> list;
				synchronized (lock) {
					while (actions.isEmpty())
						lock.wait();

					// Make sure the caller has returned.
					// Note that no actions can be added in the meantime.
					Thread.sleep(10);

					list = this.actions;
					this.actions = new ArrayList<Action>();
				}
				for (Action action : list)
					action.execute();
			}
		} catch (InterruptedException e) {
			//
		}
	}
}
