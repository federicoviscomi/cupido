package unibo.as.cupido.backendInterfacesImpl.table.bot;

import java.util.concurrent.Semaphore;

public class CardPlayingThread extends Thread {

	private final Bot bot;
	private final Semaphore playNextCardLock;
	private final Semaphore passCardsLock;
	private boolean endedGame = false;

	public CardPlayingThread(Semaphore playNextCardLock, Semaphore passCards,
			Bot bot, String botName) {
		super("CardPlayingThread " + botName);
		this.playNextCardLock = playNextCardLock;
		this.passCardsLock = passCards;
		this.bot = bot;
	}

	@Override
	public void run() {
		try {
			passCardsLock.acquire();
			//Thread.sleep(1000);
			bot.passCards();
			System.err.println("\n:\n1");
			while (!endedGame) {
				System.err.println("\n:\n2");
				playNextCardLock.acquire();
				//Thread.sleep(1000);
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
