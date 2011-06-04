package unibo.as.cupido.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;

public class CupidoMainMenuScreen extends AbsolutePanel {
	
	/// This is null when the user is not logged in.
	private String username;
	private final ScreenSwitcherInterface screenSwitcher;

	public CupidoMainMenuScreen(final ScreenSwitcherInterface screenSwitcher, String username) {
		this.screenSwitcher = screenSwitcher;
		this.username = username;
		setHeight("700px");
		setWidth("700px");
		
		// FIXME: Remove this. It was inserted for debugging purposes.
		this.username = "pippo";

		Label label = new HTML("<b>Main menu screen (TODO)</b>");
		add(label, 300, 320);
		
		PushButton tableButton = new PushButton("Vai alla schermata Tavolo");
		tableButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				screenSwitcher.displayTableScreen();
			}
		});		
		add(tableButton, 300, 400);
		
		PushButton errorButton = new PushButton("Vai alla schermata Errore generico");
		errorButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				screenSwitcher.displayGeneralErrorScreen(new IllegalStateException("n example error message"));
			}
		});		
		add(errorButton, 300, 450);
		
		PushButton observedTableButton = new PushButton("Vai alla schermata Tavolo osservato");
		observedTableButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				screenSwitcher.displayObservedTableScreen();
			}
		});		
		add(observedTableButton, 300, 500);
		
		PushButton scoresButton = new PushButton("Vai alla schermata Punteggi");
		scoresButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				screenSwitcher.displayScoresScreen();
			}
		});		
		add(scoresButton, 300, 550);
	}

	/**
	 * @return the current username, or null if the user was logged out.
	 */
	public String getUsername() {
		return username;
	}
}
