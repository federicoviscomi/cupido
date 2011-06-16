package unibo.as.cupido.backendInterfacesImpl.table.bot;

import java.util.concurrent.Semaphore;

public class CardPlayingThread extends Thread {

	private final Bot bot;
	private final Semaphore playNextCardLock;
	private boolean endedGame = false;

	public CardPlayingThread(Semaphore playNextCardLock, Bot bot) {
		this.playNextCardLock = playNextCardLock;
		this.bot = bot;
		// System.err.println("\n:\n0 sdfaaaaaakkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk\n "+
		// this);
	}

	@Override
	public void run() {
		try {
			playNextCardLock.acquire();
			bot.passCards();
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
