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

	public SingleTableManager(ServletNotificationsInterface snf,
			TableInfoForClient table, GlobalTableManagerInterface gtm)
			throws RemoteException, SQLException, NoSuchUserException,
			NoSuchLTMException {
		if (snf == null || table == null || gtm == null)
			throw new IllegalArgumentException(snf + " " + table + " " + gtm);
		this.table = table;
		this.gtm = gtm;
		playersManager = new PlayersManager(table.owner, snf,
				databaseManager.getPlayerScore(table.owner), new RemovalThread(
						this), databaseManager);
		startNotifierThread = new StartNotifierThread(this);
		startNotifierThread.start();
		endNotifierThread = new EndNotifierThread(this);
		endNotifierThread.start();
		cardsManager = new CardsManager();
	}

	@Override
	public synchronized void addBot(String userName, int position)
			throws PositionFullException, RemoteException,
			IllegalArgumentException, FullTableException, NotCreatorException,
			IllegalStateException {
		try {
			String botName = "_bot." + userName + "." + position;
			InitialTableStatus initialTableStatus = playersManager
					.getInitialTableStatus(position);

			
			NonRemoteBot bot = new NonRemoteBot(botName, initialTableStatus,
					gtm.getLTMInterface(table.tableDescriptor.ltmId).getTable(table.tableDescriptor.id));
			viewers.notifyBotJoined(botName, position);

			playersManager.addBot(userName, position, bot);
			if (playersManager.playersCount() == 4) {
				startNotifierThread.setGameStarted();
			}

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
		viewers.notifyPlayerJoined(userName, score, position);
		if (playersManager.playersCount() == 4) {
			startNotifierThread.setGameStarted();
		}
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
		playersManager.removePlayer(userName);
		viewers.notifyPlayerLeft(userName);

	}

	public void notifyGameEnded() {
		int[] matchPoints = cardsManager.getMatchPoints();
		int[] playersTotalPoint = playersManager.updateScore(matchPoints);
		playersManager.notifyGameEnded(matchPoints, playersTotalPoint);
		viewers.notifyGameEnded(matchPoints, playersTotalPoint);
		try {
			LocalTableManagerInterface ltm = gtm.getLTMInterface(table.tableDescriptor.ltmId);
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
		}
	}

	synchronized void notifyGameStarted() {
		playersManager.notifyGameStarted(cardsManager.getCards());
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
		playersManager.notifyPlayedCard(userName, card);
		viewers.notifyPlayedCard(playerPosition, card);
		if (cardsManager.gameEnded()) {
			endNotifierThread.setGameEnded();
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
