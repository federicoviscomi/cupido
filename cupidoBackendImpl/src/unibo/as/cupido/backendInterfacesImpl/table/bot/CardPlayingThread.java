package unibo.as.cupido.backendInterfacesImpl.table.bot;

import java.rmi.RemoteException;
import java.util.concurrent.Semaphore;

public class CardPlayingThread extends Thread {

	private final Bot bot;
	private final Semaphore playNextCardLock;
	private boolean endedGame;

	public CardPlayingThread(Semaphore playNextCardLock, Bot bot) {
		this.playNextCardLock = playNextCardLock;
		this.bot = bot;
	}

	@Override
	public void run() {
		try {
			bot.passCards();
			while (!endedGame) {
				playNextCardLock.acquire();
				bot.playNextCard();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setEndedGame() {
		endedGame = true;
	}

}
