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

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;

public class GeneralErrorScreen extends AbsolutePanel implements Screen {

	public GeneralErrorScreen(ScreenManager screenManager, Throwable e) {

		// Set an empty listener (one that handles no messages).
		screenManager.setListener(new CometMessageListener());

		setHeight(Cupido.height + "px");
		setWidth(Cupido.width + "px");

		ScrollPanel panel = new ScrollPanel();
		panel.setHeight(Cupido.height + "px");
		panel.setWidth(Cupido.width + "px");

		SafeHtmlBuilder htmlBuilder = new SafeHtmlBuilder();
		htmlBuilder.appendHtmlConstant("<h1>Si è verificato un errore</h1>");
		htmlBuilder
				.appendHtmlConstant("<p>Ricarica la pagina per tornare a Cupido.</p>");
		htmlBuilder
				.appendHtmlConstant("<br>Questi sono i dati relativi all'errore: l'eccezione<br>");
		htmlBuilder.appendEscaped(e.toString());
		htmlBuilder.appendHtmlConstant("<br>Si è verificata:");
		
		StackTraceElement[] stackTrace = e.getStackTrace();
		for (StackTraceElement stackTraceElement : stackTrace) {
			htmlBuilder.appendHtmlConstant("<br />in ");
			htmlBuilder.appendEscaped(stackTraceElement.toString());
		}
		panel.add(new HTML(htmlBuilder.toSafeHtml()));

		add(panel, 0, 0);
	}

	@Override
	public void prepareRemoval() {
	}

	@Override
	public void freeze() {
	}
}
