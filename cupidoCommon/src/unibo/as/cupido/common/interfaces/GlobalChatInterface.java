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

package unibo.as.cupido.common.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

import unibo.as.cupido.common.structures.ChatMessage;

/**
 * This interface is used by the Servlet. The Servlet is not notified when a
 * message is sent to the global chat. Instead the Servlet pools the global chat
 * component.
 * <p>
 * The global chat component is a remote RMI object who is registered in the
 * same remote registry of the GTM and is bounded to name
 * <code>GLOBAL_CHAT_RMI_NAME</code>
 * 
 */
public interface GlobalChatInterface extends Remote {

	/** number of messages stored */
	public static int MESSAGE_NUMBER = 10;

	/** Max number of character in a message */
	public static final int MAX_CHAT_MESSAGE_LENGTH = 200;
	
	/** rmi name for global chat remote object */
	public static final String GLOBAL_CHAT_RMI_NAME = "globalChat";


	/**
	 * Return the last MESSAGE_NUMBER messages
	 * 
	 * @return the last MESSAGE_NUMBER messages
	 */
	public ChatMessage[] getLastMessages() throws RemoteException;

	/**
	 * Send a message to the global chat.
	 * 
	 * @param message
	 *            contains user name of the user who wants to send a message to
	 *            the global chat and the message he wants to send
	 */
	public void sendMessage(ChatMessage message)
			throws IllegalArgumentException, RemoteException;

}
