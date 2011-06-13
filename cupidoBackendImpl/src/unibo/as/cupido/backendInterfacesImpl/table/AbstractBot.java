package unibo.as.cupido.backendInterfacesImpl.table;

import java.rmi.RemoteException;

import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.ChatMessage;

/**
 * Some domain terms:
 * <table>
 * <tr>
 * <td>duck</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>trick</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>round</td>
 * <td>each player gets 13 cards in a round to start with. A round is over after
 * 13 tricks. A trick always has 4 cards</td>
 * </tr>
 * 
 * </table>
 * 
 * @author cane
 * 
 */
public abstract class AbstractBot implements Bot {

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
