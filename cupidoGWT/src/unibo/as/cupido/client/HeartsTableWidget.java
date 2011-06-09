package unibo.as.cupido.client;

import java.util.ArrayList;
import java.util.List;

import unibo.as.cupido.backendInterfaces.common.Card;

import com.google.gwt.user.client.ui.AbsolutePanel;

public class HeartsTableWidget extends AbsolutePanel {
	
	/**
	 * 
	 * @param tableSize The size of the table (width and height) in pixels.
	 * @param username
	 */
	HeartsTableWidget(int tableSize, String username) {
		setWidth(tableSize + "px");
		setHeight(tableSize + "px");
		
		// FIXME: Initialize the widget with the correct values.
		// These values are only meant for debugging purposes.
		
		CardsGameWidget.PlayersCards playersCards = new CardsGameWidget.PlayersCards();
		
		playersCards.hands = new ArrayList<List<Card>>();
		for (int i = 0; i < 4; i++)
			playersCards.hands.add(new ArrayList<Card>());
		playersCards.dealt = new ArrayList<List<Card>>();
		for (int i = 0; i < 4; i++)
			playersCards.dealt.add(new ArrayList<Card>());
		
		for (int j = 0; j < 13; j++)
			playersCards.hands.get(0).add(new Card(j + 1, Card.Suit.HEARTS));
		for (int j = 0; j < 13; j++)
			playersCards.hands.get(1).add(null);
		for (int j = 0; j < 13; j++)
			playersCards.hands.get(2).add(null);
		for (int j = 0; j < 13; j++)
			playersCards.hands.get(3).add(null);
		
		for (int j = 0; j < 3; j++)
			playersCards.dealt.get(0).add(new Card(j + 1, Card.Suit.HEARTS));
		for (int j = 0; j < 1; j++)
			playersCards.dealt.get(1).add(new Card(j + 1, Card.Suit.SPADES));
		for (int j = 0; j < 1; j++)
			playersCards.dealt.get(2).add(new Card(j + 1, Card.Suit.DIAMONDS));
		for (int j = 0; j < 1; j++)
			playersCards.dealt.get(3).add(new Card(j + 1, Card.Suit.CLUBS));
		
		CardsGameWidget x = new CardsGameWidget(tableSize, playersCards);
		add(x, 0, 0);
	}
}
