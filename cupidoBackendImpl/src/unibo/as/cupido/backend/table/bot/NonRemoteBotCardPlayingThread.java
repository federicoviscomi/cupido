package unibo.as.cupido.backend.table.bot;

public class NonRemoteBotCardPlayingThread extends Thread {

	private final NonRemoteBot bot;
	boolean ableToPass = false;
	boolean ableToPlay = false;

	private Object lock = new Object();

	public NonRemoteBotCardPlayingThread(NonRemoteBot bot, String botName) {
		super("NonRemoteBotCardPlayingThread " + botName);
		ableToPlay = ableToPass;
		ableToPass = ableToPlay;
		this.bot = bot;
	}

	@Override
	public void run() {
		try {
			synchronized (lock) {
				while (!ableToPass) {
					lock.wait();
				}
				bot.passCards();
			}
			for (int i = 0; i < 13; i++) {
				synchronized (lock) {
					while (!ableToPlay) {
						lock.wait();
					}
					ableToPlay = false;
					bot.playNextCard();
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setAbleToPass() {
		synchronized (lock) {
			if (!ableToPass) {
				ableToPass = true;
				lock.notify();
			}
		}
	}

	public void setAbleToPlay() {
		synchronized (lock) {
			ableToPlay = true;
			lock.notify();
		}
	}

}
