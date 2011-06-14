package unibo.as.cupido.backendInterfacesImpl.table;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;

import unibo.as.cupido.backendInterfaces.GlobalTableManagerInterface;
import unibo.as.cupido.backendInterfaces.ServletNotificationsInterface;
import unibo.as.cupido.backendInterfaces.TableInterface;
import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.ChatMessage;
import unibo.as.cupido.backendInterfaces.common.InitialTableStatus;
import unibo.as.cupido.backendInterfaces.common.ObservedGameStatus;
import unibo.as.cupido.backendInterfaces.common.PlayerStatus;
import unibo.as.cupido.backendInterfaces.common.TableDescriptor;
import unibo.as.cupido.backendInterfaces.common.TableInfoForClient;
import unibo.as.cupido.backendInterfaces.exception.DuplicateUserNameException;
import unibo.as.cupido.backendInterfaces.exception.FullTableException;
import unibo.as.cupido.backendInterfaces.exception.IllegalMoveException;
import unibo.as.cupido.backendInterfaces.exception.NoSuchTableException;
import unibo.as.cupido.backendInterfaces.exception.NoSuchUserException;
import unibo.as.cupido.backendInterfaces.exception.NotCreatorException;
import unibo.as.cupido.backendInterfaces.exception.PlayerNotFoundException;
import unibo.as.cupido.backendInterfaces.exception.PositionFullException;
import unibo.as.cupido.backendInterfacesImpl.DummyLoggerServletNotifyer;
import unibo.as.cupido.backendInterfacesImpl.database.DatabaseManager;
import unibo.as.cupido.backendInterfacesImpl.table.PlayersManager.PlayerInfo;
import unibo.as.cupido.backendInterfacesImpl.table.bot.Bot;
import unibo.as.cupido.backendInterfacesImpl.table.bot.BotManager;

/**
 * TODO missing all game status stuff
 * 
 * @author cane
 * 
 */
public class SingleTableManager implements TableInterface {

	public static void main(String[] args) throws Exception {
		try {
			// TODO implement the following class
			ServletNotificationsInterface ownerSni = new DummyLoggerServletNotifyer(
					"Owner");
			SingleTableManager stm = new SingleTableManager(ownerSni,
					new TableInfoForClient("Owner", 0, new TableDescriptor(
							"servercane", 34453)), null);
			stm.addBot("Owner", 1);
			stm.joinTable("Cane", new DummyLoggerServletNotifyer("Cane"));
			stm.joinTable("Gatto", new DummyLoggerServletNotifyer("Gatto"));
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private final CardsManager cardsManager = new CardsManager();
	private final BotManager botManager = new BotManager();
	private final DatabaseManager databaseManager = new DatabaseManager();
	private final PlayersManager playersManager;
	private final TableInfoForClient table;
	private final ViewersSwarm viewers = new ViewersSwarm();

	public SingleTableManager(ServletNotificationsInterface snf,
			TableInfoForClient table, GlobalTableManagerInterface gtm)
			throws RemoteException, SQLException, NoSuchUserException {
		this.table = table;
		playersManager = new PlayersManager(table.owner, snf,
				databaseManager.getPlayerScore(table.owner), cardsManager);
	}

	@Override
	public void addBot(String userName, int position)
			throws PositionFullException, RemoteException,
			IllegalArgumentException, FullTableException, NotCreatorException,
			IllegalStateException {
		Bot bot = botManager.chooseBotStrategy(
				playersManager.getInitialTableStatus(position), this, "_bot."
						+ userName);
		playersManager.addBot(userName, position, bot);
		viewers.notifyBotJoined("_bot." + userName, position);
	}

	@Override
	public InitialTableStatus joinTable(String userName,
			ServletNotificationsInterface snf) throws FullTableException,
			NoSuchTableException, RemoteException, IllegalArgumentException,
			IllegalStateException, DuplicateUserNameException, SQLException,
			NoSuchUserException {
		if (userName == null || snf == null)
			throw new IllegalArgumentException();

		int score = databaseManager.getPlayerScore(userName);
		int position = playersManager.addPlayer(userName, snf, score);
		viewers.notifyPlayerJoined(userName, score, position);
		return playersManager.getInitialTableStatus(position);
	}

	@Override
	public void leaveTable(String userName) throws RemoteException,
			PlayerNotFoundException {
		if (userName == null)
			throw new IllegalArgumentException();
		playersManager.removePlayer(userName);
		viewers.notifyPlayerLeft(userName);
	}

	@Override
	public void passCards(String userName, Card[] cards)
			throws IllegalArgumentException, RemoteException {
		if (userName == null || cards == null || cards.length != 3)
			throw new IllegalArgumentException();
		int position = playersManager.getPlayerPosition(userName);
		cardsManager.setCardPassing(position, cards);
		playersManager.notifyPassedCards(position, cards);
	}

	@Override
	public void playCard(String userName, Card card)
			throws IllegalMoveException, RemoteException,
			IllegalArgumentException {
		if (userName == null || card == null)
			throw new IllegalArgumentException();
		int playerPosition = playersManager.getPlayerPosition(userName);
		cardsManager.playCard(playerPosition, card);
		playersManager.notifyPlayedCard(userName, card);
		viewers.notifyPlayedCard(playerPosition, card);
	}

	@Override
	public void sendMessage(ChatMessage message) throws RemoteException {
		if (message == null || message.message == null
				|| message.userName == null)
			throw new IllegalArgumentException();
		playersManager.notifyNewLocalChatMessage(message);
		viewers.notifyNewLocalChatMessage(message);
	}

	@Override
	public ObservedGameStatus viewTable(String viewerName,
			ServletNotificationsInterface snf) throws NoSuchTableException,
			RemoteException {
		if (viewerName == null || snf == null)
			throw new IllegalArgumentException();
		viewers.addViewer(viewerName, snf);
		PlayerStatus[] playerStatus = new PlayerStatus[4];
		playersManager.addPlayersInformationForViewers(playerStatus);
		cardsManager.addCardsInformationForViewers(playerStatus);
		return new ObservedGameStatus(playerStatus);
	}

	@Override
	public TableInfoForClient getTable() throws RemoteException {
		return table;
	}

}
