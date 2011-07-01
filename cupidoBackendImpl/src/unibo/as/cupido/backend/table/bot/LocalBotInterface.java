package unibo.as.cupido.backend.table.bot;

import unibo.as.cupido.common.interfaces.ServletNotificationsInterface;
import unibo.as.cupido.common.interfaces.TableInterface;
import unibo.as.cupido.common.structures.Card;

public interface LocalBotInterface {

	void activate(TableInterface tableInterface);

	ServletNotificationsInterface getServletNotificationsInterface();

	void passCards(Card[] cards);

	void playCard(Card card);
}
