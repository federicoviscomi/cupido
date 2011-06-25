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

import unibo.as.cupido.backend.table.bot.NonRemoteBot;
import unibo.as.cupido.common.database.DatabaseManager;
import unibo.as.cupido.common.exception.DuplicateUserNameException;
import unibo.as.cupido.common.exception.FullTableException;
import unibo.as.cupido.common.exception.IllegalMoveException;
import unibo.as.cupido.common.exception.NoSuchLTMException;
import unibo.as.cupido.common.exception.NoSuchLTMInterfaceException;
import unibo.as.cupido.common.exception.NoSuchTableException;
import unibo.as.cupido.common.exception.NoSuchUserException;
import unibo.as.cupido.common.exception.NotCreatorException;
import unibo.as.cupido.common.exception.PlayerNotFoundException;
import unibo.as.cupido.common.exception.PositionEmptyException;
import unibo.as.cupido.common.exception.PositionFullException;
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
	private String owner;
	private boolean[] passCardsNotificationSent = new boolean[4];

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
		startNotifierThread = new StartNotifierThread(this);
		startNotifierThread.start();
		endNotifierThread = new EndNotifierThread(this);
		endNotifierThread.start();
		cardsManager = new CardsManager();
	}

	@Override
	public synchronized String addBot(String userName, int position)
			throws PositionFullException, RemoteException,
			IllegalArgumentException, FullTableException, NotCreatorException,
			IllegalStateException {
		try {

			String botName = botNames[position];

			InitialTableStatus initialTableStatus = playersManager
					.getInitialTableStatus(position);

			NonRemoteBot bot = new NonRemoteBot(botName, initialTableStatus,
					gtm.getLTMInterface(table.tableDescriptor.ltmId).getTable(
							table.tableDescriptor.id));
			viewers.notifyBotJoined(botNames[position], position);

			playersManager.addBot(userName, position, bot, botNames[position]);
			playersManager.notifyBotJoined(botName, position);
			if (playersManager.playersCount() == 4) {
				startNotifierThread.setGameStarted();
			}
			gtm.notifyTableJoin(table.tableDescriptor);

			return botName;
		} catch (NoSuchTableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		} catch (NoSuchLTMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// FIXME: This must never be reached.
		return "";
	}

	@Override
	public synchronized InitialTableStatus joinTable(String userName,
			ServletNotificationsInterface snf) throws FullTableException,
			NoSuchTableException, RemoteException, IllegalArgumentException,
			IllegalStateException, DuplicateUserNameException, SQLException,
			NoSuchUserException {
		if (userName == null || snf == null)
			throw new IllegalArgumentException();
		int score = databaseManager.getPlayerScore(userName);
		int position = playersManager.addPlayer(userName, snf, score);
		playersManager.notifyPlayerJoined(userName, score, position);
		viewers.notifyPlayerJoined(userName, score, position);
		if (playersManager.playersCount() == 4) {
			startNotifierThread.setGameStarted();
		}
		gtm.notifyTableJoin(table.tableDescriptor);
		return playersManager.getInitialTableStatus(position);
	}

	public synchronized void leaveTable(Integer i) throws RemoteException,
			PlayerNotFoundException {
		this.leaveTable(playersManager.getPlayerName(i));
	}

	@Override
	public synchronized void leaveTable(String userName)
			throws RemoteException, PlayerNotFoundException {
		if (userName == null)
			throw new IllegalArgumentException();

		if (viewers.isAViewer(userName)) {
			viewers.removeViewer(userName);
			return;
		}
		if (cardsManager.gameEnded()) {
			throw new IllegalStateException(
					"game ended, cannot call leaveTable");
		}

		if (table.owner.equals(userName)) {
			this.notifyGameEndedPrematurely();
		} else {
			if (gameStarted) {
				this.replacePlayer(userName);
			} else {
				playersManager.removePlayer(userName);
				playersManager.notifyPlayerLeft(userName);
				viewers.notifyPlayerLeft(userName);
			}
		}
	}

	public void notifyGameEnded() {
		int[] matchPoints = cardsManager.getMatchPoints();
		int[] playersTotalPoint = playersManager.updateScore(matchPoints);
		playersManager.notifyGameEnded(matchPoints, playersTotalPoint);
		viewers.notifyGameEnded(matchPoints, playersTotalPoint);
		this.notifyTableDestruction();
	}

	private void notifyGameEndedPrematurely() {
		playersManager.notifyGameEnded(null, null);
		viewers.notifyGameEnded(null, null);
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
			throws IllegalArgumentException, RemoteException {
		/*
		 * NOTE: userName is name of the player who passes cards. Not name of
		 * the player who receives the cards!
		 */
		if (userName == null || cards == null || cards.length != 3)
			throw new IllegalArgumentException(userName + " "
					+ Arrays.toString(cards));
		int position = playersManager.getPlayerPosition(userName);
		cardsManager.setCardPassing(position, cards);
		int receiver = (position + 1) % 4;
		passCardsNotificationSent[receiver] = true;
		playersManager.replacementBotPassCards(position, cards);
		playersManager.notifyPassedCards(receiver, cards);
	}

	@Override
	public synchronized void playCard(String userName, Card card)
			throws IllegalMoveException, RemoteException,
			IllegalArgumentException {
		if (userName == null || card == null)
			throw new IllegalArgumentException("userName " + userName
					+ " card " + card);
		int playerPosition = playersManager.getPlayerPosition(userName);
		cardsManager.playCard(playerPosition, card);
		playersManager.replacementBotPlayCard(playerPosition, card);
		playersManager.notifyPlayedCard(userName, card);
		viewers.notifyPlayedCard(playerPosition, card);
		if (cardsManager.gameEnded()) {
			endNotifierThread.setGameEnded();
		}
	}

	private void replacePlayer(String playerName)
			throws PlayerNotFoundException {


			try {
				int position = playersManager.getPlayerPosition(playerName);
				String botName = SingleTableManager.botNames[position];
				playersManager.replacePlayer(
						playerName,
						position,
						gtm.getLTMInterface(table.tableDescriptor.ltmId).getTable(
								table.tableDescriptor.id));
				playersManager.notifyPlayerReplaced(playerName, botName, position);
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
			} catch (PositionFullException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (PositionEmptyException e) {
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
			ServletNotificationsInterface snf) throws NoSuchTableException,
			RemoteException {
		if (viewerName == null || snf == null)
			throw new IllegalArgumentException();
		viewers.addViewer(viewerName, snf);
		ObservedGameStatus observedGameStatus = new ObservedGameStatus();
		playersManager.addPlayersInformationForViewers(observedGameStatus);
		cardsManager.addCardsInformationForViewers(observedGameStatus);
		return observedGameStatus;
	}

}
