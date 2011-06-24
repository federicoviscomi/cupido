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

import unibo.as.cupido.client.Cupido;
import unibo.as.cupido.client.CupidoInterfaceAsync;
import unibo.as.cupido.client.HeartsObservedTableWidget;
import unibo.as.cupido.client.LocalChatWidget;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.ObservedGameStatus;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;

public class ObservedTableScreen extends AbsolutePanel implements Screen {

	/**
	 * The width of the chat sidebar.
	 */
	public static final int chatWidth = 200;
	private HeartsObservedTableWidget tableWidget;
	private LocalChatWidget chatWidget;

	private boolean frozen = false;

	public ObservedTableScreen(ScreenManager screenManager, String username,
			ObservedGameStatus observedGameStatus,
			final CupidoInterfaceAsync cupidoService) {
		setHeight(Cupido.height + "px");
		setWidth(Cupido.width + "px");

		// Set an empty listener (one that handles no messages).
		screenManager.setListener(new CometMessageListener());

		assert Cupido.height == Cupido.width - chatWidth;

		chatWidget = new LocalChatWidget(username,
				new LocalChatWidget.MessageSender() {
					@Override
					public void sendMessage(String message) {
						if (frozen) {
							System.out
									.println("Client: notice: the sendMessage() event was received while frozen, ignoring it.");
							return;
						}
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
		chatWidget.setHeight(Cupido.height + "px");
		chatWidget.setWidth(chatWidth + "px");
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
				System.out
						.println("Client: ObservedTableScreen: warning: received a CardPassed notification while observing a table, it was ingored.");
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
			public void onGameStarted(Card[] myCards) {
				if (frozen) {
					System.out
							.println("Client: notice: the onGameStarted() event was received while frozen, ignoring it.");
					return;
				}
				System.out
						.println("Client: ObservedTableScreen: warning: received a GameStarted notification while observing a table, it was ingored.");
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
