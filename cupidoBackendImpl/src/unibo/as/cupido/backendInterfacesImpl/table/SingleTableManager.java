package unibo.as.cupido.backendInterfacesImpl.table;

import java.rmi.RemoteException;

import unibo.as.cupido.backendInterfaces.DatabaseInterface;
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
import unibo.as.cupido.backendInterfaces.exception.NotCreatorException;
import unibo.as.cupido.backendInterfaces.exception.PlayerNotFoundException;
import unibo.as.cupido.backendInterfaces.exception.PositionFullException;
import unibo.as.cupido.backendInterfacesImpl.DummyLoggerServletNotifyer;
import unibo.as.cupido.backendInterfacesImpl.database.DatabaseManager;

/**
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

	private CheckGameStatus checkGameStatus;
	private CardsManager cardsManager;
	private PlayersManager playersManager;
	private ToBeNotifyed toNotify;
	private BotManager botManager;
	private final TableInfoForClient table;

	public SingleTableManager(ServletNotificationsInterface snf,
			TableInfoForClient table, GlobalTableManagerInterface gtm)
			throws RemoteException {
		this.table = table;
		toNotify = new ToBeNotifyed(table.owner, snf);
		checkGameStatus = new CheckGameStatus();
		cardsManager = new CardsManager();
		playersManager = new PlayersManager(table.owner, false);
		botManager = new BotManager();
		DatabaseManager databaseManager = new DatabaseManager();
	}

	@Override
	public void addBot(String userName, int position)
			throws PositionFullException, RemoteException,
			IllegalArgumentException, FullTableException, NotCreatorException,
			IllegalStateException {
		checkGameStatus.checkAddBot();
		playersManager.addBot(userName, position);
		toNotify.addBot("_bot." + userName,
				botManager.chooseBotStrategy(getInitialTableStatus()));
		toNotify.notifyBotJoined("_bot." + userName, position);
	}

	private InitialTableStatus getInitialTableStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InitialTableStatus joinTable(String userName,
			ServletNotificationsInterface snf) throws FullTableException,
			NoSuchTableException, RemoteException, IllegalArgumentException,
			IllegalStateException, DuplicateUserNameException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void leaveTable(String userName) throws RemoteException,
			PlayerNotFoundException {
		// TODO Auto-generated method stub

	}

	@Override
	public void passCards(String userName, Card[] cards)
			throws IllegalArgumentException, RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void playCard(String userName, Card card)
			throws IllegalMoveException, RemoteException,
			IllegalArgumentException {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendMessage(ChatMessage message) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public ObservedGameStatus viewTable(String userName,
			ServletNotificationsInterface snf) throws NoSuchTableException,
			RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TableInfoForClient getTable() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

}
