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

public class PlayerStatus implements Serializable {

	private static final long serialVersionUID = 1L;

	public String name;

	/*
	 * total player points
	 */
	public int score;

	/*
	 * Card == null means player has not played a card
	 */
	public Card playedCard;

	public int numOfCardsInHand;

	public boolean isBot;

	public PlayerStatus() {
	}

	public PlayerStatus(String name, int point, Card playedCard,
			int numOfCardsInHand, boolean isBot) {
		this.name = name;
		this.score = point;
		this.playedCard = playedCard;
		this.numOfCardsInHand = numOfCardsInHand;
		this.isBot = isBot;
	}

	@Override
	public String toString() {
		return "[" + "name=" + name + ", score=" + score + ", card played="
				+ playedCard + ", cards count=" + numOfCardsInHand
				+ ", is bot=" + isBot + "]";
	}
}
