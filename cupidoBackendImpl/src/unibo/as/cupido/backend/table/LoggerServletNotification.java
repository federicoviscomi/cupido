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
import java.util.Arrays;

import unibo.as.cupido.common.interfaces.ServletNotificationsInterface;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.ChatMessage;

/**
 * A servlet notification interface that just logs operations.
 */
public class LoggerServletNotification implements
		ServletNotificationsInterface {

	/** this servlet name */
	private final String name;

	/**
	 * Creates a notification interface for logger bot <tt>name</tt>
	 * 
	 * @param name
	 */
	public LoggerServletNotification(String name) {
		this.name = name;
	}

	@Override
	public void notifyGameEnded(int[] matchPoints, int[] playersTotalPoint)
			throws RemoteException {
		System.out.println("" + name + " notifyGameEnded("
				+ Arrays.toString(matchPoints) + ", "
				+ Arrays.toString(playersTotalPoint) + ")");
	}

	@Override
	public void notifyGameStarted(Card[] cards) throws RemoteException {
		System.out.println("" + name + " notifyGameStarted("
				+ Arrays.toString(cards) + ")");
	}

	@Override
	public void notifyLocalChatMessage(ChatMessage message)
			throws RemoteException {
		System.out.println("" + name + " notifyChatMessage(" + message + ")");
	}

	@Override
	public void notifyPassedCards(Card[] cards) throws RemoteException {
		System.out.println("" + name + " notifyPassedCards("
				+ Arrays.toString(cards) + ")");
	}

	@Override
	public void notifyPlayedCard(Card card, int playerPosition)
			throws RemoteException {
		System.out.println("" + name + " notifyPlayedCard(" + card + ", "
				+ playerPosition + ")");
	}

	@Override
	public void notifyPlayerJoined(String playerName, boolean isBot, int score,
			int position) throws RemoteException {
		System.out.println("" + name + " notifyPlayerJoined(" + playerName
				+ ", " + isBot + ", " + score + ")");
	}

	@Override
	public void notifyPlayerLeft(String playerName) throws RemoteException {
		System.out.println("" + name + " notifyPlayerLefy(" + playerName + ")");
	}

	@Override
	public void notifyPlayerReplaced(String name, int position)
			throws RemoteException {
		System.out.println("" + name + " notifyPlayerReplaced(" + name + ", "
				+ position + ")");
	}

}
