package unibo.as.cupido.backendInterfacesImpl;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import unibo.as.cupido.backendInterfaces.ServletNotifcationsInterface;
import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.ChatMessage;

public class ToNotify {
	private Map<String, ServletNotifcationsInterface> snfList;

	public ToNotify() {
		snfList = new HashMap<String, ServletNotifcationsInterface>(4);
	}

	public void notifyBotJoined(String botName, int position, BotNotification bni) {
		for (ServletNotifcationsInterface snfs : snfList.values()) {
			try {
				snfs.notifyPlayerJoined(botName, true, 0, position);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(-1);
			}
		}
		snfList.put(botName, bni);
	}

	public void notifyCardPassed(Card[] cards, String name) {
		try {
			snfList.get(name).notifyPassedCards(cards);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public void notifyCardPlayed(String userName, Card card, int playerPosition) {
		for (Entry<String, ServletNotifcationsInterface> snf : snfList.entrySet()) {
			try {
				if (!snf.getKey().equals(userName))
					snf.getValue().notifyPlayedCard(card, playerPosition);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}

	public void notifyGameStarted(String userName) {
		for (Entry<String, ServletNotifcationsInterface> snf : snfList.entrySet()) {
			try {
				if (!snf.getKey().equals(userName))
					snf.getValue().notifyGameStarted(null);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}

	public void notifyMessageSent(ChatMessage message) {
		for (Entry<String, ServletNotifcationsInterface> snf : snfList.entrySet()) {
			try {
				if (!snf.getKey().equals(message.userName))
					snf.getValue().notifyLocalChatMessage(message);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}

	public void notifyPlayerJoined(String name, int position, ServletNotifcationsInterface snf) {
		if (name == null || snf == null)
			throw new IllegalArgumentException();
		for (ServletNotifcationsInterface snfs : snfList.values()) {
			try {
				snfs.notifyPlayerJoined(name, false, 0, position);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(-1);
			}
		}
		snfList.put(name, snf);
	}

	public void remove(String playerName) {
		snfList.remove(playerName);
		for (ServletNotifcationsInterface snf : snfList.values()) {
			try {
				snf.notifyPlayerLeft(playerName);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}

	public void viewerJoined(String userName, ServletNotifcationsInterface snf) {
		if (userName == null || snf == null)
			throw new IllegalArgumentException();
		snfList.put(userName, snf);
	}
}
