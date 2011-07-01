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

package unibo.as.cupido.client.widgets.cardsgame;

/**
 * This contains the the data about a player needed
 * by CardsGameWidget.
 */
public class PlayerData {
	/**
	 * Specifies whether the player is a human or a bot.
	 */
	public boolean isBot;
	
	/**
	 * The username of the player.
	 */
	public String name;
	
	/**
	 * The global score of the player.
	 * This is valid only if the player is a human.
	 */
	public int score;
}