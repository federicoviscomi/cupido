package unibo.as.cupido.backend.playerUI;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import unibo.as.cupido.backend.table.LoggerServletNotification;
import unibo.as.cupido.common.exception.DuplicateViewerException;
import unibo.as.cupido.common.exception.GameInterruptedException;
import unibo.as.cupido.common.exception.NoSuchTableException;
import unibo.as.cupido.common.exception.WrongGameStateException;
import unibo.as.cupido.common.interfaces.LocalTableManagerInterface;
import unibo.as.cupido.common.interfaces.ServletNotificationsInterface;
import unibo.as.cupido.common.interfaces.TableInterface;
import unibo.as.cupido.common.interfaces.TableInterface.GameStatus;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.ObservedGameStatus;
import unibo.as.cupido.common.structures.TableInfoForClient;

/**
 * This emulates servlet of a viewer in player console user interface.
 */
public class ViewerUI extends LoggerServletNotification implements
		ServletNotificationsInterface {

	/** table interface of the table this is viewing */
	final TableInterface singleTableManager;
	/** observed game status of the table this is viewing */
	final ObservedGameStatus observedGameStatus;

	/**
	 * Creates a viewer that views table identified by
	 * <tt>tableInfo.tableDescriptor.id</tt> in lmt <tt>ltmInterface</tt>
	 * 
	 * @param viewerName
	 *            name of this viewer
	 * @param ltmInterface
	 *            table to view
	 * @param tableInfo
	 *            info of table to view
	 * 
	 * @throws RemoteException
	 * @throws NoSuchTableException
	 *             if there is no table identified by
	 *             <tt>tableInfo.tableDescriptor.id</tt> in lmt
	 *             <tt>ltmInterface</tt>
	 * @throws DuplicateViewerException
	 *             if specified table already contains a viewer named
	 *             <tt>viewerName</tt>
	 * @throws WrongGameStateException
	 *             When the game status is {@link GameStatus}.ENDED
	 * @throws GameInterruptedException
	 *             When the game status is {@link GameStatus}.INTERRUPTED
	 */
	public ViewerUI(String viewerName, LocalTableManagerInterface ltmInterface,
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
