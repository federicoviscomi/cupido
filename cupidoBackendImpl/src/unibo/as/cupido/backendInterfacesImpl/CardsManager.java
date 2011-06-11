package unibo.as.cupido.backendInterfacesImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.IllegalMoveException;
import unibo.as.cupido.backendInterfaces.common.Card.Suit;

/**
 * 
 * ELIMINARE?? ?? ?? Vi è anche un secondo modo di vincere la partita: un
 * giocatore vince se riesce a prendere tutte le carte di cuori e la donna di
 * picche lasciando gli avversari a zero punti.
 * 
 * 
 * @author cane
 * 
 */

public class CardsManager {

	/**
	 * Compare two cards just for showing them in a user friendly order
	 */
	private static final Comparator<Card> cardsComparator = new Comparator<Card>() {
		@Override
		public int compare(Card o1, Card o2) {
			return (o1.suit.ordinal() * 13 + (o1.value == 1 ? 14 : o1.value))
					- (o2.suit.ordinal() * 13 + (o2.value == 1 ? 14 : o2.value));
		}
	};

	/** the two of clubs */
	private static final Card twoOfClubs = new Card(2, Card.Suit.CLUBS);
	private static final Card womanOfSpades = new Card(12, Card.Suit.SPADES);
	/** stores the cards passed by each player */
	private Card[][] allPassedCards;
	/** stores the cards played in the current turn */
	Card[] cardPlayed;
	/** stores the cards owned by each player */
	ArrayList<Card>[] cards;
	/** the total points of the cards played in the current turn */
	private int currentTurnPoints;
	/** counts the number of card played in the current turn */
	private int playedCardsCount;
	/** position of player who plays first in the current turn */
	int playingFirst;
	/** the number of turn made in this hand */
	int turn;
	/**
	 * <code>true</code> is some player correctly played an hearts at some point
	 * in the game. <code>false</code> otherwise
	 */
	boolean brokenHearted;
	/**
	 * compare to cards according to the order given by ths suit of first card
	 * played
	 */
	private final Comparator<Card> winnerComparator = new Comparator<Card>() {
		@Override
		public int compare(Card o1, Card o2) {
			int firstSuit = cardPlayed[playingFirst].suit.ordinal();
			return ((o1.suit.ordinal() == firstSuit ? 1 : 0) * (o1.value == 1 ? 14 : o1.value))
					- ((o2.suit.ordinal() == firstSuit ? 1 : 0) * (o2.value == 1 ? 14 : o2.value));
		}
	};

	public CardsManager() {
		dealCards();
		allPassedCards = new Card[4][];
		playingFirst = whoHasTwoOfClubs();
	}

	/** deals card pseudo-uniformly at random */
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

	/**
	 * @return <code>true</code> if all player chosen cards to pass;
	 *         <code>false</code> otherwise.
	 */
	public boolean allPlayerPassedCards() {
		return Arrays.asList(allPassedCards).contains(null);
	}

	/**
	 * 
	 * @return <code>true</code> if all player played a card in current turn;
	 *         <code>false</code> otherwise.
	 */
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

	/**
	 * 
	 */
	public void passCards() {
		for (int i = 0; i < 4; i++) {
			cards[i].addAll(Arrays.asList(allPassedCards[(i - 1) % 4]));
		}
	}

	public void playCard(int playerPosition, Card card) throws IllegalMoveException {
		if (card == null || !cards[playerPosition].remove(card)) {
			throw new IllegalArgumentException("User " + playerPosition + " does not own card " + card);
		}
		if (turn == 0 && playedCardsCount == 0) {
			if (!card.equals(twoOfClubs)) {
				throw new IllegalMoveException("First turn card played has to be two of clubs");
			}
		}
		if (playerPosition != playingFirst && !card.suit.equals(cardPlayed[playingFirst].suit)) {
			for (Card currentPlayerCard : cards[playerPosition]) {
				if (currentPlayerCard.suit.equals(cardPlayed[playingFirst].suit)) {
					throw new IllegalMoveException("The player " + playerPosition + " must play a card of suit "
							+ cardPlayed[playingFirst].suit);
				}
			}
		}
		if (!brokenHearted) {
			if (card.suit.equals(Card.Suit.HEARTS)) {
				if (playerPosition == playingFirst) {
					for (Card currentPlayerCard : cards[playingFirst]) {
						if (!currentPlayerCard.suit.equals(Suit.HEARTS)) {
							throw new IllegalMoveException("Cannot play heart rigth now");
						}
					}
				}
				brokenHearted = true;
			}
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

	/**
	 * Stores the card that player whit given position wants to pass. This
	 * method does not actually move the cards to the next player because every
	 * player must pass cards in the same time. The card passing is accomplished
	 * by {@link this.passCards()}
	 * 
	 * @param position
	 *            the position of the player who passes cards
	 * @param passedCards
	 *            the cards passerd by the player
	 * @throws IllegalArgumentException
	 *             if player does not own the card he wants to pass
	 */
	public void setCardPassing(int position, Card[] passedCards) throws IllegalArgumentException {
		for (int i = 0; i < 3; i++) {
			if (!cards[position].remove(passedCards[i]))
				throw new IllegalArgumentException();
		}
		allPassedCards[position] = passedCards;
	}

	/**
	 * @return the position of the player who owns the two of clubs
	 */
	private int whoHasTwoOfClubs() {
		for (int i = 0; i < 4; i++) {
			if (cards[i].contains(twoOfClubs))
				return i;
		}
		return -1;
	}

	public boolean gameEnded() {
		return turn == 12;
	}

}
