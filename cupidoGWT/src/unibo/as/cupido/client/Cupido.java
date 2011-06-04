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
	// This is used to check that no screen switches occur while switching screen.
	boolean switchingScreen = false;

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
				assert !switchingScreen;
				switchingScreen = true;
				
				removeCurrentScreen();
				currentScreen = new CupidoMainMenuScreen(screenSwitcher, username);
				mainPanel.add(currentScreen, 0, 0);
				
				switchingScreen = false;
			}

			@Override
			public void displayScoresScreen() {
				assert !switchingScreen;
				switchingScreen = true;
				
				removeCurrentScreen();
				assert username != null;
				mainPanel.remove(currentScreen);
				currentScreen = new CupidoScoresScreen(screenSwitcher);
				mainPanel.add(currentScreen, 0, 0);
				
				switchingScreen = false;
			}

			@Override
			public void displayTableScreen() {
				assert !switchingScreen;
				switchingScreen = true;
				
				removeCurrentScreen();
				assert username != null;
				currentScreen = new CupidoTableScreen(screenSwitcher, username);
				mainPanel.add(currentScreen, 0, 0);
				
				switchingScreen = false;
			}

			@Override
			public void displayObservedTableScreen() {
				assert !switchingScreen;
				switchingScreen = true;
				
				removeCurrentScreen();
				assert username != null;
				currentScreen = new CupidoObservedTableScreen(screenSwitcher, username);
				mainPanel.add(currentScreen, 0, 0);
				
				switchingScreen = false;
			}

			@Override
			public void displayGeneralErrorScreen(Exception e) {
				assert !switchingScreen;
				switchingScreen = true;

				removeCurrentScreen();
				currentScreen = new CupidoGeneralErrorScreen(screenSwitcher, e);
				mainPanel.add(currentScreen, 0, 0);
				
				switchingScreen = false;
			}
		};
		
		screenSwitcher.displayMainMenuScreen();
	}
}
