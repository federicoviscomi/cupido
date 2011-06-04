package unibo.as.cupido.client;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

public class CupidoMainMenuScreen extends AbsolutePanel {
	
	/// This is null when the user is not logged in.
	private String username;
	private final ScreenSwitcherInterface screenSwitcher;

	public CupidoMainMenuScreen(final ScreenSwitcherInterface screenSwitcher, String username) {
		this.screenSwitcher = screenSwitcher;
		this.username = username;
		setHeight("700px");
		setWidth("700px");
		Label label = new HTML("<b>Main menu screen (TODO)</b>");
		add(label, 300, 320);
	}

	/**
	 * @return the current username, or null if the user was logged out.
	 */
	public String getUsername() {
		// TODO Auto-generated method stub
		return null;
	}
}
