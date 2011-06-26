/*  Cupido - An online Hearts game.
 *  Copyright (C) 2011 Lorenzo Belli, Marco Poletti, Federico Viscomi
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
import unibo.as.cupido.shared.cometNotification.PlayerReplaced;

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
			NewPlayerJoined.class, PlayerLeft.class, PlayerReplaced.class })
	public static abstract class CupidoCometSerializer extends CometSerializer {
	}

	/**
	 * This is the entry point method.
	 */
	@Override
	public void onModuleLoad() {
		ScreenManagerImpl screenManager = new ScreenManagerImpl();
		RootPanel.get("mainContainer").add(screenManager);

		screenManager.displayLoadingScreen();
	}
}
