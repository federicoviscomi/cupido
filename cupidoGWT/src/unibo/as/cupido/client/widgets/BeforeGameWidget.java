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

package unibo.as.cupido.client.widgets;

import java.util.ArrayList;
import java.util.List;

import unibo.as.cupido.client.CupidoInterfaceAsync;
import unibo.as.cupido.common.exception.FatalException;
import unibo.as.cupido.common.exception.FullPositionException;
import unibo.as.cupido.common.exception.FullTableException;
import unibo.as.cupido.common.exception.GameInterruptedException;
import unibo.as.cupido.common.exception.NoSuchTableException;
import unibo.as.cupido.common.exception.NotCreatorException;
import unibo.as.cupido.common.exception.UserNotAuthenticatedException;
import unibo.as.cupido.common.structures.InitialTableStatus;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.PushButton;

/**
 * A widget that represents a table with no cards, and that may have free seats
 * (and probably does).
 */
public class BeforeGameWidget extends AbsolutePanel {

	public interface Listener {
		/**
		 * This is called if the user chooses to exit.
		 */
		public void onExit();

		/**
		 * This is called if a fatal exception occurs.
		 */
		public void onFatalException(Throwable e);

		/**
		 * This is called if the game is interrupted by another player (i.e. the
		 * owner).
		 */
		public void onGameEnded();

		/**
		 * This is called when the table becomes full of players and/or bots.
		 */
		public void onTableFull();
	}

	// The height of the players' labels that contain usernames and scores.
	private static final int playerLabelHeight = 20;

	// The width of the players' labels that contain usernames and scores.
	private static final int playerLabelWidth = 200;

	private static String constructBotLabelHtml(String username) {
		SafeHtmlBuilder builder = new SafeHtmlBuilder();
		builder.appendHtmlConstant("<b><big>");
		builder.appendEscaped(username);
		builder.appendHtmlConstant("</big></b>");

		return builder.toSafeHtml().asString();
	}

	private static String constructPlayerLabelHtml(String username, int points) {
		SafeHtmlBuilder builder = new SafeHtmlBuilder();
		builder.appendHtmlConstant("<b><big>");
		builder.appendEscaped(username);
		builder.appendHtmlConstant(" (");
		builder.append(points);
		builder.appendHtmlConstant(")</big></b>");

		return builder.toSafeHtml().asString();
	}

	private HTML bottomLabel;

	/*
	 * A list containing the displayed "Add bot" buttons (if any).
	 * buttons.get(0) is the left button, and other buttons follow in clockwise
	 * order. This list has always 3 elements. Each element may be null if there
	 * is no button displayed in that position.
	 */
	private List<PushButton> buttons = new ArrayList<PushButton>();

	private CupidoInterfaceAsync cupidoService;

	private boolean frozen = false;

	private boolean isOwner;

	/*
	 * labels.get(0) is the left label, and other labels follow in clockwise
	 * order. This list has always 3 elements.
	 */
	private List<HTML> labels = new ArrayList<HTML>();

	private Listener listener;

	private int tableSize;

	private InitialTableStatus tableStatus;

	/**
	 * The widget that represents the table before the actual game.
	 * 
	 * @param username
	 *            Is the bottom player's username (it may be the current user or
	 *            not, depending whether the user is a player or just a viewer).
	 * 
	 * @param scores
	 *            The global scores of existing players. The scores contained in
	 *            tableStatus are ignored.
	 * 
	 * @param isOwner
	 *            Specifies whether or not the current user is the owner of the
	 *            table.
	 */
	public BeforeGameWidget(int tableSize, String username,
			String bottomUserName, boolean isOwner,
			InitialTableStatus tableStatus, int[] scores,
			CupidoInterfaceAsync cupidoService, final Listener listener) {

		this.tableSize = tableSize;
		this.isOwner = isOwner;
		this.tableStatus = tableStatus;
		this.listener = listener;
		this.cupidoService = cupidoService;

		for (int i = 0; i < 3; i++)
			buttons.add(null);

		setWidth(tableSize + "px");
		setHeight(tableSize + "px");
		DOM.setStyleAttribute(getElement(), "background", "green");

		bottomLabel = new HTML();

		for (int i = 0; i < 3; i++)
			labels.add(new HTML());

		bottomLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		labels.get(0).setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		labels.get(1).setHorizontalAlignment(
				HasHorizontalAlignment.ALIGN_CENTER);
		labels.get(2)
				.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

		bottomLabel.setWidth(playerLabelWidth + "px");
		bottomLabel.setHeight(playerLabelHeight + "px");

		for (HTML label : labels) {
			label.setWidth(playerLabelWidth + "px");
			label.setHeight(playerLabelHeight + "px");
		}

		add(bottomLabel, tableSize / 2 - playerLabelWidth / 2, tableSize - 10
				- playerLabelHeight);
		add(labels.get(0), 10, tableSize / 2 - playerLabelHeight / 2);
		add(labels.get(1), tableSize / 2 - playerLabelWidth / 2, 10);
		add(labels.get(2), tableSize - 10 - playerLabelWidth, tableSize / 2
				- playerLabelHeight / 2);

		bottomLabel
				.setHTML(constructPlayerLabelHtml(bottomUserName, scores[0]));

		for (int i = 0; i < 3; i++)
			if (tableStatus.opponents[i] != null) {
				if (tableStatus.whoIsBot[i]) {
					labels.get(i).setHTML(
							constructBotLabelHtml(tableStatus.opponents[i]));
				} else {
					labels.get(i).setHTML(
							constructPlayerLabelHtml(tableStatus.opponents[i],
									scores[i + 1]));
				}
			} else {
				if (isOwner)
					addBotButton(i);
				else {
					// Do nothing.

					// When changing here, also update the removePlayer()
					// method.
				}
			}

		PushButton exitButton = new PushButton("Esci");
		exitButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				listener.onExit();
			}
		});
		add(exitButton, tableSize - 100, tableSize - 40);
	}

	private void addBot(int position, String name) {
		tableStatus.opponents[position] = name;
		tableStatus.whoIsBot[position] = true;

		if (isOwner) {
			assert (buttons.get(position) != null);
			remove(buttons.get(position));
			buttons.set(position, null);
		}

		assert labels.get(position).getText().isEmpty();

		labels.get(position).setHTML(constructBotLabelHtml(name));

		if (isTableFull())
			listener.onTableFull();
	}

	private void addBotButton(final int position) {
		final int buttonWidth = 95;
		final int buttonHeight = 15;

		final PushButton button = new PushButton("Aggiungi un bot");
		buttons.set(position, button);
		button.setHeight(buttonHeight + "px");
		button.setWidth(buttonWidth + "px");
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				remove(button);
				cupidoService.addBot(position + 1, new AsyncCallback<String>() {
					@Override
					public void onFailure(Throwable caught) {
						try {
							throw caught;
						} catch (FullPositionException e) {
							// The position has been occupied in the meantime,
							// nothing to do.
						} catch (FullTableException e) {
							// The position has been occupied in the meantime,
							// nothing to do.
						} catch (NotCreatorException e) {
							listener.onFatalException(e);
						} catch (IllegalArgumentException e) {
							listener.onFatalException(e);
						} catch (NoSuchTableException e) {
							// The table has been destroyed in the meantime,
							// nothing to do. The GameEnded notification will
							// bring back the user to the main menu.
						} catch (GameInterruptedException e) {
							// The table has been destroyed in the meantime,
							// nothing to do.
						} catch (UserNotAuthenticatedException e) {
							listener.onFatalException(e);
						} catch (FatalException e) {
							listener.onFatalException(e);
						} catch (Throwable e) {
							listener.onFatalException(e);
						}
					}

					@Override
					public void onSuccess(String name) {
						addBot(position, name);
					}
				});
			}
		});

		switch (position) {
		case 0:
			add(button, 30, tableSize / 2 - buttonHeight / 2);
			break;

		case 1:
			add(button, tableSize / 2 - buttonWidth / 2, 30);
			break;

		case 2:
			add(button, tableSize - 40 - buttonWidth, tableSize / 2
					- buttonHeight / 2);
			break;
		}
	}

	private void addPlayer(String username, int points, int position) {

		tableStatus.opponents[position] = username;
		tableStatus.playerScores[position + 1] = points;
		tableStatus.whoIsBot[position] = false;

		if (buttons.get(position) != null)
			remove(buttons.get(position));
		assert labels.get(position).getText().isEmpty();
		labels.get(position)
				.setHTML(constructPlayerLabelHtml(username, points));

		if (isTableFull())
			listener.onTableFull();
	}

	public void freeze() {
		for (PushButton button : buttons)
			if (button != null)
				button.setEnabled(false);
		frozen = true;
	}

	public InitialTableStatus getInitialTableStatus() {
		return tableStatus;
	}

	public void handleGameEnded(int[] matchPoints, int[] playersTotalPoints) {
		if (frozen) {
			System.out
					.println("Client: notice: received a GameEnded notification while frozen, ignoring it.");
			return;
		}
		listener.onGameEnded();
	}

	/**
	 * Adds a new player or a bot.
	 * 
	 * @param position
	 *            The position in which the player or the bot has to be added. 0
	 *            means at the left, and other positions follow in clockwise
	 *            order.
	 */
	public void handleNewPlayerJoined(String name, boolean isBot, int points,
			int position) {
		if (frozen) {
			System.out
					.println("Client: notice: received a NewPlayerJoined notification while frozen, ignoring it.");
			return;
		}
		if (isBot)
			addBot(position, name);
		else
			addPlayer(name, points, position);
	}

	public void handlePlayerLeft(String player) {
		if (frozen) {
			System.out
					.println("Client: notice: received a PlayerLeft notification while frozen, ignoring it.");
			return;
		}
		for (int i = 0; i < 3; i++) {
			if (tableStatus.opponents[i] == null)
				continue;
			if (tableStatus.whoIsBot[i])
				continue;
			if (tableStatus.opponents[i].equals(player)) {
				removePlayer(i);
				return;
			}
		}
		System.out
				.println("Client: received an invalid PlayerLeft notification: the user does not exist.");
	}

	private boolean isTableFull() {
		return tableStatus.opponents[0] != null
				&& tableStatus.opponents[1] != null
				&& tableStatus.opponents[2] != null;
	}

	private void removePlayer(int player) {
		tableStatus.opponents[player] = null;

		labels.get(player).setText("");
		if (isOwner)
			addBotButton(player);
	}
}
