package unibo.as.cupido.client.screens;

import unibo.as.cupido.client.Cupido;
import unibo.as.cupido.client.CupidoInterfaceAsync;
import unibo.as.cupido.client.HeartsTableWidget;
import unibo.as.cupido.client.LocalChatWidget;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.InitialTableStatus;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;

public class TableScreen extends AbsolutePanel implements Screen {

	/**
	 * The width of the chat sidebar.
	 */
	public static final int chatWidth = 200;
	private HeartsTableWidget tableWidget;
	private LocalChatWidget chatWidget;

	private boolean frozen = false;

	public TableScreen(ScreenManager screenManager, String username,
			boolean isOwner, InitialTableStatus initialTableStatus,
			int userScore, final CupidoInterfaceAsync cupidoService) {
		setHeight(Cupido.height + "px");
		setWidth(Cupido.width + "px");

		assert Cupido.height == Cupido.width - chatWidth;
		tableWidget = new HeartsTableWidget(Cupido.height, username,
				initialTableStatus, isOwner, userScore, screenManager, cupidoService);
		add(tableWidget, 0, 0);

		chatWidget = new LocalChatWidget(username,
				new LocalChatWidget.MessageSender() {
					@Override
					public void sendMessage(String message) {
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

		screenManager.setListener(new CometMessageListener() {
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
		});
	}

	@Override
	public void prepareRemoval() {
	}

	public void freeze() {
		tableWidget.freeze();
		chatWidget.freeze();
		frozen = true;
	}
}
