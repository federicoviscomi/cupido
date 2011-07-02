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
 * Used to convey ranking information for a user.
 */
public class RankingEntry implements Serializable {
	private static final long serialVersionUID = 1L;
	/**name of user*/
	public String username;
	/**rank of user*/
	public int rank;
	/**score of user*/
	public int score;

	public RankingEntry() {
		//
	}

	public RankingEntry(String username, int rank, int score) {
		this.username = username;
		this.rank = rank;
		this.score = score;
	}
}
