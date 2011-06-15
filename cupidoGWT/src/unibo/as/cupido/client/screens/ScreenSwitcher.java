package unibo.as.cupido.client.screens;

public interface ScreenSwitcher {

	/**
	 * Shows the main menu screen instead of the current one.
	 */
	public void displayMainMenuScreen();

	/**
	 * Shows the scores' screen instead of the current one.
	 */
	public void displayScoresScreen();

	/**
	 * Shows the about screen instead of the current one.
	 */
	public void displayAboutScreen();
	
	/**
	 * Shows the table screen (as a player) instead of the current one.
	 */
	public void displayTableScreen();

	/**
	 * Shows the table screen (as a viewer) instead of the current one.
	 */
	public void displayObservedTableScreen();

	/**
	 * Shows the general error screen instead of the current one.
	 * 
	 * @param e
	 *            the exception that generated the error.
	 */
	public void displayGeneralErrorScreen(Exception e);

	/**
	 * Shows the loading screen instead of the current one.
	 */
	public void displayLoadingScreen();
}
