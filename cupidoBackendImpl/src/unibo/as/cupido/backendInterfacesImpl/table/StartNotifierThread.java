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
			start.acquire();
			Thread.sleep(1000);
			stm.notifyGameStarted();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
