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

package unibo.as.cupido.backend.table.bot;

import unibo.as.cupido.common.interfaces.ServletNotificationsInterface;
import unibo.as.cupido.common.interfaces.TableInterface;
import unibo.as.cupido.common.structures.Card;

/**
 * This is implemented by local bot.
 */
public interface LocalBotInterface {

	/**
	 * Activate this bot.
	 * 
	 * @param tableInterface
	 *            the new table interface that this bot should use
	 */
	void activate(TableInterface tableInterface);

	/**
	 * Return a notification interface for this bot.
	 * 
	 * @return a notification interface for this bot
	 */
	ServletNotificationsInterface getServletNotificationsInterface();

	/**
	 * Make the bot pass specified cards
	 * 
	 * @param cards
	 *            the cards to be passed by the bot
	 */
	void passCards(Card[] cards);

	/**
	 * Make the bot play specified card
	 * 
	 * @param card
	 *            the card to be played by the bot
	 */
	void playCard(Card card);
}
