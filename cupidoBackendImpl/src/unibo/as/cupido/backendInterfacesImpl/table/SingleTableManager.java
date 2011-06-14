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
			ServletNotificationsInterface sni = new DummyLoggerServletNotifyer();
			SingleTableManager stm = new SingleTableManager(sni,
					new TableInfoForClient("Owner", 0, new TableDescriptor(
							"servercane", 34453)), null);
			stm.addBot("bot", 1);
			stm.joinTable("Cane", sni);
			stm.joinTable("Gatto", sni);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private final CardsManager cardsManager = new CardsManager();
	private final BotManager botManager = new BotManager();
	private final DatabaseManager databaseManager = new DatabaseManager();
	private final PlayersManager playersManager;
	private final ToBeNotifyed toNotify;
	private final TableInfoForClient table;

	public SingleTableManager(ServletNotificationsInterface snf,
			TableInfoForClient table, GlobalTableManagerInterface gtm)
			throws RemoteException, SQLException, NoSuchUserException {
		this.table = table;
		toNotify = new ToBeNotifyed(table.owner, snf);
		playersManager = new PlayersManager(table.owner, databaseManager);
	}

	@Override
	public void addBot(String userName, int position)
			throws PositionFullException, RemoteException,
			IllegalArgumentException, FullTableException, NotCreatorException,
			IllegalStateException {
		playersManager.addBot(userName, position);
		toNotify.addBot("_bot." + userName, botManager.chooseBotStrategy(
				getInitialTableStatus(position), this));
		toNotify.notifyBotJoined("_bot." + userName, position);
	}

	private InitialTableStatus getInitialTableStatus(int position) {
		String[] opponents = new String[3];
		int[] playerPoints = new int[3];
		boolean[] whoIsBot = new boolean[3];
		for (int i = 0; i < 3; i++) {
			PlayerInfo nextOpponent = playersManager.players[(position + i + 1) % 4];
			if (nextOpponent != null) {
				opponents[i] = nextOpponent.name;
				playerPoints[i] = nextOpponent.score;
				whoIsBot[i] = nextOpponent.isBot;
			}
		}
		return new InitialTableStatus(opponents, playerPoints, whoIsBot);
	}

	@Override
	public InitialTableStatus joinTable(String userName,
			ServletNotificationsInterface snf) throws FullTableException,
			NoSuchTableException, RemoteException, IllegalArgumentException,
			IllegalStateException, DuplicateUserNameException, SQLException,
			NoSuchUserException {
		if (userName == null || snf == null)
			throw new IllegalArgumentException();
		int position = playersManager.addPlayer(userName);
		toNotify.notifyPlayerJoined(userName, position, snf);
		toNotify.addPlayer(userName, position, snf);

		// SCHIFO
		if (playersManager.playersCount == 4) {
			ArrayList<String> nbpl = playersManager.nonBotPlayersName();
			nbpl.remove(userName);
			for (String p : nbpl)
				toNotify.notifyGameStarted(p,
						cardsManager.cards[playersManager.getPlayerPosition(p)]);
		}
		return getInitialTableStatus(position);
	}

	@Override
	public void leaveTable(String userName) throws RemoteException,
			PlayerNotFoundException {
		if (userName == null)
			throw new IllegalArgumentException();
		playersManager.removePlayer(userName);
		toNotify.removePlayer(userName);
		toNotify.notifyPlayerLeft(userName);
	}

	@Override
	public void passCards(String userName, Card[] cards)
			throws IllegalArgumentException, RemoteException {
		if (userName == null || cards == null || cards.length != 3)
			throw new IllegalArgumentException();
		cardsManager.setCardPassing(playersManager.getPlayerPosition(userName),
				cards);
		toNotify.notifyCardPassed(cards, userName);
	}

	@Override
	public void playCard(String userName, Card card)
			throws IllegalMoveException, RemoteException,
			IllegalArgumentException {
		if (userName == null || card == null)
			throw new IllegalArgumentException();
		int playerPosition = playersManager.getPlayerPosition(userName);
		cardsManager.playCard(playerPosition, card);
		toNotify.notifyCardPlayed(userName, card, playerPosition);
	}

	@Override
	public void sendMessage(ChatMessage message) throws RemoteException {
		if (message == null || message.message == null
				|| message.userName == null)
			throw new IllegalArgumentException();
		toNotify.notifyNewChatMessage(message);
	}

	@Override
	public ObservedGameStatus viewTable(String userName,
			ServletNotificationsInterface snf) throws NoSuchTableException,
			RemoteException {
		if (userName == null || snf == null)
			throw new IllegalArgumentException();
		toNotify.notifyViewerJoined(userName);
		toNotify.addViewer(userName, snf);
		PlayerStatus[] playerStatus = new PlayerStatus[4];
		for (int i = 0; i < 4; i++) {
			if (playersManager.players[i] != null) {
				playerStatus[i].name = playersManager.players[i].name;
				playerStatus[i].isBot = playersManager.players[i].isBot;
				playerStatus[i].score = playersManager.players[i].score;
				playerStatus[i].numOfCardsInHand = cardsManager.cards[i].size();
				playerStatus[i].playedCard = cardsManager.cardPlayed[i];
			}
		}
		return new ObservedGameStatus(playerStatus);
	}

	@Override
	public TableInfoForClient getTable() throws RemoteException {
		return table;
	}

}
