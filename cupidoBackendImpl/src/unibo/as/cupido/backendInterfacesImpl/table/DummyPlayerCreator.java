package unibo.as.cupido.backendInterfacesImpl.table;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

import unibo.as.cupido.backendInterfaces.GlobalTableManagerInterface;
import unibo.as.cupido.backendInterfaces.ServletNotificationsInterface;
import unibo.as.cupido.backendInterfaces.TableInterface;
import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.ChatMessage;
import unibo.as.cupido.backendInterfaces.common.InitialTableStatus;
import unibo.as.cupido.backendInterfaces.exception.FullTableException;
import unibo.as.cupido.backendInterfaces.exception.IllegalMoveException;
import unibo.as.cupido.backendInterfaces.exception.NotCreatorException;
import unibo.as.cupido.backendInterfaces.exception.PositionFullException;
import unibo.as.cupido.backendInterfacesImpl.table.bot.Bot;
import unibo.as.cupido.backendInterfacesImpl.table.bot.CardPlayingThread;

public class DummyPlayerCreator implements Serializable, Bot,
		ServletNotificationsInterface {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8439829734552972246L;
	private Semaphore playNextCardLock = new Semaphore(0);

	public static void main(String[] args) throws Exception {
		DummyPlayerCreator dummyPlayerCreator = new DummyPlayerCreator("Owner");
		UnicastRemoteObject.exportObject(dummyPlayerCreator);
		dummyPlayerCreator.createTable();
		// 1 refers to absolute position
		dummyPlayerCreator.addBot(1);
	}

	private final String userName;
	private final InitialTableStatus initialTableStatus;
	private TableInterface tableInterface;
	private GlobalTableManagerInterface gtm;
	private ArrayList<Card> cards;

	public DummyPlayerCreator(String userName) {

		this.userName = userName;
		initialTableStatus = new InitialTableStatus();
		initialTableStatus.opponents = new String[3];
		initialTableStatus.playerScores = new int[3];
		initialTableStatus.whoIsBot = new boolean[3];
		try {
			Registry registry = LocateRegistry.getRegistry();
			gtm = (GlobalTableManagerInterface) registry
					.lookup(GlobalTableManagerInterface.globalTableManagerName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private synchronized void addBot(int position) throws RemoteException,
			IllegalArgumentException, IllegalStateException,
			PositionFullException, FullTableException, NotCreatorException {
		tableInterface.addBot(userName, 1);
		position--;
		initialTableStatus.opponents[position] = "_bot." + userName + "."
				+ position;
		initialTableStatus.playerScores[position] = 0;
		initialTableStatus.whoIsBot[position] = true;
		System.out.println(" " + userName + ", " + initialTableStatus);
	}

	private void createTable() {
		try {
			tableInterface = gtm.createTable(userName, this);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public synchronized void notifyGameEnded(int[] matchPoints,
			int[] playersTotalPoint) throws RemoteException {
		System.out.println("\n" + userName + ": "
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + Arrays.toString(matchPoints) + ", "
				+ Arrays.toString(playersTotalPoint) + ")");
	}

	@Override
	public synchronized void notifyGameStarted(Card[] cards)
			throws RemoteException {
		System.out.println("\nDummyLoggerPlayeJoined " + userName + ": "
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + Arrays.toString(cards) + "):" + initialTableStatus);
		this.cards = new ArrayList<Card>(Arrays.asList(cards));
		new CardPlayingThread(playNextCardLock, (Bot) this).start();
	}

	@Override
	public synchronized void notifyLocalChatMessage(ChatMessage message)
			throws RemoteException {
		System.out.println("\n" + userName + ": "
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + message + ")");
	}

	@Override
	public synchronized void notifyPassedCards(Card[] cards)
			throws RemoteException {
		System.out.println("\n" + userName + ": "
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + Arrays.toString(cards) + ")");
	}

	@Override
	public synchronized void notifyPlayedCard(Card card, int playerPosition)
			throws RemoteException {
		System.out.println("\n" + userName + ": "
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + card + ", " + playerPosition + ")");
	}

	@Override
	public synchronized void notifyPlayerJoined(String name, boolean isBot,
			int point, int position) throws RemoteException {
		System.out.print("\n DummyPlayerCreator inizio " + userName + "."
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + name + ", " + isBot + "," + point + "," + position
				+ "): initial table status " + initialTableStatus);

		if (name == null || position < 0 || position > 2)
			throw new IllegalArgumentException();
		if (initialTableStatus.opponents[position] != null)
			throw new IllegalArgumentException("Unable to add player" + name
					+ " beacuse ITS: " + initialTableStatus
					+ " already contains a player in position " + position);

		initialTableStatus.opponents[position] = name;
		initialTableStatus.playerScores[position] = point;
		initialTableStatus.whoIsBot[position] = isBot;

		System.out.print("\n DummyPlayerCreator fine " + userName + "."
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + name + ", " + isBot + "," + point + "," + position
				+ "): initial table status " + initialTableStatus);
	}

	@Override
	public synchronized void notifyPlayerLeft(String name)
			throws RemoteException {
		System.out.println("\n" + userName + ": "
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + name + ")");
		for (int i = 0; i < 3; i++) {
			if (initialTableStatus.opponents[i] != null
					&& initialTableStatus.opponents[i].equals(name))
				initialTableStatus.opponents[i] = null;
		}
		System.out.print(initialTableStatus + "\n");
	}

	@Override
	public synchronized void passCards() throws RemoteException {
		Card[] cardsToPass = new Card[3];
		for (int i = 0; i < 3; i++)
			cardsToPass[i] = cards.remove(0);
		tableInterface.passCards(userName, cardsToPass);
	}

	@Override
	public synchronized void playNextCard() throws RemoteException {
		try {
			tableInterface.playCard(userName, cards.remove(0));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
