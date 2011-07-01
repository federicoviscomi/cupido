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
 * This notification in sent when a player leaves a table during the game,
 * and he is not the craetor, so it is replaced with a bot.
 */
public class PlayerReplaced implements Serializable {

	private static final long serialVersionUID = 1L;

	public String name;

	/**
	 * For viewers, position=1 means the player at the craetor's left, and so
	 * the position range is [1-3].
	 * 
	 * For players, position=0 means the player at the user's left, and so
	 * the position range is [0-2].
	 */
	public int position;
	
	public PlayerReplaced() {
	}
	
	public PlayerReplaced(String name, int position) {
		this.name = name;
		this.position = position;
	}
}
