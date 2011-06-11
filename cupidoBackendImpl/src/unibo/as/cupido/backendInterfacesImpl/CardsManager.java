package unibo.as.cupido.backendInterfacesImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.IllegalMoveException;
import unibo.as.cupido.backendInterfacesImpl.SingleTableManager.GameStatus;

public class CardsManager {

	private static final Comparator<Card> cardsComparator = new Comparator<Card>() {
		@Override
		public int compare(Card o1, Card o2) {
			return (o1.suit.ordinal() * 13 + (o1.value == 1 ? 14 : o1.value))
					- (o2.suit.ordinal() * 13 + (o2.value == 1 ? 14 : o2.value));
		}
	};
	
	private static final Card twoOfClubs = new Card(2, Card.Suit.CLUBS);
	private Card[][] allPassedCards;
	Card[] cardPlayed;
	ArrayList<Card>[] cards;
	private int currentTurnPoints;
	private int playedCardsCount;
	int playingFirst;
	int turn;
	boolean brokenHearted;
	private final Comparator<Card> winnerComparator = new Comparator<Card>() {
		@Override
		public int compare(Card o1, Card o2) {
			int firstSuit = cardPlayed[playingFirst].suit.ordinal();
			return ((o1.suit.ordinal() == firstSuit ? 1 : 0) * (o1.value == 1 ? 14 : o1.value))
					- ((o2.suit.ordinal() == firstSuit ? 1 : 0) * (o2.value == 1 ? 14 : o2.value));
		}
	};

	@SuppressWarnings("unchecked")
	public CardsManager() {
		dealCards();
		allPassedCards = new Card[4][];
		playingFirst = whoHasTwoOfClubs();
	}

	private void dealCards() {
		cards = new ArrayList[4];
		for (int i = 0; i < 4; i++)
			cards[i] = new ArrayList<Card>(13);
		Card[] mazzo = new Card[52];
		for (int i = 0; i < 52; i++) {
			mazzo[i] = new Card(i % 13 + 1, Card.Suit.values()[i % 4]);
		}
		Collections.shuffle(Arrays.asList(mazzo), new Random(System.currentTimeMillis()));
		for (int i = 0; i < 52; i++) {
			cards[i % 4].add(mazzo[i]);
		}		
	}

	public boolean allPlayerPassedCards() {
		return Arrays.asList(allPassedCards).contains(null);
	}

	public boolean allPlayerPlayedCards() {
		return playedCardsCount == 4;
	}

	public static void main(String args[]) {
		new CardsManager().print();
	}

	private void print() {
		for (int i = 0; i < 4; i++) {
			Collections.sort(cards[i], cardsComparator);
			System.out.println((cards[i]));
		}
	}


	public int getPoints() {
		return currentTurnPoints;
	}

	public int getWinner() {
		return playingFirst;
	}

	public void passCards() {
		for (int i = 0; i < 4; i++) {
			cards[i].addAll(Arrays.asList(allPassedCards[(i - 1) % 4]));
		}
	}

	public void playCard(int playerPosition, Card card) throws IllegalMoveException {
		if (!cards[playerPosition].remove(card)) {
			throw new IllegalArgumentException("User " + playerPosition + " does not own card " + card);
		}
		if (turn == 0 && playerPosition == playingFirst) {
			if (!card.equals(twoOfClubs)) {
				throw new IllegalMoveException("First card played has to be two of clubs");
			}
		}
		if (playerPosition != playingFirst) {
			// Collections.
			// if (cards[playerPosition].)
		}
		cardPlayed[playerPosition] = card;
		if (playedCardsCount == 4) {
			/* decide who takes this hand cards and calculate this hand points */
			int maxPosition = playingFirst;

			for (int i = 0; i < 4; i++) {
				if (winnerComparator.compare(cardPlayed[i], cardPlayed[maxPosition]) > 0)
					maxPosition = i;
				if (cardPlayed[i].suit.ordinal() == Card.Suit.HEARTS.ordinal()) {
					currentTurnPoints++;
				} else if (cardPlayed[i].suit.ordinal() == Card.Suit.SPADES.ordinal() && cardPlayed[i].value == 12) {
					currentTurnPoints += 5;
				}
			}
			turn++;
		}
		playedCardsCount = (playedCardsCount + 1) % 4 + 1;
	}

	@SuppressWarnings("boxing")
	public void print(PlayersManager playersManager) {
		for (int i = 0; i < 4; i++) {
			System.out.format("\n name=%10.10s, isBot=%5.5b, cardsInHand=%2.2s, score=%3.3s, cards= ",
					playersManager.getPlayerName(i), playersManager.isBot(i), "playersManager.numOfCardsInHand(i)",
					playersManager.getScore(i));
			System.out.print(java.util.Arrays.toString(cards[i].toArray()));
		}
	}

	public void setCardPassing(int position, Card[] passedCards) throws IllegalArgumentException {
		// cards[position].removeAll(Arrays.asList(passedCards));
		// cards[(position + 1) % 4].addAll(Arrays.asList(passedCards));
		for (int i = 0; i < 3; i++) {
			if (!cards[position].remove(passedCards[i]))
				throw new IllegalArgumentException();
		}
		allPassedCards[position] = passedCards;
	}

	private int whoHasTwoOfClubs() {
		for (int i = 0; i < 4; i++) {
			if (cards[i].contains(twoOfClubs))
				return i;
		}
		return -1;
	}
}
