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
import unibo.as.cupido.client.widgets.HeartsObservedTableWidget;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.ObservedGameStatus;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;

public class ObservedTableScreen extends AbsolutePanel implements Screen {

	/**
	 * The width of the chat sidebar.
	 */
	public static final int chatWidth = Cupido.width - Cupido.height;
	private HeartsObservedTableWidget tableWidget;
	private ChatWidget chatWidget;

	private boolean frozen = false;

	public ObservedTableScreen(final ScreenManager screenManager, final String username,
			ObservedGameStatus observedGameStatus,
			final CupidoInterfaceAsync cupidoService) {
		setHeight(Cupido.height + "px");
		setWidth(Cupido.width + "px");

		// Set an empty listener (one that handles no messages).
		screenManager.setListener(new CometMessageListener());

		chatWidget = new ChatWidget(chatWidth, Cupido.height,
				new ChatListener() {
					@Override
					public void sendMessage(String message) {
						if (frozen) {
							System.out
									.println("Client: notice: the sendMessage() event was received while frozen, ignoring it.");
							return;
						}
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

		tableWidget = new HeartsObservedTableWidget(Cupido.height, username,
				screenManager, chatWidget, observedGameStatus, cupidoService);
		add(tableWidget, 0, 0);

		screenManager.setListener(new CometMessageListener() {
			@Override
			public void onNewLocalChatMessage(String user, String message) {
				if (frozen) {
					System.out
							.println("Client: notice: the onNewLocalChatMessage() event was received while frozen, ignoring it.");
					return;
				}
				chatWidget.displayMessage(user, message);
			}

			@Override
			public void onCardPassed(Card[] cards) {
				if (frozen) {
					System.out
							.println("Client: notice: the onCardPassed() event was received while frozen, ignoring it.");
					return;
				}
				screenManager.displayGeneralErrorScreen(new Exception("A CardPassed notification was received while observing a table."));
			}

			@Override
			public void onCardPlayed(Card card, int playerPosition) {
				if (frozen) {
					System.out
							.println("Client: notice: the onCardPlayed() event was received while frozen, ignoring it.");
					return;
				}
				tableWidget.handleCardPlayed(card, playerPosition);
			}

			@Override
			public void onGameEnded(int[] matchPoints, int[] playersTotalPoints) {
				if (frozen) {
					System.out
							.println("Client: notice: the onGameEnded() event was received while frozen, ignoring it.");
					return;
				}
				tableWidget.handleGameEnded(matchPoints, playersTotalPoints);
			}

			@Override
			public void onNewPlayerJoined(String name, boolean isBot,
					int points, int position) {
				if (frozen) {
					System.out
							.println("Client: notice: the onNewPlayerJoined() event was received while frozen, ignoring it.");
					return;
				}
				tableWidget
						.handleNewPlayerJoined(name, isBot, points, position);
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
			
			@Override
			public void onGameStarted(Card[] myCards) {
				if (frozen) {
					System.out
							.println("Client: notice: the onGameStarted() event was received while frozen, ignoring it.");
					return;
				}
				screenManager.displayGeneralErrorScreen(new Exception("A GameStarted notification was received while observing a table."));
			}

			@Override
			public void onPlayerLeft(String player) {
				if (frozen) {
					System.out
							.println("Client: notice: the onPlayerLeft() event was received while frozen, ignoring it.");
					return;
				}
				tableWidget.handlePlayerLeft(player);
			}
		});
	}

	@Override
	public void prepareRemoval() {
	}

	@Override
	public void freeze() {
		tableWidget.freeze();
		chatWidget.freeze();
		frozen = true;
	}
}
