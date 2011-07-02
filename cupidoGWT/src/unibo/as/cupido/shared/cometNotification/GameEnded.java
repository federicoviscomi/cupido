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
 * A <code>GameEnded</code> notification is sent when a game ends
 * 
 * The game can end either because all players have no more
 * cards in their hands, or because the table creator has
 * left the table, and so the game was interrupted.
 * In the latter case, all fields are <code>null</code>.
 */
public class GameEnded implements Serializable {

	/***/
	private static final long serialVersionUID = 1L;

	/**
	 * The points scored by all players in the current game.
	 * 
	 * The values are in clockwise order.
	 * 
	 * If the current user is a player, the first element refers to the
	 * current user.
	 * 
	 * If the current user is a viewer, the first element refers to
	 * the table creator.
	 */
	public int[] matchPoints;

	/**
	 * The global scores, after they have been updated for the just-ended
	 * game.
	 * 
	 * The values are in clockwise order.
	 * 
	 * If the current user is a player, the first element refers to the
	 * current user.
	 * 
	 * If the current user is a viewer, the first element refers to
	 * the table creator.
	 */
	public int[] playersTotalPoints;

	/**
	 * The default constructor.
	 */
	public GameEnded() {
	}

	/**
	 * A constructor that initializes all fields with the specified values.
	 * 
	 * @param matchPoints The points scored by all players in the current game.
	 * @param playersTotalPoints The global scores, after they have been updated
	 *                           for the just-ended game.
	 */
	public GameEnded(int[] matchPoints, int[] playersTotalPoints) {
		this.matchPoints = matchPoints;
		this.playersTotalPoints = playersTotalPoints;
	}
}
