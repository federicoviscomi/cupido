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

package unibo.as.cupido.backend;

import java.rmi.RemoteException;
import java.util.concurrent.ArrayBlockingQueue;

import unibo.as.cupido.common.interfaces.GlobalChatInterface;
import unibo.as.cupido.common.structures.ChatMessage;

/**
 * Implements a chat shared between all users.
 */
public class GlobalChatImpl implements GlobalChatInterface {

	/** stores MESSAGE_NUMBER chat message */
	private ArrayBlockingQueue<ChatMessage> messages;

	/**
	 * Creates a <tt>GlobalChatImpl</tt> with MESSAGE_NUMBER capacity
	 */
	public GlobalChatImpl() {
		messages = new ArrayBlockingQueue<ChatMessage>(MESSAGE_NUMBER);
	}

	@Override
	public ChatMessage[] getLastMessages() throws RemoteException {
		return messages.toArray(new ChatMessage[messages.size()]);
	}

	@Override
	public void sendMessage(ChatMessage message) throws RemoteException {
		if (messages.size() == MESSAGE_NUMBER) {
			messages.remove();
		}
		messages.add(message);
		System.out.println("global chat message " + message);
	}

}
