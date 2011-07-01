package unibo.as.cupido.backend.table;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * This class implements a queue of actions.
 * 
 * The queue has its own thread, and all actions are executed in this thread
 * in FIFO order.
 * 
 * Every action is executed at least 10 milliseconds after it has been enqueued.
 */
public class ActionQueue extends Thread {

	/**
	 * The lock used to serialize concurrent calls to enqueue()
	 */
	private Object lock;
	
	/**
	 * The ordered list of pending actions.
	 */
	private List<Action> actions;
	
	/**
	 * Specifies whether or not the user has requested to stop the
	 * consumer thread.
	 */
	boolean exit;

	/**
	 * The default constructor.
	 * The start() method has to be called to start the queue thread after
	 * the queue is constructed.
	 */
	public ActionQueue() {
		lock = new Object();
		actions = new LinkedList<Action>();
		exit = false;
	}

	/**
	 * Adds the specified action to the queue.
	 * Concurrent calls of this method are serialized by the queue.
	 * 
	 * @param action The action that needs to be executed.
	 */
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

	/**
	 * Stops the consumer thread.
	 */
	public void killConsumer() {
		synchronized (lock) {
			exit = true;
			lock.notify();
		}
	}
}
