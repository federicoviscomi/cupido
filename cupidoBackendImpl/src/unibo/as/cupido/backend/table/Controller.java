package unibo.as.cupido.backend.table;

import java.util.Iterator;
import java.util.LinkedList;

import unibo.as.cupido.backend.table.AsynchronousMessage.AddPlayerMessage;
import unibo.as.cupido.backend.table.AsynchronousMessage.BotActivateMessage;
import unibo.as.cupido.backend.table.AsynchronousMessage.BotPassCardsMessage;
import unibo.as.cupido.backend.table.AsynchronousMessage.BotPlayMessage;
import unibo.as.cupido.backend.table.AsynchronousMessage.PlayerLeaveMessage;
import unibo.as.cupido.backend.table.AsynchronousMessage.PlayerPassCardsMessage;
import unibo.as.cupido.backend.table.AsynchronousMessage.PlayerPlayCardsMessage;
import unibo.as.cupido.backend.table.AsynchronousMessage.ReplacePlayerMessage;
import unibo.as.cupido.backend.table.AsynchronousMessage.SendLocalChatMessage;
import unibo.as.cupido.backend.table.bot.NonRemoteBot;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.ChatMessage;

public class Controller extends Thread {

	private final SingleTableManager singleTableManager;
	private final Object lock = new Object();
	private final LinkedList<AsynchronousMessage> messageQueue = new LinkedList<AsynchronousMessage>();

	private boolean gameEnded = false;
	private boolean[] botActive = new boolean[4];
	private int playersCount = 1;

	public Controller(SingleTableManager singleTableManager) {
		this.singleTableManager = singleTableManager;
	}

	private void consume() {
		System.err.println("Controller >>>> ");
		Iterator<AsynchronousMessage> queue = messageQueue.iterator();
		while (queue.hasNext()) {
			AsynchronousMessage message = queue.next();
			queue.remove();
			System.err.println("Controller consuming: " + message);
			switch (message.type) {
			case ADD_PLAYER: {
				if (playersCount == 4)
					throw new IllegalStateException();
				playersCount++;
				AddPlayerMessage messageInstance = (AddPlayerMessage) message;
				if (messageInstance.isBot) {
					botActive[messageInstance.position] = true;
				} else {
					botActive[messageInstance.position] = false;
				}
				singleTableManager.notifyPlayerJoined(messageInstance);
				if (playersCount == 4) {
					singleTableManager.notifyGameStarted();
				}
				break;
			}
			case END_GAME_PREMATURELY: {
				singleTableManager.notifyGameEndedPrematurely();
				return;
			}
			case REPLACE_PLAYER: {
				ReplacePlayerMessage messageInstance = (ReplacePlayerMessage) message;
				singleTableManager.notifyPlayerReplaced(messageInstance);
				break;
			}
			case PLAYER_LEAVE: {
				playersCount--;
				PlayerLeaveMessage messageInstance = (PlayerLeaveMessage) message;
				singleTableManager.notifyPlayerLeft(messageInstance.playerName);
				break;
			}
			case PASS_CARDS: {
				PlayerPassCardsMessage messageInstance = (PlayerPassCardsMessage) message;
				singleTableManager.notifyPlayerPassedCards(messageInstance);
				break;
			}
			case PLAY_CARD: {
				PlayerPlayCardsMessage messageInstance = (PlayerPlayCardsMessage) message;
				singleTableManager.notifyPlayerPlayedCard(messageInstance);
				break;
			}
			case SEND_LOCAL_CHAT_MESSAGE: {
				SendLocalChatMessage messageInstance = (SendLocalChatMessage) message;
				singleTableManager.notifyLocalChatMessage(messageInstance);
				break;
			}
			case ALL_PASSED_CARDS: {
				//
				break;
			}
			case END_GAME: {
				singleTableManager.notifyGameEnded();
				return;
			}
				// case START_GAME: {
				// singleTableManager.notifyGameStarted();
				// break;
				// }
			case BOT_ACTIVATE: {
				BotActivateMessage messageInstance = (BotActivateMessage) message;
				botActive[messageInstance.position] = true;
				break;
			}
			case BOT_PASS: {
				BotPassCardsMessage messageInstance = (BotPassCardsMessage) message;
				if (botActive[messageInstance.position]) {
					messageInstance.nonRemoteBot.passCards();
				}
				break;
			}
			case BOT_PLAY: {
				BotPlayMessage messageInstance = (BotPlayMessage) message;
				if (botActive[messageInstance.position]) {
					messageInstance.nonRemoteBot.playCard();
				}
				break;
			}
			}
		}
		System.err.println("Controller <<<< ");
	}

	public void produceAddPlayer(String botName, boolean isBot, int score,
			int position) {
		synchronized (lock) {
			messageQueue.add(new AsynchronousMessage.AddPlayerMessage(botName,
					isBot, score, position));
			System.err.println("Controller produced: " + messageQueue.peek());
			lock.notify();
		}
	}

	public void produceAllPassedCards() {
		synchronized (lock) {
			messageQueue.add(new AsynchronousMessage.AllPassedCardsMessage());
			System.err.println("Controller produced: " + messageQueue.peek());
			lock.notify();
		}
	}

	public void produceBotActivate(int position) {
		synchronized (lock) {
			messageQueue.add(new AsynchronousMessage.BotActivateMessage(
					position));
			System.err.println("Controller produced: " + messageQueue.peek());
			lock.notify();
		}
	}

	public void produceBotPassCards(NonRemoteBot nonRemoteBot, int position) {
		synchronized (lock) {
			messageQueue.add(new AsynchronousMessage.BotPassCardsMessage(
					nonRemoteBot, position));
			System.err.println("Controller produced: " + messageQueue.peek());
			lock.notify();
		}
	}

	public void produceBotPlay(NonRemoteBot nonRemoteBot, int position) {
		synchronized (lock) {
			messageQueue.add(new AsynchronousMessage.BotPlayMessage(
					nonRemoteBot, position));
			System.err.println("Controller produced: " + messageQueue.peek());
			lock.notify();
		}
	}

	public void produceEndGame() {
		synchronized (lock) {
			messageQueue.add(new AsynchronousMessage.EndGameMessage());
			System.err.println("Controller produced: " + messageQueue.peek());
			gameEnded = true;
			lock.notify();
		}
	}

	public void produceEndGamePrematurely() {
		synchronized (lock) {
			messageQueue
					.add(new AsynchronousMessage.EndGamePrematurelyMessage());
			System.err.println("Controller produced: " + messageQueue.peek());
			gameEnded = true;
			lock.notify();
		}
	}

	public void producePassCards(String userName, int position, Card[] cards) {
		synchronized (lock) {
			messageQueue.add(new AsynchronousMessage.PlayerPassCardsMessage(
					userName, position, cards));
			System.err.println("Controller produced: " + messageQueue.peek());
			lock.notify();
		}
	}

	public void producePlayCard(String userName, int playerPosition, Card card) {
		synchronized (lock) {
			messageQueue.add(new AsynchronousMessage.PlayerPlayCardsMessage(
					userName, playerPosition, card));
			System.err.println("Controller produced: " + messageQueue.peek());
			lock.notify();
		}
	}

	public void producePlayerLeave(String userName) {
		synchronized (lock) {
			messageQueue.add(new AsynchronousMessage.PlayerLeaveMessage(
					userName));
			System.err.println("Controller produced: " + messageQueue.peek());
			lock.notify();
		}
	}

	public void produceReplacePlayer(String userName, int position) {
		synchronized (lock) {
			messageQueue.add(new AsynchronousMessage.ReplacePlayerMessage(
					userName, position));
			System.err.println("Controller produced: " + messageQueue.peek());
			lock.notify();
		}
	}

	public void produceSendLocalChatMessage(ChatMessage message) {
		synchronized (lock) {
			messageQueue.add(new AsynchronousMessage.SendLocalChatMessage(
					message));
			System.err.println("Controller produced: " + messageQueue.peek());
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
