package unibo.as.cupido.backendInterfacesImpl.table.bot;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

import unibo.as.cupido.backendInterfaces.TableInterface;
import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.ChatMessage;
import unibo.as.cupido.backendInterfaces.common.InitialTableStatus;
import unibo.as.cupido.backendInterfaces.exception.IllegalMoveException;
import unibo.as.cupido.backendInterfaces.exception.PlayerNotFoundException;
import unibo.as.cupido.backendInterfacesImpl.table.CardsManager;

public class DummyLoggerBotNotifyer extends AbstractBot {

	private final String userName;
	private final TableInterface singleTableManager;
	private final InitialTableStatus initialTableStatus;
	private ArrayList<Card> cards;
	private Card[] playedCard = new Card[4];
	private int nextToPlay = -1;
	private int turn;

	private Semaphore playNextCardLock;
	private CardPlayingThread cardPlayingThread;

	public DummyLoggerBotNotifyer(InitialTableStatus initialTableStatus,
			TableInterface singleTableManager, String userName) {
		this.initialTableStatus = initialTableStatus;
		this.singleTableManager = singleTableManager;
		this.userName = userName;
		playNextCardLock = new Semaphore(0);
		cardPlayingThread = new CardPlayingThread(playNextCardLock, this);
	}

	@Override
	public void notifyGameEnded(int[] matchPoints, int[] playersTotalPoint)
			throws RemoteException {
		System.out.println("\n" + userName + ": "
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + Arrays.toString(matchPoints) + ", "
				+ Arrays.toString(playersTotalPoint) + ")");
		cardPlayingThread.setEndedGame();
		playNextCardLock.release();
	}

	@Override
	public void notifyGameStarted(Card[] cards) throws RemoteException {
		System.out.println("\n" + userName + ": "
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + Arrays.toString(cards) + ")");

		if (turn != 0)
			throw new Error();
		this.cards = new ArrayList<Card>(Arrays.asList(cards));
		cardPlayingThread.start();
	}

	@Override
	public void notifyLocalChatMessage(ChatMessage message)
			throws RemoteException {
		System.out.println("\n" + userName + ": "
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + message + ")");
	}

	@Override
	public void notifyPassedCards(Card[] cards) throws RemoteException {
		System.out.println("\n" + userName + ": "
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + Arrays.toString(cards) + ")");
		this.cards.addAll(Arrays.asList(cards));
		for (Card card : this.cards) {
			if (card.equals(CardsManager.twoOfClubs)) {
				playNextCardLock.release();
			}
		}
	}

	@Override
	public void notifyPlayedCard(Card card, int playerPosition)
			throws RemoteException {
		System.out.println("\n" + userName + ": "
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + card + ", " + playerPosition + ")");
		playedCard[playerPosition] = card;
		if (playerPosition == 3) {
			playNextCardLock.release();
		}
	}

	@Override
	public void notifyPlayerJoined(String name, boolean isBot, int point,
			int position) throws RemoteException {
		System.out.println("\n" + userName + ": "
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + name + ", " + isBot + "," + point + "," + position
				+ ")");
		if (name == null || position < 1 || position > 3)
			throw new IllegalArgumentException();
		if (initialTableStatus.opponents[position] != null)
			throw new IllegalArgumentException();
		initialTableStatus.opponents[position] = name;
		initialTableStatus.playerScores[position] = position;
		initialTableStatus.whoIsBot[position] = isBot;
	}

	@Override
	public void notifyPlayerLeft(String name) throws RemoteException {
		System.out.println("\n" + userName + ": "
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + name + ")");
		int position = 0;
		while (!name.equals(initialTableStatus.opponents[position]))
			position++;
		if (position == 3)
			throw new IllegalArgumentException("Player not found " + name);
		initialTableStatus.opponents[position] = null;
	}

	@Override
	public void playNextCard() {
		try {
			playedCard[0] = cards.remove(0);
			singleTableManager.playCard(userName, playedCard[0]);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalMoveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void passCards() throws RemoteException {
		Card[] cardsToPass = new Card[3];
		for (int i = 0; i < 3; i++)
			cardsToPass[i] = cards.remove(0);
		singleTableManager.passCards(userName, cardsToPass);
	}
}
