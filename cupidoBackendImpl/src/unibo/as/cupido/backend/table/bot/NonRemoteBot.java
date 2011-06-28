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
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;

import unibo.as.cupido.backend.table.Action;
import unibo.as.cupido.backend.table.ActionQueue;
import unibo.as.cupido.backend.table.AsynchronousMessage;
import unibo.as.cupido.backend.table.CardsManager;
import unibo.as.cupido.backend.table.LoggerSingleTableManager;
import unibo.as.cupido.backend.table.NonRemoteBotInterface;
import unibo.as.cupido.common.exception.IllegalMoveException;
import unibo.as.cupido.common.exception.NoSuchPlayerException;
import unibo.as.cupido.common.interfaces.TableInterface;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.Card.Suit;
import unibo.as.cupido.common.structures.ChatMessage;
import unibo.as.cupido.common.structures.InitialTableStatus;

public class NonRemoteBot implements NonRemoteBotInterface {

	private final String botName;
	private TableInterface tableInterface;
	private final InitialTableStatus initialTableStatus;
	PrintWriter out;

	private ArrayList<Card> cards;
	private Card[] playedCard = new Card[4];

	private ActionQueue actionQueue;

	private int turn = 0;
	private int playedCardCount = 0;
	private int firstDealer = -1;
	private boolean alreadyGotCards = false;
	private boolean brokenHearted = false;

	private int points = 0;
	private final int position;
	private boolean active;

	/**
	 * Create a non active bot.
	 * 
	 * @param playerName
	 * @param initialTableStatus
	 * @param stmController
	 * @param bot
	 */
	public NonRemoteBot(final String botName,
			InitialTableStatus initialTableStatus, int position) {

		this.botName = botName;
		this.initialTableStatus = initialTableStatus;
		this.position = position;
		this.tableInterface = LoggerSingleTableManager.defaultInstance;
		this.active = false;
		this.actionQueue = new ActionQueue();
		
		try {
			File outputFile = new File("cupidoBackendImpl/botlog/nonremote/"
					+ botName);
			outputFile.delete();
			outputFile.createNewFile();
			// out = new PrintWriter(new FileWriter(outputFile));
			out = new PrintWriter(System.out);
		} catch (IOException e) {
			// not a real error
			e.printStackTrace();
			out = new PrintWriter(System.out);
		}

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.close();
			}
		});
		
		actionQueue.start();
	}

	/**
	 * Create an active bot
	 * 
	 * @param playerName
	 * @param initialTableStatus
	 * @param tableInterface
	 * @param controller
	 */
	public NonRemoteBot(final String botName,
			InitialTableStatus initialTableStatus,
			TableInterface tableInterface, int position) {

		this.botName = botName;
		this.initialTableStatus = initialTableStatus;
		this.tableInterface = tableInterface;
		this.actionQueue = new ActionQueue();
		this.position = position;
		this.active = true;

		try {
			File outputFile = new File("cupidoBackendImpl/botlog/nonremote/"
					+ botName);
			outputFile.delete();
			outputFile.createNewFile();
			// out = new PrintWriter(new FileWriter(outputFile));
			// out = new PrintWriter(System.out);
		} catch (IOException e) {
			// not a real error
			e.printStackTrace();
			// out = new PrintWriter(System.out);
		}

		this.actionQueue.start();
	}

	@Override
	public void activate(final TableInterface tableInterface) {
		final NonRemoteBot bot = this;
		actionQueue.enqueue(new Action() {
			@Override
			public void execute() {
				bot.tableInterface = tableInterface;
				bot.active = true;
			}
		});
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

	private Card chooseCard() {
		return chooseValidCards().get(0);
	}

	@Override
	public void notifyGameEnded(int[] matchPoints, int[] playersTotalPoint) {
		actionQueue.enqueue(new Action() {
			@Override
			public void execute() {
				// TODO: There is really nothing to do?
			}
		});
	}

	@Override
	public void notifyGameStarted(final Card[] cards) {
		final NonRemoteBot bot = this;
		actionQueue.enqueue(new Action() {
			@Override
			public void execute() {
				System.out.println("\n" + botName + ": notifyGameStarted("
						+ Arrays.toString(cards) + ")");
				bot.cards = new ArrayList<Card>(13);
				for (int i = 0; i < cards.length; i++)
					bot.cards.add(cards[i]);
			}
		});
	}

	@Override
	public void notifyLocalChatMessage(ChatMessage message)
			throws RemoteException {
		throw new UnsupportedOperationException(
				"a bot should not be notified of chat messages");
	}

	@Override
	public void notifyPassedCards(final Card[] cards) {
		final NonRemoteBot bot = this;
		actionQueue.enqueue(new Action() {
			@Override
			public void execute() {
				System.out.println("\n" + botName + ": notifyPassedCards("
						+ Arrays.toString(cards) + ")");
				if (alreadyGotCards)
					throw new IllegalArgumentException("passing cards twice to player "
							+ botName);
				alreadyGotCards = true;
				for (Card card : cards)
					bot.cards.add(card);
				System.out.println("\nplay starts. " + botName + " cards are:"
						+ bot.cards.toString());
				if (bot.cards.contains(CardsManager.twoOfClubs)) {
					firstDealer = 3;
					if (active)
						playCard();
					}		
			}
		});
	}

	@Override
	public void notifyPlayedCard(final Card card, final int playerPosition) {
		actionQueue.enqueue(new Action() {
			@Override
			public void execute() {
				System.out.println("\n" + botName + ": notifyPlayedCard(" + card + ", "
						+ playerPosition + ")");
				setCardPlayed(card, playerPosition);
			}
		});
	}

	@Override
	public void notifyPlayerJoined(final String name, final boolean isBot,
			int point, final int position) {
		actionQueue.enqueue(new Action() {
			@Override
			public void execute() {
				System.out.println("\n" + botName + ": notifyPlayerJoined(" + name
						+ ", " + isBot + ")");
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
		});
	}

	@Override
	public void notifyPlayerLeft(final String name) {
		actionQueue.enqueue(new Action() {
			@Override
			public void execute() {
				System.out.print("\n" + botName + ": "
						+ Thread.currentThread().getStackTrace()[1].getMethodName()
						+ "(" + name + ")");
				int position = 0;
				while (!name.equals(initialTableStatus.opponents[position]))
					position++;
				if (position == 3)
					throw new IllegalArgumentException("Player not found " + name);
				initialTableStatus.opponents[position] = null;
				System.out.println(initialTableStatus);
			}
		});
	}

	@Override
	public void notifyPlayerReplaced(final String botName, final int position) {
		actionQueue.enqueue(new Action() {
			@Override
			public void execute() {
				System.out.print("\n" + botName + ": notifyPlayerReplaced(" + botName
						+ ", " + position + ")");

				if (botName == null || position < 0 || position > 2)
					throw new IllegalArgumentException(position + " " + botName);

				if (initialTableStatus.opponents[position] == null) {
					(new NoSuchPlayerException()).printStackTrace();
					return;
				}
				initialTableStatus.opponents[position] = botName;
				initialTableStatus.whoIsBot[position] = true;
			}
		});
	}

	public void passCards() {
		actionQueue.enqueue(new Action() {
			@Override
			public void execute() {
				Card[] cardsToPass = new Card[3];
				for (int i = 0; i < 3; i++)
					cardsToPass[i] = cards.get(i);
				processPassCards(cardsToPass);
			}
		});
	}

	@Override
	public void passCards(final Card[] cardsToPass) {
		actionQueue.enqueue(new Action() {
			@Override
			public void execute() {
				processPassCards(cardsToPass);
			}
		});
	}

	private void processPassCards(Card[] cardsToPass) {
		try {
			System.out.println("\n" + botName + ": passCards("
					+ Arrays.toString(cardsToPass) + ")");
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

	public void playCard() {
		actionQueue.enqueue(new Action() {
			@Override
			public void execute() {
				Card cardToPlay = chooseCard();
				processPlayCard(cardToPlay);
			}
		});
	}

	@Override
	public void playCard(final Card card) {
		actionQueue.enqueue(new Action() {
			@Override
			public void execute() {
				processPlayCard(card);
			}
		});
	}

	private void processPlayCard(Card card) {
		try {
			System.out.println("\n" + botName + ": playCard(" + card + ")");
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
				for (Card c : playedCard) {
					if (c.suit == Suit.HEARTS)
						points++;
					else if (c.equals(CardsManager.twoOfClubs))
						points += 5;
				}
				if (active) {
					// TODO: Check this.
					playCard();
				}
			}
			Arrays.fill(playedCard, null);
		} else {
			if (playerPosition == 2) {
				if (active) {
					// TODO: Check this.
					playCard();
				}
			}
		}
	}

	private void setCardsPassed(Card[] cardsToPass) {
		if (cardsToPass.length != 3)
			throw new IllegalArgumentException();
		for (int i = 0; i < 3; i++)
			cards.remove(cardsToPass[i]);
	}
}
