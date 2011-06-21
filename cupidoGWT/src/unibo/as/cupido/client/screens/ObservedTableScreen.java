package unibo.as.cupido.client.screens;

import java.io.Serializable;
import java.util.List;

import net.zschech.gwt.comet.client.CometListener;
import unibo.as.cupido.client.Cupido;
import unibo.as.cupido.client.CupidoCometListener;
import unibo.as.cupido.client.CupidoInterfaceAsync;
import unibo.as.cupido.client.HeartsObservedTableWidget;
import unibo.as.cupido.client.LocalChatWidget;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.ObservedGameStatus;
import unibo.as.cupido.common.structures.PlayerStatus;
import unibo.as.cupido.shared.cometNotification.NewLocalChatMessage;

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

	public ObservedTableScreen(ScreenManager screenManager,
			String username, final CupidoInterfaceAsync cupidoService) {
		setHeight(Cupido.height + "px");
		setWidth(Cupido.width + "px");

		// Set an empty listener (one that handles no messages).
		screenManager.setListener(new CometMessageListener());
		
		// FIXME: This data should come from the servlet.
		// Remove the following code when actually communicating with the servlet.
		ObservedGameStatus observedGameStatus = new ObservedGameStatus();
		observedGameStatus.firstDealerInTrick = 0;

		observedGameStatus.playerStatus = new PlayerStatus[4];

		// Bottom player
		observedGameStatus.playerStatus[0] = new PlayerStatus();
		observedGameStatus.playerStatus[0].isBot = false;
		observedGameStatus.playerStatus[0].name = "bottom player name";
		observedGameStatus.playerStatus[0].numOfCardsInHand = 12;
		observedGameStatus.playerStatus[0].playedCard = new Card(11,
				Card.Suit.SPADES);
		observedGameStatus.playerStatus[0].score = 1234;

		// Left player
		observedGameStatus.playerStatus[1] = new PlayerStatus();
		observedGameStatus.playerStatus[1].isBot = false;
		observedGameStatus.playerStatus[1].name = "left player name";
		observedGameStatus.playerStatus[1].numOfCardsInHand = 12;
		observedGameStatus.playerStatus[1].playedCard = new Card(11,
				Card.Suit.HEARTS);
		observedGameStatus.playerStatus[1].score = 1234;

		// Top player
		observedGameStatus.playerStatus[2] = new PlayerStatus();
		observedGameStatus.playerStatus[2].isBot = true;
		observedGameStatus.playerStatus[2].name = null;
		observedGameStatus.playerStatus[2].numOfCardsInHand = 12;
		observedGameStatus.playerStatus[2].playedCard = new Card(1,
				Card.Suit.HEARTS);
		observedGameStatus.playerStatus[2].score = 1234;

		// Right player
		observedGameStatus.playerStatus[3] = new PlayerStatus();
		observedGameStatus.playerStatus[3].isBot = false;
		observedGameStatus.playerStatus[3].name = "right player name";
		observedGameStatus.playerStatus[3].numOfCardsInHand = 13;
		observedGameStatus.playerStatus[3].playedCard = null;
		observedGameStatus.playerStatus[3].score = 1234;
		
		assert Cupido.height == Cupido.width - chatWidth;
		tableWidget = new HeartsObservedTableWidget(
				Cupido.height, username, screenManager, observedGameStatus, cupidoService);
		add(tableWidget, 0, 0);

		chatWidget = new LocalChatWidget(username,
				new LocalChatWidget.MessageSender() {
					@Override
					public void sendMessage(String message) {
						if (frozen) {
							System.out.println("Client: notice: the sendMessage() event was received while frozen, ignoring it.");
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

		screenManager.setListener(new CometMessageListener() {
			@Override
			public void onNewLocalChatMessage(String user, String message) {
				if (frozen) {
					System.out.println("Client: notice: the onNewLocalChatMessage() event was received while frozen, ignoring it.");
					return;
				}
				chatWidget.displayMessage(user, message);
			}
			
			@Override
			public void onCardPassed(Card[] cards) {
				if (frozen) {
					System.out.println("Client: notice: the onCardPassed() event was received while frozen, ignoring it.");
					return;
				}
				System.out.println("Client: ObservedTableScreen: warning: received a CardPassed notification while observing a table, it was ingored.");
			}
			
			@Override
			public void onCardPlayed(Card card, int playerPosition) {
				if (frozen) {
					System.out.println("Client: notice: the onCardPlayed() event was received while frozen, ignoring it.");
					return;
				}
				tableWidget.handleCardPlayed(card, playerPosition);
			}
			
			@Override
			public void onGameEnded(int[] matchPoints, int[] playersTotalPoints) {
				if (frozen) {
					System.out.println("Client: notice: the onGameEnded() event was received while frozen, ignoring it.");
					return;
				}
				tableWidget.handleGameEnded(matchPoints, playersTotalPoints);
			}
			
			@Override
			public void onGameStarted(Card[] myCards) {
				if (frozen) {
					System.out.println("Client: notice: the onGameStarted() event was received while frozen, ignoring it.");
					return;
				}
				System.out.println("Client: ObservedTableScreen: warning: received a GameStarted notification while observing a table, it was ingored.");
			}
			
			@Override
			public void onNewPlayerJoined(String name, boolean isBot,
					int points, int position) {
				if (frozen) {
					System.out.println("Client: notice: the onNewPlayerJoined() event was received while frozen, ignoring it.");
					return;
				}
				System.out.println("Client: ObservedTableScreen: warning: received a NewPlayerJoined notification while observing a table, it was ingored.");
			}
			
			@Override
			public void onPlayerLeft(String player) {
				if (frozen) {
					System.out.println("Client: notice: the onPlayerLeft() event was received while frozen, ignoring it.");
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
