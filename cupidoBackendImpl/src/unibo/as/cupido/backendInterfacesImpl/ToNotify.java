package unibo.as.cupido.backendInterfacesImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import unibo.as.cupido.backendInterfaces.GlobalTableManagerInterface.ServletNotifcationsInterface;
import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.ChatMessage;

public class ToNotify {
	private Map<String, ServletNotifcationsInterface> snfList = new HashMap<String, ServletNotifcationsInterface>(4);

	public void add(String owner, ServletNotifcationsInterface snf) {
		snfList.put(owner, snf);
	}

	public void notifyCardPassed(Card[] cards, String name) {
		snfList.get(name).notifyPlassedCards(cards);
	}

	public void notifyCardPlayed(String userName, Card card, int playerPosition) {
		for (Entry<String, ServletNotifcationsInterface> snf : snfList.entrySet()) {
			if (!snf.getKey().equals(userName))
				snf.getValue().notifyPlayedCard(card, playerPosition);
		}
	}

	public void notifyMessageSent(ChatMessage message) {
		for (Entry<String, ServletNotifcationsInterface> snf : snfList.entrySet()) {
			if (!snf.getKey().equals(message.userName))
				snf.getValue().notifyLocalChatMessage(message);
		}
	}

	public void remove(String playerName) {
		snfList.remove(playerName);
	}
}
