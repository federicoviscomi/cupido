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

package unibo.as.cupido.client.screens;

import unibo.as.cupido.client.CometMessageListener;
import unibo.as.cupido.client.Cupido;
import unibo.as.cupido.client.CupidoInterfaceAsync;
import unibo.as.cupido.client.widgets.ChatWidget;
import unibo.as.cupido.client.widgets.ChatWidget.ChatListener;
import unibo.as.cupido.client.widgets.HeartsTableWidget;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.InitialTableStatus;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;

/**
 * This class manages the table screen for players (both creators and
 * players who join an existing table).
 * 
 * @see ObservedTableScreen
 */
public class TableScreen extends AbsolutePanel implements Screen {

	/**
	 * The width of the chat sidebar.
	 */
	public static final int chatWidth = Cupido.width - Cupido.height;
	
	/**
	 * The widget that displays the local chat in the right side of the screen.
	 */
	private ChatWidget chatWidget;
	
	/**
	 * Specifies whether the UI is frozen (i.e. does no longer react to events) or not.
	 */
	private boolean frozen = false;

	/**
	 * The widget that displays the table.
	 */
	private HeartsTableWidget tableWidget;

	/**
	 * @param screenManager The global screen manager.
	 * @param username The username of the current user.
	 * @param isOwner Specifies whether or not the current user is the creator of this table.
	 * @param initialTableStatus Contains information about the current state of the table.
	 * @param userScore The global score of the current user.
	 * @param cupidoService This is used to communicate with the servlet using RPC.
	 */
	public TableScreen(ScreenManager screenManager, final String username,
			boolean isOwner, InitialTableStatus initialTableStatus,
			int userScore, final CupidoInterfaceAsync cupidoService) {
		setHeight(Cupido.height + "px");
		setWidth(Cupido.width + "px");

		chatWidget = new ChatWidget(chatWidth, Cupido.height,
				new ChatListener() {
					@Override
					public void sendMessage(String message) {
						chatWidget.displayMessage(username, message);
						cupidoService.sendLocalChatMessage(message,
								new AsyncCallback<Void>() {
									@Override
									public void onFailure(Throwable caught) {
									}

									@Override
									public void onSuccess(Void result) {
									}
								});
					}
				});
		add(chatWidget, Cupido.width - chatWidth, 0);

		tableWidget = new HeartsTableWidget(Cupido.height, username,
				initialTableStatus, isOwner, userScore, screenManager,
				cupidoService);
		add(tableWidget, 0, 0);

		screenManager.setListener(new CometMessageListener() {
			@Override
			public void onCardPassed(Card[] cards) {
				if (frozen) {
					System.out
							.println("Client: notice: the CardPassed notification was received while frozen, ignoring it.");
					return;
				}
				tableWidget.handleCardPassed(cards);
			}

			@Override
			public void onCardPlayed(Card card, int playerPosition) {
				if (frozen) {
					System.out
							.println("Client: notice: the CardPlayed notification was received while frozen, ignoring it.");
					return;
				}
				tableWidget.handleCardPlayed(card, playerPosition);
			}

			@Override
			public void onGameEnded(int[] matchPoints, int[] playersTotalPoints) {
				if (frozen) {
					System.out
							.println("Client: notice: the GameEnded notification was received while frozen, ignoring it.");
					return;
				}
				tableWidget.handleGameEnded(matchPoints, playersTotalPoints);
			}

			@Override
			public void onGameStarted(Card[] myCards) {
				if (frozen) {
					System.out
							.println("Client: notice: the GameStarted notification was received while frozen, ignoring it.");
					return;
				}
				tableWidget.handleGameStarted(myCards);
			}

			@Override
			public void onNewLocalChatMessage(String user, String message) {
				if (frozen) {
					System.out
							.println("Client: notice: the NewLocalChatMessage notification was received while frozen, ignoring it.");
					return;
				}
				chatWidget.displayMessage(user, message);
			}

			@Override
			public void onNewPlayerJoined(String name, boolean isBot,
					int points, int position) {
				if (frozen) {
					System.out
							.println("Client: notice: the NewPlayerJoined notification was received while frozen, ignoring it.");
					return;
				}
				tableWidget
						.handleNewPlayerJoined(name, isBot, points, position);
			}

			@Override
			public void onPlayerLeft(String player) {
				if (frozen) {
					System.out
							.println("Client: notice: the PlayerLeft notification was received while frozen, ignoring it.");
					return;
				}
				tableWidget.handlePlayerLeft(player);
			}

			@Override
			public void onPlayerReplaced(String name, int position) {
				if (frozen) {
					System.out
							.println("Client: notice: the PlayerReplaced notification was received while frozen, ignoring it.");
					return;
				}
				tableWidget.handlePlayerReplaced(name, position);
			}
		});
	}

	@Override
	public void freeze() {
		tableWidget.freeze();
		chatWidget.freeze();
		frozen = true;
	}

	@Override
	public void prepareRemoval() {
	}
}
