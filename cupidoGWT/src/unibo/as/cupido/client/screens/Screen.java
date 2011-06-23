package unibo.as.cupido.client.screens;

public interface Screen {
	/**
	 * This is called by the ScreenSwitcher implementation just before switching
	 * to another screen.
	 */
	public void prepareRemoval();

	public void freeze();
}
