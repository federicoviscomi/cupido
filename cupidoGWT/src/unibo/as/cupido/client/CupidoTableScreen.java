package unibo.as.cupido.client;

import java.io.Serializable;
import java.util.List;

import unibo.as.cupido.shared.cometNotification.NewLocalChatMessage;

import net.zschech.gwt.comet.client.CometListener;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;

public class CupidoTableScreen extends AbsolutePanel {

	/**
	 *  The width of the chat sidebar.
	 */
	public static final int chatWidth = 200;

	public CupidoTableScreen(ScreenSwitcher screenSwitcher, String username,
			                 final CupidoInterfaceAsync cupidoService, CupidoCometListener listener) {
		setHeight(Cupido.height + "px");
		setWidth(Cupido.width + "px");

		assert Cupido.height == Cupido.width - chatWidth;
		HeartsTableWidget tableWidget = new HeartsTableWidget(Cupido.height, username);
		add(tableWidget, 0, 0);
		
		final LocalChatWidget chatWidget = new LocalChatWidget(username, new LocalChatWidget.MessageSender() {
			@Override
			public void sendMessage(String message) {
				cupidoService.sendLocalChatMessage(message, new AsyncCallback<Void>() {
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
		
		listener.setListener(new CometListener() {

			@Override
			public void onConnected(int heartbeat) {
			}

			@Override
			public void onDisconnected() {
			}

			@Override
			public void onError(Throwable exception, boolean connected) {
			}

			@Override
			public void onHeartbeat() {
			}

			@Override
			public void onRefresh() {
			}

			@Override
			public void onMessage(List<? extends Serializable> messages) {
				for (Serializable message : messages) {
					if (message instanceof NewLocalChatMessage) {
						NewLocalChatMessage x = (NewLocalChatMessage) message;
						System.out.println("Client: received a NewLocalChatMessage object, displaying it.");
						chatWidget.displayMessage(x.user, x.message);
					} else {
						System.out.println("Client: Received unrecognized Comet message.");
					}
				}
			}
			
		});
	}
}
