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

/**
 * Cards manager has various responsibility:
 * <ul>
 * <li>dealing cards</li>
 * <li>keeping track of all passed and played cards</li>
 * <li>checking for soundness of a move according to the rules of hearts</li>
 * </ul>
 */
public class CardsManager {

	/**
	 * <tt>Arrays.sort(cards, higherFirstCardsComparator)</tt> sorts cards by
	 * suit and then lower first. This is used just to show cards in a user
	 * friendly way.
	 */
	private static final Comparator<Card> suitLowerFirstCardsComparator = new Comparator<Card>() {
		@Override
		public int compare(Card o1, Card o2) {
			return (o1.suit.ordinal() * 13 + (o1.value == 1 ? 14 : o1.value))
					- (o2.suit.ordinal() * 13 + (o2.value == 1 ? 14 : o2.value));
		}
	};

	/** the two of clubs */
	public static final Card twoOfClubs = new Card(2, Card.Suit.CLUBS);
	/** the queen of spades */
	public static final Card queenOfSpades = new Card(12, Card.Suit.SPADES);

	/**
	 * Determines who is the winner of the trick
	 * 
	 * @param trick
	 *            cards played.
	 * @param firstDealer
	 *            the position of first dealer in trick.
	 * @return the winner of the trick
	 */
	public static int whoWins(final Card[] trick, final int firstDealer) {
		assert firstDealer >= 0;
		assert firstDealer <= 3;
		for (int i = 0; i < 4; i++)
			assert trick[i] != null;

		int winner = firstDealer;
		for (int i = 0; i < 4; i++) {
			if (trick[i].suit == trick[firstDealer].suit) {
				if (trick[i].value == 1) {
					return i;
				}
				if (trick[i].value > trick[winner].value)
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
		Card[] deck = new Card[52];
		/** fills the deck */
		for (int i = 0; i < 52; i++) {
			deck[i] = new Card(i % 13 + 1, Card.Suit.values()[i % 4]);
		}

		/** shuffles the deck */
		Collections.shuffle(Arrays.asList(deck),
				new Random(System.currentTimeMillis()));

		/** deals cards to players */
		for (int i = 0; i < 52; i++) {
			cards[i % 4].add(deck[i]);
		}

		/** sort cards by suit and the lower first */
		for (int i = 0; i < 4; i++) {
			Collections.sort(cards[i], suitLowerFirstCardsComparator);
		}
	}

	/**
	 * Adds information for viewers to <tt>observedGameStatus</tt>
	 * 
	 * @param observedGameStatus
	 */
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

	/**
	 * Return <tt>true</tt> if all player passed cards; <tt>false</tt>
	 * otherwise.
	 * 
	 * @return <tt>true</tt> if all player passed cards; <tt>false</tt>
	 *         otherwise.
	 */
	boolean allPlayerPassedCards() {
		for (int i = 0; i < 4; i++)
			if (allPassedCards[i] == null)
				return false;
		return true;
	}

	/**
	 * Check if player <tt>playerPosition</tt> can play card <tt>card</tt>.
	 * <p>
	 * Specified player cannot play specified card if:
	 * <ul>
	 * <li>if card is the first card played in the game and is not two of clubs</li>
	 * <li>if card is the first card played in this trick and the game is not
	 * broken hearted and the player owns at least a non heart card</li>
	 * <li>if card is not the first card played in this trick and card has not
	 * the same suit of the first card played and player owns at least one card
	 * with the same suit of the first card played in this trick</li>
	 * </ul>
	 * 
	 * @param playerPosition
	 *            the position of player who plays
	 * @param card
	 *            the card played
	 * @throws IllegalMoveException
	 *             if specified player cannot play specified card according to
	 *             game rules.
	 * @throws IllegalArgumentException
	 *             if specified player does not own specified card
	 */
	private void checkMoveSoundness(int playerPosition, Card card)
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
								+ cardPlayed[firstDealerInTurn].suit);
					}
				}
			}
		}
	}

	/**
	 * Returns <tt>true</tt> if game ended because every player played all
	 * cards; <tt>false</tt> otherwise.
	 * 
	 * @return <tt>true</tt> if game ended because every player played all
	 *         cards; <tt>false</tt> otherwise.
	 */
	public boolean gameEnded() {
		return turn == 13;
	}

	/**
	 * Gets cards of all players
	 * 
	 * @return cards of all players
	 */
	public Card[][] getCards() {
		Card[][] cards = new Card[4][];
		for (int i = 0; i < 4; i++) {
			cards[i] = this.cards[i].toArray(new Card[13]);
		}
		return cards;
	}

	/**
	 * Returns an array of four position which stores each player points in this
	 * match
	 * 
	 * @return an array of four position which stores each player points in this
	 *         match
	 */
	public int[] getMatchPoints() {
		return points;
	}

	/**
	 * Returns cards passed by player <tt>position</tt>
	 * 
	 * @param position
	 * @return cards passed by player <tt>position</tt>
	 */
	public Card[] getPassedCards(int position) {
		return allPassedCards[position];
	}

	/**
	 * Moves passed cards from the players who pass to the players who receive.
	 */
	private void passCards() {
		for (int i = 0; i < 4; i++) {
			cards[i].addAll(Arrays.asList(allPassedCards[(i + 3) % 4]));
			if (cards[i].contains(twoOfClubs))
				firstDealerInTurn = i;
		}
	}

	/**
	 * This is called by the STM when player <tt>playerName</tt> plays card
	 * <tt>card</tt>
	 * 
	 * @param playerName
	 *            name of player who plays a card.
	 * @param playerPosition
	 *            position of the player who plays a card.
	 * @param card
	 *            card played.
	 * @throws IllegalMoveException
	 *             if some of the condition specified in
	 *             {@link #checkMoveSoundness(int, Card)} applies
	 * 
	 * @throws WrongGameStateException
	 *             if the player who plays is not the one who should play now.
	 *             This happens only if there is an implementation error.
	 * 
	 */
	public void playCard(String playerName, int playerPosition, Card card)
			throws IllegalMoveException, WrongGameStateException {

		System.out.println(playerName + " " + playerPosition + " play card "
				+ card + " turn " + turn);

		if (card == null || playerPosition < 0 || playerPosition > 4)
			throw new IllegalArgumentException();

		checkMoveSoundness(playerPosition, card);

		if (((firstDealerInTurn + playedCardsCount + 4) % 4) != playerPosition) {
			throw new WrongGameStateException(" current player should be "
					+ ((firstDealerInTurn + playedCardsCount + 4) % 4)
					+ " instead is " + playerPosition + " first: "
					+ firstDealerInTurn + " count: " + playedCardsCount);
		}

		if (!brokenHearted && card.suit == Card.Suit.HEARTS) {
			brokenHearted = true;
		}

		cards[playerPosition].remove(card);
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

	/**
	 * Stores the card that player whit given position wants to pass. This
	 * method does not actually move the cards to the next player because every
	 * player must pass cards in the same time. The card passing is accomplished
	 * by {@link #passCards()}
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
