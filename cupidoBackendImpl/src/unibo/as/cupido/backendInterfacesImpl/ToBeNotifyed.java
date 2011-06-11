package unibo.as.cupido.backendInterfacesImpl;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import unibo.as.cupido.backendInterfaces.ServletNotifcationsInterface;
import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.ChatMessage;

public class ToBeNotifyed {
	private Map<String, ServletNotifcationsInterface> snfs;

	public ToBeNotifyed() {
		snfs = new HashMap<String, ServletNotifcationsInterface>(4);
	}

	public void notifyBotJoined(String botName, int position, int points) {
		for (ServletNotifcationsInterface snf : snfs.values()) {
			try {
				snf.notifyPlayerJoined(botName, true, points, position);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}

	public void notifyCardPassed(Card[] cards, String name) {
		for (Entry<String, ServletNotifcationsInterface> snf : snfs.entrySet()) {
			try {
				if (!snf.getKey().equals(name))
					snf.getValue().notifyPassedCards(cards);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}

	public void notifyCardPlayed(String userName, Card card, int playerPosition) {
		for (Entry<String, ServletNotifcationsInterface> snf : snfs.entrySet()) {
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

	public void notifyGameStarted(String userName, Card[] cards) {
		for (Entry<String, ServletNotifcationsInterface> snf : snfs.entrySet()) {
			try {
				if (!snf.getKey().equals(userName))
					snf.getValue().notifyGameStarted(cards);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}

	public void notifyMessageSent(ChatMessage message) {
		for (Entry<String, ServletNotifcationsInterface> snf : snfs.entrySet()) {
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

	public void notifyPlayerJoined(String name, int position, ServletNotifcationsInterface joinedSnf) {
		if (name == null || joinedSnf == null)
			throw new IllegalArgumentException();
		for (ServletNotifcationsInterface snf : snfs.values()) {
			try {
				snf.notifyPlayerJoined(name, false, 0, position);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(-1);
			}
		}
		snfs.put(name, joinedSnf);
	}

	public void remove(String playerName) {
		snfs.remove(playerName);
		for (ServletNotifcationsInterface snf : snfs.values()) {
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
		snfs.put(userName, snf);
	}

	public void notifyGameEnded(int[] matchPoints, int[] playersTotalPoint) {
		for (Entry<String, ServletNotifcationsInterface> snf : snfs.entrySet()) {
			try {
				snf.getValue().notifyGameEnded(matchPoints, playersTotalPoint);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(-1);
			}
		}
		snfs = null;
	}
}
