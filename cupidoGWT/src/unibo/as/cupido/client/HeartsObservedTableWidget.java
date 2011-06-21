package unibo.as.cupido.client;

import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.InitialTableStatus;
import unibo.as.cupido.common.structures.ObservedGameStatus;
import unibo.as.cupido.common.structures.PlayerStatus;
import unibo.as.cupido.client.screens.ScreenManager;
import unibo.as.cupido.client.viewerstates.ViewerStateManager;
import unibo.as.cupido.client.viewerstates.ViewerStateManagerImpl;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;

public class HeartsObservedTableWidget extends AbsolutePanel {

	private ScreenManager screenManager;
	private BeforeGameWidget beforeGameWidget;
	private CardsGameWidget cardsGameWidget = null;
	private int tableSize;

	private ViewerStateManager stateManager;
	
	private boolean controlsDisabled = false;
	private ObservedGameStatus observedGameStatus;

	/**
	 * 
	 * @param tableSize
	 *            The size of the table (width and height) in pixels.
	 * @param username
	 */
	public HeartsObservedTableWidget(int tableSize, final String username,
			final ScreenManager screenManager, ObservedGameStatus observedGameStatus) {
		
		this.tableSize = tableSize;
		this.screenManager = screenManager;
		this.observedGameStatus = observedGameStatus;

		setWidth(tableSize + "px");
		setHeight(tableSize + "px");
		
		int numPlayers = 1;
		
		assert observedGameStatus.playerStatus[0] != null;
		
		for (int i = 0; i < 3; i++)
			assert observedGameStatus.playerStatus[i + 1] == null || observedGameStatus.playerStatus[i + 1].name != null;
		
		for (int i = 0; i < 3; i++)
			if (observedGameStatus.playerStatus[i + 1] != null)
				numPlayers++;
		
		if (numPlayers == 4) {
			startGame(username, observedGameStatus);
			return;
		}
		
		// Fill initialTableStatus using observedGameStatus
		InitialTableStatus initialTableStatus = new InitialTableStatus();
		initialTableStatus.opponents = new String[3];
		initialTableStatus.whoIsBot = new boolean[3];
		initialTableStatus.playerScores = new int[4];

		initialTableStatus.playerScores[0] = observedGameStatus.playerStatus[0].score;
		
		for (int i = 0; i < 3; i++)
			if (observedGameStatus.playerStatus[i + 1] == null) {
				initialTableStatus.opponents[i] = null;
			} else {
				initialTableStatus.opponents[i] = observedGameStatus.playerStatus[i + 1].name;
				initialTableStatus.whoIsBot[i] = observedGameStatus.playerStatus[i + 1].isBot;
				initialTableStatus.playerScores[i + 1] = observedGameStatus.playerStatus[i + 1].score;
			}
		
		beforeGameWidget = new BeforeGameWidget(tableSize, observedGameStatus.playerStatus[0].name, false,
				initialTableStatus,
				new BeforeGameWidget.Listener() {
					@Override
					public void onTableFull(InitialTableStatus initialTableStatus) {
						startGame(username, initialTableStatus);
					}

					@Override
					public void onGameEnded() {
						screenManager.displayMainMenuScreen(username);
						Window.alert("L'owner ha lasciato il tavolo, e quindi la partita \350 stata annullata.");
					}

					@Override
					public void onExit() {
						screenManager.displayMainMenuScreen(username);
					}
				});
		add(beforeGameWidget, 0, 0);
	}

	public void startGame(final String username, InitialTableStatus initialTableStatus) {

		if (controlsDisabled)
			return;
		
		// Update observedGameStatus with initialTableStatus.
		
		observedGameStatus.firstDealerInTrick = -1;
		observedGameStatus.playerStatus[0].numOfCardsInHand = 13;
		
		for (int i = 0; i < 3; i++) {
			observedGameStatus.playerStatus[i + 1].isBot = initialTableStatus.whoIsBot[i];
			observedGameStatus.playerStatus[i + 1].name = initialTableStatus.opponents[i];
			observedGameStatus.playerStatus[i + 1].numOfCardsInHand = 13;
			observedGameStatus.playerStatus[i + 1].score = initialTableStatus.playerScores[i + 1];
		}
		
		remove(beforeGameWidget);
		beforeGameWidget = null;
		
		startGame(username, observedGameStatus);
	}
	
	public void startGame(String username, ObservedGameStatus observedGameStatus) {
		
		stateManager = new ViewerStateManagerImpl(tableSize, screenManager,
				observedGameStatus, username);

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
		controlsDisabled = true;
	}

	public void handleCardPlayed(Card card, int playerPosition) {
		stateManager.handleCardPlayed(card, playerPosition);
	}

	public void handleGameEnded(int[] matchPoints, int[] playersTotalPoints) {
		stateManager.handleGameEnded(matchPoints, playersTotalPoints);
	}

	public void handlePlayerLeft(String player) {
		stateManager.handlePlayerLeft(player);
	}
}
