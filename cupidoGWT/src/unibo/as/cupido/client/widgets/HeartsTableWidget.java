/*  Cupido - An online Hearts game.
 *  Copyright (C) 2011 Lorenzo Belli, Marco Poletti, Federico Viscomi
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package unibo.as.cupido.client.widgets;

import unibo.as.cupido.client.CupidoInterfaceAsync;
import unibo.as.cupido.client.playerstates.PlayerStateManager;
import unibo.as.cupido.client.playerstates.PlayerStateManagerImpl;
import unibo.as.cupido.client.screens.ScreenManager;
import unibo.as.cupido.common.exception.GameEndedException;
import unibo.as.cupido.common.exception.GameInterruptedException;
import unibo.as.cupido.common.exception.NoSuchTableException;
import unibo.as.cupido.common.exception.UserNotAuthenticatedException;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.InitialTableStatus;

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
	private String username;
	private CupidoInterfaceAsync cupidoService;
	private int[] scores;
	private ChatWidget chatWidget;

	/**
	 * 
	 * @param tableSize
	 *            The size of the table (width and height) in pixels.
	 * @param username
	 * @param initialTableStatus
	 * @param isOwner
	 * @param userScore
	 */
	public HeartsTableWidget(int tableSize, final String username,
			InitialTableStatus initialTableStatus, final boolean isOwner,
			int userScore, final ScreenManager screenManager,
			ChatWidget chatWidget, final CupidoInterfaceAsync cupidoService) {

		this.username = username;
		this.tableSize = tableSize;
		this.screenManager = screenManager;
		this.chatWidget = chatWidget;
		this.cupidoService = cupidoService;

		setWidth(tableSize + "px");
		setHeight(tableSize + "px");

		scores = new int[4];

		scores[0] = userScore;

		for (int i = 0; i < 3; i++)
			scores[i + 1] = initialTableStatus.playerScores[i];

		beforeGameWidget = new BeforeGameWidget(tableSize, username, username,
				isOwner, initialTableStatus, scores, cupidoService,
				new BeforeGameWidget.Listener() {
					@Override
					public void onTableFull() {
						// Just wait for the GameStarted notification.
						beforeGameWidget.freeze();
					}

					@Override
					public void onGameEnded() {
						if (frozen) {
							System.out
									.println("Client: notice: the onGameEnded() event was received while frozen, ignoring it.");
							return;
						}

						assert !isOwner;
						screenManager.displayMainMenuScreen(username);
						Window.alert("L'owner ha lasciato il tavolo, e quindi la partita \350 stata annullata.");
					}

					@Override
					public void onExit() {
						if (frozen) {
							System.out
									.println("Client: notice: the onExit() event was received while frozen, ignoring it.");
							return;
						}
						beforeGameWidget.freeze();
						cupidoService.leaveTable(new AsyncCallback<Void>() {
							@Override
							public void onFailure(Throwable caught) {
								try {
									throw caught;
								} catch (NoSuchTableException e) {
									// This can happen even if no problems occur.
								} catch (GameInterruptedException e) {
									// This can happen even if no problems occur.
								} catch (GameEndedException e) {
									// This can happen even if no problems occur.
								} catch (Throwable e) {
									screenManager.displayGeneralErrorScreen(e);
									return;
								}
								screenManager.displayMainMenuScreen(username);
							}

							@Override
							public void onSuccess(Void result) {
								screenManager.displayMainMenuScreen(username);
							}
						});
					}

					@Override
					public void onFatalException(Throwable e) {
						screenManager.displayGeneralErrorScreen(e);
					}
				});
		add(beforeGameWidget, 0, 0);
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

	public void handleGameStarted(Card[] myCards) {
		if (frozen) {
			System.out
					.println("Client: notice: handleGameStarted() was called while frozen, ignoring it.");
			return;
		}

		InitialTableStatus initialTableStatus = beforeGameWidget
				.getInitialTableStatus();

		remove(beforeGameWidget);
		beforeGameWidget = null;

		stateManager = new PlayerStateManagerImpl(tableSize, screenManager,
				chatWidget, initialTableStatus, scores, myCards, username,
				cupidoService);

		cardsGameWidget = stateManager.getWidget();
		add(cardsGameWidget, 0, 0);
	}

	public void handleCardPassed(Card[] cards) {
		if (frozen) {
			System.out
					.println("Client: notice: handleCardPassed() was called while frozen, ignoring it.");
			return;
		}

		if (cardsGameWidget == null) {
			System.out
					.println("Client: HeartsTableWidget: warning: CardPassed received before the game start, it was ignored.");
		} else {
			stateManager.handleCardPassed(cards);
		}
	}

	public void handleCardPlayed(Card card, int playerPosition) {
		if (frozen) {
			System.out
					.println("Client: notice: handleCardPlayed() was called while frozen, ignoring it.");
			return;
		}

		if (cardsGameWidget == null) {
			System.out
					.println("Client: HeartsTableWidget: warning: CardPlayed received before the game start, it was ignored.");
		} else {
			stateManager.handleCardPlayed(card, playerPosition);
		}
	}

	public void handleGameEnded(int[] matchPoints, int[] playersTotalPoints) {
		if (frozen) {
			System.out
					.println("Client: notice: handleGameEnded() was called while frozen, ignoring it.");
			return;
		}
		if (cardsGameWidget == null) {
			beforeGameWidget.handleGameEnded(matchPoints, playersTotalPoints);
		} else {
			stateManager.handleGameEnded(matchPoints, playersTotalPoints);
		}
	}

	public void handleNewPlayerJoined(String name, boolean isBot, int points,
			int position) {
		if (frozen) {
			System.out
					.println("Client: notice: handleNewPlayerJoined() was called while frozen, ignoring it.");
			return;
		}

		if (cardsGameWidget == null) {
			beforeGameWidget.handleNewPlayerJoined(name, isBot, points,
					position);
		} else {
			System.out
					.println("Client: HeartsTableWidget: warning: NewPlayerJoined received after the game start, it was ignored.");
		}
	}

	public void handlePlayerLeft(String player) {
		if (frozen) {
			System.out
					.println("Client: notice: handlePlayerLeft() was called while frozen, ignoring it.");
			return;
		}

		if (cardsGameWidget == null) {
			beforeGameWidget.handlePlayerLeft(player);
		} else {
			screenManager
			.displayGeneralErrorScreen(new Exception(
					"A PlayerLeft notification was received while playing a game that was already started."));
		}
	}

	public void handlePlayerReplaced(String name, int position) {
		if (frozen) {
			System.out
					.println("Client: notice: received a PlayerReplaced notification while frozen, ignoring it.");
			return;
		}
		if (beforeGameWidget != null) {
			screenManager
			.displayGeneralErrorScreen(new Exception(
					"A PlayerReplaced notification was received while playing a game that wasn't started yet."));
		} else {
			// The +1 is needed for the different meaning that `position' has
			// for players.
			stateManager.handlePlayerReplaced(name, position + 1);
		}
	}
}
