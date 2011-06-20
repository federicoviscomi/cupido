package unibo.as.cupido.client.screens;

import unibo.as.cupido.common.structures.InitialTableStatus;


public interface ScreenSwitcher {
	
	/**
	 * Shows the login screen instead of the current one.
	 */
	public void displayLoginScreen();
	
	/**
	 * Shows the login screen instead of the current one.
	 */
	public void displayRegistrationScreen();

	/**
	 * Shows the main menu screen instead of the current one.
	 */
	public void displayMainMenuScreen(String username);

	/**
	 * Shows the scores' screen instead of the current one.
	 */
	public void displayScoresScreen(String username);

	/**
	 * Shows the about screen instead of the current one.
	 */
	public void displayAboutScreen(String username);

	/**
	 * Shows the table screen (as a player) instead of the current one.
	 * @param inititalTableStatus 
	 */
	public void displayTableScreen(String username, boolean isOwner, InitialTableStatus inititalTableStatus);

	/**
	 * Shows the table screen (as a viewer) instead of the current one.
	 */
	public void displayObservedTableScreen(String username);

	/**
	 * Shows the general error screen instead of the current one.
	 * 
	 * @param caught
	 *            the exception that generated the error.
	 */
	public void displayGeneralErrorScreen(Throwable caught);

	/**
	 * Shows the loading screen instead of the current one.
	 */
	public void displayLoadingScreen();
	
	public void disableControls();
	
	public void setListener(CometMessageListener listener);
}
