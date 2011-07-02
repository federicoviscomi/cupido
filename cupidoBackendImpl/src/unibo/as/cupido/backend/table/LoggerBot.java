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

package unibo.as.cupido.backend.table;

import java.util.Arrays;

import unibo.as.cupido.backend.table.bot.LocalBotInterface;
import unibo.as.cupido.common.interfaces.ServletNotificationsInterface;
import unibo.as.cupido.common.interfaces.TableInterface;
import unibo.as.cupido.common.structures.Card;

/**
 * A local bot implementation that does nothing but logs methods calls. This is
 * used only for logging creator operation for test purpose.
 */
public class LoggerBot implements LocalBotInterface {

	/** this bot name */
	private final String botName;

	/**
	 * Create a new logger bot whit name <tt>name</tt>
	 * 
	 * @param botName
	 *            this bot name
	 */
	public LoggerBot(final String botName) {
		this.botName = botName;
	}

	@Override
	public void activate(TableInterface tableInterface) {
		System.out.println("" + botName + " activate(" + tableInterface + ")");
	}


	@Override
	public ServletNotificationsInterface getServletNotificationsInterface() {
		return new LoggerServletNotification(botName);
	}

	@Override
	public void passCards(Card[] cards) {
		System.out.println("" + botName + " passCards("
				+ Arrays.toString(cards) + ")");
	}

	@Override
	public void playCard(Card card) {
		System.out.println("" + botName + " playCard(" + card + ")");
	}
}
