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
