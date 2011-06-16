package unibo.as.cupido.backendInterfacesImpl.table;

import java.util.concurrent.Semaphore;

public class EndNotifierThread extends Thread {

	private final SingleTableManager stm;
	private final Semaphore end;

	public EndNotifierThread(Semaphore end, SingleTableManager stm) {
		this.end = end;
		this.stm = stm;
	}

	@Override
	public void run() {
		try {
			end.acquire();
			stm.notifyGameEnded();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
