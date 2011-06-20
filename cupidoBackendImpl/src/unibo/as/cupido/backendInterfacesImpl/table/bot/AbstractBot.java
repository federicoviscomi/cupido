package unibo.as.cupido.backendInterfacesImpl.table.bot;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

import javax.management.openmbean.OpenDataException;

import unibo.as.cupido.common.interfaces.ServletNotificationsInterface;
import unibo.as.cupido.common.interfaces.TableInterface;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.ChatMessage;
import unibo.as.cupido.common.structures.InitialTableStatus;
import unibo.as.cupido.backendInterfacesImpl.table.CardsManager;

public class AbstractBot implements Bot, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5795667604185475447L;
	final String userName;
	TableInterface singleTableManager;
	InitialTableStatus initialTableStatus;
	ArrayList<Card> cards;
	Card[] playedCard = new Card[4];
	int point;

	/**
	 * </code>firstDealer == 0</code> means this player is the first dealer.
	 * Otherwise first dealer is the player in position
	 * </code>firstDealer-1</code> relative to this player
	 */
	protected int firstDealer = -1;

	public AbstractBot(InitialTableStatus initialTableStatus,
			TableInterface singleTableManager, String userName) {
		this.initialTableStatus = initialTableStatus;
		this.singleTableManager = singleTableManager;
		this.userName = userName;

		System.out.println("abstarct bot constructor " + userName + ". "
				+ initialTableStatus);
	}

	public AbstractBot(String userName) {
		this(new InitialTableStatus(new String[3], new int[3], new boolean[3]),
				null, userName);
		for (int i = 0; i < 3; i++)
			initialTableStatus.opponents[i] = null;
	}

	@Override
	public void addBot(int position) throws RemoteException {
		if (position < 0 || position > 2
				|| initialTableStatus.opponents[position] != null)
			throw new IllegalArgumentException("illegal position " + position
					+ " " + initialTableStatus.opponents[position]);
		initialTableStatus.opponents[position] = "_bot." + userName + "."
				+ position;
		initialTableStatus.whoIsBot[position] = true;
	}

	@Override
	public void createTable() throws RemoteException {
		throw new UnsupportedOperationException("method not implemented yet");
	}

	@Override
	public synchronized void notifyGameEnded(int[] matchPoints,
			int[] playersTotalPoint) {
		System.out.println("\n" + userName + ": "
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + Arrays.toString(matchPoints) + ", "
				+ Arrays.toString(playersTotalPoint) + ")");

	}

	@Override
	public synchronized void notifyGameStarted(Card[] cards) {
		System.err.println("\n" + userName + ": "
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + Arrays.toString(cards) + "). initial table status "
				+ initialTableStatus);
		System.err.flush();
		this.cards = new ArrayList<Card>(4);
		this.cards.addAll(Arrays.asList(cards));
		if (this.cards.contains(CardsManager.twoOfClubs)) {
			firstDealer = 0;
		}

	}

	@Override
	public synchronized void notifyLocalChatMessage(ChatMessage message) {
		System.out.println("\n" + userName + ": "
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + message + ")");
	}

	@Override
	public synchronized void notifyPassedCards(Card[] cards) {
		System.out.println("\n" + userName + ": "
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + Arrays.toString(cards) + ")");
		this.cards.addAll(Arrays.asList(cards));
		for (Card card : this.cards) {
			if (card.equals(CardsManager.twoOfClubs)) {
				System.err.println("\n:\n:\n:\n:");

				return;
			}
		}
	}

	@Override
	public synchronized void notifyPlayedCard(Card card, int playerPosition) {
		System.out.println("\n" + userName + ": "
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + card + ", " + playerPosition + ")");
		playedCard[playerPosition] = card;
		if (playerPosition == 2) {
		
		}
	}

	@Override
	public synchronized void notifyPlayerJoined(String name, boolean isBot,
			int point, int position) {
		System.out.println("\n AbstractBot inizio " + userName + "."
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + name + ", " + isBot + "," + point + "," + position
				+ ")\n table status " + initialTableStatus);
		if (name == null || position < 0 || position > 2)
			throw new IllegalArgumentException();
		if (initialTableStatus.opponents[position] != null)
			throw new IllegalArgumentException("Unable to add player " + name
					+ " in player " + userName + " beacuse ITS: "
					+ initialTableStatus
					+ " already contains a player in position " + position);
		initialTableStatus.opponents[position] = name;
		initialTableStatus.playerScores[position] = position;
		initialTableStatus.whoIsBot[position] = isBot;

		System.out.println("\n AbstractBot fine " + userName + "."
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + name + ", " + isBot + "," + point + "," + position
				+ ")\n table status " + initialTableStatus);
	}

	@Override
	public synchronized void notifyPlayerLeft(String name) {
		System.out.print("\n" + userName + ": "
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + name + ")");
		int position = 0;
		while (!name.equals(initialTableStatus.opponents[position]))
			position++;
		if (position == 3)
			throw new IllegalArgumentException("Player not found " + name);
		initialTableStatus.opponents[position] = null;
		System.out.println(initialTableStatus);
	}

	@Override
	public synchronized void passCards() {
		Card[] cardsToPass = new Card[3];
		for (int i = 0; i < 3; i++)
			cardsToPass[i] = cards.remove(0);
		try {
			singleTableManager.passCards(userName, cardsToPass);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public synchronized void playNextCard() {
		try {
			if (cards.remove(CardsManager.twoOfClubs)) {
				playedCard[0] = CardsManager.twoOfClubs;
			} else {
				playedCard[0] = cards.remove(0);
			}
			singleTableManager.playCard(userName, playedCard[0]);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
