package unibo.as.cupido.backendInterfacesImpl;

import java.rmi.RemoteException;
import java.util.Arrays;

import unibo.as.cupido.backendInterfaces.ServletNotificationsInterface;
import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.ChatMessage;

public class DummyLoggerServletNotifyer implements
		ServletNotificationsInterface {

	// private PrintStream log;

	public DummyLoggerServletNotifyer() {
		// log = new PrintStream(System.out);
	}

	@Override
	public void notifyGameEnded(int[] matchPoints, int[] playersTotalPoint)
			throws RemoteException {
		System.out.println("\n"
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + Arrays.toString(matchPoints) + ", "
				+ Arrays.toString(playersTotalPoint) + ")");
	}

	@Override
	public void notifyGameStarted(Card[] cards) throws RemoteException {
		System.out.println("\n"
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + Arrays.toString(cards) + ")");
	}

	@Override
	public void notifyLocalChatMessage(ChatMessage message)
			throws RemoteException {
		System.out.println("\n"
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + message + ")");
	}

	@Override
	public void notifyPassedCards(Card[] cards) throws RemoteException {
		System.out.println("\n"
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + Arrays.toString(cards) + ")");
	}

	@Override
	public void notifyPlayedCard(Card card, int playerPosition)
			throws RemoteException {
		System.out.println("\n"
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + card + ", " + playerPosition + ")");
	}

	@Override
	public void notifyPlayerJoined(String name, boolean isBot, int point,
			int position) throws RemoteException {
		System.out.println("\n"
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + name + ", " + isBot + "," + point + "," + position
				+ ")");
	}

	@Override
	public void notifyPlayerLeft(String name) throws RemoteException {
		System.out.println("\n"
				+ Thread.currentThread().getStackTrace()[1].getMethodName()
				+ "(" + name + ")");
	}

}
