package unibo.as.cupido.backendInterfacesImpl.table;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Arrays;

import unibo.as.cupido.backendInterfaces.ServletNotificationsInterface;
import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.ChatMessage;
import unibo.as.cupido.backendInterfaces.common.InitialTableStatus;

public class DummyLoggerServletNotifyer implements
		ServletNotificationsInterface, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8439829734552972246L;
	private final String userName;
	private final InitialTableStatus initialTableStatus;

	public DummyLoggerServletNotifyer(String userName) {
		this.userName = userName;
		initialTableStatus = new InitialTableStatus();
		initialTableStatus.opponents = new String[3];
		initialTableStatus.playerScores = new int[3];
		initialTableStatus.whoIsBot = new boolean[3];
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
		System.out.print("\n DummyLoggerServletNotifier " + userName + "."
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + name + ", " + isBot + "," + point + "," + position
				+ ")");

		initialTableStatus.opponents[position] = name;
		initialTableStatus.playerScores[position] = point;
		initialTableStatus.whoIsBot[position] = isBot;
		System.out.print(initialTableStatus + "\n");
	}

	@Override
	public void notifyPlayerLeft(String name) throws RemoteException {
		System.out.println("\n" + userName + ": "
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + name + ")");
		for (int i = 0; i < 3; i++) {
			if (initialTableStatus.opponents[i] != null
					&& initialTableStatus.opponents[i].equals(name))
				initialTableStatus.opponents[i] = null;
		}
		System.out.print(initialTableStatus + "\n");
	}

}
