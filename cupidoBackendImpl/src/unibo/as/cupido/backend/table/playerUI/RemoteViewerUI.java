package unibo.as.cupido.backend.table.playerUI;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import unibo.as.cupido.backend.table.LoggerServletNotificationInterface;
import unibo.as.cupido.common.exception.DuplicateViewerException;
import unibo.as.cupido.common.exception.GameInterruptedException;
import unibo.as.cupido.common.exception.NoSuchTableException;
import unibo.as.cupido.common.exception.WrongGameStateException;
import unibo.as.cupido.common.interfaces.LocalTableManagerInterface;
import unibo.as.cupido.common.interfaces.ServletNotificationsInterface;
import unibo.as.cupido.common.interfaces.TableInterface;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.ObservedGameStatus;
import unibo.as.cupido.common.structures.TableInfoForClient;

/**
 * This emulates
 */
public class RemoteViewerUI extends LoggerServletNotificationInterface
		implements ServletNotificationsInterface {

	final TableInterface singleTableManager;
	final ObservedGameStatus observedGameStatus;

	public RemoteViewerUI(String viewerName,
			LocalTableManagerInterface ltmInterface,
			TableInfoForClient tableInfo) throws RemoteException,
			NoSuchTableException, DuplicateViewerException,
			WrongGameStateException, GameInterruptedException {
		super(viewerName);
		if (viewerName == null)
			throw new IllegalArgumentException();

		singleTableManager = ltmInterface
				.getTable(tableInfo.tableDescriptor.id);
		observedGameStatus = singleTableManager.viewTable(viewerName,
				(ServletNotificationsInterface) UnicastRemoteObject
						.exportObject(this));
		System.out.println("viewing table\n" + observedGameStatus
				+ "\n press a key to exit");
	}

	@Override
	public synchronized void notifyGameStarted(Card[] cards) {
		throw new UnsupportedOperationException("cannot call this on a viewer");
	}

	@Override
	public synchronized void notifyPassedCards(Card[] cards) {
		throw new UnsupportedOperationException("cannot call this on a viewer");
	}

}
