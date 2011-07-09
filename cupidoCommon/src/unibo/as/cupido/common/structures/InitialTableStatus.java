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
 * InitialTableStatus is the status of the game before the cards are dealt (may
 * have less than 4 player). When a player joins the table, he gets an instance
 * of this class.
 */
public class InitialTableStatus implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Stores opponents' name. <code>opponents.length</code> must be three.
	 * <code>opponents[i]</code> is the name of the opponent i positions next to
	 * you in clockwise order. A <code>null</code> value indicates that opponent
	 * does not exist yet
	 */
	public String[] opponents;

	/**
	 * Stores opponents' score. <code>playerScores.length</code> must be 3.
	 * <code>playerScores[i]</code> is the score of the opponent i positions
	 * next to you in clockwise order. If the opponent does not exist yet, the
	 * value is unspecified.
	 */
	public int[] playerScores;

	/**
	 * Stores opponents' bot flag. <code>whoIsBot.length</code> is 3.
	 * <code>whoIsBot[i]</code> is the score of the opponent i positions next to
	 * you in clockwise order. If the opponent does not exist yet, the value is
	 * unspecified.
	 */
	public boolean[] whoIsBot;

	/**
	 * GWT requires this constructor.
	 */
	public InitialTableStatus() {
		//
	}

	/**
	 * Create an initial table status with specified values.
	 * 
	 * @param opponents
	 *            names of opponents
	 * @param playerScores
	 *            scores of opponents
	 * @param whoIsBot
	 *            bot flag of opponents
	 */
	public InitialTableStatus(String[] opponents, int[] playerScores,
			boolean[] whoIsBot) {
		this.opponents = opponents;
		this.playerScores = playerScores;
		this.whoIsBot = whoIsBot;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int i = 0; i < 3; i++) {
			sb.append("[" + opponents[i] + ", " + playerScores[i] + ", "
					+ whoIsBot[i] + "]");
			if (i != 2)
				sb.append(", ");
		}
		sb.append("]");
		return sb.toString();
	}
}