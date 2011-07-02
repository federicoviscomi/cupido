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

package unibo.as.cupido.common.structures;

import java.io.Serializable;

/**
 * A chat message.
 */
public class ChatMessage implements Serializable {

	private static final long serialVersionUID = 1L;

	/** name of user who creates this chat message */
	public final String userName;
	/** actual content of chat message */
	public final String message;

	/**
	 * Create a chat message with specified user name and message
	 * 
	 * @param userName
	 * @param message
	 */
	public ChatMessage(String userName, String message) {
		if (userName == null || message == null) {
			throw new IllegalArgumentException();
		}
		this.userName = userName;
		this.message = message;
	}

	/**
	 * Create a chat message with <tt>null</tt> values
	 */
	public ChatMessage() {
		this.userName = null;
		this.message = null;
	}

	@Override
	public String toString() {
		return userName + ": " + message;
	}

	@Override
	public ChatMessage clone() {
		return new ChatMessage(userName, message);
	}
}
