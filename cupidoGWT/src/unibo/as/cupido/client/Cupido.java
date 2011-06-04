package unibo.as.cupido.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Cupido implements EntryPoint {
	
	AbsolutePanel mainPanel = null;
	Widget currentScreen = null;

	/// This is null when the user is not logged in.
	String username;
	
	ScreenSwitcherInterface screenSwitcher = null;
	
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		mainPanel = new AbsolutePanel();
		mainPanel.setHeight("700px");
		mainPanel.setWidth("700px");
		RootPanel.get("mainContainer").add(mainPanel);
		
		screenSwitcher = new ScreenSwitcherInterface() {
			
			public void removeCurrentScreen() {
				if (currentScreen == null)
					return;
				if (currentScreen instanceof CupidoMainMenuScreen)
					// Update the `username' field on login and logout.
					username = ((CupidoMainMenuScreen) currentScreen).getUsername();
				mainPanel.remove(currentScreen);
			}

			@Override
			public void displayMainMenuScreen() {
				removeCurrentScreen();
				currentScreen = new CupidoMainMenuScreen(screenSwitcher);
				mainPanel.add(currentScreen, 0, 0);
			}

			@Override
			public void displayScoresScreen() {
				removeCurrentScreen();
				assert username != null;
				mainPanel.remove(currentScreen);
				currentScreen = new CupidoScoresScreen(screenSwitcher);
				mainPanel.add(currentScreen, 0, 0);
			}

			@Override
			public void displayTableScreen() {
				removeCurrentScreen();
				assert username != null;
				currentScreen = new CupidoTableScreen(screenSwitcher, username);
				mainPanel.add(currentScreen, 0, 0);
			}

			@Override
			public void displayObservedTableScreen() {
				removeCurrentScreen();
				assert username != null;
				currentScreen = new CupidoObservedTableScreen(screenSwitcher, username);
				mainPanel.add(currentScreen, 0, 0);
			}

			@Override
			public void displayGeneralErrorScreen(Exception e) {
				removeCurrentScreen();
				currentScreen = new CupidoGeneralErrorScreen(screenSwitcher, e);
				mainPanel.add(currentScreen, 0, 0);				
			}
		};
		
		screenSwitcher.displayMainMenuScreen();
	}
}
