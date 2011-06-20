package unibo.as.cupido.backend.table;

public class StartNotifierThread extends Thread {

	private final SingleTableManager stm;
	private Object lock = new Object();
	private boolean gameStarted = false;

	public StartNotifierThread(SingleTableManager stm) {
		this.stm = stm;
	}

	@Override
	public void run() {
		try {
			synchronized (lock) {
				while (!gameStarted)
					lock.wait();
			}
			stm.notifyGameStarted();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setGameStarted() {
		synchronized (lock) {
			gameStarted = true;
			lock.notify();
		}
	}
}
