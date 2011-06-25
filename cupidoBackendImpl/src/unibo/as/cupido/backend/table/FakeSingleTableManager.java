package unibo.as.cupido.backend.table;

import java.rmi.RemoteException;
import java.sql.SQLException;

import unibo.as.cupido.common.exception.DuplicateUserNameException;
import unibo.as.cupido.common.exception.DuplicateViewerException;
import unibo.as.cupido.common.exception.FullTableException;
import unibo.as.cupido.common.exception.IllegalMoveException;
import unibo.as.cupido.common.exception.NoSuchTableException;
import unibo.as.cupido.common.exception.NoSuchUserException;
import unibo.as.cupido.common.exception.NotCreatorException;
import unibo.as.cupido.common.exception.PlayerNotFoundException;
import unibo.as.cupido.common.exception.PositionFullException;
import unibo.as.cupido.common.interfaces.ServletNotificationsInterface;
import unibo.as.cupido.common.interfaces.TableInterface;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.ChatMessage;
import unibo.as.cupido.common.structures.InitialTableStatus;
import unibo.as.cupido.common.structures.ObservedGameStatus;

public final class FakeSingleTableManager implements TableInterface {

	public static final TableInterface defaultInstance = new FakeSingleTableManager();

	private FakeSingleTableManager(){
		//
	}
	
	@Override
	public String addBot(String userName, int position)
			throws PositionFullException, RemoteException,
			IllegalArgumentException, FullTableException, NotCreatorException,
			IllegalStateException {
		throw new IllegalStateException(
				"a replacement bot should never call this");
	}

	@Override
	public InitialTableStatus joinTable(String userName,
			ServletNotificationsInterface snf) throws FullTableException,
			NoSuchTableException, RemoteException, IllegalArgumentException,
			IllegalStateException, DuplicateUserNameException, SQLException,
			NoSuchUserException {
		throw new IllegalStateException(
				"a replacement bot should never call this");
	}

	@Override
	public void leaveTable(String userName) throws RemoteException,
			PlayerNotFoundException {
		throw new IllegalStateException(
				"a replacement bot should not call this before it is awoken");
	}

	@Override
	public void passCards(String userName, Card[] cards)
			throws IllegalArgumentException, IllegalStateException,
			RemoteException {
		//
	}

	@Override
	public void playCard(String userName, Card card)
			throws IllegalMoveException, RemoteException,
			IllegalArgumentException {
		//
	}

	@Override
	public void sendMessage(ChatMessage message) throws NoSuchUserException,
			RemoteException {
		throw new IllegalStateException(
				"a replacement bot should not call this before it is awoken");
	}

	@Override
	public ObservedGameStatus viewTable(String userName,
			ServletNotificationsInterface snf) throws RemoteException,
			DuplicateViewerException {
		throw new IllegalStateException(
				"a replacement bot should not call this before it is awoken");
	}

}
