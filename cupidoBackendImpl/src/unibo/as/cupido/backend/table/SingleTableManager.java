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

import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Arrays;

import unibo.as.cupido.common.database.DatabaseManager;
import unibo.as.cupido.common.exception.DuplicateUserNameException;
import unibo.as.cupido.common.exception.DuplicateViewerException;
import unibo.as.cupido.common.exception.EmptyTableException;
import unibo.as.cupido.common.exception.FullPositionException;
import unibo.as.cupido.common.exception.FullTableException;
import unibo.as.cupido.common.exception.IllegalMoveException;
import unibo.as.cupido.common.exception.NoSuchLTMException;
import unibo.as.cupido.common.exception.NoSuchLTMInterfaceException;
import unibo.as.cupido.common.exception.NoSuchPlayerException;
import unibo.as.cupido.common.exception.NoSuchTableException;
import unibo.as.cupido.common.exception.NoSuchUserException;
import unibo.as.cupido.common.exception.NoSuchViewerException;
import unibo.as.cupido.common.exception.NotCreatorException;
import unibo.as.cupido.common.exception.EmptyPositionException;
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
 * TODO missing all game status stuff
 * 
 * @author cane
 * 
 */
public class SingleTableManager implements TableInterface {

	private final CardsManager cardsManager;
	private final DatabaseManager databaseManager = new DatabaseManager();
	private final PlayersManager playersManager;
	private final TableInfoForClient table;
	private final ViewersSwarm viewers = new ViewersSwarm();
	private final GlobalTableManagerInterface gtm;
	private final StartNotifierThread startNotifierThread;
	private final EndNotifierThread endNotifierThread;
	private boolean gameStarted = false;
	private boolean gameEnded = false;
	private String owner;
	private TableInterface tableInterface;

	private GameStatus gameStatus = GameStatus.INIT;
	private STMControllerThread stmController;

	public static final String[] botNames = { "", "cupido", "venere", "marte" };

	public SingleTableManager(ServletNotificationsInterface snf,
			TableInfoForClient table, GlobalTableManagerInterface gtm)
			throws RemoteException, SQLException, NoSuchUserException,
			NoSuchLTMException {

		if (snf == null || table == null || gtm == null)
			throw new IllegalArgumentException(snf + " " + table + " " + gtm);
		this.table = table;
		this.gtm = gtm;
		this.owner = table.owner;
		playersManager = new PlayersManager(owner, snf,
				databaseManager.getPlayerScore(table.owner), new RemovalThread(
						this), databaseManager);

		// TODO use stmController only
		startNotifierThread = new StartNotifierThread(this);
		startNotifierThread.start();
		endNotifierThread = new EndNotifierThread(this);
		endNotifierThread.start();
		stmController = new STMControllerThread(this);
		stmController.start();
		//

		cardsManager = new CardsManager();
	}

	@Override
	public synchronized String addBot(String userName, int position)
			throws FullPositionException, RemoteException,
			IllegalArgumentException, FullTableException, NotCreatorException,
			IllegalStateException {
		if (this.gameStarted)
			throw new IllegalStateException();
		if (userName == null)
			throw new IllegalArgumentException();
		synchronized (startNotifierThread.lock) {
			try {
				String botName = botNames[position];

				if (tableInterface == null) {
					tableInterface = gtm.getLTMInterface(
							table.tableDescriptor.ltmId).getTable(
							table.tableDescriptor.id);
				}

				viewers.notifyBotJoined(botNames[position], position);

				playersManager.addBot(userName, position, botNames[position],
						tableInterface);
				playersManager.notifyBotJoined(botName, position);
				gtm.notifyTableJoin(table.tableDescriptor);
				if (playersManager.playersCount() == 4) {
					startNotifierThread.gameStarted = true;
					startNotifierThread.lock.notify();
				}
				return botName;
			} catch (NoSuchTableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchLTMException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		throw new Error();
	}

	@Override
	public synchronized InitialTableStatus joinTable(String userName,
			ServletNotificationsInterface snf) throws FullTableException,
			NoSuchTableException, RemoteException, IllegalArgumentException,
			IllegalStateException, DuplicateUserNameException, SQLException,
			NoSuchUserException {
		if (this.gameStarted)
			throw new IllegalStateException();
		if (userName == null || snf == null)
			throw new IllegalArgumentException();

		synchronized (startNotifierThread.lock) {
			int score = databaseManager.getPlayerScore(userName);
			int position = playersManager.addPlayer(userName, snf, score);
			playersManager.notifyPlayerJoined(userName, score, position);
			viewers.notifyPlayerJoined(userName, score, position);
			gtm.notifyTableJoin(table.tableDescriptor);
			if (playersManager.playersCount() == 4) {
				startNotifierThread.gameStarted = true;
				startNotifierThread.lock.notify();
			}
			return playersManager.getInitialTableStatus(position);
		}
	}

	public void leaveTable(Integer i) throws RemoteException,
			NoSuchPlayerException, NoSuchPlayerException {
		this.leaveTable(playersManager.getPlayerName(i));
	}

	@Override
	public synchronized void leaveTable(String userName)
			throws RemoteException, NoSuchPlayerException {
		if (userName == null)
			throw new IllegalArgumentException();

		if (cardsManager.gameEnded()) {
			throw new IllegalStateException(
					"game ended, cannot call leaveTable");
		}

		if (viewers.isAViewer(userName)) {
			System.out.println("viewer " + userName + " left");
			try {
				viewers.removeViewer(userName);
			} catch (NoSuchViewerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (table.owner.equals(userName)) {
			System.out.println("owner " + userName + " left 0");
			synchronized (endNotifierThread.lock) {
				this.gameEnded = true;
				endNotifierThread.gameEndedPrematurely = true;
				endNotifierThread.lock.notify();
			}
			System.out.println("owner " + userName + " left 1");
		} else if (gameStarted) {
			System.out.println("player " + userName
					+ " left after game start. Replaycing...");
			this.replacePlayer(userName);
		} else {
			System.out
					.println("player " + userName + " left before game start");
			playersManager.removePlayer(userName);
			playersManager.notifyPlayerLeft(userName);
			viewers.notifyPlayerLeft(userName);
			try {
				gtm.notifyTableLeft(table.tableDescriptor);
			} catch (NoSuchTableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (EmptyTableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public synchronized void notifyGameEnded() {
		gameEnded = true;
		int[] matchPoints = cardsManager.getMatchPoints();
		int[] playersTotalPoint = playersManager.updateScore(matchPoints);
		playersManager.notifyGameEnded(matchPoints, playersTotalPoint);
		viewers.notifyGameEnded(matchPoints, playersTotalPoint);
		this.notifyTableDestruction();
	}

	synchronized void notifyGameStarted() {
		gameStarted = true;
		playersManager.notifyGameStarted(cardsManager.getCards());
	}

	private void notifyTableDestruction() {
		try {
			LocalTableManagerInterface ltm = gtm
					.getLTMInterface(table.tableDescriptor.ltmId);
			ltm.notifyTableDestruction(table.tableDescriptor.id);
			gtm.notifyTableDestruction(table.tableDescriptor, ltm);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchLTMInterfaceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchLTMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchTableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public synchronized void passCards(String userName, Card[] cards)
			throws IllegalArgumentException, RemoteException,
			NoSuchPlayerException {

		if (!gameStarted || gameEnded)
			throw new IllegalStateException();

		System.out.println("\n single table manager passCards(" + userName + ", "
				+ Arrays.toString(cards) + ")");

		/*
		 * NOTE: userName is name of the player who passes cards. Not name of
		 * the player who receives the cards!
		 */
		if (userName == null || cards == null || cards.length != 3)
			throw new IllegalArgumentException(userName + " "
					+ Arrays.toString(cards));

		int position = playersManager.getPlayerPosition(userName);
		cardsManager.setCardPassing(position, cards);
		playersManager.replacementBotPassCards(position, cards);

		if (cardsManager.allPlayerPassedCards()) {
			System.out.println("stm >>> all player passed cards ");
			stmController.setAllPlayerPassedCards();
		}
	}

	@Override
	public synchronized void playCard(String userName, Card card)
			throws IllegalMoveException, RemoteException,
			IllegalArgumentException, NoSuchPlayerException {
		System.out.println("single table manager play card " + userName + " "
				+ card);
		if (!gameStarted || gameEnded)
			throw new IllegalStateException();
		if (userName == null || card == null)
			throw new IllegalArgumentException("userName " + userName
					+ " card " + card);
		int playerPosition = playersManager.getPlayerPosition(userName);
		cardsManager.playCard(userName, playerPosition, card);
		playersManager.replacementBotPlayCard(playerPosition, card);
		playersManager.notifyPlayedCard(userName, card);
		viewers.notifyPlayedCard(playerPosition, card);
		synchronized (endNotifierThread.lock) {
			if (cardsManager.gameEnded()) {
				this.gameEnded = true;
				endNotifierThread.gameEnded = true;
				endNotifierThread.lock.notify();
			}
		}
	}

	private void replacePlayer(String playerName) throws NoSuchPlayerException {
		try {
			int position = playersManager.getPlayerPosition(playerName);
			if (tableInterface == null) {
				tableInterface = gtm.getLTMInterface(
						table.tableDescriptor.ltmId).getTable(
						table.tableDescriptor.id);
			}
			playersManager.replacePlayer(playerName, position, tableInterface);
			playersManager.notifyPlayerReplaced(playerName, position);
			viewers.notifyPlayerReplaced(playerName, position);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchTableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchLTMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FullPositionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EmptyPositionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public synchronized void sendMessage(ChatMessage message)
			throws RemoteException {
		if (message == null || message.message == null
				|| message.userName == null)
			throw new IllegalArgumentException();
		playersManager.notifyNewLocalChatMessage(message);
		viewers.notifyNewLocalChatMessage(message);
	}

	@Override
	public synchronized ObservedGameStatus viewTable(String viewerName,
			ServletNotificationsInterface snf) throws DuplicateViewerException,
			RemoteException {
		if (gameEnded)
			throw new IllegalStateException();
		if (viewerName == null || snf == null)
			throw new IllegalArgumentException();
		viewers.addViewer(viewerName, snf);
		ObservedGameStatus observedGameStatus = new ObservedGameStatus();
		playersManager.addPlayersInformationForViewers(observedGameStatus);
		cardsManager.addCardsInformationForViewers(observedGameStatus);
		return observedGameStatus;
	}

	public synchronized void notifyGameEndedPrematurely() {
		System.err.println(">>>>>>>>>>>>>>> 0");
		this.gameEnded = true;
		System.err.println(">>>>>>>>>>>>>>> 1");
		playersManager.notifyGameEndedPrematurely();
		System.err.println(">>>>>>>>>>>>>>> 2");
		viewers.notifyGameEndedPrematurely();
		System.err.println(">>>>>>>>>>>>>>> 3");
		this.notifyTableDestruction();
		System.err.println(">>>>>>>>>>>>>>> 4");
	}

	public synchronized void notifyPassedCards() {
		for (int i = 0; i < 4; i++) {
			playersManager.notifyPassedCards((i + 5) % 4,
					cardsManager.getPassedCards(i));
		}
	}
}
