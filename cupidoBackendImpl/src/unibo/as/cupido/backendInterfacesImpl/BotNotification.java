package unibo.as.cupido.backendInterfacesImpl;


import java.rmi.RemoteException;

import unibo.as.cupido.backendInterfaces.ServletNotifcationsInterface;
import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.ChatMessage;

public class BotNotification implements ServletNotifcationsInterface {

	
	
	@Override
	public void notifyGameEnded(int[] matchPoints, int[] playersTotalPoint) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyGameStarted(Card[] cards) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyLocalChatMessage(ChatMessage message) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyPassedCards(Card[] cards) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyPlayedCard(Card card, int playerPosition) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyPlayerJoined(String name, boolean isBot, int point, int position) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyPlayerLeft(String name) throws RemoteException {
		// TODO Auto-generated method stub

	}
}
