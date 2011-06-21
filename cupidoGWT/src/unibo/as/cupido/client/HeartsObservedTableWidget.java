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
	
	private ObservedGameStatus observedGameStatus;
	private boolean frozen = false;
	private CupidoInterfaceAsync cupidoService;

	/**
	 * 
	 * @param tableSize
	 *            The size of the table (width and height) in pixels.
	 * @param username
	 */
	public HeartsObservedTableWidget(int tableSize, final String username,
			final ScreenManager screenManager, ObservedGameStatus observedGameStatus,
			CupidoInterfaceAsync cupidoService) {
		
		this.tableSize = tableSize;
		this.screenManager = screenManager;
		this.observedGameStatus = observedGameStatus;
		this.cupidoService = cupidoService;

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
				cupidoService, new BeforeGameWidget.Listener() {
					@Override
					public void onTableFull() {
						if (frozen) {
							System.out.println("Client: notice: received a onTableFull() event while frozen, ignoring it.");
							return;
						}
						startGame(username, beforeGameWidget.getInitialTableStatus());
					}

					@Override
					public void onGameEnded() {
						if (frozen) {
							System.out.println("Client: notice: received a onGameEnded() event while frozen, ignoring it.");
							return;
						}
						screenManager.displayMainMenuScreen(username);
						Window.alert("L'owner ha lasciato il tavolo, e quindi la partita \350 stata annullata.");
					}

					@Override
					public void onExit() {
						if (frozen) {
							System.out.println("Client: notice: received a onExit() event while frozen, ignoring it.");
							return;
						}
						screenManager.displayMainMenuScreen(username);
					}

					@Override
					public void onFatalException(Throwable e) {
						screenManager.displayGeneralErrorScreen(e);
					}
				});
		add(beforeGameWidget, 0, 0);
	}

	public void startGame(final String username, InitialTableStatus initialTableStatus) {

		if (frozen) {
			System.out.println("Client: notice: startGame() was called while frozen, ignoring it.");
			return;
		}
		
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
		
		assert !frozen;
		
		stateManager = new ViewerStateManagerImpl(tableSize, screenManager,
				observedGameStatus, username);

		cardsGameWidget = stateManager.getWidget();
		add(cardsGameWidget, 0, 0);
	}
	
	public void freeze() {
		if (beforeGameWidget != null)
			beforeGameWidget.freeze();
		if (cardsGameWidget != null) {
			cardsGameWidget.freeze();
			stateManager.freeze();
		}
		frozen = true;
	}

	public void handleCardPlayed(Card card, int playerPosition) {
		if (frozen) {
			System.out.println("Client: notice: received a CardPlayed notification while frozen, ignoring it.");
			return;
		}
		stateManager.handleCardPlayed(card, playerPosition);
	}

	public void handleGameEnded(int[] matchPoints, int[] playersTotalPoints) {
		if (frozen) {
			System.out.println("Client: notice: received a GameEnded notification while frozen, ignoring it.");
			return;
		}
		stateManager.handleGameEnded(matchPoints, playersTotalPoints);
	}

	public void handlePlayerLeft(String player) {
		if (frozen) {
			System.out.println("Client: notice: received a PlayerLeft notification while frozen, ignoring it.");
			return;
		}
		stateManager.handlePlayerLeft(player);
	}
}
