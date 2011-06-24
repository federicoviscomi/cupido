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

import unibo.as.cupido.client.Cupido;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

public class ScoresScreen extends AbsolutePanel implements Screen {

	private boolean frozen = false;

	public ScoresScreen(ScreenManager screenManager) {
		setHeight(Cupido.height + "px");
		setWidth(Cupido.width + "px");
		Label label = new HTML("<b>Scores scteen (TODO)</b>");
		add(label, 300, 320);
	}

	@Override
	public void prepareRemoval() {
	}

	@Override
	public void freeze() {
		frozen = true;
	}
}
