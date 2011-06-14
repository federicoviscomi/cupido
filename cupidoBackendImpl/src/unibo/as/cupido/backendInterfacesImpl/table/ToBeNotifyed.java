package unibo.as.cupido.backendInterfacesImpl.table;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import unibo.as.cupido.backendInterfaces.ServletNotificationsInterface;
import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.ChatMessage;

public class ToBeNotifyed {
	private Map<String, ServletNotificationsInterface> snfs;

	public ToBeNotifyed(String owner, ServletNotificationsInterface snf) {
		snfs = new HashMap<String, ServletNotificationsInterface>(4);
		snfs.put(owner, snf);
	}

	public void notifyBotJoined(String botName, int position) {
		for (ServletNotificationsInterface snf : snfs.values()) {
			try {
				snf.notifyPlayerJoined(botName, true, 0, position);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}

	public void notifyCardPassed(Card[] cards, String name) {
		for (Entry<String, ServletNotificationsInterface> snf : snfs.entrySet()) {
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
		for (Entry<String, ServletNotificationsInterface> snf : snfs.entrySet()) {
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

	public void notifyGameEnded(int[] matchPoints, int[] playersTotalPoint) {
		for (Entry<String, ServletNotificationsInterface> snf : snfs.entrySet()) {
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

	public void notifyGameStarted(String userName, ArrayList<Card> cards) {
		try {
			snfs.get(userName).notifyGameStarted(
					cards.toArray(new Card[cards.size()]));
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void notifyPlayerJoined(String name, int position,
			ServletNotificationsInterface joinedSnf) {
		for (ServletNotificationsInterface snf : snfs.values()) {
			try {
				snf.notifyPlayerJoined(name, false, 0, position);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}

	public void removePlayer(String playerName) {
		snfs.remove(playerName);
	}

	public void viewerJoined(String userName, ServletNotificationsInterface snf) {
		snfs.put(userName, snf);
	}

	public void addBot(String string, ServletNotificationsInterface sni) {
		snfs.put(string, sni);
	}

	public void notifyNewChatMessage(ChatMessage message) {
		for (Entry<String, ServletNotificationsInterface> snf : snfs.entrySet()) {
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

	public void addPlayer(String userName, int position,
			ServletNotificationsInterface snf) {
		snfs.put(userName, snf);
	}

	public void notifyPlayerLeft(String userName) {
		for (ServletNotificationsInterface snf : snfs.values()) {
			try {
				snf.notifyPlayerLeft(userName);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}

	public void notifyViewerJoined(String userName) {
		for (ServletNotificationsInterface snf : snfs.values()) {
			try {
				snf.notifyLocalChatMessage(new ChatMessage(userName, "joined"));
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}

	public void addViewer(String userName, ServletNotificationsInterface snf) {
		snfs.put(userName, snf);
	}

}
