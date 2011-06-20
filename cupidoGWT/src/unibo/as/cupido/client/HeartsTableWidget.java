package unibo.as.cupido.client;

import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.InitialTableStatus;
import unibo.as.cupido.client.playerstates.PlayerStateManager;
import unibo.as.cupido.client.playerstates.PlayerStateManagerImpl;
import unibo.as.cupido.client.screens.ScreenManager;

import com.google.gwt.user.client.ui.AbsolutePanel;

public class HeartsTableWidget extends AbsolutePanel {

	private ScreenManager screenManager;
	private BeforeGameWidget beforeGameWidget;
	private CardsGameWidget cardsGameWidget = null;
	private int tableSize;

	private PlayerStateManager stateManager = null;
	private boolean controlsDisabled = false;

	/**
	 * 
	 * @param tableSize
	 *            The size of the table (width and height) in pixels.
	 * @param username
	 * @param initialTableStatus 
	 * @param isOwner 
	 */
	public HeartsTableWidget(int tableSize, final String username,
			InitialTableStatus initialTableStatus, boolean isOwner, final ScreenManager screenManager) {
		this.tableSize = tableSize;
		this.screenManager = screenManager;

		setWidth(tableSize + "px");
		setHeight(tableSize + "px");
				
		beforeGameWidget = new BeforeGameWidget(tableSize, username, initialTableStatus.playerScores[0], isOwner,
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

	public void startGame(final String username) {

		if (controlsDisabled)
			return;
		
		remove(beforeGameWidget);
		beforeGameWidget = null;

		// FIXME: Initialize the widget with the correct values.
		// These values are only meant for debugging purposes.

		InitialTableStatus initialTableStatus = new InitialTableStatus();

		initialTableStatus.opponents = new String[3];
		initialTableStatus.opponents[0] = "Pinco";
		initialTableStatus.opponents[1] = "This must *not* be displayed";
		initialTableStatus.opponents[2] = "Pallo";

		initialTableStatus.playerScores = new int[4];
		initialTableStatus.playerScores[0] = 1234;
		initialTableStatus.playerScores[1] = 5678;
		initialTableStatus.playerScores[2] = 9012;
		initialTableStatus.playerScores[3] = 3456;

		initialTableStatus.whoIsBot = new boolean[3];
		initialTableStatus.whoIsBot[0] = false;
		initialTableStatus.whoIsBot[1] = true;
		initialTableStatus.whoIsBot[2] = false;

		Card[] bottomPlayerCards = new Card[13];

		for (int i = 0; i < 13; i++)
			bottomPlayerCards[i] = RandomCardGenerator.generateCard();

		stateManager = new PlayerStateManagerImpl(tableSize, screenManager,
				initialTableStatus, bottomPlayerCards, username);

		cardsGameWidget = stateManager.getWidget();
		add(cardsGameWidget, 0, 0);
	}
	
	public void disableControls() {
		if (beforeGameWidget != null)
			beforeGameWidget.disableControls();
		if (cardsGameWidget != null) {
			cardsGameWidget.disableControls();
			stateManager.disableControls();
		}
		controlsDisabled  = true;
	}

	public void handleCardPassed(Card[] cards) {
		if (cardsGameWidget == null) {
			System.out.println("Client: HeartsTableWidget: warning: CardPassed received before the game start, it was ignored.");
		} else {
			stateManager.handleCardPassed(cards);
		}
	}

	public void handleCardPlayed(Card card, int playerPosition) {
		if (cardsGameWidget == null) {
			System.out.println("Client: HeartsTableWidget: warning: CardPlayed received before the game start, it was ignored.");
		} else {
			stateManager.handleCardPlayed(card, playerPosition);
		}
	}

	public void handleGameEnded(int[] matchPoints, int[] playersTotalPoints) {
		if (cardsGameWidget == null) {
			beforeGameWidget.handleGameEnded(matchPoints, playersTotalPoints);
		} else {
			stateManager.handleGameEnded(matchPoints, playersTotalPoints);
		}
	}

	public void handleGameStarted(Card[] myCards) {
		if (cardsGameWidget == null) {
			System.out.println("Client: HeartsTableWidget: warning: GameStarted received before the game start, it was ignored.");
		} else {
			stateManager.handleGameStarted(myCards);
		}
	}

	public void handleNewPlayerJoined(String name, boolean isBot, int points,
			int position) {
		if (cardsGameWidget == null) {
			beforeGameWidget.handleNewPlayerJoined(name, isBot, points, position);
		} else {
			System.out.println("Client: HeartsTableWidget: warning: NewPlayerJoined received after the game start, it was ignored.");
		}
	}

	public void handlePlayerLeft(String player) {
		if (cardsGameWidget == null) {
			beforeGameWidget.handlePlayerLeft(player);
		} else {
			stateManager.handlePlayerLeft(player);
		}
	}
}
