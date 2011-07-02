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
