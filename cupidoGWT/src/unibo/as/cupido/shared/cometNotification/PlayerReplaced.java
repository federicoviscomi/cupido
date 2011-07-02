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
 * A <code>PlayerReplaced</code> notification is sent when a player
 * leaves the table during the game, and he is not the
 * creator, so it is replaced with a bot.
 */
public class PlayerReplaced implements Serializable {

	/***/
	private static final long serialVersionUID = 1L;

	/**
	 * The name of the bot that replaces the player.
	 */
	public String name;

	/**
	 * The position in the table of the player that is being replaced.
	 * 
	 * For viewers, <code>position==1</code> means the player at the creator's left, and so
	 * the position range is [1-3].
	 * 
	 * For players, <code>position==0</code> means the player at the user's left, and so
	 * the position range is [0-2].
	 */
	public int position;
	
	/**
	 * The default constructor.
	 */
	public PlayerReplaced() {
	}
	
	/**
	 * A constructor that fills all fields with the provided values.
	 * 
	 * @param name The name of the bot that replaces the player.
	 * @param position The position in the table of the player that is being replaced.
	 */
	public PlayerReplaced(String name, int position) {
		this.name = name;
		this.position = position;
	}
}
