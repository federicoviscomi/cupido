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
import java.util.Comparator;

import unibo.as.cupido.backend.table.Action;
import unibo.as.cupido.backend.table.ActionQueue;
import unibo.as.cupido.backend.table.CardsManager;
import unibo.as.cupido.backend.table.LoggerSingleTableManager;
import unibo.as.cupido.common.exception.GameInterruptedException;
import unibo.as.cupido.common.exception.IllegalMoveException;
import unibo.as.cupido.common.exception.NoSuchPlayerException;
import unibo.as.cupido.common.exception.WrongGameStateException;
import unibo.as.cupido.common.interfaces.ServletNotificationsInterface;
import unibo.as.cupido.common.interfaces.TableInterface;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.Card.Suit;
import unibo.as.cupido.common.structures.ChatMessage;
import unibo.as.cupido.common.structures.InitialTableStatus;

/**
 * A local bot.
 */
public class LocalBot implements LocalBotInterface {

	/** this bot name */
	private final String botName;
	/** the table interface */
	private TableInterface tableInterface;
	/** initial table status */
	private final InitialTableStatus initialTableStatus;
	/** output goes through here */
	private PrintWriter out;

	/** cards owned by this bot */
	private ArrayList<Card> cards;
	/** cards played in current trick */
	private Card[] playedCard = new Card[4];

	/** execute action on this bot */
	private final ActionQueue actionQueue;

	/** number of played trick */
	private int turn = 0;
	/** number of cards played in current trick */
	private int playedCardCount = 0;
	/** position of first dealer in current trick */
	private int firstDealer = -1;
	/** <tt>true</tt> if this bot passed cards; <tt>false</tt> otherwise */
	private boolean alreadyPassedCards = false;
	/** <tt>true</tt> if this received passed cards; <tt>false</tt> otherwise */
	private boolean alreadyGotCards = false;
	/** <tt>true</tt> if game is broken hearted; <tt>false</tt> otherwise */
	private boolean brokenHearted = false;
	/** this bot score in this match */
	private int points = 0;
	/**
	 * <tt>true</tt> if this bot is active; <tt>false</tt> otherwise, in this
	 * case the bot is a replacement bot.
	 */
	private boolean active;

	/**
	 * <tt>Arrays.sort(cards, higherFirstCardsComparator)</tt> sorts cards
	 * higher first and then by suit.
	 */
	private static final Comparator<Card> higherFirstCardsComparator = new Comparator<Card>() {
		@Override
		public int compare(Card o1, Card o2) {
			return (o2.suit.ordinal() + (o2.value == 1 ? 14 : o2.value) * 4)
					- (o1.suit.ordinal() + (o1.value == 1 ? 14 : o1.value) * 4);
		}
	};

	/**
	 * Create a non active bot, i.e. a replacement bot. When a player P joins a
	 * table, a replacement bot R is added to the game. R mimics the move of P,
	 * i.e. after P does a move, R does the same move. If after the game starts,
	 * player P leaves, he is replaced by R and R becames active. If a bot is
	 * inactive it pass and play the same cards of its player but the bot moves
	 * are not conveyed to the STM but to a logger STM.
	 * 
	 * @param botName
	 * @param initialTableStatus
	 */
	public LocalBot(final String botName, InitialTableStatus initialTableStatus) {

		this.botName = botName;
		this.initialTableStatus = initialTableStatus;
		this.tableInterface = LoggerSingleTableManager.defaultInstance;
		this.active = false;
		this.actionQueue = new ActionQueue();

		try {
			File outputFile = new File("cupidoBackendImpl/botlog/nonremote/"
					+ botName);
			outputFile.delete();
			outputFile.createNewFile();
			out = new PrintWriter(new FileWriter(outputFile));
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					out.close();
				}
			});
		} catch (IOException e) {
			// not a real error
			e.printStackTrace();
			out = new PrintWriter(System.out);
		}

		actionQueue.start();
	}

	/**
	 * Create an active bot.
	 * 
	 * @param botName
	 * @param initialTableStatus
	 * @param tableInterface
	 */
	public LocalBot(final String botName,
			InitialTableStatus initialTableStatus, TableInterface tableInterface) {

		this.botName = botName;
		this.initialTableStatus = initialTableStatus;
		this.tableInterface = tableInterface;
		this.actionQueue = new ActionQueue();
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
		final LocalBot bot = this;
		actionQueue.enqueue(new Action() {
			@Override
			public void execute() {
				bot.tableInterface = tableInterface;
				bot.active = true;
				if (firstDealer != -1 && (firstDealer + playedCardCount) == 3)
					playCard();
				if (cards != null && !alreadyPassedCards)
					passCards();
			}
		});
	}

	/**
	 * Choose a card to play. The card is the first one in the bot cards that is
	 * sound to play.
	 * 
	 * @return a card to play
	 */
	private Card chooseCard() {
		return getAllSoundCards().get(0);
	}

	/**
	 * Returns all the bot cards that are sound to play.
	 * 
	 * @return all the bot cards that are sound to play.
	 */
	private ArrayList<Card> getAllSoundCards() {
		ArrayList<Card> validCards = new ArrayList<Card>(13);

		if (cards.contains(CardsManager.twoOfClubs)) {
			validCards.add(CardsManager.twoOfClubs);
			return validCards;
		}

		if (playedCardCount == 0) {
			// first player in trick
			if (brokenHearted) {
				validCards.addAll(cards);
				return validCards;
			}
			// not broken hearted
			for (int i = 0; i < cards.size(); i++) {
				if (cards.get(i).suit != Card.Suit.HEARTS) {
					validCards.add(cards.get(i));
				}
			}
			if (validCards.size() == 0) {
				// not broken hearted but does not own non-heart cards
				validCards.addAll(cards);
			}
			return validCards;
		}
		// not first player in trick
		for (int i = 0; i < cards.size(); i++) {
			if (cards.get(i).suit == playedCard[firstDealer].suit) {
				validCards.add(cards.get(i));
			}
		}
		if (validCards.size() == 0) {
			// does not own cards of same suit as first card played
			validCards.addAll(cards);
		}
		return validCards;
	}

	@Override
	public ServletNotificationsInterface getServletNotificationsInterface() {
		return new ServletNotificationsInterface() {
			@Override
			public void notifyGameEnded(final int[] matchPoints,
					final int[] playersTotalPoint) throws RemoteException {
				actionQueue.enqueue(new Action() {
					@Override
					public void execute() {
						onGameEnded(matchPoints, playersTotalPoint);
					}
				});
			}

			@Override
			public void notifyGameStarted(final Card[] cards)
					throws RemoteException {
				actionQueue.enqueue(new Action() {
					@Override
					public void execute() {
						onGameStarted(cards);
					}
				});
			}

			@Override
			public void notifyLocalChatMessage(final ChatMessage message)
					throws RemoteException {
				actionQueue.enqueue(new Action() {
					@Override
					public void execute() {
						onLocalChatMessage(message);
					}
				});
			}

			@Override
			public void notifyPassedCards(final Card[] cards)
					throws RemoteException {
				actionQueue.enqueue(new Action() {
					@Override
					public void execute() {
						onPassedCards(cards);
					}
				});
			}

			@Override
			public void notifyPlayedCard(final Card card,
					final int playerPosition) throws RemoteException {
				actionQueue.enqueue(new Action() {
					@Override
					public void execute() {
						onPlayedCard(card, playerPosition);
					}
				});
			}

			@Override
			public void notifyPlayerJoined(final String playerName,
					final boolean isBot, final int score, final int position)
					throws RemoteException {
				actionQueue.enqueue(new Action() {
					@Override
					public void execute() {
						onPlayerJoined(playerName, isBot, score, position);
					}
				});
			}

			@Override
			public void notifyPlayerLeft(final String playerName)
					throws RemoteException {
				actionQueue.enqueue(new Action() {
					@Override
					public void execute() {
						onPlayerLeft(playerName);
					}
				});
			}

			@Override
			public void notifyPlayerReplaced(final String botName,
					final int position) throws RemoteException {
				actionQueue.enqueue(new Action() {
					@Override
					public void execute() {
						onPlayerReplaced(botName, position);
					}
				});
			}
		};
	}

	/**
	 * On game ended stop the action queue consumer.
	 * 
	 * @param matchPoints
	 *            score made by every player in this game
	 * @param playersTotalPoint
	 *            new scores of every players
	 */
	private void onGameEnded(int[] matchPoints, int[] playersTotalPoint) {
		actionQueue.killConsumer();
	}

	/**
	 * On game started take cards and eventually pass cards
	 * 
	 * @param cards
	 *            the card received by this bot
	 */
	private void onGameStarted(Card[] cards) {
		Arrays.sort(cards, higherFirstCardsComparator);
		System.out.println("\n" + botName + ": notifyGameStarted("
				+ Arrays.toString(cards) + ")");
		this.cards = new ArrayList<Card>(13);
		for (int i = 0; i < cards.length; i++)
			this.cards.add(cards[i]);
		if (active)
			passCards();
	}

	/**
	 * On local chat message throw an exception because a bot should not be
	 * notified of a new chat message
	 * 
	 * @param message
	 *            the new local chat message
	 */
	private void onLocalChatMessage(ChatMessage message) {
		throw new UnsupportedOperationException(
				"a bot should not be notified of chat messages");
	}

	/**
	 * On passed cards take cards and eventually play
	 * 
	 * @param cards
	 *            the cards received form another player
	 */
	private void onPassedCards(final Card[] cards) {
		System.out.println("\n" + botName + ": notifyPassedCards("
				+ Arrays.toString(cards) + ")");
		if (alreadyGotCards)
			throw new IllegalArgumentException("passing cards twice to player "
					+ botName);
		alreadyGotCards = true;
		for (Card card : cards)
			this.cards.add(card);
		System.out.println("\nplay starts. " + botName + " cards are:"
				+ this.cards.toString());
		if (this.cards.contains(CardsManager.twoOfClubs)) {
			firstDealer = 3;
			if (active)
				playCard();
		}
	}

	/**
	 * On played card, record card played and eventually play a card
	 * 
	 * @param card
	 *            card played
	 * @param playerPosition
	 *            position of played who played
	 */
	private void onPlayedCard(Card card, int playerPosition) {
		System.out.println("\n" + botName + ": notifyPlayedCard(" + card + ", "
				+ playerPosition + ")");
		setCardPlayed(card, playerPosition);
	}

	/**
	 * On player joined, add specified player in this bot game status
	 * 
	 * @param name
	 *            name of player who joined
	 * @param isBot
	 *            <tt>true</tt> if player who joined is bot; <tt>false</tt>
	 *            otherwise
	 * @param score
	 *            score of player who joined
	 * @param position
	 *            position of player who joined
	 */
	private void onPlayerJoined(String name, boolean isBot, int score,
			int position) {
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

	/**
	 * On player left, remove specified player form this bot game status
	 * 
	 * @param name
	 *            name of player who left
	 */
	private void onPlayerLeft(String name) {
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

	/**
	 * On player replaced, replace specified player in this bot game status
	 * 
	 * @param botName
	 *            name of bot who replaced player in specified position
	 * @param position
	 *            position of player who left
	 */
	private void onPlayerReplaced(String botName, int position) {
		System.out.print("\n" + botName + ": notifyPlayerReplaced(" + botName
				+ ", " + position + ")");

		if (botName == null || position < 0 || position > 2)
			throw new IllegalArgumentException(position + " " + botName);

		if (initialTableStatus.opponents[position] == null) {
			(new NoSuchPlayerException(position)).printStackTrace();
			return;
		}
		initialTableStatus.opponents[position] = botName;
		initialTableStatus.whoIsBot[position] = true;
	}

	/**
	 * Pass arbitrary sound cards
	 */
	private void passCards() {
		Card[] cardsToPass = new Card[3];
		for (int i = 0; i < 3; i++)
			cardsToPass[i] = cards.get(i);
		processPassCards(cardsToPass);
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

	/**
	 * Add a play card action in the queue
	 */
	private void playCard() {
		actionQueue.enqueue(new Action() {
			@Override
			public void execute() {
				processPlayCard(chooseCard());
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

	/**
	 * Pass specified cards
	 * 
	 * @param cardsToPass
	 *            cards to pass
	 */
	private void processPassCards(Card[] cardsToPass) {
		try {
			System.out.println("\n" + botName + ": passCards("
					+ Arrays.toString(cardsToPass) + ")");
			setCardsPassed(cardsToPass);
			tableInterface.passCards(botName, cardsToPass);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPlayerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GameInterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WrongGameStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Play specified card
	 * 
	 * @param card
	 *            card to play
	 */
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
		} catch (GameInterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WrongGameStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Change this bot status when specified player play specified card
	 * 
	 * @param card
	 *            card played
	 * @param playerPosition
	 *            position of player who played
	 */
	private void setCardPlayed(Card card, int playerPosition) {
		if (firstDealer == -1) {
			firstDealer = playerPosition;
		}
		if (((firstDealer + playedCardCount) % 4) != playerPosition) {
			throw new IllegalStateException(" current player should be "
					+ ((firstDealer + playedCardCount) % 4) + " instead is "
					+ playerPosition + " " + botName + " first: " + firstDealer
					+ " count: " + playedCardCount);
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
				if (active && !cards.isEmpty()) {
					// TODO: Check this.
					playCard();
				}
			}
			Arrays.fill(playedCard, null);
		} else {
			if (playerPosition == 2) {
				if (active && !cards.isEmpty()) {
					// TODO: Check this.
					playCard();
				}
			}
		}
	}

	/**
	 * Change this bot status when this bot pass specified cards
	 * 
	 * @param cardsToPass
	 *            card played
	 */
	private void setCardsPassed(Card[] cardsToPass) {
		alreadyPassedCards = true;
		if (cardsToPass.length != 3)
			throw new IllegalArgumentException();
		for (int i = 0; i < 3; i++)
			cards.remove(cardsToPass[i]);
	}
}
