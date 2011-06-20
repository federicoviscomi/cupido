package unibo.as.cupido.backendInterfacesImpl.table;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

import unibo.as.cupido.backendInterfacesImpl.table.bot.NonRemoteBot;
import unibo.as.cupido.common.interfaces.GlobalTableManagerInterface;
import unibo.as.cupido.common.interfaces.ServletNotificationsInterface;
import unibo.as.cupido.common.interfaces.TableInterface;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.ChatMessage;
import unibo.as.cupido.common.structures.InitialTableStatus;
import unibo.as.cupido.common.structures.ObservedGameStatus;
import unibo.as.cupido.common.structures.TableInfoForClient;
import unibo.as.cupido.common.database.DatabaseManager;
import unibo.as.cupido.common.exception.DuplicateUserNameException;
import unibo.as.cupido.common.exception.FullTableException;
import unibo.as.cupido.common.exception.IllegalMoveException;
import unibo.as.cupido.common.exception.NoSuchLTMException;
import unibo.as.cupido.common.exception.NoSuchTableException;
import unibo.as.cupido.common.exception.NoSuchUserException;
import unibo.as.cupido.common.exception.NotCreatorException;
import unibo.as.cupido.common.exception.PlayerNotFoundException;
import unibo.as.cupido.common.exception.PositionFullException;

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
	private final Integer turns = new Integer(0);
	private final Semaphore start;
	private final Semaphore end;
	private final GlobalTableManagerInterface gtm;

	public SingleTableManager(ServletNotificationsInterface snf,
			TableInfoForClient table, GlobalTableManagerInterface gtm)
			throws RemoteException, SQLException, NoSuchUserException {
		if (snf == null || table == null || gtm == null)
			throw new IllegalArgumentException(snf + " " + table + " " + gtm);
		this.table = table;
		this.gtm = gtm;
		playersManager = new PlayersManager(table.owner, snf,
				databaseManager.getPlayerScore(table.owner), new RemovalThread(
						this));
		start = new Semaphore(0);
		end = new Semaphore(0);
		new StartNotifierThread(start, this).start();
		new EndNotifierThread(end, this).start();
		cardsManager = new CardsManager();
	}

	@Override
	public synchronized void addBot(String userName, int position)
			throws PositionFullException, RemoteException,
			IllegalArgumentException, FullTableException, NotCreatorException,
			IllegalStateException {
		System.out.print("\n\n\nSingleTableManager inizio ."
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + userName + ", " + position + ").\n Table status is: ");
		playersManager.print();
		System.out.println();

		try {
			String botName = "_bot." + userName + "." + position;
			InitialTableStatus initialTableStatus = playersManager
					.getInitialTableStatus(position);

			NonRemoteBot bot = new NonRemoteBot(botName, initialTableStatus,
					gtm.getLTMInterface(table.tableDescriptor.ltmId).getTable(
							table.tableDescriptor.id));
			viewers.notifyBotJoined(botName, position);

			playersManager.addNonRemoteBot(userName, position, bot);
			if (playersManager.playersCount() == 4)
				start.release();

		} catch (NoSuchTableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchLTMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.print("\nSingleTableManager fine ."
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + userName + ", " + position + ").\n Table status is:");
		playersManager.print();
		System.out.println();
	}

	@Override
	public synchronized InitialTableStatus joinTable(String userName,
			ServletNotificationsInterface snf) throws FullTableException,
			NoSuchTableException, RemoteException, IllegalArgumentException,
			IllegalStateException, DuplicateUserNameException, SQLException,
			NoSuchUserException {
		System.out.print("\n\n\n SingleTableManager inizio ."
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + userName + ", " + snf + "). \n Table status is:");
		playersManager.print();
		System.out.println();

		if (userName == null || snf == null)
			throw new IllegalArgumentException();
		int score = databaseManager.getPlayerScore(userName);
		int position = playersManager.addPlayer(userName, snf, score);
		viewers.notifyPlayerJoined(userName, score, position);
		if (playersManager.playersCount() == 4)
			start.release();

		System.out.print("\n\n\n SingleTableManager fine ."
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + userName + ", " + snf + "). \n Table status is:");
		playersManager.print();
		System.out.println();
		return playersManager.getInitialTableStatus(position);
	}

	public synchronized void leaveTable(Integer i) throws RemoteException,
			PlayerNotFoundException {
		this.leaveTable(playersManager.getPlayerName(i));
	}

	@Override
	public synchronized void leaveTable(String userName)
			throws RemoteException, PlayerNotFoundException {
		System.out.println("\n"
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + userName + ")");
		if (userName == null)
			throw new IllegalArgumentException();
		playersManager.removePlayer(userName);
		viewers.notifyPlayerLeft(userName);

	}

	public void notifyGameEnded() {
		System.out.println("\n"
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "()");
		int[] matchPoints = cardsManager.getMatchPoints();
		int[] playersTotalPoint = playersManager.updateScore(matchPoints);
		playersManager.notifyGameEnded(matchPoints, playersTotalPoint);
		viewers.notifyGameEnded(matchPoints, playersTotalPoint);
	}

	synchronized void notifyGameStarted() {
		System.out.println("\nSingleTableManager."
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "()1");
		playersManager.notifyGameStarted(cardsManager.getCards());
		System.out.println("\nSingleTableManager."
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "()2");
		cardsManager.startGame();
		System.out.println("\nSingleTableManager."
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "()3");
	}

	@Override
	public synchronized void passCards(String userName, Card[] cards)
			throws IllegalArgumentException, RemoteException {
		/*
		 * NOTE: userName is name of the player who passes cards. Not name of
		 * the player who receives the cards!
		 */
		System.out.println("\n SingleTableManager."
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + userName + ", " + Arrays.toString(cards) + ")");
		// playersManager.print();
		// cardsManager.print();
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
		System.out.println("\n"
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + userName + ", " + card + ")");
		if (userName == null || card == null)
			throw new IllegalArgumentException();
		int playerPosition = playersManager.getPlayerPosition(userName);
		cardsManager.playCard(playerPosition, card);
		playersManager.notifyPlayedCard(userName, card);
		viewers.notifyPlayedCard(playerPosition, card);
		if (cardsManager.gameEnded())
			end.release();
	}

	@Override
	public synchronized void sendMessage(ChatMessage message)
			throws RemoteException {
		System.out.println("\n"
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + message + ")");
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
		System.out.println("\n"
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + viewerName + ", " + snf + ")");
		if (viewerName == null || snf == null)
			throw new IllegalArgumentException();
		viewers.addViewer(viewerName, snf);
		ObservedGameStatus observedGameStatus = new ObservedGameStatus();
		playersManager.addPlayersInformationForViewers(observedGameStatus);
		cardsManager.addCardsInformationForViewers(observedGameStatus);
		return observedGameStatus;
	}

}
