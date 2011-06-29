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

package unibo.as.cupido.backend.table.playerUI;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import unibo.as.cupido.backend.table.Action;
import unibo.as.cupido.backend.table.ActionQueue;
import unibo.as.cupido.backend.table.CardsManager;
import unibo.as.cupido.common.exception.GameInterruptedException;
import unibo.as.cupido.common.exception.NoSuchPlayerException;
import unibo.as.cupido.common.interfaces.ServletNotificationsInterface;
import unibo.as.cupido.common.interfaces.TableInterface;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.Card.Suit;
import unibo.as.cupido.common.structures.ChatMessage;
import unibo.as.cupido.common.structures.InitialTableStatus;
import unibo.as.cupido.common.structures.ObservedGameStatus;

public class RemoteBot implements Bot, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5795667604185475447L;
	final String userName;
	TableInterface singleTableManager;
	InitialTableStatus initialTableStatus;
	ArrayList<Card> cards;
	Card[] playedCard = new Card[4];
	private int turn = 0;
	private int playedCardCount = 0;
	private int firstDealer = -1;
	private boolean brokenHearted = false;
	private boolean ableToPlay = false;
	private boolean ableToPass = false;
	PrintWriter out;
	private int points = 0;
	public ObservedGameStatus observedGameStatus;
	private ActionQueue actionQueue = new ActionQueue();
	
	/**
	 * This is notified after a bot is added.
	 */
	private Object addBotSignal = new Object();

	/**
	 * This is notified after the cards have been passed.
	 */
	private Object passCardsSignal = new Object();

	/**
	 * This is notified after a card has been played.
	 */
	private Object playNextCardSignal = new Object();
	
	List<Command> pendingCommands = new ArrayList<Command>();
	
	public RemoteBot(InitialTableStatus initialTableStatus,
			TableInterface singleTableManager, final String userName)
			throws IOException {
		if (initialTableStatus == null || userName == null)
			throw new IllegalArgumentException();

		this.initialTableStatus = initialTableStatus;
		this.singleTableManager = singleTableManager;
		this.userName = userName;

		File outputFile = new File("cupidoBackendImpl/botlog/remote/"
				+ userName);
		outputFile.delete();
		outputFile.createNewFile();
		out = new PrintWriter(new FileWriter(outputFile));
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.err.println("shuting down remote user " + userName);
				out.close();
			}
		});

		actionQueue.start();
	}
	
	@Override
	public ServletNotificationsInterface getServletNotificationsInterface()
			throws RemoteException {
		return new ServletNotificationsInterface() {
			@Override
			public void notifyPlayerReplaced(final String botName, final int position)
					throws RemoteException {
				actionQueue.enqueue(new Action() {
					@Override
					public void execute() {
						System.out.println("ActionQueue: executing onPlayerReplaced()");
						onPlayerReplaced(botName, position);
						System.out.println("ActionQueue: exiting from onPlayerReplaced()");
					}
				});
			}
			
			@Override
			public void notifyPlayerLeft(final String playerName) throws RemoteException {
				actionQueue.enqueue(new Action() {
					@Override
					public void execute() {
						System.out.println("ActionQueue: executing onPlayerLeft()");
						onPlayerLeft(playerName);
						System.out.println("ActionQueue: exiting from onPlayerLeft()");
					}
				});
			}
			
			@Override
			public void notifyPlayerJoined(final String playerName, final boolean isBot, final int score,
					final int position) throws RemoteException {
				actionQueue.enqueue(new Action() {
					@Override
					public void execute() {
						System.out.println("ActionQueue: executing onPlayerjoined()");
						onPlayerJoined(playerName, isBot, score, position);
						System.out.println("ActionQueue: exiting from onPlayerJoined()");
					}
				});
			}
			
			@Override
			public void notifyPlayedCard(final Card card, final int playerPosition)
					throws RemoteException {
				actionQueue.enqueue(new Action() {
					@Override
					public void execute() {
						System.out.println("ActionQueue: executing onPlayedCard()");
						onPlayedCard(card, playerPosition);
						System.out.println("ActionQueue: exiting from onPlayedCard()");
					}
				});
			}
			
			@Override
			public void notifyPassedCards(final Card[] cards) throws RemoteException {
				actionQueue.enqueue(new Action() {
					@Override
					public void execute() {
						System.out.println("ActionQueue: executing onPassedCards()");
						onPassedCards(cards);
						System.out.println("ActionQueue: exiting from onPassedCards()");
					}
				});
			}
			
			@Override
			public void notifyLocalChatMessage(final ChatMessage message)
					throws RemoteException {
				actionQueue.enqueue(new Action() {
					@Override
					public void execute() {
						System.out.println("ActionQueue: executing onLocalChatMessage()");
						onLocalChatMessage(message);
						System.out.println("ActionQueue: exiting from onLocalChatMessage()");
					}
				});
			}
			
			@Override
			public void notifyGameStarted(final Card[] cards) throws RemoteException {
				actionQueue.enqueue(new Action() {
					@Override
					public void execute() {
						System.out.println("ActionQueue: executing onGameStarted()");
						onGameStarted(cards);
						System.out.println("ActionQueue: exiting from onGameStarted()");
					}
				});
			}
			
			@Override
			public void notifyGameEnded(final int[] matchPoints, final int[] playersTotalPoint)
					throws RemoteException {
				actionQueue.enqueue(new Action() {
					@Override
					public void execute() {
						System.out.println("ActionQueue: executing onGameEnded()");
						onGameEnded(matchPoints, playersTotalPoint);
						System.out.println("ActionQueue: exiting from onGameEnded()");
					}
				});
			}
		};
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
		if (playedCard[firstDealer] == null) {
			throw new Error("owned:" + cards.toString() + " first:"
					+ firstDealer + " played:" + Arrays.toString(playedCard)
					+ " count:" + playedCardCount);
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

	private void onGameEnded(int[] matchPoints, int[] playersTotalPoint) {
		out.println("\n" + userName + ": "
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + Arrays.toString(matchPoints) + ", "
				+ Arrays.toString(playersTotalPoint) + ")");
		out.close();
	}

	private void onGameStarted(Card[] cards) {
		this.cards = new ArrayList<Card>(13);
		for (int i = 0; i < cards.length; i++)
			this.cards.add(cards[i]);
		out.println("\n game started: " + cards.toString());
		ableToPass = true;
		processPendingCommands();
	}

	private void onLocalChatMessage(ChatMessage message) {
		out.println("\nlocal chat message: " + message);
	}

	private void onPassedCards(Card[] cards) {
		for (int i = 0; i < cards.length; i++)
			this.cards.add(cards[i]);
		out.println("\npassed cards received. all cards:"
				+ this.cards.toString());
		if (this.cards.contains(CardsManager.twoOfClubs)) {
			firstDealer = 3;
			ableToPlay = true;
			processPendingCommands();
		}
	}

	private void onPlayedCard(Card card, int playerPosition) {
		out.println("\n" + userName + " player " + playerPosition
				+ " played card " + card);

		setCardPlayed(card, playerPosition);
	}

	private void onPlayerJoined(String name, boolean isBot,
			int point, int position) {
		if (name == null || position < 0 || position > 2)
			throw new IllegalArgumentException(name + " " + position);
		if (initialTableStatus.opponents[position] != null)
			throw new IllegalArgumentException("Unable to add player " + name
					+ " in player " + userName + " beacuse ITS: "
					+ initialTableStatus
					+ " already contains a player in position " + position);
		initialTableStatus.opponents[position] = name;
		initialTableStatus.playerScores[position] = position;
		initialTableStatus.whoIsBot[position] = isBot;
	}

	private void onPlayerLeft(String name) {
		int position = 0;
		while (!name.equals(initialTableStatus.opponents[position]))
			position++;
		if (position == 3)
			throw new IllegalArgumentException("Player not found " + name);
		initialTableStatus.opponents[position] = null;
		out.println(initialTableStatus);
	}

	private void onPlayerReplaced(String botName, int position) {
		out.print("\n" + botName + ": notifyPlayerReplaced(" + botName + ", "
				+ position + ")");

		if (botName == null || position < 0 || position > 2)
			throw new IllegalArgumentException(position + " " + botName);

		if (initialTableStatus.opponents[position] == null) {
			(new NoSuchPlayerException()).printStackTrace();
			return;
		}
		initialTableStatus.opponents[position] = botName;
		initialTableStatus.whoIsBot[position] = true;
	}

	private boolean processPassCards() {
		
		if (!ableToPass)
			return false;
		
		System.out.println("ActionQueue: executing the body of processPassCards()");
		try {
			if (turn != 0) {
				throw new IllegalStateException();
			}
			Card[] cardsToPass = new Card[3];
			for (int i = 0; i < 3; i++)
				cardsToPass[i] = cards.remove(0);
			singleTableManager.passCards(userName, cardsToPass);
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
		} catch (GameInterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("ActionQueue: exiting from the body of processPassCards()");
		
		synchronized (passCardsSignal) {
			passCardsSignal.notify();
		}
		
		return true;
	}

	private boolean processPlayNextCard() {
		if (!ableToPlay)
			return false;
		
		System.out.println("ActionQueue: executing the body of processPlayNextCard()");
		try {
			assert turn != 13;
			
			ableToPlay = false;

			/** choose a valid card */
			Card cardToPlay = choseCard();

			/** play chosen card */
			singleTableManager.playCard(userName, cardToPlay);

			/** update status */
			setCardPlayed(cardToPlay, 3);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("ActionQueue: exiting from the body of processPassCards()");
		
		synchronized (playNextCardSignal) {
			playNextCardSignal.notify();
		}
		
		return true;
	}

	private void setCardPlayed(Card card, int playerPosition) {
		if (firstDealer == -1) {
			firstDealer = playerPosition;
		}
		if (((firstDealer + playedCardCount + 4) % 4) != playerPosition) {
			throw new IllegalStateException(" current player should be "
					+ ((firstDealer + playedCardCount + 4) % 4)
					+ " instead is " + playerPosition + " " + userName
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
				ableToPlay = true;
				processPendingCommands();
				for (Card c : playedCard) {
					if (c.suit == Suit.HEARTS)
						points++;
					else if (c.equals(CardsManager.queenOfSpades))
						points += 5;
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

	@Override
	public void addBot(final int position) {
		synchronized (addBotSignal) {
			actionQueue.enqueue(new Action() {
				@Override
				public void execute() {
					System.out.println("ActionQueue: executing addBot()");
					if (position < 0 || position > 2
							|| initialTableStatus.opponents[position] != null)
						throw new IllegalArgumentException("illegal position " + position
								+ " " + initialTableStatus.opponents[position]);
					initialTableStatus.opponents[position] = "_bot." + userName + "."
							+ position;
					initialTableStatus.whoIsBot[position] = true;
					System.out.println("ActionQueue: exiting from addBot()");
					synchronized (addBotSignal) {
						addBotSignal.notify();
					}
				}
			});
			try {
				addBotSignal.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void passCards() throws RemoteException {
		synchronized (passCardsSignal) {
			actionQueue.enqueue(new Action() {
				@Override
				public void execute() {
					System.out.println("ActionQueue: executing passCards()");
					tryProcessingPassCards();
					System.out.println("ActionQueue: exiting from passCards()");
				}
			});
			try {
				passCardsSignal.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void playNextCard() throws RemoteException, GameEndedException {
		synchronized (playNextCardSignal) {
			actionQueue.enqueue(new Action() {
				@Override
				public void execute() {
					System.out.println("ActionQueue: executing playNextCard()");
					tryProcessingPlayNextCard();
					System.out.println("ActionQueue: exiting from playNextCard()");
				}
			});
			try {
				playNextCardSignal.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void tryProcessingPassCards() {
		boolean executed = processPassCards();
		if (!executed)
			pendingCommands.add(new PassCardsCommand());
	}

	private void tryProcessingPlayNextCard() {
		boolean executed = processPlayNextCard();
		if (!executed)
			pendingCommands.add(new PlayNextCardCommand());
	}

	private void processPendingCommands() {
		List<Command> pendingCommands = this.pendingCommands;
		this.pendingCommands = new ArrayList<Command>();
		for (Command command : pendingCommands) {
			if (command instanceof PassCardsCommand) {
				tryProcessingPassCards();
			} else if (command instanceof PlayNextCardCommand) {
				tryProcessingPlayNextCard();
			} else {
				assert false;
			}
		}
	}
	
}
