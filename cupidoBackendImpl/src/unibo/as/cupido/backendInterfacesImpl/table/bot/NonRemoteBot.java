package unibo.as.cupido.backendInterfacesImpl.table.bot;

import java.util.ArrayList;
import java.util.Arrays;
import unibo.as.cupido.backendInterfacesImpl.table.CardsManager;
import unibo.as.cupido.common.interfaces.TableInterface;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.Card.Suit;
import unibo.as.cupido.common.structures.InitialTableStatus;

public class NonRemoteBot implements BotNotificationInterface {

	private final String botName;
	private final TableInterface singleTableManager;
	private final InitialTableStatus initialTableStatus;
	private ArrayList<Card> cards;
	private Card[] playedCard = new Card[4];
	private int point;
	private final NonRemoteBotCardPlayingThread cardPlayingThread;
	private int turn = 0;
	private int playedCardCount = 0;
	private int firstDealer = -1;
	private boolean alreadyGotCards = false;

	public NonRemoteBot(String botName, InitialTableStatus initialTableStatus,
			TableInterface singleTableManager) {
		this.botName = botName;
		this.initialTableStatus = initialTableStatus;
		cardPlayingThread = new NonRemoteBotCardPlayingThread(this, botName);
		cardPlayingThread.start();
		this.singleTableManager = singleTableManager;
	}

	@Override
	public synchronized void notifyGameEnded(int[] matchPoints,
			int[] playersTotalPoint) {
		System.out.println("\n" + botName + ": "
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + Arrays.toString(matchPoints) + ", "
				+ Arrays.toString(playersTotalPoint) + ")");
		cardPlayingThread.interrupt();
	}

	@Override
	public synchronized void notifyGameStarted(Card[] cards) {
		System.err.println("\n" + botName + ": "
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + Arrays.toString(cards) + "). initial table status "
				+ initialTableStatus);
		System.err.flush();
		this.cards = new ArrayList<Card>(4);
		this.cards.addAll(Arrays.asList(cards));
		if (this.cards.contains(CardsManager.twoOfClubs)) {
			cardPlayingThread.setAbleToPass();
		}
	}

	@Override
	public synchronized void notifyPassedCards(Card[] cards) {
		System.out.println("\n" + botName + ": "
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + Arrays.toString(cards) + ")");
		if (alreadyGotCards)
			throw new IllegalArgumentException("passing cards twice to player "
					+ botName);
		alreadyGotCards = true;
		this.cards.addAll(Arrays.asList(cards));
		cardPlayingThread.setAbleToPass();
		if (this.cards.contains(CardsManager.twoOfClubs)) {
			cardPlayingThread.setAbleToPlay();
			firstDealer = 3;
		}
	}

	@Override
	public synchronized void notifyPlayedCard(Card card, int playerPosition) {
		System.out.println("\nNonRemoteBot inizio" + botName + ": "
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + card + ", " + playerPosition + ") played:"
				+ Arrays.toString(playedCard) + " count:" + playedCardCount
				+ " turn:" + turn + " first:" + firstDealer);
		if (firstDealer == -1) {
			firstDealer = playerPosition;
		}
		playedCard[playerPosition] = card;
		playedCardCount++;
		if (playedCardCount == 4) {
			firstDealer = CardsManager.whoWins(playedCard, firstDealer);
		} else if (playedCardCount == 5) {
			playedCardCount = 1;
			turn++;
		}
		System.out.println("\nNonRemoteBot fine" + botName + ": "
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + card + ", " + playerPosition + ") played:"
				+ Arrays.toString(playedCard) + " count:" + playedCardCount
				+ " turn:" + turn + " first:" + firstDealer);
		if (playerPosition == 2) {
			cardPlayingThread.setAbleToPlay();
		}
	}

	@Override
	public synchronized void notifyPlayerJoined(String name, boolean isBot,
			int point, int position) {

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
	}

	@Override
	public synchronized void notifyPlayerLeft(String name) {
		System.out.print("\n" + botName + ": "
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
			singleTableManager.passCards(botName, cardsToPass);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public synchronized void playNextCard() {
		try {
			System.err.println("play next card start. played:"
					+ Arrays.toString(playedCard) + " count:" + playedCardCount
					+ " turn:" + turn + " first:" + firstDealer);
			if (cards.remove(CardsManager.twoOfClubs)) {
				playedCard[3] = CardsManager.twoOfClubs;
			} else {
				if (playedCardCount != 4) {
					Suit firstSuit = playedCard[firstDealer].suit;
					for (int i = 0; i < cards.size(); i++)
						if (cards.get(i).suit == firstSuit)
							playedCard[3] = cards.remove(i);
				} else {
					playedCard[3] = cards.remove(0);
				}
			}
			singleTableManager.playCard(botName, playedCard[3]);
			playedCardCount++;
			if (playedCardCount == 5) {
				playedCardCount = 1;
				turn++;
			}
			System.err.println("play next card end. played:"
					+ Arrays.toString(playedCard) + " count:" + playedCardCount
					+ " turn:" + turn + " first:" + firstDealer);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
