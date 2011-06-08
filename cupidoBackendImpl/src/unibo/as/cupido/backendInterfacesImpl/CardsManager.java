package unibo.as.cupido.backendInterfacesImpl;

import java.util.Comparator;
import java.util.Random;

import unibo.as.cupido.backendInterfaces.common.Card;

public class CardsManager {

	private Card[][] cards;
	private Card[][] allPassedCards;
	private int passed;
	private Card[] cardPlayed;
	private int playedCardsCount;
	private int startPlayerPosition;
	private static final Comparator<Card> cardsComparator = new Comparator<Card>() {
		@Override
		public int compare(Card o1, Card o2) {
			return (o1.suit.ordinal() * 13 + (o1.value == 1 ? 14 : o1.value))
					- (o2.suit.ordinal() * 13 + (o2.value == 1 ? 14 : o2.value));
		}
	};
	private static final Card twoOfClubs = new Card(2, Card.Suit.CLUBS);
	private static final Card donnaDiPicche = new Card(12, Card.Suit.SPADES);

	public CardsManager() {
		cards = new Card[4][13];
		allPassedCards = new Card[4][];
		daiCarte();
		startPlayerPosition = whoHasTwoOfClubs();
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

	private void sortCards(Card[] cards) {
		java.util.Arrays.sort(cards, cardsComparator);
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

	public void passCards() {
		for (int i = 0, from = 3, nextCard = 0; i < 4; i++, from = (from++) % 4) {
			for (int j = 0; j < 13; j++) {
				if (cards[i][j] == null)
					cards[i][j] = allPassedCards[from][nextCard++];
			}
			sortCards(cards[i]);
		}
	}

	public void print(PlayersManager playersManager) {
		for (int i = 0; i < 4; i++) {
			System.out.format("\n name=%10.10s, isBot=%5.5b, cardsInHand=%2.2s, score=%3.3s, cards= ",
					playersManager.getPlayerName(i), playersManager.isBot(i), playersManager.numOfCardsInHand(i),
					playersManager.getScore(i));
			System.out.print(java.util.Arrays.toString(cards[i]));
		}
	}

	public boolean allPlayerPassedCards() {
		return passed == 4;
	}

	public void playCard(int playerPosition, Card card) {
		cardPlayed[playerPosition] = card;
		if (playedCardsCount == 4) {
			
		}
		playedCardsCount = (playedCardsCount + 1) % 4;
	}

	private int whoHasTwoOfClubs() {
		for (int i = 0; i < 4; i++) {
			if (java.util.Arrays.binarySearch(cards[i], twoOfClubs, cardsComparator) >= 0) {
				return i;
			}
		}
		return -1;
	}
}
