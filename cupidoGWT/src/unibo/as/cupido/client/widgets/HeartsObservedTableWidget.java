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
import unibo.as.cupido.client.screens.ScreenManager;
import unibo.as.cupido.client.viewerstates.ViewerStateManager;
import unibo.as.cupido.client.viewerstates.ViewerStateManagerImpl;
import unibo.as.cupido.common.exception.GameEndedException;
import unibo.as.cupido.common.exception.GameInterruptedException;
import unibo.as.cupido.common.exception.NoSuchTableException;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.InitialTableStatus;
import unibo.as.cupido.common.structures.ObservedGameStatus;
import unibo.as.cupido.common.structures.PlayerStatus;
import unibo.as.cupido.shared.cometNotification.CardPlayed;
import unibo.as.cupido.shared.cometNotification.GameEnded;
import unibo.as.cupido.shared.cometNotification.NewPlayerJoined;
import unibo.as.cupido.shared.cometNotification.PlayerLeft;
import unibo.as.cupido.shared.cometNotification.PlayerReplaced;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;

/**
 * This class displays the table for a Hearts game, for users that are viewing a
 * game. The game may or may not be started.
 * 
 * If the current user is a player, <code>HeartsTableWidget</code> is used
 * instead.
 * 
 * @see HeartsTableWidget
 */
public class HeartsObservedTableWidget extends AbsolutePanel {

	/**
	 * The widget used to display the table before the game start. If the game
	 * is already started, this is <code>null</code>.
	 */
	private BeforeGameWidget beforeGameWidget;

	/**
	 * The widget used to display the table after the game start. If the game
	 * isn't started yet, this is <code>null</code>.
	 */
	private CardsGameWidget cardsGameWidget = null;

	/**
	 * This is used to communicate with the servlet using RPC.
	 */
	private CupidoInterfaceAsync cupidoService;

	/**
	 * Specifies whether the UI is frozen (i.e. does no longer react to events)
	 * or not.
	 */
	private boolean frozen = false;

	/**
	 * This contains some information about the game status.
	 * 
	 * It is only updated once, when the game starts (if it is not already
	 * started).
	 */
	private ObservedGameStatus observedGameStatus;

	/**
	 * The global screen manager.
	 */
	private ScreenManager screenManager;

	/**
	 * The manager of the game states.
	 * 
	 * @see ViewerStateManager
	 * @see ViewerStateManagerImpl
	 */
	private ViewerStateManager stateManager;

	/**
	 * The size of the table (both width and height), in pixels.
	 */
	private int tableSize;

	/**
	 * The username of the current user.
	 */
	private String username;

	/**
	 * @param tableSize
	 *            The size of the table (both width and height) in pixels.
	 * @param username
	 *            The username of the current user.
	 * @param screenManager
	 *            The global screen manager.
	 * @param observedGameStatus
	 *            The current game status.
	 * @param cupidoService
	 *            This is used to communicate with the servlet using RPC.
	 */
	public HeartsObservedTableWidget(int tableSize, final String username,
			final ScreenManager screenManager,
			ObservedGameStatus observedGameStatus,
			final CupidoInterfaceAsync cupidoService) {

		this.tableSize = tableSize;
		this.screenManager = screenManager;
		this.cupidoService = cupidoService;
		this.observedGameStatus = observedGameStatus;
		this.username = username;

		setWidth(tableSize + "px");
		setHeight(tableSize + "px");

		int numPlayers = 1;

		assert observedGameStatus.playerStatus[0] != null;

		for (int i = 0; i < 3; i++)
			assert observedGameStatus.playerStatus[i + 1] == null
					|| observedGameStatus.playerStatus[i + 1].name != null;

		for (int i = 0; i < 3; i++)
			if (observedGameStatus.playerStatus[i + 1] != null)
				numPlayers++;

		if (numPlayers == 4) {
			startGame(observedGameStatus);
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

		beforeGameWidget = new BeforeGameWidget(tableSize, username,
				observedGameStatus.playerStatus[0].name, false,
				initialTableStatus, initialTableStatus.playerScores,
				cupidoService, new BeforeGameWidget.Listener() {
					@Override
					public void onExit() {
						if (frozen) {
							System.out
									.println("Client: notice: received a onExit() event while frozen, ignoring it.");
							return;
						}
						beforeGameWidget.freeze();
						cupidoService.leaveTable(new AsyncCallback<Void>() {
							@Override
							public void onFailure(Throwable caught) {
								try {
									throw caught;
								} catch (NoSuchTableException e) {
									// This can happen even if no problems
									// occur.
								} catch (GameInterruptedException e) {
									// This can happen even if no problems
									// occur.
								} catch (GameEndedException e) {
									// This can happen even if no problems
									// occur.
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

					@Override
					public void onGameEnded() {
						if (frozen) {
							System.out
									.println("Client: notice: received a onGameEnded() event while frozen, ignoring it.");
							return;
						}
						screenManager.displayMainMenuScreen(username);
						Window.alert("Il creatore del tavolo \350 uscito dalla partita, quindi la partita \350 stata interrotta.");
					}

					@Override
					public void onTableFull() {
						if (frozen) {
							System.out
									.println("Client: notice: received a onTableFull() event while frozen, ignoring it.");
							return;
						}
						startGame(beforeGameWidget.getTableStatus());
					}
				});
		add(beforeGameWidget, 0, 0);
	}

	/**
	 * When this is called, the widget stops responding to events and disables
	 * all user controls.
	 */
	public void freeze() {
		if (beforeGameWidget != null)
			beforeGameWidget.freeze();
		if (cardsGameWidget != null) {
			cardsGameWidget.freeze();
			stateManager.freeze();
		}
		frozen = true;
	}

	/**
	 * This is called when a <code>CardPlayed</code> notification is received
	 * from the servlet.
	 * 
	 * @param card
	 *            The card that has been played.
	 * @param playerPosition
	 *            The position of the player that played this card.
	 * 
	 * @see CardPlayed
	 */
	public void handleCardPlayed(Card card, int playerPosition) {
		if (frozen) {
			System.out
					.println("Client: notice: received a CardPlayed notification while frozen, ignoring it.");
			return;
		}
		if (beforeGameWidget != null) {
			screenManager
					.displayGeneralErrorScreen(new Exception(
							"A CardPlayed notification was received before the game start."));
		} else {
			stateManager.handleCardPlayed(card, playerPosition);
		}
	}

	/**
	 * This is called when a <code>GameEnded</code> notification is received
	 * from the servlet.
	 * 
	 * @param matchPoints
	 *            The score scored by the players during the current game.
	 * @param playersTotalPoints
	 *            The total score of the players, already updated with the
	 *            results of the current game.
	 * 
	 * @see GameEnded
	 */
	public void handleGameEnded(int[] matchPoints, int[] playersTotalPoints) {
		if (frozen) {
			System.out
					.println("Client: notice: received a GameEnded notification while frozen, ignoring it.");
			return;
		}
		if (beforeGameWidget != null) {
			beforeGameWidget.handleGameEnded(matchPoints, playersTotalPoints);
		} else {
			stateManager.handleGameEnded(matchPoints, playersTotalPoints);
		}
	}

	/**
	 * This is called when a <code>NewPlayerJoined</code> notification is
	 * received from the servlet.
	 * 
	 * @param name
	 *            The name of the player who joined the game.
	 * @param isBot
	 *            Specifies whether the player is a user or a bot.
	 * @param points
	 *            The (global) score of the player.
	 * @param position
	 *            The position of the player in the table.
	 * 
	 * @see NewPlayerJoined
	 */
	public void handleNewPlayerJoined(String name, boolean isBot, int points,
			int position) {
		if (frozen) {
			System.out
					.println("Client: notice: received a NewPlayerJoined notification while frozen, ignoring it.");
			return;
		}
		if (beforeGameWidget != null) {
			// The -1 is needed for the different meaning that `position' has
			// for viewers.
			beforeGameWidget.handleNewPlayerJoined(name, isBot, points,
					position - 1);
		} else {
			screenManager
					.displayGeneralErrorScreen(new Exception(
							"A NewPlayerJoined notification was received while viewing an already-started game."));
		}
	}

	/**
	 * This is called when a <code>PlayerLeft</code> notification is received
	 * from the servlet.
	 * 
	 * @param player
	 *            The player that left the game.
	 * 
	 * @see PlayerLeft
	 */
	public void handlePlayerLeft(String player) {
		if (frozen) {
			System.out
					.println("Client: notice: received a PlayerLeft notification while frozen, ignoring it.");
			return;
		}
		if (beforeGameWidget != null) {
			beforeGameWidget.handlePlayerLeft(player);
		} else {
			screenManager
					.displayGeneralErrorScreen(new Exception(
							"A PlayerLeft notification was received while playing a game that was already started."));
		}
	}

	/**
	 * This is called when a <code>PlayerReplaced</code> notification is
	 * received from the servlet.
	 * 
	 * @param name
	 *            The name of the bot that replaced the player.
	 * @param position
	 *            The position in the table where the player resided.
	 * 
	 * @see PlayerReplaced
	 */
	public void handlePlayerReplaced(String name, int position) {
		if (frozen) {
			System.out
					.println("Client: notice: received a PlayerReplaced notification while frozen, ignoring it.");
			return;
		}
		if (beforeGameWidget != null) {
			screenManager
					.displayGeneralErrorScreen(new Exception(
							"A PlayerReplaced notification was received while viewing a game that wasn't started yet."));
		} else {
			stateManager.handlePlayerReplaced(name, position);
		}
	}

	/**
	 * This is called when the game is started, but only if it wasn't yet
	 * started when this widget was constructed.
	 * 
	 * @param initialTableStatus
	 *            The current table status.
	 */
	private void startGame(InitialTableStatus initialTableStatus) {

		if (frozen) {
			System.out
					.println("Client: notice: startGame() was called while frozen, ignoring it.");
			return;
		}

		// Update observedGameStatus with initialTableStatus.

		observedGameStatus.firstDealerInTrick = -1;
		observedGameStatus.playerStatus[0].numOfCardsInHand = 13;

		for (int i = 0; i < 3; i++) {
			observedGameStatus.playerStatus[i + 1] = new PlayerStatus();
			observedGameStatus.playerStatus[i + 1].isBot = initialTableStatus.whoIsBot[i];
			observedGameStatus.playerStatus[i + 1].name = initialTableStatus.opponents[i];
			observedGameStatus.playerStatus[i + 1].playedCard = null;
			observedGameStatus.playerStatus[i + 1].numOfCardsInHand = 13;
			observedGameStatus.playerStatus[i + 1].score = initialTableStatus.playerScores[i + 1];
		}

		remove(beforeGameWidget);
		beforeGameWidget = null;

		startGame(observedGameStatus);
	}

	/**
	 * This is called when the game is started.
	 * 
	 * @param observedGameStatus
	 *            The current game status.
	 */
	private void startGame(ObservedGameStatus observedGameStatus) {

		assert !frozen;

		stateManager = new ViewerStateManagerImpl(tableSize, screenManager,
				observedGameStatus, username, cupidoService);

		cardsGameWidget = stateManager.getWidget();
		add(cardsGameWidget, 0, 0);
	}
}
