package unibo.as.cupido.backendInterfacesImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import unibo.as.cupido.backendInterfaces.common.Card;

public class CardsManager {

	ArrayList<Card>[] cards;
	private Card[][] allPassedCards;
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

	@SuppressWarnings("unchecked")
	public CardsManager() {
		cards = new ArrayList[4];
		for (int i = 0; i < 4; i++)
			cards[i] = new ArrayList<Card>(13);
		allPassedCards = new Card[4][];
		daiCarte();
		playingFirst = whoHasTwoOfClubs();
	}

	public boolean allPlayerPassedCards() {
		return Arrays.asList(allPassedCards).contains(null);
	}

	public boolean allPlayerPlayedCards() {
		return playedCardsCount == 4;
	}

	private void daiCarte() {
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 13; j++) {
				Card card = new Card();
				card.suit = Card.Suit.values()[i];
				card.value = j + 1;
				cards[i].add(card);
			}
		}
	
		Random random = new Random(System.currentTimeMillis());
		for (int i = 0; i < 26; i++) {
			int randPlayer1 = random.nextInt(4);
			int randPlayer2 = random.nextInt(4);
			int randCard1 = random.nextInt(13);
			int randCard2 = random.nextInt(13);
			Card temp = cards[randPlayer1].get(randCard1);
			cards[randPlayer1].set(randCard1, cards[randPlayer2].get(randCard2));
			cards[randPlayer2].set(randCard2, temp);
		}

		for (int i = 0; i < 4; i++) {
			Collections.sort(cards[i], cardsComparator);
		}
	}

	private int getCardPoints(Card card) {
		if (card.suit.ordinal() == Card.Suit.HEARTS.ordinal()) {
			return 1;
		}
		if (card.suit.ordinal() == Card.Suit.SPADES.ordinal() && card.value == 12) {
			return 13;
		}
		return 0;
	}

	public void passCards() {
		for (int i = 0; i < 4; i++) {
			cards[i].addAll(Arrays.asList(allPassedCards[(i - 1) % 4]));
		}
	}

	public void playCard(int playerPosition, Card card) {
		if (!cards[playerPosition].remove(card)) {
			throw new IllegalArgumentException("User " + playerPosition + " does not own card " + card);
		}
		cardPlayed[playerPosition] = card;
		if (playedCardsCount == 4) {

			/* decide who takes this hand cards */
			int maxPosition = playingFirst;
			for (int i = 0; i < 4; i++) {
				if (winnerComparator.compare(cardPlayed[i], cardPlayed[maxPosition]) > 0)
					maxPosition = i;
			}

			/* calculate this hand point */
			for (int i = 0; i < 4; i++) {
				currentHandPoints = currentHandPoints + getCardPoints(cardPlayed[i]);
			}
		}
		playedCardsCount = (playedCardsCount + 1) % 4;
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

	public int getWinner() {
		return playingFirst;
	}

	public int getPoints() {
		return currentHandPoints;
	}
}
