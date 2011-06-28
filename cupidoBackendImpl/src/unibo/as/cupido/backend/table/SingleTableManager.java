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

import unibo.as.cupido.backend.table.AsynchronousMessage.AddPlayerMessage;
import unibo.as.cupido.backend.table.AsynchronousMessage.PlayerPassCardsMessage;
import unibo.as.cupido.backend.table.AsynchronousMessage.PlayerPlayCardsMessage;
import unibo.as.cupido.backend.table.AsynchronousMessage.ReplacePlayerMessage;
import unibo.as.cupido.backend.table.AsynchronousMessage.SendLocalChatMessage;
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
	private final DatabaseManager databaseManager;
	private final PlayersManager playersManager;
	private final TableInfoForClient table;
	private final ViewersSwarm viewers;
	private final GlobalTableManagerInterface gtm;
	private final String owner;
	private final Controller controller;

	private GameStatus gameStatus;
	private TableInterface tableInterface;

	public static final String[] botNames = { "", "cupido", "venere", "marte" };

	public SingleTableManager(ServletNotificationsInterface snf,
			TableInfoForClient table, GlobalTableManagerInterface gtm)
			throws RemoteException, SQLException, NoSuchUserException,
			NoSuchLTMException {

		if (snf == null || table == null || gtm == null) {
			throw new IllegalArgumentException(snf + " " + table + " " + gtm);
		}

		this.table = table;
		this.gtm = gtm;
		this.owner = table.owner;
		this.gameStatus = GameStatus.INIT;
		this.viewers = new ViewersSwarm();
		this.databaseManager = new DatabaseManager();
		this.controller = new Controller(this);
		this.playersManager = new PlayersManager(owner, snf, databaseManager,
				controller);
		this.cardsManager = new CardsManager();
		this.controller.start();
	}

	@Override
	public synchronized String addBot(String userName, int position)
			throws FullPositionException, RemoteException,
			IllegalArgumentException, FullTableException, NotCreatorException,
			IllegalStateException {
		if (!gameStatus.equals(GameStatus.INIT)) {
			throw new IllegalStateException();
		}
		if (userName == null || position < 0 || position > 3) {
			throw new IllegalArgumentException();
		}
		try {
			String botName = botNames[position];

			if (tableInterface == null) {
				tableInterface = gtm.getLTMInterface(
						table.tableDescriptor.ltmId).getTable(
						table.tableDescriptor.id);
			}

			playersManager.addBot(userName, position, botNames[position],
					tableInterface);

			for (int i = 1; i < 4; i++) {
				if (i != position) {
					controller
							.produceMessageSendPlayerJoinedNotification(
									botName,
									true,
									0,
									position,
									playersManager.players[i].playerNotificationInterface);
				}
			}
			//viewers.notifyPlayerJoined(playerName, isBot, score, position)
			if (playersManager.playersCount() == 4) {
				gameStatus = GameStatus.PASSING_CARDS;
				this.setStartGame();
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
		throw new Error();
	}

	private void setStartGame() {
		for (int i = 0; i < 4; i++) {
			controller.produceMessageSendStartGameNotification(
					playersManager.players[i].playerNotificationInterface,
					cardsManager.cards[i]);
		}
	}

	@Override
	public synchronized InitialTableStatus joinTable(String userName,
			ServletNotificationsInterface snf) throws FullTableException,
			NoSuchTableException, RemoteException, IllegalArgumentException,
			IllegalStateException, DuplicateUserNameException, SQLException,
			NoSuchUserException {
		if (!gameStatus.equals(GameStatus.INIT)) {
			throw new IllegalStateException();
		}
		if (userName == null || snf == null) {
			throw new IllegalArgumentException();
		}

		int score = databaseManager.getPlayerScore(userName);
		int position = playersManager.addPlayer(userName, snf, score);

		controller.produceAddPlayer(userName, false, score, position);
		if (playersManager.playersCount() == 4) {
			gameStatus = GameStatus.PASSING_CARDS;
			// controller.produceStartGame();
		}
		return playersManager.getInitialTableStatus(position);
	}

	@Override
	public synchronized void leaveTable(String userName)
			throws RemoteException, NoSuchPlayerException {
		if (userName == null) {
			throw new IllegalArgumentException();
		}

		if (cardsManager.gameEnded()) {
			throw new IllegalStateException(
					"game ended, cannot call leaveTable");
		}

		if (viewers.isAViewer(userName)) {
			try {
				viewers.removeViewer(userName);
			} catch (NoSuchViewerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (table.owner.equals(userName)) {
			controller.produceEndGamePrematurely();
		} else if (!gameStatus.equals(GameStatus.INIT)) {
			int position = playersManager.getPlayerPosition(userName);
			if (tableInterface == null) {
				try {
					tableInterface = gtm.getLTMInterface(
							table.tableDescriptor.ltmId).getTable(
							table.tableDescriptor.id);
				} catch (NoSuchTableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchLTMException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			playersManager.replacePlayer(userName, position, tableInterface);
			controller.produceReplacePlayer(userName, position);
		} else {
			playersManager.removePlayer(userName);
			controller.producePlayerLeave(userName);
		}
	}

	public synchronized void notifyGameEnded() {
		int[] matchPoints = cardsManager.getMatchPoints();
		int[] playersTotalPoint = playersManager.updateScore(matchPoints);
		playersManager.notifyGameEnded(matchPoints, playersTotalPoint);
		viewers.notifyGameEnded(matchPoints, playersTotalPoint);
		this.notifyTableDestruction();
	}

	public synchronized void notifyGameEndedPrematurely() {
		playersManager.notifyGameEndedPrematurely();
		viewers.notifyGameEndedPrematurely();
		this.notifyTableDestruction();
	}

	public synchronized void notifyGameStarted() {
		playersManager.notifyGameStarted(cardsManager.getCards());
	}

	public synchronized void notifyLocalChatMessage(SendLocalChatMessage message) {
		playersManager.notifyNewLocalChatMessage(message.message);
		viewers.notifyNewLocalChatMessage(message.message);
	}

	public synchronized void notifyPassedCards() {
		for (int i = 0; i < 4; i++) {
			playersManager.notifyPlayerPassedCards((i + 5) % 4,
					cardsManager.getPassedCards(i));
		}
	}

	public void notifyPlayerJoined(AddPlayerMessage message) {

		if (message.isBot) {
			playersManager
					.notifyBotJoined(message.playerName, message.position);
		} else {
			playersManager.notifyPlayerJoined(message.playerName,
					message.score, message.position);
		}
		viewers.notifyPlayerJoined(message.playerName, message.isBot,
				message.score, message.position);
		try {
			gtm.notifyTableJoin(table.tableDescriptor);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchTableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FullTableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public synchronized void notifyPlayerLeft(String userName) {
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
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public synchronized void notifyPlayerPassedCards(
			PlayerPassCardsMessage message) {
		playersManager.notifyPlayerPassedCards(message.position, message.cards);
	}

	public synchronized void notifyPlayerPlayedCard(
			PlayerPlayCardsMessage message) {
		try {
			playersManager.notifyPlayedCard(message.playerName, message.card);
		} catch (NoSuchPlayerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		viewers.notifyPlayedCard(message.position, message.card);
	}

	public synchronized void notifyPlayerReplaced(ReplacePlayerMessage message) {
		try {
			playersManager.notifyPlayerReplaced(message.playerName,
					message.position);
			viewers.notifyPlayerReplaced(message.playerName, message.position);
		} catch (FullPositionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EmptyPositionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPlayerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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

		if (!gameStatus.equals(GameStatus.PASSING_CARDS))
			throw new IllegalStateException();
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

		controller.producePassCards(userName, position, cards);
		if (cardsManager.allPlayerPassedCards()) {
			gameStatus = GameStatus.STARTED;
			controller.produceAllPassedCards();
		}
	}

	@Override
	public synchronized void playCard(String userName, Card card)
			throws IllegalMoveException, RemoteException,
			IllegalArgumentException, NoSuchPlayerException {
		if (!gameStatus.equals(GameStatus.STARTED))
			throw new IllegalStateException();
		if (userName == null || card == null)
			throw new IllegalArgumentException("playerName " + userName
					+ " card " + card);
		int playerPosition = playersManager.getPlayerPosition(userName);
		cardsManager.playCard(userName, playerPosition, card);
		playersManager.replacementBotPlayCard(playerPosition, card);
		controller.producePlayCard(userName, playerPosition, card);
		if (cardsManager.gameEnded()) {
			gameStatus = GameStatus.ENDED;
			controller.produceEndGame();
		}
	}

	@Override
	public synchronized void sendMessage(ChatMessage message)
			throws RemoteException {
		if (message == null || message.message == null
				|| message.userName == null)
			throw new IllegalArgumentException();

		controller.produceSendLocalChatMessage(message);
	}

	@Override
	public synchronized ObservedGameStatus viewTable(String viewerName,
			ServletNotificationsInterface snf) throws DuplicateViewerException,
			RemoteException {
		if (gameStatus.equals(GameStatus.ENDED))
			throw new IllegalStateException();
		if (viewerName == null || snf == null)
			throw new IllegalArgumentException();
		viewers.addViewer(viewerName, snf);
		ObservedGameStatus observedGameStatus = new ObservedGameStatus();
		playersManager.addPlayersInformationForViewers(observedGameStatus);
		cardsManager.addCardsInformationForViewers(observedGameStatus);
		return observedGameStatus;
	}

}
