package unibo.as.cupido.client;

import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.Card.Suit;
import unibo.as.cupido.backendInterfaces.common.ObservedGameStatus;
import unibo.as.cupido.backendInterfaces.common.PlayerStatus;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class HeartsTableWidget extends AbsolutePanel {
	
	private ScreenSwitcher screenSwitcher;
	private CardsGameWidget cardsGameWidget;
	
	/**
	 * 
	 * @param tableSize The size of the table (width and height) in pixels.
	 * @param username
	 */
	HeartsTableWidget(int tableSize, String username, final ScreenSwitcher screenSwitcher) {
		setWidth(tableSize + "px");
		setHeight(tableSize + "px");
		
		this.screenSwitcher = screenSwitcher;
		
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
		
		final VerticalPanel controllerPanel = new VerticalPanel();
		controllerPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		controllerPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		
		final PushButton exitButton = new PushButton("Esci dal gioco");
		exitButton.setWidth("80px");
		exitButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				screenSwitcher.displayMainMenuScreen();
			}
		});
		controllerPanel.add(exitButton);
		
		cardsGameWidget = new CardsGameWidget(tableSize, observedGameStatus, bottomPlayerCards,
				                                      controllerPanel, new CardsGameWidget.GameEventListener() {			
			@Override
			public void onAnimationStart() {
				exitButton.setEnabled(false);
				
			}
			@Override
			public void onAnimationEnd() {
				exitButton.setEnabled(true);
			}
			@Override
			public void onCardClicked(int player, Card card, CardsGameWidget.CardRole.State state) {
				if (card != null && state == CardsGameWidget.CardRole.State.HAND)
					cardsGameWidget.dealCard(player, card, new GWTAnimation.AnimationCompletedListener() {
						@Override
						public void onComplete() {
						}
					});
			}
		});
		add(cardsGameWidget, 0, 0);
	}
}
