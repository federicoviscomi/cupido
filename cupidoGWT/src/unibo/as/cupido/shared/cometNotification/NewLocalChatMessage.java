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

package unibo.as.cupido.shared.cometNotification;

import java.io.Serializable;

/**
 * This class is used for comet notifications.
 * 
 * A NewLocalChatMessage notification is sent when another user
 * (either a player or a viewer) sends a message to a table's
 * chat.
 * 
 * Then an user sends a message, other users at the same table receive
 * this notification, but the sender does not receive this.
 */
public class NewLocalChatMessage implements Serializable {

	/***/
	private static final long serialVersionUID = 1L;

	/**
	 * The name of the user who sent this message.
	 */
	public String user;
	
	/**
	 * The actual message.
	 */
	public String message;
	
	/**
	 * The default constructor.
	 */
	public NewLocalChatMessage() {
	}

	/**
	 * A constructor that initializes all fields with the provided values.
	 * 
	 * @param user The name of the user who sent this message.
	 * @param message The actual message.
	 */
	public NewLocalChatMessage(String user, String message) {
		this.user = user;
		this.message = message;
	}
}
