package unibo.as.cupido.backend.table.playerUI;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;

import unibo.as.cupido.backend.table.CardsManager;
import unibo.as.cupido.backend.table.bot.BotNotificationInterface;
import unibo.as.cupido.common.exception.DuplicateViewerException;
import unibo.as.cupido.common.exception.NoSuchTableException;
import unibo.as.cupido.common.interfaces.LocalTableManagerInterface;
import unibo.as.cupido.common.interfaces.ServletNotificationsInterface;
import unibo.as.cupido.common.interfaces.TableInterface;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.ChatMessage;
import unibo.as.cupido.common.structures.InitialTableStatus;
import unibo.as.cupido.common.structures.ObservedGameStatus;
import unibo.as.cupido.common.structures.Card.Suit;
import unibo.as.cupido.common.structures.TableInfoForClient;

public class RemoteViewerUI implements ServletNotificationsInterface {

	private final String viewerName;
	private final TableInterface singleTableManager;
	private final ObservedGameStatus observedGameStatus;

	public RemoteViewerUI(String viewerName,
			LocalTableManagerInterface ltmInterface,
			TableInfoForClient tableInfo) throws RemoteException,
			NoSuchTableException, DuplicateViewerException {
		if (viewerName == null)
			throw new IllegalArgumentException();

		this.viewerName = viewerName;

		singleTableManager = ltmInterface
				.getTable(tableInfo.tableDescriptor.id);
		observedGameStatus = singleTableManager.viewTable(viewerName,
				(ServletNotificationsInterface) UnicastRemoteObject
						.exportObject(this));
		System.out.println(observedGameStatus);
	}

	@Override
	public synchronized void notifyGameEnded(int[] matchPoints,
			int[] playersTotalPoint) {
		System.out.println("\n" + viewerName + ": "
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + Arrays.toString(matchPoints) + ", "
				+ Arrays.toString(playersTotalPoint) + ")");
	}

	@Override
	public synchronized void notifyGameStarted(Card[] cards) {
		throw new UnsupportedOperationException("cannot call this on a viewer");
	}

	@Override
	public synchronized void notifyLocalChatMessage(ChatMessage message) {
		System.out.println("\n" + viewerName + ": "
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + message + ")");
	}

	@Override
	public synchronized void notifyPassedCards(Card[] cards) {
		throw new UnsupportedOperationException("cannot call this on a viewer");
	}

	@Override
	public synchronized void notifyPlayedCard(Card card, int playerPosition) {
		System.out.println("\n" + viewerName + ": "
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + card + ", " + playerPosition + ")");
	}

	@Override
	public synchronized void notifyPlayerJoined(String name, boolean isBot,
			int point, int position) {
		System.out.println("\n" + viewerName + ": "
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + name + ", " + isBot + ", " + point + ", " + position
				+ ")");
	}

	@Override
	public synchronized void notifyPlayerLeft(String name) {
		System.out.println("\n" + viewerName + ": "
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + name + ")");
	}

	@Override
	public void notifyPlayerReplaced(String botName, int position)
			throws RemoteException {
		System.out.println("\n" + viewerName + ": "
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + botName + ", " + position + ")");
	}

}
