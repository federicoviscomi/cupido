package unibo.as.cupido.backendInterfacesImpl.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

import unibo.as.cupido.backendInterfaces.TableInterface;
import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.ChatMessage;
import unibo.as.cupido.backendInterfaces.common.InitialTableStatus;
import unibo.as.cupido.backendInterfacesImpl.table.bot.ServletNotificationsInterfaceNotRemote;

public class NonRemoteBot implements ServletNotificationsInterfaceNotRemote {

	private final int position;
	private final String userName;
	private final TableInterface singleTableManager;
	private final InitialTableStatus initialTableStatus;
	private ArrayList<Card> cards;
	private Card[] playedCard = new Card[4];
	private int point;
	private final Semaphore playNextCardLock;
	private final Semaphore passCardsLock;
	private final NonRemoteBotCardPlayingThread cardPlayingThread;
	/**
	 * </code>firstDealer == 0</code> means this player is the first dealer.
	 * Otherwise first dealer is the player in position
	 * </code>firstDealer-1</code> relative to this player
	 */
	protected int firstDealer = -1;

	public NonRemoteBot(String userName, int position,
			InitialTableStatus initialTableStatus,
			TableInterface singleTableManager) {
		this.userName = userName;
		// TODO Auto-generated constructor stub
		this.position = position;
		playNextCardLock = new Semaphore(0);
		passCardsLock = new Semaphore(0);
		cardPlayingThread = new NonRemoteBotCardPlayingThread(playNextCardLock,
				passCardsLock, this, "_bot." + userName + "." + position);
		cardPlayingThread.start();
		this.initialTableStatus = initialTableStatus;
		this.singleTableManager = singleTableManager;
		System.out.println("\n constructor " + userName + ". "
				+ initialTableStatus);
	}

	@Override
	public synchronized void notifyGameEnded(int[] matchPoints,
			int[] playersTotalPoint) {
		System.out.println("\n" + userName + ": "
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + Arrays.toString(matchPoints) + ", "
				+ Arrays.toString(playersTotalPoint) + ")");
		cardPlayingThread.setEndedGame();
		playNextCardLock.release();
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
		passCardsLock.release();
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
				playNextCardLock.release();
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
			playNextCardLock.release();
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
			throw new IllegalArgumentException("illegal position " + position
					+ ". " + name + " " + isBot);
		if (initialTableStatus.opponents[position] != null)
			throw new IllegalArgumentException("Unable to add player" + name
					+ " beacuse ITS: " + initialTableStatus
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