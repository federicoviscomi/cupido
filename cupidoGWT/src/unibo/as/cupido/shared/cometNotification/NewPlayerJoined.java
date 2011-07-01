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
 * This notification in sent when a player or a bot joins the table
 * before the game start.
 * 
 * If {@link isBot}==true, the variable {@link points} is meaningless.
 */
public class NewPlayerJoined implements Serializable {

	private static final long serialVersionUID = 1L;

	public String name;

	/**
	 * isBot is true is the player is a bot; otherwise is false
	 */
	public boolean isBot;

	/*
	 * player total points
	 */
	public int points;

	/**
	 * For viewers, position=1 means the player at the craetor's left, and so
	 * the position range is [1-3].
	 * 
	 * For players, position=0 means the player at the user's left, and so
	 * the position range is [0-2].
	 */
	public int position;
	
	public NewPlayerJoined() {
	}
	
	public NewPlayerJoined(String name, boolean isBot, int points, int position) {
		this.name = name;
		this.isBot = isBot;
		this.points = points;
		this.position = position;
	}
}
