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
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.InitialTableStatus;
import unibo.as.cupido.shared.cometNotification.CardPassed;
import unibo.as.cupido.shared.cometNotification.CardPlayed;
import unibo.as.cupido.shared.cometNotification.GameEnded;
import unibo.as.cupido.shared.cometNotification.GameStarted;
import unibo.as.cupido.shared.cometNotification.NewPlayerJoined;
import unibo.as.cupido.shared.cometNotification.PlayerLeft;
import unibo.as.cupido.shared.cometNotification.PlayerReplaced;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;

/**
 * This class displays the table for a Hearts game, for
 * users that are viewing a game. The game may or may not
 * be started.
 * 
 * If the current user is a player, HeartsTableWidget
 * is used instead.
 * 
 * @see HeartsObservedTableWidget
 */
public class HeartsTableWidget extends AbsolutePanel {

	/**
	 * The widget used to display the table before the game start.
	 * If the game is already started, this is null.
	 */
	private BeforeGameWidget beforeGameWidget;

	/**
	 * The widget used to display the table after the game start.
	 * If the game isn't started yet, this is null.
	 */
	private CardsGameWidget cardsGameWidget = null;

	/**
	 * This is used to communicate with the servlet using RPC.
	 */
	private CupidoInterfaceAsync cupidoService;

	/**
	 * Specifies whether the UI is frozen (i.e. does no longer react to events) or not.
	 */
	private boolean frozen = false;

	/**
	 * An array containing the players' global scores.
	 * 
	 * The first element refers to the current user, and other positions
	 * follow in clockwise order. If there is no player at some position
	 * (or if there is a bot), the associated value is unspecified.
	 */
	private int[] scores;
	
	/**
	 * The global screen manager.
	 */
	private ScreenManager screenManager;

	/**
	 * The manager of the game states.
	 * 
	 * @see PlayerStateManager
	 * @see PlayerStateManagerImpl
	 */
	private PlayerStateManager stateManager = null;

	/**
	 * The size of the table (both width and height), in pixels.
	 */
	private int tableSize;
	
	/**
	 * The username of the current user.
	 */
	private String username;

	/**
	 * 
	 * @param tableSize
	 *            The size of the table (width and height) in pixels.
	 * @param username The username of the current user.
	 * @param initialTableStatus The current table status.
	 * @param isOwner Specifies whether the current user is the table owner or not.
	 * @param userScore The global score of the current user.
	 * @param screenManager The global screen manager.
	 * @param cupidoService This is used to communicate with the servlet using RPC.
	 */
	public HeartsTableWidget(int tableSize, final String username,
			InitialTableStatus initialTableStatus, final boolean isOwner,
			int userScore, final ScreenManager screenManager,
			final CupidoInterfaceAsync cupidoService) {

		this.username = username;
		this.tableSize = tableSize;
		this.screenManager = screenManager;
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
									.println("Client: notice: the onGameEnded() event was received while frozen, ignoring it.");
							return;
						}

						assert !isOwner;
						screenManager.displayMainMenuScreen(username);
						Window.alert("L'creator ha lasciato il tavolo, e quindi la partita \350 stata annullata.");
					}

					@Override
					public void onTableFull() {
						// Just wait for the GameStarted notification.
						beforeGameWidget.freeze();
					}
				});
		add(beforeGameWidget, 0, 0);
	}

	/**
	 * When this is called, the widget stops responding to events
	 * and disables all user controls.
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
	 * This is called when a PassedCards notification is received
	 * from the servlet.
	 * 
	 * @param cards The cards that were passed to the user.
	 * 
	 * @see CardPassed
	 */
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

	/**
	 * This is called when a CardPlayed notification is received
	 * from the servlet.
	 * 
	 * @param card The card that has been played.
	 * @param playerPosition The position of the player that played this card.
	 * 
	 * @see CardPlayed
	 */
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

	/**
	 * This is called when a GameEnded notification is received
	 * from the servlet.
	 * 
	 * @param matchPoints The score scored by the players during the current game.
	 * @param playersTotalPoints The total score of the players, already updated
	 *                           with the results of the current game.
	 * 
	 * @see GameEnded
	 */
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

	/**
	 * This is called when a GameStarted notification is received
	 * from the servlet.
	 * 
	 * @param myCards The cards that the player received from the dealer.
	 * 
	 * @see GameStarted
	 */
	public void handleGameStarted(Card[] myCards) {
		if (frozen) {
			System.out
					.println("Client: notice: handleGameStarted() was called while frozen, ignoring it.");
			return;
		}

		InitialTableStatus initialTableStatus = beforeGameWidget
				.getTableStatus();

		remove(beforeGameWidget);
		beforeGameWidget = null;

		stateManager = new PlayerStateManagerImpl(tableSize, screenManager,
				initialTableStatus, scores, myCards, username, cupidoService);

		cardsGameWidget = stateManager.getWidget();
		add(cardsGameWidget, 0, 0);
	}

	/**
	 * This is called when a NewPlayerJoined notification is received
	 * from the servlet.
	 * 
	 * @param name The name of the player who joined the game.
	 * @param isBot Specifies whether the player is a user or a bot.
	 * @param points The (global) score of the player.
	 * @param position The position of the player in the table.
	 * 
	 * @see NewPlayerJoined
	 */
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

	/**
	 * This is called when a Playerleft notification is received
	 * from the servlet.
	 * 
	 * @param player The player that left the game.
	 * 
	 * @see PlayerLeft
	 */
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

	/**
	 * This is called when a PlayerReplaced notification is received
	 * from the servlet.
	 * 
	 * @param name The name of the bot that replaced the player.
	 * @param position The position in the table where the player resided.
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
							"A PlayerReplaced notification was received while playing a game that wasn't started yet."));
		} else {
			// The +1 is needed for the different meaning that `position' has
			// for players.
			stateManager.handlePlayerReplaced(name, position + 1);
		}
	}
}
