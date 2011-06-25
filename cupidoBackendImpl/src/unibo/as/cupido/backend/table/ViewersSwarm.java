/*  Cupido - An online Hearts game.
 *  Copyright (C) 2011 Lorenzo Belli, Marco Poletti, Federico Viscomi
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package unibo.as.cupido.backend.table;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import unibo.as.cupido.common.exception.DuplicateViewerException;
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

	public void addViewer(String viewerName, ServletNotificationsInterface snf)
			throws DuplicateViewerException {
		if (snfs.put(viewerName, snf) != null)
			throw new DuplicateViewerException();
	}

	public boolean isAViewer(String userName) {
		return snfs.containsKey(userName);
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

	public void notifyPlayerReplaced(String botName, int position) {
		for (ServletNotificationsInterface snf : snfs.values()) {
			try {
				snf.notifyPlayerReplaced(botName, position);
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
