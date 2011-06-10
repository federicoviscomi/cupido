package unibo.as.cupido.backendInterfacesImpl;

import java.util.Comparator;
import java.util.Random;

import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.Card.Suit;

public class CardsManager4 {

	private Card[][] cards;
	private Card[][] allPassedCards;
	private int passed;
	Card[] cardPlayed;
	private int playedCardsCount;

	/* the player who starts the hand */
	int playingFirst;
	private Comparator<Card> winnerComparator = new Comparator<Card>() {
		@Override
		public int compare(Card o1, Card o2) {
			int firstSuit = cardPlayed[playingFirst].suit.ordinal();
			int value1 = (o1.suit.ordinal() == firstSuit ? 1 : 0) * (o1.value == 1 ? 14 : o1.value);
			int value2 = (o2.suit.ordinal() == firstSuit ? 1 : 0) * (o2.value == 1 ? 14 : o2.value);
			return value1 - value2;
		}
	};
	private int currentHandPoints;

	private static final Comparator<Card> cardsComparator = new Comparator<Card>() {
		@Override
		public int compare(Card o1, Card o2) {
			int value1, value2;
			if (o1 == null) {
				value1 = -1;
			} else {
				value1 = o1.suit.ordinal() * 13 + (o1.value == 1 ? 14 : o1.value);
			}
			if (o2 == null) {
				value2 = -1;
			} else {
				value2 = o2.suit.ordinal() * 13 + (o2.value == 1 ? 14 : o2.value);
			}
			return value1 - value2;
		}
	};
	private static final Card twoOfClubs = new Card(2, Card.Suit.CLUBS);
	private static final Card donnaDiPicche = new Card(12, Card.Suit.SPADES);

	public CardsManager4() {
		cards = new Card[4][13];
		allPassedCards = new Card[4][];
		daiCarte();
		playingFirst = whoHasTwoOfClubs();
	}

	public boolean allPlayerPassedCards() {
		return passed == 4;
	}

	public boolean allPlayerPlayedCards() {
		return playedCardsCount == 4;
	}

	private void daiCarte() {
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 13; j++) {
				cards[i][j] = new Card();
				cards[i][j].suit = Card.Suit.values()[i];
				cards[i][j].value = j + 1;
			}
		}

		Random random = new Random(System.currentTimeMillis());
		for (int i = 0; i < 26; i++) {
			int randPlayer1 = random.nextInt(4);
			int randPlayer2 = random.nextInt(4);
			int randCard1 = random.nextInt(13);
			int randCard2 = random.nextInt(13);
			Card temp = cards[randPlayer1][randCard1];
			cards[randPlayer1][randCard1] = cards[randPlayer2][randCard2];
			cards[randPlayer2][randCard2] = temp;
		}

		for (int i = 0; i < 4; i++) {
			sortCards(cards[i]);
		}
	}

	private int getCardPoints(Card card) {
		if (card.suit.ordinal() == Card.Suit.HEARTS.ordinal()) {
			return 1;
		}
		if (card.suit.ordinal() == Card.Suit.CLUBS.ordinal() && card.value == 12) {
			return 13;
		}
		return 0;
	}

	public void passCards() {
		for (int i = 0, from = 3, nextCard = 0; i < 4; i++, from = (from++) % 4) {
			for (int j = 0; j < 13; j++) {
				if (cards[i][j] == null)
					cards[i][j] = allPassedCards[from][nextCard++];
			}
			sortCards(cards[i]);
		}
	}

	public void playCard(int playerPosition, Card card) {
		/* checks it the player owns the card */
		int p = java.util.Arrays.binarySearch(cards[playerPosition], card, cardsComparator);
		if (p < 0)
			throw new IllegalArgumentException("User " + playerPosition + " does not own card " + card);
		cards[playerPosition][p] = null;
		cardPlayed[playerPosition] = card;
		if (playedCardsCount == 4) {
			/* decide who takes this hand cards */
			Card[] cardPlayedCopy = java.util.Arrays.copyOf(cardPlayed, 4);
			java.util.Arrays.sort(cardPlayedCopy, winnerComparator);
			Card winningCard = cardPlayedCopy[4];
			for (int i = 0; i < 4; i++) {
				if (cardPlayed[i].equals(winningCard)) {
					playingFirst = i;
				}
			}

			/* calculate this hand point */
			for (int i = 0; i < 4; i++) {
				currentHandPoints = currentHandPoints + getCardPoints(cardPlayed[i]);
			}
		}
		playedCardsCount = (playedCardsCount + 1) % 4;
	}

	public void print(PlayersManager playersManager) {
		for (int i = 0; i < 4; i++) {
			System.out.format("\n name=%10.10s, isBot=%5.5b, cardsInHand=%2.2s, score=%3.3s, cards= ",
					playersManager.getPlayerName(i), playersManager.isBot(i), "playersManager.numOfCardsInHand(i)",
					playersManager.getScore(i));
			System.out.print(java.util.Arrays.toString(cards[i]));
		}
	}

	public void setCardPassing(int position, Card[] passedCards) throws IllegalArgumentException {
		/* check if the player owns the cards he wants to pass */
		sortCards(passedCards);
		for (int i = 0; i < 3; i++) {
			int p = java.util.Arrays.binarySearch(cards[position], passedCards[i], cardsComparator);
			if (p < 0)
				throw new IllegalArgumentException();
			cards[position][p] = null;
		}
		this.allPassedCards[position] = passedCards;
		passed++;
	}

	private void sortCards(Card[] cards) {
		java.util.Arrays.sort(cards, cardsComparator);
	}

	private int whoHasTwoOfClubs() {
		for (int i = 0; i < 4; i++) {
			if (java.util.Arrays.binarySearch(cards[i], twoOfClubs, cardsComparator) >= 0) {
				return i;
			}
		}
		return -1;
	}

	public int getWinner() {
		return playingFirst;
	}

	public int getPoints() {
		return currentHandPoints;
	}
}
