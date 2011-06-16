package unibo.as.cupido.backendInterfacesImpl.table;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteStub;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

import unibo.as.cupido.backendInterfaces.GlobalTableManagerInterface;
import unibo.as.cupido.backendInterfaces.LocalTableManagerInterface;
import unibo.as.cupido.backendInterfaces.ServletNotificationsInterface;
import unibo.as.cupido.backendInterfaces.TableInterface;
import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.ChatMessage;
import unibo.as.cupido.backendInterfaces.common.InitialTableStatus;
import unibo.as.cupido.backendInterfaces.common.TableInfoForClient;
import unibo.as.cupido.backendInterfaces.exception.IllegalMoveException;
import unibo.as.cupido.backendInterfacesImpl.table.bot.Bot;
import unibo.as.cupido.backendInterfacesImpl.table.bot.CardPlayingThread;

public class DummyPlayerJoiner implements Serializable, Bot,
		ServletNotificationsInterface {

	public static void main(String[] args) throws RemoteException {
		DummyPlayerJoiner dummyPlayerJoiner = new DummyPlayerJoiner(args[0]);
		// UnicastRemoteObject.exportObject(dummyPlayerJoiner);
		dummyPlayerJoiner.joinATable();
	}

	private final String userName;

	private InitialTableStatus initialTableStatus;
	private TableInterface table;
	private GlobalTableManagerInterface gtm;

	private ArrayList<Card> cards;

	private Semaphore playNextCardLock = new Semaphore(0);

	public DummyPlayerJoiner(String userName) {
		this.userName = userName;
		try {
			Registry registry = LocateRegistry.getRegistry();
			gtm = (GlobalTableManagerInterface) registry
					.lookup(GlobalTableManagerInterface.globalTableManagerName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void joinATable() {
		try {
			TableInfoForClient tifc = gtm.getTableList().iterator().next();
			LocalTableManagerInterface ltmInterface = gtm
					.getLTMInterface(tifc.tableDescriptor.ltmId);
			table = ltmInterface.getTable(tifc.tableDescriptor.id);
			initialTableStatus = table.joinTable(userName,
					(ServletNotificationsInterface) UnicastRemoteObject
							.exportObject(this));
			System.out.println("DummiPlayerJoiner: " + userName + " "
					+ initialTableStatus);
			System.out.flush();
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
		System.out.println("\nDummyPlayerJoiner " + userName + ": "
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
		System.out.print("\nDummyPlayerJoiner inizio " + userName + "."
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + name + ", " + isBot + "," + point + "," + position
				+ ")\n initial table status " + initialTableStatus + " ");

		if (name == null || position < 0 || position > 2)
			throw new IllegalArgumentException();
		if (initialTableStatus.opponents[position] != null)
			throw new IllegalArgumentException("Unable to add player" + name
					+ " beacuse ITS: " + initialTableStatus
					+ " already contains a player in position " + position);

		initialTableStatus.opponents[position] = name;
		initialTableStatus.playerScores[position] = point;
		initialTableStatus.whoIsBot[position] = isBot;

		System.out.print("\nDummyPlayerJoiner fine " + userName + "."
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + name + ", " + isBot + "," + point + "," + position
				+ ")\n initial table status " + initialTableStatus + " ");
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
	public void passCards() throws RemoteException {
		Card[] cards = new Card[3];
		for (int i = 0; i < 3; i++)
			cards[i] = this.cards.remove(0);
		table.passCards(userName, cards);
	}

	@Override
	public void playNextCard() throws RemoteException {
		try {
			table.playCard(userName, cards.remove(0));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
