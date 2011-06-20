package unibo.as.cupido.backend.table;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import unibo.as.cupido.common.interfaces.ServletNotificationsInterface;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.ChatMessage;

/***
 * Keeps track of a viewers of a table
 */
public class ViewersSwarm {
	private Map<String, ServletNotificationsInterface> snfs;

	public ViewersSwarm() {
		snfs = new HashMap<String, ServletNotificationsInterface>(4);
	}

	public void addViewer(String viewerName, ServletNotificationsInterface snf) {
		snfs.put(viewerName, snf);
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

	public void notifyGameEnded(int[] matchPoints, int[] playersTotalPoint) {
		for (ServletNotificationsInterface snf : snfs.values()) {
			try {
				snf.notifyGameEnded(matchPoints, playersTotalPoint);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(-1);
			}
		}
		snfs = null;
	}

	public void notifyNewLocalChatMessage(ChatMessage message) {
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

	public void notifyPlayedCard(int playerPosition, Card card) {
		for (ServletNotificationsInterface snf : snfs.values()) {
			try {
				snf.notifyPlayedCard(card, playerPosition);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void notifyPlayerJoined(String playerName, int score, int position) {
		for (ServletNotificationsInterface snf : snfs.values()) {
			try {
				snf.notifyPlayerJoined(playerName, false, score, position);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(-1);
			}
		}
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

	private void print() {
		for (Entry<String, ServletNotificationsInterface> e : snfs.entrySet()) {
			System.out.print("\n\t[" + e.getKey() + ", " + e.getValue() + "]");
		}
	}

	public void removeViewer(String viewerName) {
		snfs.remove(viewerName);
	}

}
