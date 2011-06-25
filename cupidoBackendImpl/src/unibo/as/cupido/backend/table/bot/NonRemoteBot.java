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

package unibo.as.cupido.backend.table.bot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;

import unibo.as.cupido.backend.table.CardsManager;
import unibo.as.cupido.backend.table.NonRemoteBotInterface;
import unibo.as.cupido.common.exception.IllegalMoveException;
import unibo.as.cupido.common.exception.NoSuchPlayerException;
import unibo.as.cupido.common.interfaces.ServletNotificationsInterface;
import unibo.as.cupido.common.interfaces.TableInterface;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.Card.Suit;
import unibo.as.cupido.common.structures.ChatMessage;
import unibo.as.cupido.common.structures.InitialTableStatus;

public class NonRemoteBot implements NonRemoteBotInterface {

	private final String botName;
	private TableInterface tableInterface;
	private final InitialTableStatus initialTableStatus;
	private final PrintWriter out;

	private ArrayList<Card> cards;
	private Card[] playedCard = new Card[4];
	private final NonRemoteBotCardPlayingThread cardPlayingThread;
	private int turn = 0;
	private int playedCardCount = 0;
	private int firstDealer = -1;
	private boolean alreadyGotCards = false;
	private boolean brokenHearted = false;

	private int points = 0;

	public NonRemoteBot(final String botName,
			InitialTableStatus initialTableStatus, TableInterface tableInterface)
			throws IOException {

		this.botName = botName;
		this.initialTableStatus = initialTableStatus;
		this.tableInterface = tableInterface;
		this.cardPlayingThread = new NonRemoteBotCardPlayingThread(this,
				botName);

		File outputFile = new File("cupidoBackendImpl/botlog/nonremote/"
				+ botName);
		outputFile.delete();
		outputFile.createNewFile();
		out = new PrintWriter(new FileWriter(outputFile));
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.err.println("shuting down non remote replacementBot "
						+ botName);
				out.close();
			}
		});

		cardPlayingThread.start();
	}

	public void activate(TableInterface tableInterface) {
		this.tableInterface = tableInterface;
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
	public synchronized void notifyGameEnded(int[] matchPoints,
			int[] playersTotalPoint) {
		out.println("\n" + botName + ": "
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + Arrays.toString(matchPoints) + ", "
				+ Arrays.toString(playersTotalPoint) + ")");
		cardPlayingThread.interrupt();
	}

	@Override
	public synchronized void notifyGameStarted(Card[] cards) {
		this.cards = new ArrayList<Card>(13);
		for (int i = 0; i < cards.length; i++)
			this.cards.add(cards[i]);
		if (this.cards.contains(CardsManager.twoOfClubs)) {
			cardPlayingThread.setAbleToPass();
		}
	}

	@Override
	public synchronized void notifyPassedCards(Card[] cards) {
		if (alreadyGotCards)
			throw new IllegalArgumentException("passing cards twice to player "
					+ botName);
		alreadyGotCards = true;
		for (Card card : cards)
			this.cards.add(card);
		cardPlayingThread.setAbleToPass();
		out.println("\nplay starts. " + botName + " cards are:"
				+ this.cards.toString());
		if (this.cards.contains(CardsManager.twoOfClubs)) {
			firstDealer = 3;
			cardPlayingThread.setAbleToPlay();
		}
	}

	@Override
	public synchronized void notifyPlayedCard(Card card, int playerPosition) {
		// out.println("\n" + botName + " player " + playerPosition+
		// " played card " + card);
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
			throw new IllegalArgumentException("illegal position " + position
					+ ". " + name + " " + isBot);
		if (initialTableStatus.opponents[position] != null)
			throw new IllegalArgumentException("Unable to add player" + name
					+ " beacuse ITS: " + initialTableStatus
					+ " already contains a player in position " + position);
		initialTableStatus.opponents[position] = name;
		initialTableStatus.playerScores[position] = position;
		initialTableStatus.whoIsBot[position] = isBot;
	}

	@Override
	public synchronized void notifyPlayerLeft(String name) {
		out.print("\n" + botName + ": "
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + name + ")");
		int position = 0;
		while (!name.equals(initialTableStatus.opponents[position]))
			position++;
		if (position == 3)
			throw new IllegalArgumentException("Player not found " + name);
		initialTableStatus.opponents[position] = null;
		out.println(initialTableStatus);
	}

	@Override
	public synchronized void notifyPlayerReplaced(String botName, int position) {
		out.print("\n" + botName + ": "
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + botName + ", " + position + ")");

		if (botName == null || position < 0 || position > 2)
			throw new IllegalArgumentException(position + " " + botName);

		if (initialTableStatus.opponents[position] == null)
			throw new IllegalArgumentException();
		initialTableStatus.opponents[position] = botName;
		initialTableStatus.whoIsBot[position] = true;
	}

	public synchronized void passCards() {
		Card[] cardsToPass = new Card[3];
		for (int i = 0; i < 3; i++)
			cardsToPass[i] = cards.get(i);
		this.passCards(cardsToPass);
	}

	public synchronized void passCards(Card[] cardsToPass) {
		try {
			setCardsPassed(cardsToPass);
			tableInterface.passCards(botName, cardsToPass);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPlayerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public synchronized void playCard(Card card) {
		try {
			setCardPlayed(card, 3);
			tableInterface.playCard(botName, card);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalMoveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPlayerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public synchronized void playNextCard() {
		// out.println("\n" + botName + " plays ");
		// out.println(" count:" + playedCardCount + " turn:" + turn+
		// " first:" + firstDealer + " broken hearted "+ brokenHearted +
		// " turn cards:"+ Arrays.toString(playedCard) + "\n owned "+
		// cards.toString());
		out.println(" count:" + playedCardCount + " turn:" + turn + " first:"
				+ firstDealer + " broken hearted " + brokenHearted
				+ " turn cards:" + Arrays.toString(playedCard));

		/** choose a valid card */
		Card cardToPlay = choseCard();

		this.playCard(cardToPlay);

		// out.println(" count:" + playedCardCount + " turn:" + turn+
		// " first:" + firstDealer + " broken hearted "+ brokenHearted +
		// " turn cards:"+ Arrays.toString(playedCard) + " played " +
		// cardToPlay+ "\n owned " + cards.toString());
		out.println(" count:" + playedCardCount + " turn:" + turn + " first:"
				+ firstDealer + " broken hearted " + brokenHearted
				+ " turn cards:" + Arrays.toString(playedCard));
	}

	private void setCardPlayed(Card card, int playerPosition) {
		if (firstDealer == -1) {
			firstDealer = playerPosition;
		}
		if (((firstDealer + playedCardCount + 4) % 4) != playerPosition) {
			throw new IllegalStateException(" current player should be "
					+ ((firstDealer + playedCardCount + 4) % 4)
					+ " instead is " + playerPosition + " " + botName
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
				cardPlayingThread.setAbleToPlay();
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
				cardPlayingThread.setAbleToPlay();
			}
		}
	}

	private void setCardsPassed(Card[] cardsToPass) {
		if (cardsToPass.length != 3)
			throw new IllegalArgumentException();
		for (int i = 0; i < 3; i++)
			cards.remove(cardsToPass[i]);
	}

	@Override
	public void notifyLocalChatMessage(ChatMessage message)
			throws RemoteException {
		throw new UnsupportedOperationException(
				"a replacementBot shold not be notified of chat messages");
	}

}
