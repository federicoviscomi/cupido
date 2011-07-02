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
 * A <code>PlayerLeft</code> notification is sent when a player leaves and
 * the game is not started yet, so that position becomes free.
 */
public class PlayerLeft implements Serializable {

	/***/
	private static final long serialVersionUID = 1L;

	/**
	 * The username of the player who has left the table.
	 */
	public String player;
	
	/**
	 * The default constructor.
	 */
	public PlayerLeft(){
	}
	
	/**
	 * A constructor that initializes the field with the specified value.
	 * 
	 * @param player The username of the player who has left the table.
	 */
	public PlayerLeft(String player){
		this.player=player;
	}
}
