package unibo.as.cupido.backend.table;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Arrays;

import unibo.as.cupido.common.exception.DuplicateUserNameException;
import unibo.as.cupido.common.exception.DuplicateViewerException;
import unibo.as.cupido.common.exception.FullTableException;
import unibo.as.cupido.common.exception.IllegalMoveException;
import unibo.as.cupido.common.exception.NoSuchPlayerException;
import unibo.as.cupido.common.exception.NoSuchTableException;
import unibo.as.cupido.common.exception.NoSuchUserException;
import unibo.as.cupido.common.exception.NotCreatorException;
import unibo.as.cupido.common.exception.FullPositionException;
import unibo.as.cupido.common.interfaces.ServletNotificationsInterface;
import unibo.as.cupido.common.interfaces.TableInterface;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.ChatMessage;
import unibo.as.cupido.common.structures.InitialTableStatus;
import unibo.as.cupido.common.structures.ObservedGameStatus;

public final class LoggerSingleTableManager implements TableInterface {

	public static final TableInterface defaultInstance = new LoggerSingleTableManager();

	private LoggerSingleTableManager() {
		//
	}

	@Override
	public String addBot(String userName, int position)
			throws FullPositionException, RemoteException,
			IllegalArgumentException, NotCreatorException {
		throw new IllegalStateException(
				"a replacement inactiveReplacementBot should never call this");
	}

	@Override
	public InitialTableStatus joinTable(String userName,
			ServletNotificationsInterface snf) throws FullTableException,
			RemoteException, IllegalArgumentException,
			DuplicateUserNameException, NoSuchUserException {
		throw new IllegalStateException(
				"a replacement inactiveReplacementBot should never call this");
	}

	@Override
	public void leaveTable(String userName) throws RemoteException,
			NoSuchPlayerException {
		throw new IllegalStateException(
				"a replacement inactiveReplacementBot should not call this before it is awoken");
	}

	@Override
	public void passCards(String userName, Card[] cards)
			throws IllegalArgumentException, IllegalStateException,
			RemoteException {
		System.err.println("fake stm: passCards(" + userName + ", "
				+ Arrays.toString(cards) + ")");
	}

	@Override
	public void playCard(String userName, Card card)
			throws IllegalMoveException, RemoteException,
			IllegalArgumentException {
		System.err
				.println("fake stm: playCard(" + userName + ", " + card + ")");
	}

	@Override
	public void sendMessage(ChatMessage message) throws RemoteException {
		throw new IllegalStateException(
				"a replacement inactiveReplacementBot should not call this before it is awoken");
	}

	@Override
	public ObservedGameStatus viewTable(String userName,
			ServletNotificationsInterface snf) throws RemoteException,
			DuplicateViewerException {
		throw new IllegalStateException(
				"a replacement inactiveReplacementBot should not call this before it is awoken");
	}

}
