package unibo.as.cupido.client;

import java.util.ArrayList;
import java.util.List;

import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.ObservedGameStatus;
import unibo.as.cupido.backendInterfaces.common.PlayerStatus;

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
		
		ObservedGameStatus observedGameStatus = new ObservedGameStatus();
		
		observedGameStatus.ogs = new PlayerStatus[4];
		
		// Bottom player
		observedGameStatus.ogs[0] = new PlayerStatus();
		observedGameStatus.ogs[0].isBot = false;
		observedGameStatus.ogs[0].name = "bottom player name";
		observedGameStatus.ogs[0].numOfCardsInHand = 13;
		observedGameStatus.ogs[0].playedCard = new Card(11, Card.Suit.SPADES);
		observedGameStatus.ogs[0].score = 1234;
		
		// Left player
		observedGameStatus.ogs[1] = new PlayerStatus();
		observedGameStatus.ogs[1].isBot = false;
		observedGameStatus.ogs[1].name = "left player name";
		observedGameStatus.ogs[1].numOfCardsInHand = 13;
		observedGameStatus.ogs[1].playedCard = new Card(11, Card.Suit.HEARTS);
		observedGameStatus.ogs[1].score = 1234;
		
		// Top player
		observedGameStatus.ogs[2] = new PlayerStatus();
		observedGameStatus.ogs[2].isBot = true;
		observedGameStatus.ogs[2].name = null;
		observedGameStatus.ogs[2].numOfCardsInHand = 5;
		observedGameStatus.ogs[2].playedCard = new Card(1, Card.Suit.HEARTS);
		observedGameStatus.ogs[2].score = 1234;
		
		// Right player
		observedGameStatus.ogs[3] = new PlayerStatus();
		observedGameStatus.ogs[3].isBot = false;
		observedGameStatus.ogs[3].name = "right player name";
		observedGameStatus.ogs[3].numOfCardsInHand = 13;
		observedGameStatus.ogs[3].playedCard = null;
		observedGameStatus.ogs[3].score = 1234;
		
		Card[] bottomPlayerCards = new Card[13];
				
		for (int i = 0; i < 13; i++)
			bottomPlayerCards[i] = new Card(i + 1, Card.Suit.CLUBS);
		
		CardsGameWidget x = new CardsGameWidget(tableSize, observedGameStatus, bottomPlayerCards);
		add(x, 0, 0);		
	}
}
