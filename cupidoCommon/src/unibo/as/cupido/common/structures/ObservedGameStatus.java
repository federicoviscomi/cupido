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
 * 
 * Contains the information that an observer needs when he joins a table
 * 
 * @author cane
 * 
 */
public class ObservedGameStatus implements Serializable {

	private static final long serialVersionUID = 1L;

	public PlayerStatus[] playerStatus;

	/**
	 * The index of the first player that dealt a card in the current trick.
	 * When this is 0, the bottom player led the trick, when it's 1 the trick
	 * was led by the left player, and so on for other players, in clockwise
	 * order.
	 * 
	 * If there is currently no trick, this is -1. This can happen in three
	 * cases: when some players are still missing in the table, when players are
	 * passing cards, when the players have passed cards but no-one has dealt
	 * the two of clubs yet.
	 */
	public int firstDealerInTrick;

	public ObservedGameStatus() {
		playerStatus = new PlayerStatus[4];
	}

	public ObservedGameStatus(PlayerStatus[] ogs, int firstDealerInTrick) {
		this.playerStatus = ogs;
		this.firstDealerInTrick = firstDealerInTrick;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 4; i++)
			if (playerStatus[i] != null)
				sb.append(playerStatus[i].toString());
		return sb.toString();
	}
}
