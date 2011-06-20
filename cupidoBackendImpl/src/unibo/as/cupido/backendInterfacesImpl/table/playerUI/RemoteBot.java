package unibo.as.cupido.backendInterfacesImpl.table.playerUI;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;

import unibo.as.cupido.common.interfaces.TableInterface;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.ChatMessage;
import unibo.as.cupido.common.structures.InitialTableStatus;
import unibo.as.cupido.common.structures.Card.Suit;
import unibo.as.cupido.backendInterfacesImpl.table.CardsManager;

public class RemoteBot implements Bot, Serializable {

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
	private int turn = 0;
	private int playedCardCount = 0;
	private int firstDealer = -1;

	private boolean ableToPlay = false;
	private boolean ableToPass = false;

	private Object lock = new Object();

	public RemoteBot(InitialTableStatus initialTableStatus,
			TableInterface singleTableManager, String userName) {
		this.initialTableStatus = initialTableStatus;
		this.singleTableManager = singleTableManager;
		this.userName = userName;
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
			synchronized (lock) {
				ableToPass = true;
				lock.notify();
			}
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
		synchronized (lock) {
			if (!ableToPass) {
				ableToPass = true;
				lock.notify();
			}
		}
		for (Card card : this.cards) {
			if (card.equals(CardsManager.twoOfClubs)) {
				synchronized (lock) {
					ableToPlay = true;
					lock.notify();
				}
				firstDealer = 3;
				return;
			}
		}
	}

	@Override
	public synchronized void notifyPlayedCard(Card card, int playerPosition) {
		System.out.println("\nRemoteBot inizio" + userName + ": "
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
		System.out.println("\nRemoteBot fine" + userName + ": "
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + card + ", " + playerPosition + ") played:"
				+ Arrays.toString(playedCard) + " count:" + playedCardCount
				+ " turn:" + turn + " first:" + firstDealer);

		if (playerPosition == 2) {
			synchronized (lock) {
				ableToPlay = true;
				lock.notify();
			}
		}
	}

	@Override
	public synchronized void notifyPlayerJoined(String name, boolean isBot,
			int point, int position) {
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
	public void passCards() {
		try {
			synchronized (lock) {
				while (!ableToPass) {
					lock.wait();
				}
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
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	@Override
	public void playNextCard() {
		try {
			synchronized (lock) {
				while (!ableToPlay) {
					lock.wait();
				}
				ableToPlay = false;
				System.err.println("play next card start. played:"
						+ Arrays.toString(playedCard) + " count:"
						+ playedCardCount + " turn:" + turn + " first:"
						+ firstDealer);
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
				singleTableManager.playCard(userName, playedCard[3]);
				playedCardCount++;
				if (playedCardCount == 5) {
					playedCardCount = 1;
					turn++;
				}
				System.err.println("play next card end. played:"
						+ Arrays.toString(playedCard) + " count:"
						+ playedCardCount + " turn:" + turn + " first:"
						+ firstDealer);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
