package unibo.as.cupido.client;

import unibo.as.cupido.common.exception.NoSuchTableException;
import unibo.as.cupido.common.exception.UserNotAuthenticatedException;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.InitialTableStatus;
import unibo.as.cupido.client.playerstates.PlayerStateManager;
import unibo.as.cupido.client.playerstates.PlayerStateManagerImpl;
import unibo.as.cupido.client.screens.ScreenManager;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;

public class HeartsTableWidget extends AbsolutePanel {

	private ScreenManager screenManager;
	private BeforeGameWidget beforeGameWidget;
	private CardsGameWidget cardsGameWidget = null;
	private int tableSize;

	private PlayerStateManager stateManager = null;
	private boolean frozen = false;
	
	/**
	 * 
	 * @param tableSize
	 *            The size of the table (width and height) in pixels.
	 * @param username
	 * @param initialTableStatus 
	 * @param isOwner 
	 */
	public HeartsTableWidget(int tableSize, final String username,
			InitialTableStatus initialTableStatus, final boolean isOwner,
			final ScreenManager screenManager, final CupidoInterfaceAsync cupidoService) {
		this.tableSize = tableSize;
		this.screenManager = screenManager;

		setWidth(tableSize + "px");
		setHeight(tableSize + "px");
		
		beforeGameWidget = new BeforeGameWidget(tableSize, username, isOwner, initialTableStatus,
				new BeforeGameWidget.Listener() {
					@Override
					public void onTableFull(InitialTableStatus initialTableStatus) {
						
						if (frozen) {
							System.out.println("Client: notice: the onTableFull() event was received while frozen, ignoring it.");
							return;
						}
						
						startGame(username, initialTableStatus);
					}

					@Override
					public void onGameEnded() {
						if (frozen) {
							System.out.println("Client: notice: the onGameEnded() event was received while frozen, ignoring it.");
							return;
						}
						
						assert !isOwner;
						screenManager.displayMainMenuScreen(username);
						Window.alert("L'owner ha lasciato il tavolo, e quindi la partita \350 stata annullata.");
					}

					@Override
					public void onExit() {
						if (frozen) {
							System.out.println("Client: notice: the onExit() event was received while frozen, ignoring it.");
							return;
						}
						beforeGameWidget.freeze();
						cupidoService.leaveTable(new AsyncCallback<Void>() {
							@Override
							public void onFailure(Throwable caught) {
								try {
									throw caught;
								} catch (UserNotAuthenticatedException e) {
									screenManager.displayGeneralErrorScreen(e);
								} catch (NoSuchTableException e) {
									// The table has been deleted by the owner, before
									// the leaveTable() request was processed.
									// Just ignore this exception.
									screenManager.displayMainMenuScreen(username);
								} catch (Throwable e) {
									screenManager.displayGeneralErrorScreen(e);
								}
							}

							@Override
							public void onSuccess(Void result) {
								screenManager.displayMainMenuScreen(username);
							}
						});
						
					}
				});
		add(beforeGameWidget, 0, 0);
	}

	public void startGame(final String username, InitialTableStatus initialTableStatus) {

		if (frozen) {
			System.out.println("Client: notice: startGame() was called while frozen, ignoring it.");
			return;
		}
		
		remove(beforeGameWidget);
		beforeGameWidget = null;

		// FIXME: Initialize the widget with the correct values.
		// These values are only meant for debugging purposes.

		Card[] bottomPlayerCards = new Card[13];

		for (int i = 0; i < 13; i++)
			bottomPlayerCards[i] = RandomCardGenerator.generateCard();

		stateManager = new PlayerStateManagerImpl(tableSize, screenManager,
				initialTableStatus, bottomPlayerCards, username);

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

	public void handleCardPassed(Card[] cards) {
		if (frozen) {
			System.out.println("Client: notice: handleCardPassed() was called while frozen, ignoring it.");
			return;
		}
		
		if (cardsGameWidget == null) {
			System.out.println("Client: HeartsTableWidget: warning: CardPassed received before the game start, it was ignored.");
		} else {
			stateManager.handleCardPassed(cards);
		}
	}

	public void handleCardPlayed(Card card, int playerPosition) {
		if (frozen) {
			System.out.println("Client: notice: handleCardPlayed() was called while frozen, ignoring it.");
			return;
		}
		
		if (cardsGameWidget == null) {
			System.out.println("Client: HeartsTableWidget: warning: CardPlayed received before the game start, it was ignored.");
		} else {
			stateManager.handleCardPlayed(card, playerPosition);
		}
	}

	public void handleGameEnded(int[] matchPoints, int[] playersTotalPoints) {
		if (frozen) {
			System.out.println("Client: notice: handleGameEnded() was called while frozen, ignoring it.");
			return;
		}
		if (cardsGameWidget == null) {
			beforeGameWidget.handleGameEnded(matchPoints, playersTotalPoints);
		} else {
			stateManager.handleGameEnded(matchPoints, playersTotalPoints);
		}
	}

	public void handleGameStarted(Card[] myCards) {
		if (frozen) {
			System.out.println("Client: notice: handleGameStarted() was called while frozen, ignoring it.");
			return;
		}
		
		if (cardsGameWidget == null) {
			System.out.println("Client: HeartsTableWidget: warning: GameStarted received before the game start, it was ignored.");
		} else {
			stateManager.handleGameStarted(myCards);
		}
	}

	public void handleNewPlayerJoined(String name, boolean isBot, int points,
			int position) {
		if (frozen) {
			System.out.println("Client: notice: handleNewPlayerJoined() was called while frozen, ignoring it.");
			return;
		}
		
		if (cardsGameWidget == null) {
			beforeGameWidget.handleNewPlayerJoined(name, isBot, points, position);
		} else {
			System.out.println("Client: HeartsTableWidget: warning: NewPlayerJoined received after the game start, it was ignored.");
		}
	}

	public void handlePlayerLeft(String player) {
		if (frozen) {
			System.out.println("Client: notice: handlePlayerLeft() was called while frozen, ignoring it.");
			return;
		}
		
		if (cardsGameWidget == null) {
			beforeGameWidget.handlePlayerLeft(player);
		} else {
			stateManager.handlePlayerLeft(player);
		}
	}
}
