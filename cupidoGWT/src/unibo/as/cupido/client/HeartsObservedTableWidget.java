package unibo.as.cupido.client;

import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.InitialTableStatus;
import unibo.as.cupido.backendInterfaces.common.ObservedGameStatus;
import unibo.as.cupido.backendInterfaces.common.PlayerStatus;
import unibo.as.cupido.client.playerstates.PlayerStateManagerImpl;
import unibo.as.cupido.client.screens.ScreenSwitcher;
import unibo.as.cupido.client.viewerstates.ViewerStateManager;
import unibo.as.cupido.client.viewerstates.ViewerStateManagerImpl;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class HeartsObservedTableWidget extends AbsolutePanel {

	private ScreenSwitcher screenSwitcher;
	private BeforeGameWidget beforeGameWidget;
	private CardsGameWidget cardsGameWidget;
	private int tableSize;
	
	private ViewerStateManager stateManager;

	/**
	 * 
	 * @param tableSize
	 *            The size of the table (width and height) in pixels.
	 * @param username
	 */
	public HeartsObservedTableWidget(int tableSize, final String username,
			final ScreenSwitcher screenSwitcher) {
		this.tableSize = tableSize;
		this.screenSwitcher = screenSwitcher;
		
		setWidth(tableSize + "px");
		setHeight(tableSize + "px");

		// FIXME: Set `isOwner' to false when the servlet is ready.
		beforeGameWidget = new BeforeGameWidget(tableSize, "pippo", 1234, true,
				new BeforeGameWidget.Callback() {
					private int numPlayers = 1;
					@Override
					public void onAddBot(int position) {
						numPlayers++;
						if (numPlayers == 4)
							startGame(username);
					}
				});
		add(beforeGameWidget, 0, 0);
	}
	
	public void startGame(String username) {
				
		remove(beforeGameWidget);
		
		// FIXME: Initialize the widget with the correct values.
		// These values are only meant for debugging purposes.

		ObservedGameStatus observedGameStatus = new ObservedGameStatus();
		observedGameStatus.firstDealerInTrick = 0;

		observedGameStatus.playerStatus = new PlayerStatus[4];

		// Bottom player
		observedGameStatus.playerStatus[0] = new PlayerStatus();
		observedGameStatus.playerStatus[0].isBot = false;
		observedGameStatus.playerStatus[0].name = "bottom player name";
		observedGameStatus.playerStatus[0].numOfCardsInHand = 12;
		observedGameStatus.playerStatus[0].playedCard = new Card(11, Card.Suit.SPADES);
		observedGameStatus.playerStatus[0].score = 1234;

		// Left player
		observedGameStatus.playerStatus[1] = new PlayerStatus();
		observedGameStatus.playerStatus[1].isBot = false;
		observedGameStatus.playerStatus[1].name = "left player name";
		observedGameStatus.playerStatus[1].numOfCardsInHand = 12;
		observedGameStatus.playerStatus[1].playedCard = new Card(11, Card.Suit.HEARTS);
		observedGameStatus.playerStatus[1].score = 1234;

		// Top player
		observedGameStatus.playerStatus[2] = new PlayerStatus();
		observedGameStatus.playerStatus[2].isBot = true;
		observedGameStatus.playerStatus[2].name = null;
		observedGameStatus.playerStatus[2].numOfCardsInHand = 12;
		observedGameStatus.playerStatus[2].playedCard = new Card(1, Card.Suit.HEARTS);
		observedGameStatus.playerStatus[2].score = 1234;

		// Right player
		observedGameStatus.playerStatus[3] = new PlayerStatus();
		observedGameStatus.playerStatus[3].isBot = false;
		observedGameStatus.playerStatus[3].name = "right player name";
		observedGameStatus.playerStatus[3].numOfCardsInHand = 13;
		observedGameStatus.playerStatus[3].playedCard = null;
		observedGameStatus.playerStatus[3].score = 1234;
		
		stateManager = new ViewerStateManagerImpl(tableSize, screenSwitcher, observedGameStatus);
			
		final VerticalPanel controllerPanel = new VerticalPanel();
		controllerPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		controllerPanel
				.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

		final PushButton exitButton = new PushButton("Esci dal gioco");
		exitButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				screenSwitcher.displayMainMenuScreen();
			}
		});
		controllerPanel.add(exitButton);

		cardsGameWidget = stateManager.getWidget();
		add(cardsGameWidget, 0, 0);
	}
}
