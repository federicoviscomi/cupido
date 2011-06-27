package unibo.as.cupido.backend.table;

public class STMControllerThread extends Thread {

	private final SingleTableManager singleTableManager;
	private final Object lock = new Object();
	private boolean allPlayerPassedCards = false;

	public STMControllerThread(SingleTableManager singleTableManager) {
		this.singleTableManager = singleTableManager;
	}

	@Override
	public void run() {
		try {
			synchronized (lock) {
				while (!allPlayerPassedCards) {
					System.out.println("stm controller waiting ...");
					lock.wait();
				}
				System.out.println("stm controller sending passed cards notification ...");
				singleTableManager.notifyPassedCards();
				System.out.println("stm controller done");
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void setAllPlayerPassedCards() {
		synchronized (lock) {
			allPlayerPassedCards = true;
			lock.notify();
		}
	}

}
