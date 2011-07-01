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
 * Keeps track of all viewers of a single table
 */
public class ViewersSwarm {
	/**
	 * Stores association between viewers name and viewers notification
	 * interfaces.
	 */
	private final Map<String, ServletNotificationsInterface> snfs;

	private final ActionQueue actionQueue;

	public ViewersSwarm(ActionQueue actionQueue) {
		snfs = new HashMap<String, ServletNotificationsInterface>(4);
		this.actionQueue = actionQueue;
	}

	/**
	 * Add specified viewers to the swarm
	 * 
	 * @param viewerName
	 * @param snf
	 * @throws DuplicateViewerException
	 *             if the swarms already contains viewers <tt>viewerName</tt>
	 * @throws IllegalArgumentException
	 *             if some argument is <tt>null</tt>
	 */
	public void addViewer(String viewerName, ServletNotificationsInterface snf)
			throws DuplicateViewerException, IllegalArgumentException {
		if (viewerName == null || snf == null)
			throw new IllegalArgumentException();
		if (snfs.put(viewerName, snf) != null)
			throw new DuplicateViewerException();
	}

	/**
	 * Returns <tt>true</tt> if specified user is a viewers; <tt>false</tt>
	 * otherwise.
	 * 
	 * @param userName
	 * @return <tt>true</tt> if specified user is a viewers; <tt>false</tt>
	 *         otherwise.
	 */
	public boolean isAViewer(String userName) {
		return snfs.containsKey(userName);
	}

	/**
	 * Notify all viewers game ended.
	 * 
	 * @param matchPoints
	 *            points of the match
	 * @param playersTotalPoint
	 *            score of players in the match
	 */
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
	}

	/**
	 * Notify all viewers game ended prematurely
	 */
	public void notifyGameEndedPrematurely() {
		this.notifyGameEnded(null, null);
	}

	/**
	 * Notify all viewers that there is a new message in local chat.
	 * 
	 * @param message
	 *            the new message in local chat.
	 */
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

	/**
	 * Notify all viewers that player in position <tt>playerPosition</tt> played
	 * card <tt>card</tt>
	 * 
	 * @param playerPosition
	 *            the position of player who plays
	 * @param card
	 *            the card played
	 */
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

	/**
	 * Notify all viewers that a player joined
	 * 
	 * @param playerName
	 *            the name of player who joined
	 * @param isBot
	 *            <tt>true</tt> if the player who joined is a bot;
	 *            <tt>false</tt> otherwise.
	 * @param score
	 *            the score of player who joined. This is meaningful only if
	 *            <tt>isBot==false</tt>
	 * @param position
	 *            the position of player who joined.
	 */
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

	/**
	 * Notify all viewers that a plyer left the game.
	 * 
	 * @param playerName
	 *            name of player who left.
	 */
	public void notifyPlayerLeft(final String playerName) {
		for (final ServletNotificationsInterface snf : snfs.values()) {
			actionQueue.enqueue(new RemoteAction() {
				@Override
				public void onExecute() throws RemoteException {
					snf.notifyPlayerLeft(playerName);
				}
			});
		}
	}

	/**
	 * Notify all viewers that a player has been replaced.
	 * 
	 * @param botName
	 *            name of bot who replaced the player.
	 * @param position
	 *            position occupied by the replaced player.
	 */
	public void notifyPlayerReplaced(final String botName, final int position) {
		for (final ServletNotificationsInterface snf : snfs.values()) {
			actionQueue.enqueue(new RemoteAction() {
				@Override
				public void onExecute() throws RemoteException {
					snf.notifyPlayerReplaced(botName, position);
				}
			});
		}
	}

	/**
	 * Notify all viewers that a viewer joined the table.
	 * 
	 * @param viewerName
	 *            name of viewer who joined
	 */
	public void notifyViewerJoined(final String viewerName) {
		for (final ServletNotificationsInterface snf : snfs.values()) {
			actionQueue.enqueue(new RemoteAction() {
				@Override
				public void onExecute() throws RemoteException {
					// FIXME: Is this correct?
					snf.notifyLocalChatMessage(new ChatMessage(viewerName,
							"joined"));
				}
			});
		}
	}

	/**
	 * Removes specified viewer.
	 * 
	 * @param viewerName
	 *            name of viewer to remove.
	 * @throws NoSuchViewerException
	 *             if there is no such viewer named <tt>viewerName</tt>
	 * 
	 */
	public void removeViewer(String viewerName) throws NoSuchViewerException {
		if (viewerName == null)
			throw new IllegalArgumentException();
		if (snfs.remove(viewerName) == null)
			throw new NoSuchViewerException();
	}

	/**
	 * Returns the number of viewers in the table
	 * 
	 * @return the number of viewers in the table
	 */
	public int viewersCount() {
		return snfs.size();
	}

	/**
	 * Kills the action queue
	 */
	public void killConsumer() {
		actionQueue.killConsumer();
	}

}
