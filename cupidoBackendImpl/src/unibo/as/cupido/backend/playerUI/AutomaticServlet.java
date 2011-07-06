/*  Cupido - An online Hearts game.
 *  Copyright (C) 2011 Lorenzo Belli, Marco Poletti, Federico Viscomi
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHSystem.out ANY WARRANTY; withSystem.out even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package unibo.as.cupido.backend.playerUI;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import unibo.as.cupido.backend.table.Action;
import unibo.as.cupido.backend.table.ActionQueue;
import unibo.as.cupido.backend.table.CardsManager;
import unibo.as.cupido.common.exception.GameEndedException;
import unibo.as.cupido.common.exception.GameInterruptedException;
import unibo.as.cupido.common.exception.NoSuchPlayerException;
import unibo.as.cupido.common.exception.WrongGameStateException;
import unibo.as.cupido.common.interfaces.ServletNotificationsInterface;
import unibo.as.cupido.common.interfaces.TableInterface;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.Card.Suit;
import unibo.as.cupido.common.structures.ChatMessage;
import unibo.as.cupido.common.structures.InitialTableStatus;

/**
 * This is used by player console user interface to emulate a real user servlet.
 */
public class AutomaticServlet {

	/**
	 * This automatic servelet notification interface
	 */
	public class AutomaticServletNotificationInterface implements
			ServletNotificationsInterface, Serializable {

		/**
		 * generated serial version uid
		 */
		private static final long serialVersionUID = -1499894147888629423L;

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
		public void notifyPlayedCard(final Card card, final int playerPosition)
				throws RemoteException {
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
	}

	/** this servlet user name */
	final String userName;
	/** reference to an stm */
	TableInterface singleTableManager;
	/** initial table status */
	InitialTableStatus initialTableStatus;
	/** cards eventually owned by this */
	ArrayList<Card> cards;
	/** cards eventually playd in current trick */
	Card[] playedCard = new Card[4];
	/** number of trick played */
	private int turn = 0;
	/** number of cards played in current trick */
	private int playedCardCount = 0;
	/** position of first dealer in trick */
	private int firstDealerInTrick = -1;
	/** <tt>true</tt> if game is broken hearte; <tt>false</tt> otherwise */
	private boolean brokenHearted = false;
	/** <tt>true</tt> if this can play a card; <tt>false</tt>otherwise */
	private boolean ableToPlay = false;
	/** <tt>true</tt> if this can pass cards; <tt>false</tt>otherwise */
	private boolean ableToPass = false;
	/** this player current score */
	private int points = 0;
	/** execute action */
	ActionQueue actionQueue = new ActionQueue();
	/** This is notified after a bot is added. */
	private Object addBotSignal = new Object();
	/** This is notified after the cards have been passed. */
	private Object passCardsSignal = new Object();
	/** This is notified after a card has been played. */
	private Object playNextCardSignal = new Object();
	/** list of pending commands to be executed */
	List<Command> pendingCommands = new ArrayList<Command>();

	/**
	 * Create an automatic servlet
	 * 
	 * @param initialTableStatus
	 *            status of table given by create or join
	 * @param singleTableManager
	 *            stm interface of the table
	 * @param userName
	 *            name of this player
	 */
	public AutomaticServlet(InitialTableStatus initialTableStatus,
			TableInterface singleTableManager, final String userName) {
		if (initialTableStatus == null || userName == null) {
			throw new IllegalArgumentException();
		}

		this.initialTableStatus = initialTableStatus;
		this.singleTableManager = singleTableManager;
		this.userName = userName;

		actionQueue.start();
	}

	/**
	 * If this is creator of a table, add a bot in specified position.
	 * 
	 * @param position
	 *            position of bot to add
	 */
	public void addBot(final int position) {
		synchronized (addBotSignal) {
			actionQueue.enqueue(new Action() {
				@Override
				public void execute() {
					if (position < 0 || position > 2
							|| initialTableStatus.opponents[position] != null) {
						throw new IllegalArgumentException("illegal position "
								+ position + " "
								+ initialTableStatus.opponents[position]);
					}
					initialTableStatus.opponents[position] = "_bot." + userName
							+ "." + position;
					initialTableStatus.whoIsBot[position] = true;
					synchronized (addBotSignal) {
						addBotSignal.notify();
					}
				}
			});
			try {
				addBotSignal.wait();
			} catch (InterruptedException e) {
				//
			}
		}
	}

	/**
	 * Get all cards that are sound to play according to game rules
	 * 
	 * @return all cards that are sound to play according to game rules
	 */
	private ArrayList<Card> getAllSoundCards() {
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
		if (playedCard[firstDealerInTrick] == null) {
			throw new Error("owned:" + cards.toString() + " first:"
					+ firstDealerInTrick + " played:"
					+ Arrays.toString(playedCard) + " count:" + playedCardCount);
		}
		for (int i = 0; i < cards.size(); i++) {
			if (cards.get(i).suit == playedCard[firstDealerInTrick].suit) {
				validCards.add(cards.get(i));
			}
		}
		if (validCards.size() == 0) {
			validCards.addAll(cards);
		}
		return validCards;
	}

	/**
	 * Get a card that is sound to play according to game rules
	 * 
	 * @return a card that is sound to play according to game rules
	 */
	private Card getASoundCard() {
		return getAllSoundCards().get(0);
	}

	/**
	 * Return an objects who handles notification from the stm to this
	 * 
	 * @return an objects who handles notification from the stm to this
	 */
	public ServletNotificationsInterface getServletNotificationsInterface() {
		return this.new AutomaticServletNotificationInterface();
	}

	/**
	 * On game ended just log and exit.
	 * 
	 * @param matchPoints
	 *            all players point in this match
	 * @param playersTotalPoint
	 *            new players score after this match
	 */
	void onGameEnded(int[] matchPoints, int[] playersTotalPoint) {
		System.out.println("\n" + userName + ": "
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + Arrays.toString(matchPoints) + ", "
				+ Arrays.toString(playersTotalPoint) + ")");
		actionQueue.killConsumer();
	}

	/**
	 * On game started take cards and eventually pass
	 * 
	 * @param cards
	 *            the cards dealt to this player
	 */
	void onGameStarted(Card[] cards) {
		this.cards = new ArrayList<Card>(13);
		for (int i = 0; i < cards.length; i++) {
			this.cards.add(cards[i]);
		}
		System.out.println("\n game started: " + Arrays.toString(cards));
		ableToPass = true;
		processPendingCommands();
	}

	/**
	 * On local chat message just print the message
	 * 
	 * @param message
	 *            the new local chat message
	 */
	void onLocalChatMessage(ChatMessage message) {
		System.out.println("\nlocal chat message: " + message);
	}

	/**
	 * On passed cards, take cards and eventually play
	 * 
	 * @param cards
	 *            the cards received from an other player
	 */
	void onPassedCards(Card[] cards) {
		for (int i = 0; i < cards.length; i++) {
			this.cards.add(cards[i]);
		}
		System.out.println("\npassed cards received. all cards:"
				+ this.cards.toString());
		if (this.cards.contains(CardsManager.twoOfClubs)) {
			firstDealerInTrick = 3;
			ableToPlay = true;
			processPendingCommands();
		}
	}

	/**
	 * On played card, change this bot game status accordingly and the
	 * eventually play
	 * 
	 * @param card
	 *            the card played
	 * @param playerPosition
	 *            position of played who played
	 */
	void onPlayedCard(Card card, int playerPosition) {
		System.out.println("\n" + userName + " player " + playerPosition
				+ " played card " + card);
		setCardPlayed(card, playerPosition);
	}

	/**
	 * On player joined, add specified player.
	 * 
	 * @param name
	 *            name of player who joined
	 * @param isBot
	 *            <tt>true</tt> if joined player is a bot; <tt>false</tt>
	 *            otherwise
	 * @param score
	 *            score of joined player, meaningfull if and only if
	 *            <tt>isBot==true</tt>
	 * @param position
	 *            position of player who joined
	 */
	void onPlayerJoined(String name, boolean isBot, int score, int position) {
		System.out.println("\n player joined(" + name + ", " + isBot + ", "
				+ score + "," + position);
		if (name == null || position < 0 || position > 2) {
			throw new IllegalArgumentException(name + " " + position);
		}
		if (initialTableStatus.opponents[position] != null) {
			throw new IllegalArgumentException("Unable to add player " + name
					+ " in player " + userName + " beacuse ITS: "
					+ initialTableStatus
					+ " already contains a player in position " + position);
		}
		initialTableStatus.opponents[position] = name;
		initialTableStatus.playerScores[position] = position;
		initialTableStatus.whoIsBot[position] = isBot;
	}

	/**
	 * On player left, remove specified player.
	 * 
	 * @param name
	 *            name of player who left
	 */
	void onPlayerLeft(String name) {
		System.out.println("\n player left(" + name + ")");
		int position = 0;
		while (!name.equals(initialTableStatus.opponents[position]))
			position++;
		if (position == 3) {
			throw new IllegalArgumentException("Player not found " + name);
		}
		initialTableStatus.opponents[position] = null;
		System.out.println(initialTableStatus);
	}

	/**
	 * On player replaced, replace player specified by <tt>position</tt> with
	 * bot <tt>botName</tt>
	 * 
	 * @param botName
	 *            name of bot who replaces the left player
	 * @param position
	 *            position of player who left
	 */
	void onPlayerReplaced(String botName, int position) {
		System.out.print("\n" + botName + ": notifyPlayerReplaced(" + botName
				+ ", " + position + ")");

		if (botName == null || position < 0 || position > 2) {
			throw new IllegalArgumentException(position + " " + botName);
		}

		if (initialTableStatus.opponents[position] == null) {
			(new NoSuchPlayerException(position)).printStackTrace();
			return;
		}
		initialTableStatus.opponents[position] = botName;
		initialTableStatus.whoIsBot[position] = true;
	}

	/**
	 * Pass arbitrary sound cards.
	 */
	public void passCards() {
		synchronized (passCardsSignal) {
			actionQueue.enqueue(new Action() {
				@Override
				public void execute() {
					tryProcessingPassCards();
				}
			});
			try {
				passCardsSignal.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Play arbitrary sound card.
	 * 
	 * @throws GameEndedException
	 *             if game ended.
	 */
	public void playNextCard() throws GameEndedException {
		synchronized (playNextCardSignal) {
			actionQueue.enqueue(new Action() {
				@Override
				public void execute() {
					tryProcessingPlayNextCard();
				}
			});
			try {
				playNextCardSignal.wait();
			} catch (InterruptedException e) {
				//
				e.printStackTrace();
			}
		}
	}

	/**
	 * Pass arbitrary cards
	 * 
	 * @return <tt>true</tt> if this passed cards; <tt>false</tt> otherwise.
	 */
	private boolean processPassCards() {

		if (!ableToPass) {
			return false;
		}

		try {
			if (turn != 0) {
				throw new IllegalStateException();
			}
			Card[] cardsToPass = new Card[3];
			for (int i = 0; i < 3; i++) {
				cardsToPass[i] = cards.remove(0);
			}
			singleTableManager.passCards(userName, cardsToPass);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NoSuchPlayerException e) {
			e.printStackTrace();
		} catch (GameInterruptedException e) {
			e.printStackTrace();
		} catch (WrongGameStateException e) {
			e.printStackTrace();
		}

		synchronized (passCardsSignal) {
			passCardsSignal.notify();
		}

		return true;
	}

	/**
	 * Process pending commands.
	 */
	private void processPendingCommands() {
		List<Command> pendingCommands = this.pendingCommands;
		this.pendingCommands = new ArrayList<Command>();
		for (Command command : pendingCommands) {
			if (command instanceof PassCardsCommand) {
				tryProcessingPassCards();
			} else if (command instanceof PlayNextCardCommand) {
				tryProcessingPlayNextCard();
			}
		}
	}

	/**
	 * Play arbitrary cards.
	 * 
	 * @return <tt>true</tt> if this succeeded in playing a card; <tt>false</tt>
	 *         otherwise, i.e. this was not able to play now.
	 */
	private boolean processPlayNextCard() {
		if (!ableToPlay) {
			return false;
		}

		try {
			assert turn != 13;

			ableToPlay = false;

			/** choose a valid card */
			Card cardToPlay = getASoundCard();

			/** play chosen card */
			singleTableManager.playCard(userName, cardToPlay);

			/** update status */
			setCardPlayed(cardToPlay, 3);
		} catch (Exception e) {
			e.printStackTrace();
		}

		synchronized (playNextCardSignal) {
			playNextCardSignal.notify();
		}

		return true;
	}

	/**
	 * Change this bot game status after player in position
	 * <tt>playerPosition</tt> played card <tt>card</tt>
	 * 
	 * @param card
	 *            card played
	 * @param playerPosition
	 *            position of player who played
	 */
	private void setCardPlayed(Card card, int playerPosition) {
		if (firstDealerInTrick == -1) {
			firstDealerInTrick = playerPosition;
		}
		if (((firstDealerInTrick + playedCardCount + 4) % 4) != playerPosition) {
			throw new IllegalStateException(" current player should be "
					+ ((firstDealerInTrick + playedCardCount + 4) % 4)
					+ " instead is " + playerPosition + " " + userName
					+ " first: " + firstDealerInTrick + " count: "
					+ playedCardCount);
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
			firstDealerInTrick = CardsManager.whoWins(playedCard,
					firstDealerInTrick);
			playedCardCount = 0;
			turn++;
			if (firstDealerInTrick == 3) {
				ableToPlay = true;
				processPendingCommands();
				for (Card c : playedCard) {
					if (c.suit == Suit.HEARTS) {
						points++;
					} else if (c.equals(CardsManager.queenOfSpades)) {
						points += 5;
					}
				}
			}
			Arrays.fill(playedCard, null);
		} else {
			if (playerPosition == 2) {
				ableToPlay = true;
				processPendingCommands();
			}
		}
	}

	/**
	 * Try to pass cards. If this is not able to pass rigth now, add a pass
	 * cards command in pending commands that will be executed later
	 */
	void tryProcessingPassCards() {
		boolean executed = processPassCards();
		if (!executed) {
			pendingCommands.add(new PassCardsCommand());
		}
	}

	/**
	 * Try to play a card. If this is not able to play rigth now, add a pass
	 * cards command in pending commands that will be executed later
	 */
	void tryProcessingPlayNextCard() {
		boolean executed = processPlayNextCard();
		if (!executed) {
			pendingCommands.add(new PlayNextCardCommand());
		}
	}

}
