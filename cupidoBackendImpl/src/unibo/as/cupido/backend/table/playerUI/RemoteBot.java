package unibo.as.cupido.backend.table.playerUI;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;

import unibo.as.cupido.backend.table.CardsManager;
import unibo.as.cupido.common.interfaces.TableInterface;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.Card.Suit;
import unibo.as.cupido.common.structures.ChatMessage;
import unibo.as.cupido.common.structures.InitialTableStatus;

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
	private int turn = 0;
	private int playedCardCount = 0;
	private int firstDealer = -1;
	private boolean brokenHearted = false;
	private boolean ableToPlay = false;
	private boolean ableToPass = false;
	PrintWriter out;
	private final Object lock = new Object();
	private int points = 0;

	public RemoteBot(InitialTableStatus initialTableStatus,
			TableInterface singleTableManager, final String userName)
			throws IOException {
		this.initialTableStatus = initialTableStatus;
		this.singleTableManager = singleTableManager;
		this.userName = userName;

		File outputFile = new File("cupidoBackendImpl/botlog/remote/"
				+ userName);
		outputFile.delete();
		outputFile.createNewFile();
		out = new PrintWriter(new FileWriter(outputFile));
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.err.println("shuting down remote user " + userName);
				out.close();
			}
		});

	}

	@Override
	public synchronized void addBot(int position) throws RemoteException {
		if (position < 0 || position > 2
				|| initialTableStatus.opponents[position] != null)
			throw new IllegalArgumentException("illegal position " + position
					+ " " + initialTableStatus.opponents[position]);
		initialTableStatus.opponents[position] = "_bot." + userName + "."
				+ position;
		initialTableStatus.whoIsBot[position] = true;
	}

	private ArrayList<Card> chooseValidCards() {
		ArrayList<Card> validCards = new ArrayList<Card>(13);
		if (cards.contains(CardsManager.twoOfClubs)) {
			validCards.add(CardsManager.twoOfClubs);
			return validCards;
		}

		if (playedCardCount == 0) {
			if (!brokenHearted) {
				for (int i = 0; i < cards.size(); i++) {
					if (cards.get(i).suit != Card.Suit.HEARTS) {
						validCards.add(cards.get(i));
					}
				}
			}
			if (validCards.size() == 0) {
				validCards.addAll(cards);
			}
			return validCards;
		}
		if (playedCard[firstDealer] == null) {
			throw new Error("owned:" + cards.toString() + " first:"
					+ firstDealer + " played:" + Arrays.toString(playedCard)
					+ " count:" + playedCardCount);
		}
		for (int i = 0; i < cards.size(); i++) {
			if (cards.get(i).suit == playedCard[firstDealer].suit) {
				validCards.add(cards.get(i));
			}
		}
		if (validCards.size() == 0) {
			validCards.addAll(cards);
		}
		return validCards;
	}

	private Card choseCard() {
		return chooseValidCards().get(0);
	}

	@Override
	public synchronized void createTable() throws RemoteException {
		throw new UnsupportedOperationException("method not implemented yet");
	}

	@Override
	public synchronized void notifyGameEnded(int[] matchPoints,
			int[] playersTotalPoint) {
		out.println("\n" + userName + ": "
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + Arrays.toString(matchPoints) + ", "
				+ Arrays.toString(playersTotalPoint) + ")");

	}

	@Override
	public synchronized void notifyGameStarted(Card[] cards) {
		this.cards = new ArrayList<Card>(13);
		for (int i = 0; i < cards.length; i++)
			this.cards.add(cards[i]);
		if (this.cards.contains(CardsManager.twoOfClubs)) {
			synchronized (lock) {
				ableToPass = true;
				lock.notify();
			}
		}
	}

	@Override
	public synchronized void notifyLocalChatMessage(ChatMessage message) {
		out.println("\nlocal chat message: " + message);
	}

	@Override
	public synchronized void notifyPassedCards(Card[] cards) {
		for (int i = 0; i < cards.length; i++)
			this.cards.add(cards[i]);
		synchronized (lock) {
			if (!ableToPass) {
				ableToPass = true;
				lock.notify();
			}
		}
		if (this.cards.contains(CardsManager.twoOfClubs)) {
			synchronized (lock) {
				ableToPlay = true;
				lock.notify();
			}
			firstDealer = 3;
		}
		out.println("\nplay starts. " + userName + " cards are:"
				+ this.cards.toString());
	}

	@Override
	public synchronized void notifyPlayedCard(Card card, int playerPosition) {
		out.println("\n" + userName + " player " + playerPosition
				+ " played card " + card);
		out.println(" count:" + playedCardCount + " turn:" + turn + " first:"
				+ firstDealer + " broken hearted " + brokenHearted
				+ " turn cards:" + Arrays.toString(playedCard));

		setCardPlayed(card, playerPosition);

		out.println(" count:" + playedCardCount + " turn:" + turn + " first:"
				+ firstDealer + " broken hearted " + brokenHearted
				+ " turn cards:" + Arrays.toString(playedCard));
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
		int position = 0;
		while (!name.equals(initialTableStatus.opponents[position]))
			position++;
		if (position == 3)
			throw new IllegalArgumentException("Player not found " + name);
		initialTableStatus.opponents[position] = null;
		out.println(initialTableStatus);
	}

	@Override
	public synchronized void passCards() {
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
	public synchronized void playNextCard() {
		try {
			synchronized (lock) {
				while (!ableToPlay) {
					lock.wait();
				}
				ableToPlay = false;

				out.println("\n" + userName + " plays ");
				out.println(" count:" + playedCardCount + " turn:" + turn
						+ " first:" + firstDealer + " broken hearted "
						+ brokenHearted + " turn cards:"
						+ Arrays.toString(playedCard) + "\n owned "
						+ cards.toString());

				/** choose a valid card */
				Card cardToPlay = choseCard();

				/** play chosen card */
				singleTableManager.playCard(userName, cardToPlay);

				/** update status */
				setCardPlayed(cardToPlay, 3);

				out.println(" count:" + playedCardCount + " turn:" + turn
						+ " first:" + firstDealer + " broken hearted "
						+ brokenHearted + " turn cards:"
						+ Arrays.toString(playedCard) + " played " + cardToPlay
						+ "\n owned " + cards.toString());

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void setCardPlayed(Card card, int playerPosition) {
		if (firstDealer == -1) {
			firstDealer = playerPosition;
		}
		if (((firstDealer + playedCardCount + 4) % 4) != playerPosition) {
			throw new IllegalStateException(" current player should be "
					+ ((firstDealer + playedCardCount + 4) % 4)
					+ " instead is " + playerPosition + " " + userName
					+ " first: " + firstDealer + " count: " + playedCardCount);
		}
		if (card.suit == Suit.HEARTS) {
			brokenHearted = true;
		}
		if (playerPosition == 3) {
			cards.remove(card);
		}
		playedCard[playerPosition] = card;
		playedCardCount++;
		if (playedCardCount == 4) {
			firstDealer = CardsManager.whoWins(playedCard, firstDealer);
			playedCardCount = 0;
			turn++;
			if (firstDealer == 3) {
				synchronized (lock) {
					ableToPlay = true;
					lock.notify();
				}
				for (Card c : playedCard) {
					if (c.suit == Suit.HEARTS)
						points++;
					else if (c.equals(CardsManager.twoOfClubs))
						points += 5;
				}
			}
			Arrays.fill(playedCard, null);
		} else {
			if (playerPosition == 2) {
				synchronized (lock) {
					ableToPlay = true;
					lock.notify();
				}
			}
		}
	}

}
