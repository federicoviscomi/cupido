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

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Arrays;

import unibo.as.cupido.common.database.DatabaseManager;
import unibo.as.cupido.common.exception.DuplicateUserNameException;
import unibo.as.cupido.common.exception.DuplicateViewerException;
import unibo.as.cupido.common.exception.EmptyTableException;
import unibo.as.cupido.common.exception.FullPositionException;
import unibo.as.cupido.common.exception.FullTableException;
import unibo.as.cupido.common.exception.GameInterruptedException;
import unibo.as.cupido.common.exception.IllegalMoveException;
import unibo.as.cupido.common.exception.NoSuchLTMException;
import unibo.as.cupido.common.exception.NoSuchPlayerException;
import unibo.as.cupido.common.exception.NoSuchTableException;
import unibo.as.cupido.common.exception.NoSuchUserException;
import unibo.as.cupido.common.exception.NoSuchViewerException;
import unibo.as.cupido.common.exception.NotCreatorException;
import unibo.as.cupido.common.exception.WrongGameStateException;
import unibo.as.cupido.common.interfaces.DatabaseInterface;
import unibo.as.cupido.common.interfaces.GlobalTableManagerInterface;
import unibo.as.cupido.common.interfaces.LocalTableManagerInterface;
import unibo.as.cupido.common.interfaces.ServletNotificationsInterface;
import unibo.as.cupido.common.interfaces.TableInterface;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.ChatMessage;
import unibo.as.cupido.common.structures.InitialTableStatus;
import unibo.as.cupido.common.structures.ObservedGameStatus;
import unibo.as.cupido.common.structures.TableInfoForClient;

/**
 * Manages a single table
 */
public class SingleTableManager implements TableInterface {

	/** handles this table cards */
	private final CardsManager cardsManager;
	/** used to communicate with the database */
	private final DatabaseManager databaseManager;
	/** handles this table players including bots */
	private final PlayersManager playersManager;
	/** this table info */
	private final TableInfoForClient table;
	/** handles viewers of this table */
	private final ViewersSwarm viewers;
	/** interface of GTM */
	private final GlobalTableManagerInterface gtm;
	/** name of player who created the table */
	private final String creator;
	/** queue of action this STM has to execute */
	private final ActionQueue actionQueue;
	/** stores game current status */
	private GameStatus gameStatus;
	/** stores name used for bots */
	public static final String[] botNames = { "", "cupido", "marte", "venere" };

	public SingleTableManager(ServletNotificationsInterface snf,
			TableInfoForClient table, GlobalTableManagerInterface gtm,
			String databaseAddress) throws SQLException, NoSuchUserException,
			NoSuchLTMException {

		if (snf == null || table == null || gtm == null) {
			throw new IllegalArgumentException(snf + " " + table + " " + gtm);
		}

		this.table = table;
		this.gtm = gtm;
		this.creator = table.creator;
		this.gameStatus = GameStatus.INIT;
		this.databaseManager = new DatabaseManager(databaseAddress);
		this.actionQueue = new ActionQueue();
		this.actionQueue.start();
		this.viewers = new ViewersSwarm(actionQueue);
		this.playersManager = new PlayersManager(creator, snf, databaseManager,
				actionQueue);
		this.cardsManager = new CardsManager();
	}

	@Override
	public synchronized String addBot(String userName, int position)
			throws FullPositionException, IllegalArgumentException,
			NotCreatorException, GameInterruptedException {
		if (gameStatus.equals(GameStatus.INTERRUPTED))
			throw new GameInterruptedException();
		if (!gameStatus.equals(GameStatus.INIT))
			throw new FullPositionException(position);

		if (userName == null || position < 0 || position > 3) {
			throw new IllegalArgumentException();
		}
		String botName = botNames[position];

		playersManager.addBot(userName, position, botNames[position], this);

		notifyPlayerJoined(botName, true, 0, position);

		if (playersManager.getPlayersCount() == 4) {
			notifyGameStarted();
			gameStatus = GameStatus.PASSING_CARDS;
			// controller.produceStartGame();
		}
		return botName;
	}

	@Override
	public synchronized InitialTableStatus joinTable(String userName,
			ServletNotificationsInterface snf) throws FullTableException,
			IllegalArgumentException, DuplicateUserNameException,
			NoSuchUserException, GameInterruptedException {
		if (gameStatus.equals(GameStatus.INTERRUPTED))
			throw new GameInterruptedException();
		if (!gameStatus.equals(GameStatus.INIT))
			throw new FullTableException();
		if (userName == null || snf == null)
			throw new IllegalArgumentException();

		int score;
		try {
			score = databaseManager.getPlayerScore(userName);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new NoSuchUserException(userName);
		}
		int position = playersManager.addPlayer(userName, snf, score);

		notifyPlayerJoined(userName, false, score, position);

		if (playersManager.getPlayersCount() == 4) {
			notifyGameStarted();
			gameStatus = GameStatus.PASSING_CARDS;
		}
		return playersManager.getInitialTableStatus(position);
	}

	@Override
	public synchronized void leaveTable(String userName)
			throws IllegalArgumentException, NoSuchPlayerException,
			GameInterruptedException {
		if (userName == null)
			throw new IllegalArgumentException();
		if (gameStatus == GameStatus.INTERRUPTED)
			throw new GameInterruptedException();

		if (cardsManager.gameEnded() || gameStatus == GameStatus.ENDED) {
			if (viewers.isAViewer(userName)) {
				try {
					viewers.removeViewer(userName);
				} catch (NoSuchViewerException e) {
					//
				}
			} else {
				playersManager.removePlayer(userName);
			}
			if (playersManager.nonBotPlayersCount() == 0) {
				actionQueue.killConsumer();
				viewers.killConsumer();
			}
			return;
		}

		if (viewers.isAViewer(userName)) {
			try {
				viewers.removeViewer(userName);
			} catch (NoSuchViewerException e) {
				e.printStackTrace();
			}
		} else if (table.creator.equals(userName)) {

			gameStatus = GameStatus.INTERRUPTED;
			notifyGameEndedPrematurely();

		} else if (!gameStatus.equals(GameStatus.INIT)) {
			int position = playersManager.getPlayerPosition(userName);
			playersManager.replacePlayer(userName, position, this);

			notifyPlayerReplaced(userName, position);

		} else {
			playersManager.removePlayer(userName);

			notifyPlayerLeft(userName);
		}
	}

	/**
	 * Notify every players and viewers that game ended. Also notify table
	 * destruction to gtm and stm.
	 */
	private void notifyGameEnded() {
		int[] matchPoints = cardsManager.getMatchPoints();
		int[] playersTotalPoint = playersManager.updateScore(matchPoints);
		playersManager.notifyGameEnded(matchPoints, playersTotalPoint);
		viewers.notifyGameEnded(matchPoints, playersTotalPoint);
		this.notifyTableDestruction();
	}

	/**
	 * Notify every players and viewers that game ended prematurely. Also notify
	 * table destruction to gtm and stm.
	 */
	private void notifyGameEndedPrematurely() {
		playersManager.notifyGameEndedPrematurely();
		viewers.notifyGameEndedPrematurely();
		this.notifyTableDestruction();
	}

	/**
	 * Notify every players that game started.
	 */
	private void notifyGameStarted() {
		playersManager.notifyGameStarted(cardsManager.getCards());
	}

	/**
	 * Notify every other player and viewers that a player joined.
	 * 
	 * @param userName
	 *            name of player who joined
	 * @param isBot
	 *            <tt>true</tt> if the player who joined is a bot;
	 *            <tt>false</tt> otherwise.
	 * @param score
	 *            score of player who joined
	 * @param position
	 *            position of player who joined
	 */
	private void notifyPlayerJoined(String userName, boolean isBot, int score,
			int position) {

		if (isBot) {
			playersManager.notifyBotJoined(userName, position);
		} else {
			playersManager.notifyPlayerJoined(userName, score, position);
		}
		viewers.notifyPlayerJoined(userName, isBot, score, position);
		actionQueue.enqueue(new RemoteAction() {
			@Override
			public void onExecute() throws RemoteException {
				try {
					gtm.notifyTableJoin(table.tableDescriptor);
				} catch (NoSuchTableException e) {
					e.printStackTrace();
				} catch (FullTableException e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Notify every other player and viewers that a player left.
	 * 
	 * @param userName
	 *            name of player who left.
	 */
	private void notifyPlayerLeft(String userName) {
		playersManager.notifyPlayerLeft(userName);
		viewers.notifyPlayerLeft(userName);
		actionQueue.enqueue(new RemoteAction() {
			@Override
			public void onExecute() throws RemoteException {
				try {
					gtm.notifyTableLeft(table.tableDescriptor);
				} catch (NoSuchTableException e) {
					e.printStackTrace();
				} catch (EmptyTableException e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Notify the receiver player that player in specified position passed
	 * specified cards
	 * 
	 * @param position
	 *            position of player who passed cards
	 * @param cards
	 *            cards passed
	 */
	private void notifyPlayerPassedCards(int position, Card[] cards) {
		playersManager.notifyPlayerPassedCards(position, cards);
	}

	/**
	 * Notify every players and viewers that specified player played specified
	 * card
	 * 
	 * @param playerName
	 *            name of player who played card
	 * @param position
	 *            position of player who playd card
	 * @param card
	 *            card played
	 */
	private void notifyPlayerPlayedCard(String playerName, int position,
			Card card) {
		try {
			playersManager.notifyPlayedCard(playerName, card);
		} catch (NoSuchPlayerException e) {
			e.printStackTrace();
		}
		viewers.notifyPlayedCard(position, card);
	}

	/**
	 * Notify every players and viewers that specified player has been replaced
	 * 
	 * @param playerName
	 *            name of player who has been replaced
	 * @param position
	 *            position of player who has been replaced
	 */
	private void notifyPlayerReplaced(String playerName, int position) {
		try {
			playersManager.notifyPlayerReplaced(playerName, position);
			viewers.notifyPlayerReplaced(playerName, position);
		} catch (NoSuchPlayerException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Notify gtm and stm of this table destruction.
	 */
	private void notifyTableDestruction() {
		actionQueue.enqueue(new RemoteAction() {
			@Override
			public void onExecute() throws RemoteException {
				try {
					LocalTableManagerInterface ltm = gtm
							.getLTMInterface(table.tableDescriptor.ltmId);
					gtm.notifyTableDestruction(table.tableDescriptor, ltm);
					ltm.notifyTableDestruction(table.tableDescriptor.id);
				} catch (NoSuchLTMException e) {
					e.printStackTrace();
				} catch (NoSuchTableException e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public synchronized void passCards(String userName, Card[] cards)
			throws IllegalArgumentException, NoSuchPlayerException,
			GameInterruptedException, WrongGameStateException {

		System.out.println("STM: entering passCards(" + userName + ", {...})");

		if (gameStatus.equals(GameStatus.INTERRUPTED))
			throw new GameInterruptedException();
		if (!gameStatus.equals(GameStatus.PASSING_CARDS))
			throw new WrongGameStateException(
					GameStatus.PASSING_CARDS.toString());
		/*
		 * NOTE: playerName is name of the player who passes cards. Not name of
		 * the player who receives the cards!
		 */
		if (userName == null || cards == null || cards.length != 3)
			throw new IllegalArgumentException(userName + " "
					+ Arrays.toString(cards));

		int position = playersManager.getPlayerPosition(userName);
		cardsManager.setCardPassing(position, cards);
		playersManager.replacementBotPassCards(position, cards);

		if (cardsManager.allPlayerPassedCards()) {
			gameStatus = GameStatus.STARTED;

			for (int i = 0; i < 4; i++)
				notifyPlayerPassedCards(i, cardsManager.getPassedCards(i));
		}

		System.out.println("STM: exiting from passCards(" + userName
				+ ", {...})");
	}

	@Override
	public synchronized void playCard(String userName, Card card)
			throws IllegalMoveException, IllegalArgumentException,
			NoSuchPlayerException, GameInterruptedException,
			WrongGameStateException {
		if (gameStatus.equals(GameStatus.INTERRUPTED))
			throw new GameInterruptedException();
		if (!gameStatus.equals(GameStatus.STARTED))
			throw new WrongGameStateException(GameStatus.STARTED.toString());
		if (userName == null || card == null)
			throw new IllegalArgumentException("playerName " + userName
					+ " card " + card);
		int playerPosition = playersManager.getPlayerPosition(userName);
		cardsManager.playCard(userName, playerPosition, card);
		playersManager.replacementBotPlayCard(playerPosition, card);

		notifyPlayerPlayedCard(userName, playerPosition, card);
		if (cardsManager.gameEnded()) {
			gameStatus = GameStatus.ENDED;
			notifyGameEnded();
		}
	}

	@Override
	public synchronized void sendMessage(final ChatMessage message)
			throws GameInterruptedException {
		if (gameStatus == GameStatus.INTERRUPTED)
			throw new GameInterruptedException();
		if (message == null || message.message == null
				|| message.userName == null)
			throw new IllegalArgumentException();
		playersManager.notifyNewLocalChatMessage(message);
		viewers.notifyNewLocalChatMessage(message);
	}

	@Override
	public synchronized ObservedGameStatus viewTable(String viewerName,
			ServletNotificationsInterface snf) throws DuplicateViewerException,
			WrongGameStateException, GameInterruptedException {
		if (gameStatus == GameStatus.INTERRUPTED)
			throw new GameInterruptedException();
		if (gameStatus == GameStatus.ENDED)
			throw new WrongGameStateException(GameStatus.ENDED.toString());
		if (viewerName == null || snf == null)
			throw new IllegalArgumentException();
		viewers.addViewer(viewerName, snf);
		ObservedGameStatus observedGameStatus = new ObservedGameStatus();
		playersManager.addPlayersInformationForViewers(observedGameStatus);
		cardsManager.addCardsInformationForViewers(observedGameStatus);
		return observedGameStatus;
	}
}
