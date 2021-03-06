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

package unibo.as.cupido.client.screens;

import unibo.as.cupido.client.CometMessageListener;
import unibo.as.cupido.client.Cupido;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * This class manages the Loading screen, that is displayed during startup. Note
 * that the user has no means to change the screen during the loading. The
 * screen manager itself will display the login screen when the application is
 * loaded.
 */
public class LoadingScreen extends VerticalPanel implements Screen {

	/**
	 * @param screenManager
	 *            The global screen manager.
	 */
	public LoadingScreen(ScreenManager screenManager) {
		setHeight(Cupido.height + "px");
		setWidth(Cupido.width + "px");

		// Set an empty listener (one that handles no messages).
		screenManager.setListener(new CometMessageListener());

		setHorizontalAlignment(ALIGN_CENTER);
		setVerticalAlignment(ALIGN_MIDDLE);

		VerticalPanel panel = new VerticalPanel();
		panel.setWidth("300px");
		panel.setHeight("100px");
		panel.setSpacing(20);
		panel.setHorizontalAlignment(ALIGN_CENTER);
		panel.setVerticalAlignment(ALIGN_MIDDLE);
		add(panel);

		panel.add(new HTML("Caricamento in corso, attendere..."));

		panel.add(new Image("loading.gif"));
	}

	@Override
	public void freeze() {
	}

	@Override
	public void prepareRemoval() {
	}
}
