package unibo.as.cupido.backend.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import unibo.as.cupido.common.exception.IllegalMoveException;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.Card.Suit;
import unibo.as.cupido.common.structures.ObservedGameStatus;

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
	public static final Card twoOfClubs = new Card(2, Card.Suit.CLUBS);
	public static final Card womanOfSpades = new Card(12, Card.Suit.SPADES);

	public static int whoWins(final Card[] playedCard, final int firstDealer) {
		int winner = 0;
		for (int i = 1; i < 4; i++) {
			if (playedCard[i].suit == playedCard[firstDealer].suit) {
				if (playedCard[i].value == 1) {
					System.err.println("\n\nWHO WINS? first dealer "
							+ firstDealer + ", winner " + winner + ", cards "
							+ Arrays.toString(playedCard));
					return i;
				}
				if (playedCard[i].value > playedCard[firstDealer].value)
					winner = i;
			}
		}
		return winner;
	}

	/** stores the cards passed by each player */
	private Card[][] allPassedCards = new Card[4][];
	/** stores the cards played in the current turn */
	private Card[] cardPlayed = new Card[4];
	/** stores the cards owned by each player */
	@SuppressWarnings("unchecked")
	private ArrayList<Card>[] cards = new ArrayList[4];
	/** the total points of the cards played in the current turn */
	private int currentTurnPoints;
	/** counts the number of card played in the current turn */
	private int playedCardsCount;
	/** position of player who plays first in the current turn */
	private int firstDealerInTurn;
	/** the number of turn made in this hand */
	private int turn = 0;

	/** stores round points of every player */
	private int[] points = new int[4];

	/**
	 * <code>true</code> if some player correctly played an hearts at some point
	 * in the game. <code>false</code> otherwise
	 */
	private boolean brokenHearted = false;

	public CardsManager() {
		dealCards();
	}

	void addCardsInformationForViewers(ObservedGameStatus observedGameStatus) {
		for (int i = 0; i < 4; i++) {
			observedGameStatus.playerStatus[i].numOfCardsInHand = cards[i]
					.size();
			observedGameStatus.playerStatus[i].playedCard = cardPlayed[i];
		}
		if (observedGameStatus.firstDealerInTrick != -1) {
			if (!allPlayerPassedCards()) {
				observedGameStatus.firstDealerInTrick = -1;
			} else if (turn == 0 && playedCardsCount == 0) {
				observedGameStatus.firstDealerInTrick = -1;
			} else {
				observedGameStatus.firstDealerInTrick = firstDealerInTurn;
			}
		}
	}

	private boolean allPlayerPassedCards() {
		for (int i = 0; i < 4; i++)
			if (allPassedCards[i] == null)
				return false;
		return true;
	}

	/** deals card pseudo-uniformly at random */
	@SuppressWarnings("unchecked")
	private void dealCards() {
		cards = new ArrayList[4];
		for (int i = 0; i < 4; i++)
			cards[i] = new ArrayList<Card>(13);
		Card[] mazzo = new Card[52];
		for (int i = 0; i < 52; i++) {
			mazzo[i] = new Card(i % 13 + 1, Card.Suit.values()[i % 4]);
		}
		Collections.shuffle(Arrays.asList(mazzo),
				new Random(System.currentTimeMillis()));
		for (int i = 0; i < 52; i++) {
			cards[i % 4].add(mazzo[i]);
		}
		for (int i = 0; i < 4; i++) {
			Collections.sort(cards[i], cardsComparator);
		}
	}

	public int firstDealer() {
		return firstDealerInTurn;
	}

	public boolean gameEnded() {
		return turn == 12 && playedCardsCount == 4;
	}

	public Card[][] getCards() {
		Card[][] cards = new Card[4][];
		for (int i = 0; i < 4; i++) {
			cards[i] = this.cards[i].toArray(new Card[13]);
		}
		return cards;
	}

	public int[] getMatchPoints() {
		return points;
	}

	public Card[] getPlayerCards(int position) {
		return cards[position].toArray(new Card[13]);
	}

	@SuppressWarnings("boxing")
	public ArrayList<Integer> getWinners() {
		ArrayList<Integer> winners = new ArrayList<Integer>();
		int minimumPoint = points[0];
		for (int i = 1; i < 4; i++) {
			if (points[i] < minimumPoint)
				minimumPoint = points[i];
		}
		for (int i = 0; i < 4; i++) {
			if (points[i] == minimumPoint)
				winners.add(i);
		}
		return winners;
	}

	private void passCards() {
		for (int i = 0; i < 4; i++) {
			cards[i].addAll(Arrays.asList(allPassedCards[(i + 3) % 4]));
			if (cards[i].contains(twoOfClubs))
				firstDealerInTurn = i;
		}
	}

	public void playCard(int playerPosition, Card card)
			throws IllegalMoveException {
		if (card == null)
			throw new IllegalArgumentException();
		if (!cards[playerPosition].remove(card)) {
			throw new IllegalArgumentException("User " + playerPosition
					+ " does not own card " + card);
		}
		if (turn == 0 && playedCardsCount == 0) {
			if (!card.equals(twoOfClubs)) {
				throw new IllegalMoveException(
						"First turn card played has to be two of clubs");
			}
		}
		if (playerPosition != firstDealerInTurn
				&& card.suit != cardPlayed[firstDealerInTurn].suit) {
			for (Card currentPlayerCard : cards[playerPosition]) {
				if (currentPlayerCard.suit == cardPlayed[firstDealerInTurn].suit) {
					throw new IllegalMoveException("\nPlayer " + playerPosition
							+ " must play a card of suit "
							+ cardPlayed[firstDealerInTurn].suit + "\n first:"
							+ firstDealerInTurn + " first card:"
							+ cardPlayed[firstDealerInTurn] + " card played:"
							+ card + " cards played "
							+ Arrays.toString(cardPlayed)
							+ " played cards count " + playedCardsCount + "\n");
				}
			}
		}
		if (!brokenHearted) {
			if (card.suit.equals(Card.Suit.HEARTS)) {
				if (playerPosition == firstDealerInTurn) {
					for (Card currentPlayerCard : cards[firstDealerInTurn]) {
						if (currentPlayerCard.suit != Suit.HEARTS) {
							throw new IllegalMoveException(
									"Cannot play heart rigth now. player "
											+ playerPosition + " cards "
											+ cards[playerPosition].toString()
											+ " card played " + card);
						}
					}

				}
				brokenHearted = true;
			}
		}

		cardPlayed[playerPosition] = card;
		playedCardsCount++;
		if (playedCardsCount == 4) {
			firstDealerInTurn = CardsManager.whoWins(cardPlayed,
					firstDealerInTurn);
			for (int i = 0; i < 4; i++) {
				if (cardPlayed[i].suit.ordinal() == Card.Suit.HEARTS.ordinal()) {
					currentTurnPoints++;
				} else if (cardPlayed[i].suit.ordinal() == Card.Suit.SPADES
						.ordinal() && cardPlayed[i].value == 12) {
					currentTurnPoints += 13;
				}
			}
			Arrays.fill(cardPlayed, null);
			turn++;
			playedCardsCount = 0;
		}
	}

	void print() {
		for (int i = 0; i < 4; i++) {
			Collections.sort(cards[i], cardsComparator);
			System.out.println("\n" + cardPlayed[i] + ". " + (cards[i]));
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
	 *            the cards passed by the player
	 * @throws IllegalArgumentException
	 *             if player does not own the card he wants to pass
	 */
	public void setCardPassing(int position, Card[] passedCards)
			throws IllegalArgumentException {
		for (int i = 0; i < 3; i++) {
			if (!cards[position].remove(passedCards[i])) {
				throw new IllegalArgumentException("Player " + position
						+ " wants to pass cards "
						+ Arrays.toString(passedCards) + " but he owns "
						+ Arrays.toString(cards[position].toArray()));
			}
		}
		allPassedCards[position] = passedCards;
		if (allPlayerPassedCards()) {
			this.passCards();
		}
	}

}
