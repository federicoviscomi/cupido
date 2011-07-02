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

import java.util.ArrayList;

import unibo.as.cupido.client.Cupido;
import unibo.as.cupido.common.structures.RankingEntry;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * This class handles the scores screen.
 */
public class ScoresScreen extends VerticalPanel implements Screen {

	/**
	 * The button that takes back to the main menu.
	 */
	private PushButton exitButton;

	/**
	 * Specifies whether the UI is frozen (i.e. does no longer react to events)
	 * or not.
	 */
	@SuppressWarnings("unused")
	private boolean frozen = false;

	/**
	 * @param screenManager
	 *            The global screen manager.
	 * @param username
	 *            The username of the current user.
	 * @param topRanks
	 *            The <code>RankingEntry</code> objects referring to the top 10
	 *            users.
	 * @param localRanks
	 *            The <code>RankingEntry</code> objects referring to the current
	 *            user and to users with similar ranks.
	 */
	public ScoresScreen(final ScreenManager screenManager,
			final String username, ArrayList<RankingEntry> topRanks,
			ArrayList<RankingEntry> localRanks) {
		setHeight((Cupido.height - 50) + "px");
		setWidth(Cupido.width + "px");

		setVerticalAlignment(ALIGN_MIDDLE);
		setHorizontalAlignment(ALIGN_CENTER);

		add(new HTML("<h1>Classifica</h1>"));

		VerticalPanel panelContainer = new VerticalPanel();
		panelContainer.setHorizontalAlignment(ALIGN_LEFT);
		panelContainer.setWidth("325px");
		panelContainer.setHeight("425px");
		add(panelContainer);

		// If the user font causes the scores not to fit in the panel,
		// display vertical scrollbars instead of overflow.
		ScrollPanel scrollPanel = new ScrollPanel();
		scrollPanel.setAlwaysShowScrollBars(false);
		scrollPanel.setWidth("320px");
		scrollPanel.setHeight("420px");
		panelContainer.add(scrollPanel);
		DOM.setStyleAttribute(scrollPanel.getElement(), "borderWidth", "1px");
		DOM.setStyleAttribute(scrollPanel.getElement(), "borderStyle", "solid");

		FlowPanel panel = new FlowPanel();
		panel.setWidth("300px");
		panel.setHeight("400px");
		scrollPanel.add(panel);
		DOM.setStyleAttribute(panel.getElement(), "padding", "10px");

		for (RankingEntry entry : topRanks)
			panel.add(constructRow(entry, entry.username.equals(username)));

		int lastTopRank = topRanks.get(topRanks.size() - 1).rank;
		int jumpedIndexes = localRanks.get(0).rank - lastTopRank;
		boolean jumpingIndexes = (jumpedIndexes > 1);

		if (jumpingIndexes)
			panel.add(constructVerticalDots());

		for (RankingEntry entry : localRanks)
			if (entry.rank > lastTopRank)
				panel.add(constructRow(entry, entry.username.equals(username)));

		panel.add(constructVerticalDots());

		exitButton = new PushButton("Torna al menu");
		exitButton.setWidth("200px");
		exitButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				screenManager.displayMainMenuScreen(username);
			}
		});
		add(exitButton);
	}

	@Override
	public void freeze() {
		exitButton.setEnabled(false);
		frozen = true;
	}

	@Override
	public void prepareRemoval() {
	}

	/**
	 * Constructs a widget that displays the rank and the score of a user in a
	 * row.
	 * 
	 * @param entry
	 *            The <code>RankingEntry</code> of the user.
	 * @param highlight
	 *            Whether or not to highlight this row.
	 * @return The constructed widget.
	 */
	private static HTML constructRow(RankingEntry entry, boolean highlight) {
		SafeHtmlBuilder builder = new SafeHtmlBuilder();
		builder.append(entry.rank);
		builder.appendHtmlConstant(". ");
		if (highlight)
			builder.appendHtmlConstant("<b>");
		builder.appendEscaped(entry.username);
		if (highlight)
			builder.appendHtmlConstant("</b>");
		builder.appendHtmlConstant(": ");
		builder.append(entry.score);
		builder.appendHtmlConstant("<br />");
		HTML row = new HTML(builder.toSafeHtml());
		row.setWidth("290px");
		DOM.setStyleAttribute(row.getElement(), "padding", "2px");
		if (highlight) {
			DOM.setStyleAttribute(row.getElement(), "borderWidth", "1px");
			DOM.setStyleAttribute(row.getElement(), "borderStyle", "solid");
		}
		return row;
	}

	/**
	 * @return Constructs a widget that displays a vertical column of black
	 *         dots.
	 */
	private static Widget constructVerticalDots() {
		VerticalPanel boxContainer = new VerticalPanel();
		boxContainer.setVerticalAlignment(ALIGN_MIDDLE);
		boxContainer.setWidth("290px");
		boxContainer.setHeight("30px");
		SimplePanel box = new SimplePanel();
		box.setWidth("260px");
		box.setHeight("21px");
		boxContainer.add(box);
		boxContainer.setCellHorizontalAlignment(box, ALIGN_CENTER);
		DOM.setStyleAttribute(box.getElement(), "borderLeftWidth", "3px");
		DOM.setStyleAttribute(box.getElement(), "borderLeftStyle", "dotted");
		return boxContainer;
	}
}
