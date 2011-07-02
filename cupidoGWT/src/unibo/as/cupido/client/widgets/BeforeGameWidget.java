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
import unibo.as.cupido.shared.cometNotification.GameEnded;
import unibo.as.cupido.shared.cometNotification.NewPlayerJoined;
import unibo.as.cupido.shared.cometNotification.PlayerLeft;

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

	/**
	 * An interface used by client code to be notified of
	 * various events.
	 */
	public interface Listener {
		/**
		 * This is called if the user chooses to exit.
		 */
		public void onExit();

		/**
		 * This is called if a fatal exception occurs.
		 * 
		 * @param e The exception that was caught.
		 */
		public void onFatalException(Throwable e);

		/**
		 * This is called if the game is interrupted by another player (i.e. the
		 * creator).
		 */
		public void onGameEnded();

		/**
		 * This is called when the table becomes full of players and/or bots.
		 */
		public void onTableFull();
	}

	/**
	 *  The height of the players' labels that contain usernames and scores.
	 */
	private static final int playerLabelHeight = 20;

	/**
	 *  The width of the players' labels that contain usernames and scores.
	 */
	private static final int playerLabelWidth = 200;

	/**
	 * Construct the text for a bot's label.
	 * 
	 * @param name The name of the bot.
	 * 
	 * @return The text for the bot's label.
	 */
	private static String constructBotLabelHtml(String name) {
		SafeHtmlBuilder builder = new SafeHtmlBuilder();
		builder.appendHtmlConstant("<b><big>");
		builder.appendEscaped(name);
		builder.appendHtmlConstant("</big></b>");

		return builder.toSafeHtml().asString();
	}

	/**
	 * Construct the text for a user's label.
	 * 
	 * @param username The name of the user.
	 * @param score The global score of the specified user.
	 * 
	 * @return The text for the user's label.
	 */
	private static String constructPlayerLabelHtml(String username, int points) {
		SafeHtmlBuilder builder = new SafeHtmlBuilder();
		builder.appendHtmlConstant("<b><big>");
		builder.appendEscaped(username);
		builder.appendHtmlConstant(" (");
		builder.append(points);
		builder.appendHtmlConstant(")</big></b>");

		return builder.toSafeHtml().asString();
	}

	/**
	 * The label of the bottom player.
	 */
	private HTML bottomLabel;

	/**
	 * A list containing the displayed "Add bot" buttons (if any).
	 * buttons.get(0) is the left button, and other buttons follow in clockwise
	 * order. This list has always 3 elements. Each element may be null if there
	 * is no button displayed in that position.
	 */
	private List<PushButton> buttons = new ArrayList<PushButton>();

	/**
	 * This is used to communicate with the servlet using RPC.
	 */
	private CupidoInterfaceAsync cupidoService;

	/**
	 * Specifies whether the UI is frozen (i.e. does no longer react to events) or not.
	 */
	private boolean frozen = false;

	/**
	 * Specifies whether or not the current user is the owner of the table.
	 */
	private boolean isOwner;

	/**
	 * labels.get(0) is the left label, and other labels follow in clockwise
	 * order. This list has always 3 elements.
	 */
	private List<HTML> labels = new ArrayList<HTML>();

	/**
	 * The listener used to notify the client code about various events.
	 */
	private Listener listener;

	/**
	 * The size of the widget (both width and height) in pixels.
	 */
	private int tableSize;

	/**
	 * The current status of the table.
	 */
	private InitialTableStatus tableStatus;

	/**
	 * The widget that represents the table before the actual game.
	 * 
	 * @param tableSize The size of the widget (both width and height), in pixels.
	 * @param username The username of the current user.
	 * @param bottomUserName
	 *            Is the bottom player's username (it may be the current user or
	 *            not, depending whether the user is a player or just a viewer).
	 * @param isOwner
	 *            Specifies whether or not the current user is the creator of the
	 *            table.
	 * @param tableStatus This contains some information about the table status.
	 * @param scores
	 *            The global scores of existing players. The scores contained in
	 *            tableStatus are ignored.
	 * @param cupidoService This is used to communicate with the servlet using RPC.
	 * @param listener The listener used to notify the client code about various events.
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

	/**
	 * A helper method to add a bot to the table.
	 * 
	 * @param position The position where the bot has to be added.
	 * @param name The name of the bot.
	 */
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

	/**
	 * A helper method to add an 'Add bot' button in the specified position.
	 * 
	 * @param position The position where the button has to be added.
	 */
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

	/**
	 * A helper method to add a player to the table.
	 * 
	 * @param username The name of the player.
	 * @param score The global score of the specified player.
	 * @param position The position where the player has to be added.
	 */
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

	/**
	 * When this is called, the widget stops responding to events
	 * and disables all user controls.
	 */
	public void freeze() {
		for (PushButton button : buttons)
			if (button != null)
				button.setEnabled(false);
		frozen = true;
	}

	/**
	 * @return The current status of the table.
	 */
	public InitialTableStatus getTableStatus() {
		return tableStatus;
	}

	/**
	 * This is called when a GameEnded notification is received
	 * from the servlet.
	 * 
	 * @param matchPoints The score scored by the players during the current game.
	 * @param playersTotalPoints The total score of the players, already updated
	 *                           with the results of the current game.
	 * 
	 * @see GameEnded
	 */
	public void handleGameEnded(int[] matchPoints, int[] playersTotalPoints) {
		if (frozen) {
			System.out
					.println("Client: notice: received a GameEnded notification while frozen, ignoring it.");
			return;
		}
		listener.onGameEnded();
	}

	/**
	 * This is called when a NewPlayerJoined notification is received
	 * from the servlet.
	 * 
	 * @param name The name of the player who joined the game.
	 * @param isBot Specifies whether the player is a user or a bot.
	 * @param score The (global) score of the player.
	 * @param position The position of the player in the table.
	 * 
	 * @see NewPlayerJoined
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

	/**
	 * This is called when a Playerleft notification is received
	 * from the servlet.
	 * 
	 * @param player The player that left the game.
	 * 
	 * @see PlayerLeft
	 */
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

	/**
	 * @return true if the table has no free seats.
	 */
	private boolean isTableFull() {
		return tableStatus.opponents[0] != null
				&& tableStatus.opponents[1] != null
				&& tableStatus.opponents[2] != null;
	}

	/**
	 * Removes the specified player from the table.
	 * 
	 * @param player The position of the player that has to be removed.
	 */
	private void removePlayer(int player) {
		tableStatus.opponents[player] = null;

		labels.get(player).setText("");
		if (isOwner)
			addBotButton(player);
	}
}
