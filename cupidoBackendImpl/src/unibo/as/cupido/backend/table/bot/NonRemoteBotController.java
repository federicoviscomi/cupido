package unibo.as.cupido.backend.table.bot;

import java.util.Iterator;
import java.util.LinkedList;

import unibo.as.cupido.backend.table.AsynchronousMessage;

public class NonRemoteBotController extends Thread {

	private final NonRemoteBot nonRemoteBot;
	private final String botName;
	private boolean active;
	private final Object lock = new Object();
	private final int position;
	private LinkedList<AsynchronousMessage> messageQueue = new LinkedList<AsynchronousMessage>();
	private boolean gameEnded;
	private int passed = 0;

	public NonRemoteBotController(NonRemoteBot nonRemoteBot, String botName,
			boolean active, int position) {
		this.nonRemoteBot = nonRemoteBot;
		this.botName = botName;
		this.active = active;
		this.position = position;
	}

	private void consume() {
		System.err.println("Non remote bot controller " + botName + "  >>>> ");
		Iterator<AsynchronousMessage> queue = messageQueue.iterator();
		while (queue.hasNext()) {
			AsynchronousMessage message = queue.next();
			queue.remove();
			System.err.println("Non remote bot controller " + botName
					+ "  consuming: " + message);
			switch (message.type) {
			case END_GAME: {
				return;
			}
			case BOT_ACTIVATE: {
				this.active = true;
				break;
			}
			case BOT_PASS: {
				passed++;
				if (active) {
					nonRemoteBot.passCards();
				}
				if (passed == 4) {
					nonRemoteBot.notifyAllPlayerPassedCards();
				}
				break;
			}
			case BOT_PLAY: {
				if (active) {
					// nonRemoteBot.playCard();
				}
				break;
			}
			default: {
				throw new Error();
			}
			}
		}
		System.err.println("Non remote bot controller " + botName + "  <<<< ");
	}

	public void produceBotActivate(int position) {
		synchronized (lock) {
			messageQueue.add(new AsynchronousMessage.BotActivateMessage(
					position));
			System.err.println("Non remote bot controller " + botName
					+ "  produced: " + messageQueue.peek());
			lock.notify();
		}
	}

	public void produceBotPassCards() {
		synchronized (lock) {
			messageQueue.add(new AsynchronousMessage.BotPassCardsMessage(
					nonRemoteBot, position));
			System.err.println("Non remote bot controller " + botName
					+ "  produced: " + messageQueue.peek());
			lock.notify();
		}
	}

	public void produceBotPlay() {
		synchronized (lock) {
			messageQueue.add(new AsynchronousMessage.BotPlayMessage(
					nonRemoteBot, position));
			System.err.println("Non remote bot controller " + botName
					+ "  produced: " + messageQueue.peek());
			lock.notify();
		}
	}

	public void produceEndGame() {
		synchronized (lock) {
			messageQueue.add(new AsynchronousMessage.EndGameMessage());
			gameEnded = true;
			System.err.println("Non remote bot controller " + botName
					+ "  produced: " + messageQueue.peek());
			lock.notify();
		}
	}

	@Override
	public void run() {
		try {
			synchronized (lock) {
				while (!gameEnded) {
					lock.wait();
					this.consume();
				}
			}
		} catch (InterruptedException e) {
			//
		}
	}

}
