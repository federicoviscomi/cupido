/*  Cupido - An online Hearts game.
 *  Copyright (C) 2011 Lorenzo Belli, Marco Poletti, Federico Viscomi
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package unibo.as.cupido.backend.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import unibo.as.cupido.common.exception.IllegalMoveException;
import unibo.as.cupido.common.exception.WrongGameStateException;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.Card.Suit;
import unibo.as.cupido.common.structures.ObservedGameStatus;

public class CardsManager {

	/** used just for showing cards in a user friendly order */
	private static final Comparator<Card> cardsComparator = new Comparator<Card>() {
		@Override
		public int compare(Card o1, Card o2) {
			return (o1.suit.ordinal() * 13 + (o1.value == 1 ? 14 : o1.value))
					- (o2.suit.ordinal() * 13 + (o2.value == 1 ? 14 : o2.value));
		}
	};

	public static final Card twoOfClubs = new Card(2, Card.Suit.CLUBS);
	public static final Card queenOfSpades = new Card(12, Card.Suit.SPADES);

	public static int whoWins(final Card[] playedCard, final int firstDealer) {
		assert firstDealer >= 0;
		assert firstDealer <= 3;		
		for (int i = 0; i < 4; i++)
			assert playedCard[i] != null;
		
		int winner = firstDealer;
		for (int i = 0; i < 4; i++) {
			if (playedCard[i].suit == playedCard[firstDealer].suit) {
				if (playedCard[i].value == 1) {
					return i;
				}
				if (playedCard[i].value > playedCard[winner].value)
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
	/** counts the number of card played in the current turn */
	private int playedCardsCount = 0;
	/** position of player who plays first in the current turn */
	private int firstDealerInTurn = -1;
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

	void addCardsInformationForViewers(ObservedGameStatus observedGameStatus) {
		for (int i = 0; i < 4; i++) {
			if (observedGameStatus.playerStatus[i] != null) {
				observedGameStatus.playerStatus[i].numOfCardsInHand = cards[i]
						.size();
				observedGameStatus.playerStatus[i].playedCard = cardPlayed[i];
			}
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

	boolean allPlayerPassedCards() {
		for (int i = 0; i < 4; i++)
			if (allPassedCards[i] == null)
				return false;
		return true;
	}

	private void checkMoveValidity(int playerPosition, Card card)
			throws IllegalMoveException {
		if (!cards[playerPosition].contains(card)) {
			throw new IllegalArgumentException("User " + playerPosition
					+ " does not own card " + card);
		}

		if (playedCardsCount == 0) {
			if (turn == 0 && !card.equals(twoOfClubs)) {
				throw new IllegalMoveException(
						"First card played must be two of clubs");
			}
			if (!brokenHearted && card.suit.equals(Card.Suit.HEARTS)) {
				for (Card currentPlayerCard : cards[playerPosition]) {
					if (currentPlayerCard.suit != Suit.HEARTS) {
						throw new IllegalMoveException(
								"Cannot play heart rigth now");
					}
				}
			}
		} else {
			if (card.suit != cardPlayed[firstDealerInTurn].suit) {
				for (Card currentPlayerCard : cards[playerPosition]) {
					if (currentPlayerCard.suit == cardPlayed[firstDealerInTurn].suit) {
						throw new IllegalMoveException("\nPlayer "
								+ playerPosition + " must play a card of suit "
								+ cardPlayed[firstDealerInTurn].suit
								+ "\n card played:" + card + ", first card:"
								+ cardPlayed[firstDealerInTurn]
								+ " all played:" + Arrays.toString(cardPlayed)
								+ "\n player: " + playerPosition
								+ ", player cards:"
								+ this.cards[playerPosition].toString()
								+ " \n played cards count: " + playedCardsCount);
					}
				}
			}
		}
	}

	public boolean gameEnded() {
		return turn == 13;
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

	public Card[] getPassedCards(int i) {
		return allPassedCards[i];
	}

	public boolean hasPassedCards(int position) {
		return allPassedCards[position] != null;
	}

	private void passCards() {
		for (int i = 0; i < 4; i++) {
			cards[i].addAll(Arrays.asList(allPassedCards[(i + 3) % 4]));
			if (cards[i].contains(twoOfClubs))
				firstDealerInTurn = i;
		}
	}

	public void playCard(String playerName, int playerPosition, Card card)
			throws IllegalMoveException, WrongGameStateException {

		System.out.println(playerName + " " + playerPosition + " play card "
				+ card + " turn " + turn);

		if (card == null || playerPosition < 0 || playerPosition > 4)
			throw new IllegalArgumentException();

		checkMoveValidity(playerPosition, card);

		if (((firstDealerInTurn + playedCardsCount + 4) % 4) != playerPosition) {
			throw new WrongGameStateException(" current player should be "
					+ ((firstDealerInTurn + playedCardsCount + 4) % 4)
					+ " instead is " + playerPosition + " first: "
					+ firstDealerInTurn + " count: " + playedCardsCount);
		}

		setCardPlayed(playerPosition, card);

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

	private void setCardPlayed(int playerPosition, Card card) {
		cards[playerPosition].remove(card);
		if (!brokenHearted && card.suit == Card.Suit.HEARTS)
			brokenHearted = true;
		cardPlayed[playerPosition] = card;
		playedCardsCount++;
		if (playedCardsCount == 4) {
			turn++;
			playedCardsCount = 0;
			firstDealerInTurn = CardsManager.whoWins(cardPlayed,
					firstDealerInTurn);
			for (int i = 0; i < 4; i++) {
				if (cardPlayed[i].suit == Card.Suit.HEARTS) {
					points[firstDealerInTurn]++;
				} else if (cardPlayed[i].equals(queenOfSpades)) {
					points[firstDealerInTurn] += 5;
				}
			}
			Arrays.fill(cardPlayed, null);
		}
	}

	public int whoShouldPlay() {
		return (firstDealerInTurn + playedCardsCount + 4) % 4;
	}

}
