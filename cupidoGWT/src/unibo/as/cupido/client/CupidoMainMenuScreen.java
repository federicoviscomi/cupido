package unibo.as.cupido.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;

public class CupidoMainMenuScreen extends AbsolutePanel {
	
	/// This is null when the user is not logged in.
	private String username;
	private final ScreenSwitcherInterface screenSwitcher;

	/**
	 *  The width of the chat sidebar.
	 */
	public static final int chatWidth = 300;

	public CupidoMainMenuScreen(final ScreenSwitcherInterface screenSwitcher, String username) {
		this.screenSwitcher = screenSwitcher;
		this.username = username;
		setHeight(Cupido.height + "px");
		setWidth(Cupido.width + "px");
		
		// FIXME: Remove this. It was inserted for debugging purposes.
		this.username = "pippo";

		Label label = new HTML("<b>Main menu screen (TODO)</b>");
		add(label, 200, 320);
		
		PushButton tableButton = new PushButton("Vai alla schermata Tavolo");
		tableButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				screenSwitcher.displayTableScreen();
			}
		});		
		add(tableButton, 200, 400);
		
		PushButton errorButton = new PushButton("Vai alla schermata Errore generico");
		errorButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				screenSwitcher.displayGeneralErrorScreen(new IllegalStateException("n example error message"));
			}
		});		
		add(errorButton, 200, 450);
		
		PushButton observedTableButton = new PushButton("Vai alla schermata Tavolo osservato");
		observedTableButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				screenSwitcher.displayObservedTableScreen();
			}
		});		
		add(observedTableButton, 200, 500);
		
		PushButton scoresButton = new PushButton("Vai alla schermata Punteggi");
		scoresButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				screenSwitcher.displayScoresScreen();
			}
		});		
		add(scoresButton, 200, 550);
		
		GlobalChatWidget chatWidget = new GlobalChatWidget(this.username);
		chatWidget.setHeight(Cupido.height + "px");
		chatWidget.setWidth(chatWidth + "px");
		add(chatWidget, Cupido.width - chatWidth, 0);
		
		DOM.setStyleAttribute(chatWidget.getElement(), "borderLeftStyle", "solid");
		DOM.setStyleAttribute(chatWidget.getElement(), "borderLeftWidth", "1px");
	}

	/**
	 * @return the current username, or null if the user was logged out.
	 */
	public String getUsername() {
		return username;
	}
}
