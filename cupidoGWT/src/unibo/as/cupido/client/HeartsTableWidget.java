package unibo.as.cupido.client;

import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.ObservedGameStatus;
import unibo.as.cupido.backendInterfaces.common.PlayerStatus;
import unibo.as.cupido.client.screens.ScreenSwitcher;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class HeartsTableWidget extends AbsolutePanel {

	private ScreenSwitcher screenSwitcher;
	private BeforeGameWidget beforeGameWidget;
	private CardsGameWidget cardsGameWidget;
	private int tableSize;
	
	public enum UserRole {
		/**
		 * The user is the creator of the table.
		 */
		OWNER,
		/**
		 * The user is a player, but not the owner.
		 */
		PLAYER,
		/**
		 * The user is not a player, but it's only viewing the table.
		 */
		VIEWER
	};
	
	private int numPlayers = 1;

	/**
	 * 
	 * @param tableSize
	 *            The size of the table (width and height) in pixels.
	 * @param username
	 */
	public HeartsTableWidget(int tableSize, String username,
			final ScreenSwitcher screenSwitcher) {
		this.tableSize = tableSize;
		this.screenSwitcher = screenSwitcher;
		
		setWidth(tableSize + "px");
		setHeight(tableSize + "px");

		beforeGameWidget = new BeforeGameWidget(tableSize, "pippo", 1234, true,
				new BeforeGameWidget.Callback() {
					@Override
					public void onAddBot(int position) {
						numPlayers++;
						if (numPlayers == 4)
							startGame();
					}
				});
		add(beforeGameWidget, 0, 0);
	}
	
	public void startGame() {
		
		remove(beforeGameWidget);

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
		controllerPanel
				.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

		final PushButton exitButton = new PushButton("Esci dal gioco");
		exitButton.setWidth("80px");
		exitButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				screenSwitcher.displayMainMenuScreen();
			}
		});
		controllerPanel.add(exitButton);

		cardsGameWidget = new CardsGameWidget(tableSize, observedGameStatus,
				bottomPlayerCards, controllerPanel,
				new CardsGameWidget.GameEventListener() {
			
					private int n = 0;
					private int nextPlayer = 0;
			
					@Override
					public void onAnimationStart() {
						exitButton.setEnabled(false);

					}

					@Override
					public void onAnimationEnd() {
						exitButton.setEnabled(true);
					}

					@Override
					public void onCardClicked(int player, Card card,
							CardsGameWidget.CardRole.State state,
							boolean isRaised) {
						
						GWTAnimation.AnimationCompletedListener emptyListener = new GWTAnimation.AnimationCompletedListener() {
							@Override
							public void onComplete() {
							}
						};
						
						if (card != null
								&& state == CardsGameWidget.CardRole.State.HAND) {
							if (isRaised) {
								cardsGameWidget.lowerRaisedCard(player, card);
								cardsGameWidget.runPendingAnimations(300, emptyListener);
							} else {
								if (n == 4) {
									cardsGameWidget.animateTrickTaking(nextPlayer, 2000, 2000, emptyListener);
									n = 0;
									nextPlayer++;
									nextPlayer = nextPlayer % 4;
								} else {
									n++;
									cardsGameWidget.revealCoveredCard(1, card);
	
									cardsGameWidget.raiseCard(player, card);
									cardsGameWidget.dealCard(1, card);
									cardsGameWidget.runPendingAnimations(300, emptyListener);
								}
							}
						}
					}
				});
		add(cardsGameWidget, 0, 0);
	}
}
