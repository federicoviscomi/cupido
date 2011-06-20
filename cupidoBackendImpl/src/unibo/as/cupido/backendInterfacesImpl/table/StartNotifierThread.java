package unibo.as.cupido.backendInterfacesImpl.table;

import java.util.concurrent.Semaphore;

public class StartNotifierThread extends Thread {

	private final SingleTableManager stm;
	private final Semaphore start;

	public StartNotifierThread(Semaphore start, SingleTableManager stm) {
		this.start = start;
		this.stm = stm;
	}

	@Override
	public void run() {
		try {
			System.err.println("StartNotifierThread 1");
			start.acquire();
			System.err.println("StartNotifierThread 2");
			Thread.sleep(1000);
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
