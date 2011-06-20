package unibo.as.cupido.client;

import net.zschech.gwt.comet.client.CometSerializer;
import net.zschech.gwt.comet.client.SerialTypes;
import unibo.as.cupido.client.screens.ScreenManagerImpl;
import unibo.as.cupido.shared.cometNotification.CardPassed;
import unibo.as.cupido.shared.cometNotification.CardPlayed;
import unibo.as.cupido.shared.cometNotification.GameEnded;
import unibo.as.cupido.shared.cometNotification.GameStarted;
import unibo.as.cupido.shared.cometNotification.NewLocalChatMessage;
import unibo.as.cupido.shared.cometNotification.NewPlayerJoined;
import unibo.as.cupido.shared.cometNotification.PlayerLeft;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Cupido implements EntryPoint {

	public static final int width = 900;
	public static final int height = 700;

	@SerialTypes({ CardPassed.class, CardPlayed.class, GameEnded.class,
			GameStarted.class, NewLocalChatMessage.class,
			NewPlayerJoined.class, PlayerLeft.class })
	public static abstract class CupidoCometSerializer extends CometSerializer {
	}

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		ScreenManagerImpl screenManager = new ScreenManagerImpl();
		RootPanel.get("mainContainer").add(screenManager);

		screenManager.displayLoadingScreen();
	}
}
