package unibo.as.cupido.client;

public interface ScreenSwitcher {

	/**
	 * Shows the main menu screen instead of the current one.
	 */
	public void displayMainMenuScreen();

	/**
	 * Shows the main menu screen instead of the current one.
	 */
	public void displayScoresScreen();

	/**
	 * Shows the main menu screen instead of the current one.
	 */
	public void displayTableScreen();

	/**
	 * Shows the main menu screen instead of the current one.
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
