package unibo.as.cupido.backendInterfacesImpl.table;

public class StartNotifierThread extends Thread {

	private final SingleTableManager stm;
	private Object lock = new Object();
	private boolean gameStarted = false;

	public StartNotifierThread(SingleTableManager stm) {
		this.stm = stm;
	}

	public void setGameStarted() {
		synchronized (lock) {
			gameStarted = true;
			lock.notify();
		}
	}

	@Override
	public void run() {
		try {
			synchronized (lock) {
				while (!gameStarted)
					lock.wait();
			}
			System.err.println("StartNotifierThread 3");
			stm.notifyGameStarted();
			System.err.println("StartNotifierThread 4");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.err.println("StartNotifierThread 5");
	}
}
