package unibo.as.cupido.backendInterfacesImpl;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.Random;

import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.Card.Suit;

public class CardsManager3 {

	Card[] cards;
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
			return (o1.suit.ordinal() * 13 + (o1.value == 1 ? 14 : o1.value))
					- (o2.suit.ordinal() * 13 + (o2.value == 1 ? 14 : o2.value));
		}
	};
	private static final Card twoOfClubs = new Card(2, Card.Suit.CLUBS);
	private static final Card donnaDiPicche = new Card(12, Card.Suit.SPADES);

	public CardsManager3() {
		cards = new Card[52];
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
		for (int i = 0, j = 0; i < 52; i++) {
			cards[i] = new Card();
			cards[i].suit = Card.Suit.values()[j];
			cards[i].value = i % 13;
			if ((i % 13) == 0)
				j++;
		}

		Random random = new Random(System.currentTimeMillis());
		for (int i = 0; i < 26; i++) {
			int rand1 = random.nextInt(52);
			int rand2 = random.nextInt(52);
			Card temp = cards[rand1];
			cards[rand1] = cards[rand2];
			cards[rand2] = temp;
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
		for (int i = 0, nextCard = 0; i < 52; i++, nextCard = nextCard % 3) {
			if (cards[i] == null)
				cards[i] = allPassedCards[((i % 13) - 1) % 4][nextCard++];
		}
	}

	public void playCard(int playerPosition, Card card) {
		/* checks it the player owns the card */
		int p = java.util.Arrays.binarySearch(cards, playerPosition * 13, (playerPosition) * 13, card, cardsComparator);
		if (p < 0)
			throw new IllegalArgumentException("User " + playerPosition + " does not own card " + card);
		cards[p] = null;
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
			System.out.print(java.util.Arrays.toString(cards));
		}
	}

	public void setCardPassing(int position, Card[] passedCards) throws IllegalArgumentException {
		/* check if the player owns the cards he wants to pass */
		sortCards(passedCards);
		for (int i = 0; i < 3; i++) {
			int p = java.util.Arrays.binarySearch(cards, (position) * 13, (position + 1) * 13, passedCards[i],
					cardsComparator);
			if (p < 0)
				throw new IllegalArgumentException();
			cards[p] = null;
		}
		this.allPassedCards[position] = passedCards;
		passed++;
	}

	private void sortCards(Card[] cards) {
		java.util.Arrays.sort(cards, cardsComparator);
	}

	private int whoHasTwoOfClubs() {
		for (int i = 0; i < 4; i++) {
			if (java.util.Arrays.binarySearch(cards, i * 13, (i + 1) * 13, twoOfClubs, cardsComparator) >= 0) {
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
