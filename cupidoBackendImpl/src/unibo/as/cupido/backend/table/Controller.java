package unibo.as.cupido.backend.table;

import java.util.LinkedList;

import unibo.as.cupido.backend.table.AsynchronousMessage.AddPlayerMessage;
import unibo.as.cupido.backend.table.AsynchronousMessage.EndGamePrematurelyMessage;
import unibo.as.cupido.backend.table.AsynchronousMessage.PlayerLeaveMessage;
import unibo.as.cupido.backend.table.AsynchronousMessage.PlayerPassCardsMessage;
import unibo.as.cupido.backend.table.AsynchronousMessage.PlayerPlayCardsMessage;
import unibo.as.cupido.backend.table.AsynchronousMessage.ReplacePlayerMessage;
import unibo.as.cupido.backend.table.AsynchronousMessage.SendLocalChatMessage;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.ChatMessage;
import unibo.as.cupido.common.structures.TableInfoForClient;

public class Controller extends Thread {

	private final SingleTableManager singleTableManager;
	private final Object lock = new Object();
	private int playersCount = 0;

	private LinkedList<AsynchronousMessage> messageQueue = new LinkedList<AsynchronousMessage>();
	private boolean gameEnded = false;
	private boolean gameEndedPrematurely = false;
	private final TableInfoForClient table;

	public Controller(SingleTableManager singleTableManager,
			TableInfoForClient table) {
		this.singleTableManager = singleTableManager;
		this.table = table;
	}

	public void addPlayer(String botName, boolean isBot, int score, int position) {
		synchronized (lock) {
			messageQueue.add(new AsynchronousMessage.AddPlayerMessage(botName,
					isBot, score, position));
			lock.notify();
		}
	}

	private void consume() {
		for (AsynchronousMessage message : messageQueue) {
			System.err.println("Controller consuming: " + message);
			switch (message.type) {
			case ADD_PLAYER: {
				AddPlayerMessage messageInstance = (AddPlayerMessage) message;
				singleTableManager.notifyPlayerJoined(messageInstance);
				break;
			}
			case END_GAME_PREMATURELY: {
				singleTableManager.notifyGameEndedPrematurely();
				break;
			}
			case REPLACE_PLAYER: {
				ReplacePlayerMessage messageInstance = (ReplacePlayerMessage) message;
				singleTableManager.notifyPlayerReplaced(messageInstance);
				break;
			}
			case PLAYER_LEAVE: {
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
				break;
			}
			case START_GAME: {
				singleTableManager.notifyGameStarted();
				break;
			}
			}
		}
		messageQueue.clear();
	}

	public void endGamePrematurely() {
		synchronized (lock) {
			messageQueue
					.add(new AsynchronousMessage.EndGamePrematurelyMessage());
			lock.notify();
		}
	}

	public void passCards(String userName, int position, Card[] cards) {
		synchronized (lock) {
			messageQueue.add(new AsynchronousMessage.PlayerPassCardsMessage(
					userName, position, cards));
			lock.notify();
		}
	}

	public void playCard(String userName, int playerPosition, Card card) {
		synchronized (lock) {
			messageQueue.add(new AsynchronousMessage.PlayerPlayCardsMessage(
					userName, playerPosition, card));
			lock.notify();
		}
	}

	public void playerLeave(String userName) {
		synchronized (lock) {
			messageQueue.add(new AsynchronousMessage.PlayerLeaveMessage(
					userName));
			lock.notify();
		}
	}

	public void replacePlayer(String userName, int position) {
		synchronized (lock) {
			messageQueue.add(new AsynchronousMessage.ReplacePlayerMessage(
					userName, position));
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
				if (gameEndedPrematurely) {
					singleTableManager.notifyGameEndedPrematurely();
				} else {
					singleTableManager.notifyGameEnded();
				}
			}
		} catch (InterruptedException e) {
			//
		}
	}

	public void sendLocalChatMessage(ChatMessage message) {
		synchronized (lock) {
			messageQueue.add(new AsynchronousMessage.SendLocalChatMessage(
					message));
			lock.notify();
		}
	}

	public void startGame() {
		synchronized (lock) {
			messageQueue.add(new AsynchronousMessage.StartGameMessage());
			lock.notify();
		}
	}

	public void allPassedCards() {
		synchronized (lock) {
			messageQueue.add(new AsynchronousMessage.AllPassedCardsMessage());
			lock.notify();
		}
	}

	public void endGame() {
		synchronized (lock) {
			messageQueue.add(new AsynchronousMessage.EndGameMessage());
			lock.notify();
		}
	}
}
