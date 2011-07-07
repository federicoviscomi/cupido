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

package unibo.as.cupido.backend.gtm;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ArrayBlockingQueue;

import unibo.as.cupido.common.interfaces.GlobalChatInterface;
import unibo.as.cupido.common.interfaces.GlobalTableManagerInterface;
import unibo.as.cupido.common.structures.ChatMessage;

/**
 * Implements a chat shared between all users.
 */
public class GlobalChatImpl implements GlobalChatInterface {

	private static Registry registry;
	private static Thread shutdownHook;

	public static void main(String[] args) throws RemoteException,
			UnknownHostException, AlreadyBoundException {
		new GlobalChatImpl();
	}

	/** calls the global chat shutdown method on exit if necessary */
	private static final class ShutdownHook extends Thread {
		/** the global chat to shut down */
		private final GlobalChatImpl gc;

		/**
		 * Create a new shutdown hook
		 * 
		 * @param gc
		 *            the global chat to shut down
		 */
		public ShutdownHook(GlobalChatImpl gc) {
			this.gc = gc;
		}

		@Override
		public void run() {
			gc.shutDown();
		}
	}

	/** stores MESSAGE_NUMBER chat message */
	private ArrayBlockingQueue<ChatMessage> messages;

	/**
	 * Creates a <tt>GlobalChatImpl</tt> with MESSAGE_NUMBER capacity
	 * 
	 * @throws RemoteException
	 * @throws AlreadyBoundException
	 * @throws UnknownHostException
	 */
	public GlobalChatImpl() throws RemoteException, AlreadyBoundException,
			UnknownHostException {
		messages = new ArrayBlockingQueue<ChatMessage>(MESSAGE_NUMBER);
		registry = LocateRegistry.getRegistry();

		registry.bind(GlobalChatInterface.GLOBAL_CHAT_RMI_NAME,
				UnicastRemoteObject.exportObject(new GlobalChatImpl()));

		shutdownHook = new ShutdownHook(this);
		Runtime.getRuntime().addShutdownHook(shutdownHook);

		System.out.println("Global chat started correctly at address "
				+ InetAddress.getLocalHost());
	}

	/**
	 * Shut the global chat down.
	 */
	public void shutDown() {
		try {
			registry.unbind(GlobalChatInterface.GLOBAL_CHAT_RMI_NAME);
		} catch (Exception e) {
			//
		}
		try {
			Runtime.getRuntime().removeShutdownHook(shutdownHook);
		} catch (IllegalStateException e) {
			//
		}
	}

	@Override
	public ChatMessage[] getLastMessages() throws RemoteException {
		return messages.toArray(new ChatMessage[messages.size()]);
	}

	@Override
	public void sendMessage(ChatMessage message)
			throws IllegalArgumentException, RemoteException {
		if (message.message.length() > GlobalChatInterface.MAX_CHAT_MESSAGE_LENGTH)
			throw new IllegalArgumentException();
		if (messages.size() == MESSAGE_NUMBER) {
			messages.remove();
		}
		messages.add(message);
		System.out.println("global chat message " + message);
	}

}
