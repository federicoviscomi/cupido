package unibo.as.cupido.backendInterfacesImpl.table;

import java.util.Random;
import java.util.concurrent.Semaphore;

public class NonRemoteBotCardPlayingThread extends Thread {

	private final NonRemoteBot bot;
	private final Semaphore playNextCardLock;
	private final Semaphore passCardsLock;
	private boolean endedGame = false;

	public NonRemoteBotCardPlayingThread(Semaphore playNextCardLock,
			Semaphore passCardsLock, NonRemoteBot bot, String botName) {
		super("NonRemoteBotCardPlayingThread " + botName);
		this.playNextCardLock = playNextCardLock;
		this.passCardsLock = passCardsLock;
		this.bot = bot;
	}

	@Override
	public void run() {
		try {
			passCardsLock.acquire();
			Thread.sleep(1000);
			System.err.println("\n:\n0");
			bot.passCards();
			int a = (new Random().nextInt(10)) / 10;
			Thread.sleep(100 * a);
			System.err.println("\n:\n1");
			while (!endedGame) {
				System.err.println("\n:\n2");
				playNextCardLock.acquire();
				System.err.println("\n:\n3");
				bot.playNextCard();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setEndedGame() {
		endedGame = true;
	}

}
