package unibo.as.cupido.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import net.zschech.gwt.comet.server.CometSession;

import unibo.as.cupido.backendInterfaces.ServletNotificationsInterface;
import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.ChatMessage;

public class ServletNotificationsInterfaceImpl extends UnicastRemoteObject
		implements ServletNotificationsInterface {

	private static final long serialVersionUID = 1L;

	protected ServletNotificationsInterfaceImpl() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * Called by a Table to notify the end of the game.
	 */
	@Override
	public void notifyGameEnded(int[] matchPoints, int[] playersTotalPoint)
			throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyGameStarted(Card[] cards) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyLocalChatMessage(ChatMessage message)
			throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyPassedCards(Card[] cards) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyPlayedCard(Card card, int playerPosition)
			throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyPlayerJoined(String name, boolean isBot, int point,
			int position) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyPlayerLeft(String name) throws RemoteException {
		// TODO Auto-generated method stub

	}

}
