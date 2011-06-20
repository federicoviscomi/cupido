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

	public ObservedTableScreen(ScreenManager screenManager,
			String username, final CupidoInterfaceAsync cupidoService) {
		setHeight(Cupido.height + "px");
		setWidth(Cupido.width + "px");

		// Set an empty listener (one that handles no messages).
		screenManager.setListener(new CometMessageListener());
		
		assert Cupido.height == Cupido.width - chatWidth;
		tableWidget = new HeartsObservedTableWidget(
				Cupido.height, username, screenManager);
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
				chatWidget.displayMessage(user, message);
			}
			
			@Override
			public void onCardPassed(Card[] cards) {
				System.out.println("Client: ObservedTableScreen: warning: received a CardPassed notification while observing a table, it was ingored.");
			}
			
			@Override
			public void onCardPlayed(Card card, int playerPosition) {
				tableWidget.handleCardPlayed(card, playerPosition);
			}
			
			@Override
			public void onGameEnded(int[] matchPoints, int[] playersTotalPoints) {
				tableWidget.handleGameEnded(matchPoints, playersTotalPoints);
			}
			
			@Override
			public void onGameStarted(Card[] myCards) {
				System.out.println("Client: ObservedTableScreen: warning: received a GameStarted notification while observing a table, it was ingored.");
			}
			
			@Override
			public void onNewPlayerJoined(String name, boolean isBot,
					int points, int position) {
				System.out.println("Client: ObservedTableScreen: warning: received a NewPlayerJoined notification while observing a table, it was ingored.");
			}
			
			@Override
			public void onPlayerLeft(String player) {
				tableWidget.handlePlayerLeft(player);
			}
		});
	}

	@Override
	public void prepareRemoval() {
	}

	@Override
	public void disableControls() {
		tableWidget.disableControls();
		chatWidget.disableControls();
	}
}
