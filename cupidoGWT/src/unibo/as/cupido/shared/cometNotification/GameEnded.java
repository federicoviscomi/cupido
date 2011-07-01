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
 * Every players and every viewers in the table get this notification when game
 * ends. The game could end normally or prematurely. The last happens when
 * player creator leaves the table before normal end of the game, in this case
 * and only in this case all fileds are <code>null</code>.
 * 
 */
public class GameEnded implements Serializable {

	private static final long serialVersionUID = 1L;

	/*
	 * matchPoint[0] are your points, if you have played matchPoint[0] are the
	 * creator's points if you were viewing the others are in clockwise order
	 */
	public int[] matchPoints;

	/*
	 * matchPoint[0] are your points, if you have played matchPoint[0] are the
	 * creator's points if you were viewing the others are in clockwise order
	 */
	public int[] playersTotalPoints;

	public GameEnded() {
	}

	public GameEnded(int[] matchPoints, int[] playersTotalPoints) {
		this.matchPoints = matchPoints;
		this.playersTotalPoints = playersTotalPoints;
	}
}
