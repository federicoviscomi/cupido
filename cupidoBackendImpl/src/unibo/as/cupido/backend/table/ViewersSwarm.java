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
import unibo.as.cupido.common.exception.NoSuchPlayerException;
import unibo.as.cupido.common.exception.NoSuchViewerException;
import unibo.as.cupido.common.interfaces.ServletNotificationsInterface;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.ChatMessage;

/***
 * Keeps track of a viewers of a table
 */
public class ViewersSwarm {
	private Map<String, ServletNotificationsInterface> snfs;
	private ActionQueue actionQueue;

	public ViewersSwarm(ActionQueue actionQueue) {
		snfs = new HashMap<String, ServletNotificationsInterface>(4);
		this.actionQueue = actionQueue;
	}

	public void addViewer(String viewerName, ServletNotificationsInterface snf)
			throws DuplicateViewerException, IllegalArgumentException {
		if (viewerName == null || snf == null)
			throw new IllegalArgumentException();
		if (snfs.put(viewerName, snf) != null)
			throw new DuplicateViewerException();
	}

	public boolean isAViewer(String userName) {
		return snfs.containsKey(userName);
	}

	public void notifyGameEnded(int[] matchPoints, int[] playersTotalPoint) {
		for (final ServletNotificationsInterface snf : snfs.values()) {
			final int[] matchPointsClone;
			if (matchPoints != null)
				matchPointsClone = matchPoints.clone();
			else
				matchPointsClone = null;
			final int[] playersTotalPointsClone;
			if (playersTotalPoint != null)
				playersTotalPointsClone = playersTotalPoint.clone();
			else
				playersTotalPointsClone = null;
			actionQueue.enqueue(new RemoteAction() {
				@Override
				public void onExecute() throws RemoteException {
					snf.notifyGameEnded(matchPointsClone,
							playersTotalPointsClone);
				}
			});
		}
		snfs = null;
	}

	public void notifyGameEndedPrematurely() {
		this.notifyGameEnded(null, null);
	}

	public void notifyNewLocalChatMessage(ChatMessage message) {
		for (Entry<String, ServletNotificationsInterface> e : snfs.entrySet())
			if (!e.getKey().equals(message.userName)) {
				final ServletNotificationsInterface snf = e.getValue();
				final ChatMessage messageClone = message.clone();
				actionQueue.enqueue(new RemoteAction() {
					@Override
					public void onExecute() throws RemoteException {
						snf.notifyLocalChatMessage(messageClone);
					}
				});
			}
	}

	public void notifyPlayedCard(final int playerPosition, Card card) {
		for (final ServletNotificationsInterface snf : snfs.values()) {
			final Card cardClone = card.clone();
			actionQueue.enqueue(new RemoteAction() {
				@Override
				public void onExecute() throws RemoteException {
					snf.notifyPlayedCard(cardClone, playerPosition);
				}
			});
		}
	}

	public void notifyPlayerJoined(final String playerName,
			final boolean isBot, final int score, final int position) {
		for (final ServletNotificationsInterface snf : snfs.values()) {
			actionQueue.enqueue(new RemoteAction() {
				@Override
				public void onExecute() throws RemoteException {
					snf.notifyPlayerJoined(playerName, isBot, score, position);
				}
			});
		}
	}

	public void notifyPlayerLeft(final String userName) {
		for (final ServletNotificationsInterface snf : snfs.values()) {
			actionQueue.enqueue(new RemoteAction() {
				@Override
				public void onExecute() throws RemoteException {
					snf.notifyPlayerLeft(userName);
				}
			});
		}
	}

	public void notifyPlayerReplaced(final String botName, final int position)
			throws NoSuchPlayerException {
		for (final ServletNotificationsInterface snf : snfs.values()) {
			actionQueue.enqueue(new RemoteAction() {
				@Override
				public void onExecute() throws RemoteException {
					snf.notifyPlayerReplaced(botName, position);
				}
			});
		}
	}

	public void notifyViewerJoined(final String userName) {
		for (final ServletNotificationsInterface snf : snfs.values()) {
			actionQueue.enqueue(new RemoteAction() {
				@Override
				public void onExecute() throws RemoteException {
					// FIXME: Is this correct?
					snf.notifyLocalChatMessage(new ChatMessage(userName,
							"joined"));
				}
			});
		}
	}

	public void removeViewer(String viewerName) throws NoSuchViewerException {
		if (viewerName == null)
			throw new IllegalArgumentException();
		if (snfs.remove(viewerName) == null)
			throw new NoSuchViewerException();
	}

}
