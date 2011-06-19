package unibo.as.cupido.client.screens;

import java.util.ArrayList;
import java.util.List;

import unibo.as.cupido.backendInterfaces.common.ChatMessage;
import unibo.as.cupido.backendInterfaces.exception.FatalException;
import unibo.as.cupido.backendInterfaces.exception.UserNotAuthenticatedException;
import unibo.as.cupido.client.Cupido;
import unibo.as.cupido.client.CupidoInterfaceAsync;
import unibo.as.cupido.client.GlobalChatWidget;
import unibo.as.cupido.client.GlobalChatWidget.ChatListener;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;

public class MainMenuScreen extends AbsolutePanel implements Screen {
	
	// The interval between subsequent polls to the (global) chat, in
	// milliseconds.
	final static int chatRefreshInterval = 2000;
	
	List<PushButton> buttons = new ArrayList<PushButton>();

	// This is null when the user is not logged in.
	private String username;
	private final ScreenSwitcher screenSwitcher;
	private Timer chatTimer;
	
	private boolean stoppedRefreshing = false;
	private boolean waitingServletResponse = false;
	
	/**
	 * This is true if the user sent a message and no refresh request
	 * has yet been sent to the servlet after that.
	 */
	private boolean needRefresh = false;

	private GlobalChatWidget chatWidget;

	/**
	 * The width of the chat sidebar.
	 */
	public static final int chatWidth = 300;

	public MainMenuScreen(final ScreenSwitcher screenSwitcher,
			final String username, final CupidoInterfaceAsync cupidoService) {
		this.screenSwitcher = screenSwitcher;
		this.username = username;
		setHeight(Cupido.height + "px");
		setWidth(Cupido.width + "px");

		// FIXME: Remove this. It was inserted for debugging purposes.
		this.username = "pippo";

		Label label = new HTML("<b>Main menu screen (TODO)</b>");
		add(label, 200, 320);

		PushButton tableButton = new PushButton("Vai alla schermata Tavolo");
		buttons.add(tableButton);
		tableButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				screenSwitcher.displayTableScreen(username);
			}
		});
		add(tableButton, 200, 400);

		PushButton errorButton = new PushButton(
				"Vai alla schermata Errore generico");
		buttons.add(errorButton);
		errorButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				screenSwitcher
						.displayGeneralErrorScreen(new IllegalStateException(
								"An example error message"));
			}
		});
		add(errorButton, 200, 450);

		PushButton observedTableButton = new PushButton(
				"Vai alla schermata Tavolo osservato");
		buttons.add(observedTableButton);
		observedTableButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				screenSwitcher.displayObservedTableScreen(username);
			}
		});
		add(observedTableButton, 200, 500);

		PushButton scoresButton = new PushButton("Vai alla schermata Punteggi");
		buttons.add(scoresButton);
		scoresButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				screenSwitcher.displayScoresScreen(username);
			}
		});
		add(scoresButton, 200, 550);

		PushButton aboutButton = new PushButton("Informazioni su Cupido");
		buttons.add(aboutButton);
		aboutButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				screenSwitcher.displayAboutScreen(username);
			}
		});
		add(aboutButton, 200, 600);

		PushButton logoutButton = new PushButton("Informazioni su Cupido");
		buttons.add(logoutButton);
		logoutButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				screenSwitcher.displayAboutScreen(username);
			}
		});
		add(logoutButton, 200, 650);

		chatWidget = new GlobalChatWidget(this.username, new ChatListener() {
			@Override
			public void sendMessage(String message) {
				cupidoService.sendGlobalChatMessage(message, new AsyncCallback<Void>() {
					@Override
					public void onFailure(Throwable caught) {
						try {
							throw caught;
						} catch (IllegalArgumentException e) {
							// FIXME: Can this happen?
							screenSwitcher.displayGeneralErrorScreen(e);
						} catch (UserNotAuthenticatedException e) {
							screenSwitcher.displayGeneralErrorScreen(e);
						} catch (FatalException e) {
							screenSwitcher.displayGeneralErrorScreen(e);
						} catch (Throwable e) {
							assert false;
						}
					}

					@Override
					public void onSuccess(Void result) {
						needRefresh = true;
						chatTimer.cancel();
						chatTimer.run();
					}
				});
			}
		});
		chatWidget.setHeight(Cupido.height + "px");
		chatWidget.setWidth(chatWidth + "px");
		add(chatWidget, Cupido.width - chatWidth, 0);

		DOM.setStyleAttribute(chatWidget.getElement(), "borderLeftStyle",
				"solid");
		DOM.setStyleAttribute(chatWidget.getElement(), "borderLeftWidth", "1px");
		
		chatTimer = new Timer() {
			@Override
			public void run() {
				if (waitingServletResponse)
					return;
				needRefresh = false;
				cupidoService.viewLastMessages(new AsyncCallback<ChatMessage[]>() {
					@Override
					public void onFailure(Throwable caught) {
						waitingServletResponse = false;
						try {
							throw caught;
						} catch (UserNotAuthenticatedException e) {
							screenSwitcher.displayGeneralErrorScreen(e);
						} catch (FatalException e) {
							screenSwitcher.displayGeneralErrorScreen(e);
						} catch (Throwable e) {
							// Should never get here.
							assert false;
						}
					}

					@Override
					public void onSuccess(ChatMessage[] messages) {
						waitingServletResponse = false;
						
						chatWidget.setLastMessages(messages);
						
						if (!stoppedRefreshing) {
							if (needRefresh)
								// Refresh immediately.
								chatTimer.run();
							else
								chatTimer.schedule(chatRefreshInterval);
						}
					}
				});
				waitingServletResponse = true;
			}
		};
		chatTimer.run();
	}
	
	public void disableControls() {
		for (PushButton w : buttons)
			w.setEnabled(false);
		chatWidget.disableControls();
	}
	
	@Override
	public void prepareRemoval() {
		stoppedRefreshing = true;
		chatTimer.cancel();
	}
}
