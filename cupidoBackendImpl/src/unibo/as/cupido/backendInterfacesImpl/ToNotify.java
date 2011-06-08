package unibo.as.cupido.backendInterfacesImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import unibo.as.cupido.backendInterfaces.GlobalTableManagerInterface.ServletNotifcationsInterface;
import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.ChatMessage;

public class ToNotify {
	private Map<String, ServletNotifcationsInterface> snfList;

	public ToNotify() {
		snfList = new HashMap<String, ServletNotifcationsInterface>(4);
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
		for (ServletNotifcationsInterface snf : snfList.values()) {
			snf.notifyPlayerLeft(playerName);
		}
	}

	public void notifyGameStarted(String userName) {
		for (Entry<String, ServletNotifcationsInterface> snf : snfList.entrySet()) {
			if (!snf.getKey().equals(userName))
				snf.getValue().notifyGameStarted(null);
		}
	}

	public void notifyPlayerJoined(String name, int position, ServletNotifcationsInterface snf) {
		if (name == null || snf == null)
			throw new IllegalArgumentException();
		for (ServletNotifcationsInterface snfs : snfList.values()) {
			snfs.notifyPlayerJoined(name, false, 0, position);
		}
		snfList.put(name, snf);
	}

	public void notifyBotJoined(String botName, int position) {
		for (ServletNotifcationsInterface snfs : snfList.values()) {
			// System.out.println(snfs);
			snfs.notifyPlayerJoined(botName, true, 0, position);
		}
	}

	public void viewerJoined(String userName, ServletNotifcationsInterface snf) {
		if (userName == null || snf == null)
			throw new IllegalArgumentException();
		snfList.put(userName, snf);
	}
}
