package unibo.as.cupido.backendInterfacesImpl.table.bot;

import java.rmi.RemoteException;
import java.util.Arrays;

import unibo.as.cupido.backendInterfaces.TableInterface;
import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.ChatMessage;
import unibo.as.cupido.backendInterfaces.common.InitialTableStatus;

public class DummyLoggerBotNotifyer extends AbstractBot {

	private final String userName;
	private final TableInterface singleTableManager;
	private final InitialTableStatus initialTableStatus;

	public DummyLoggerBotNotifyer(InitialTableStatus initialTableStatus,
			TableInterface singleTableManager, String userName) {
		this.initialTableStatus = initialTableStatus;
		this.singleTableManager = singleTableManager;
		this.userName = userName;
	}

	@Override
	public void notifyGameEnded(int[] matchPoints, int[] playersTotalPoint)
			throws RemoteException {
		System.out.println("\n" + userName + ": "
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + Arrays.toString(matchPoints) + ", "
				+ Arrays.toString(playersTotalPoint) + ")");
	}

	@Override
	public void notifyGameStarted(Card[] cards) throws RemoteException {
		System.out.println("\n" + userName + ": "
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + Arrays.toString(cards) + ")");
	}

	@Override
	public void notifyLocalChatMessage(ChatMessage message)
			throws RemoteException {
		System.out.println("\n" + userName + ": "
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + message + ")");
	}

	@Override
	public void notifyPassedCards(Card[] cards) throws RemoteException {
		System.out.println("\n" + userName + ": "
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + Arrays.toString(cards) + ")");
	}

	@Override
	public void notifyPlayedCard(Card card, int playerPosition)
			throws RemoteException {
		System.out.println("\n" + userName + ": "
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + card + ", " + playerPosition + ")");
	}

	@Override
	public void notifyPlayerJoined(String name, boolean isBot, int point,
			int position) throws RemoteException {
		System.out.println("\n" + userName + ": "
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + name + ", " + isBot + "," + point + "," + position
				+ ")");
	}

	@Override
	public void notifyPlayerLeft(String name) throws RemoteException {
		System.out.println("\n" + userName + ": "
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + name + ")");
	}

}