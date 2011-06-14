package unibo.as.cupido.backendInterfacesImpl.table.bot;

import java.rmi.RemoteException;

import unibo.as.cupido.backendInterfaces.TableInterface;
import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.ChatMessage;
import unibo.as.cupido.backendInterfaces.common.InitialTableStatus;

/**
 * 
 * This is a bot who plays heart with the following strategy:
 * 
 * 
 * @author cane
 * 
 */
public class BotFewTricks extends AbstractBot {

	public BotFewTricks(InitialTableStatus initialTableStatus,
			TableInterface singleTableManager) {
		// TODO Auto-generated constructor stub
	}

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
	public void notifyPlayerJoined(String playerName, boolean isBot, int score,
			int position) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyPlayerLeft(String playerName) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

}
