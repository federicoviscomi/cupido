package unibo.as.cupido.backend.table;

public class EndNotifierThread extends Thread {

	private final SingleTableManager stm;
	private final Object lock = new Object();
	private boolean gameEnded = false;

	public EndNotifierThread(SingleTableManager stm) {
		this.stm = stm;
	}

	@Override
	public void run() {
		try {
			synchronized (lock) {
				while (!gameEnded) {
					lock.wait();
				}
			}
			stm.notifyGameEnded();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setGameEnded() {
		synchronized (lock) {
			gameEnded = true;
			lock.notify();
		}
	}
}
