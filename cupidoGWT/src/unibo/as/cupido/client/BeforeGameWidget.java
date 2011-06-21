package unibo.as.cupido.client;

import java.util.ArrayList;
import java.util.List;

import unibo.as.cupido.common.structures.InitialTableStatus;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.PushButton;

public class BeforeGameWidget extends AbsolutePanel {

	// The width of the players' labels that contain usernames and scores.
	private static final int playerLabelWidth = 200;

	// The height of the players' labels that contain usernames and scores.
	private static final int playerLabelHeight = 20;

	HTML bottomLabel;
	/*
	 * labels.get(0) is the left label, and other labels follow in clockwise order.
	 * This list has always 3 elements.
	 */
	List<HTML> labels = new ArrayList<HTML>();

	/*
	 * A list containing the displayed "Add bot" buttons (if any).
	 * buttons.get(0) is the left button, and other buttons follow in clockwise order.
	 * This list has always 3 elements.
	 * Each element may be null if there is no button displayed in that position.
	 */
	List<PushButton> buttons = new ArrayList<PushButton>();

	public interface Listener {
		/**
		 * This is called when the table is full of players and/or bots.
		 */
		void onTableFull(InitialTableStatus initialTableStatus);
		
		/**
		 * This is called if the game is interrupted by another player (i.e. the owner).
		 */
		void onGameEnded();
		
		/**
		 * This is called if the user chooses to exit.
		 */
		void onExit();
	}
	
	InitialTableStatus initialTableStatus;

	private boolean isOwner;

	private int tableSize;

	private Listener listener;

	private boolean frozen = false;

	/*
	 * The widget that represents the table before the actual game.
	 * 
	 * @param username is the bottom player's username (it may be the current
	 * user or not, depending whether the user is a player or just a viewer).
	 * 
	 * @points are is the total points of the bottom player.
	 * 
	 * @param isOwner specifies whether the current user is the owner of the
	 * table (and thus also a player).
	 */
	public BeforeGameWidget(int tableSize, String bottomUserName, boolean isOwner,
			InitialTableStatus initialTableStatus, final Listener listener) {
		
		this.tableSize = tableSize;
		this.isOwner = isOwner;
		this.initialTableStatus = initialTableStatus;
		this.listener = listener;

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
		labels.get(1).setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		labels.get(2).setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

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
		
		bottomLabel.setHTML(constructLabelHtml(bottomUserName, initialTableStatus.playerScores[0]));
		
		for (int i = 0; i < 3; i++)
			if (initialTableStatus.opponents[i] != null) {
				labels.get(i).setHTML(constructLabelHtml(initialTableStatus.opponents[i],
						initialTableStatus.playerScores[i + 1]));
			} else {
				if (isOwner)
					addBotButton(i);
				else {
					// TODO: Decide what the label should show in this case.
					// When changing here, also update the removePlayer() method.
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
	
	private void addBotButton(final int position) {
		final int buttonWidth = 95;
		final int buttonHeight = 15;
		
		PushButton button = new PushButton("Aggiungi un bot");
		buttons.set(position, button);
		button.setHeight(buttonHeight + "px");
		button.setWidth(buttonWidth + "px");
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				addBot(position);
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
	
	private static String constructLabelHtml(String username, int points) {
		SafeHtmlBuilder builder = new SafeHtmlBuilder();
		builder.appendHtmlConstant("<b><big>");
		builder.appendEscaped(username);
		builder.appendHtmlConstant(" (");
		builder.append(points);
		builder.appendHtmlConstant(")</big></b>");
		
		return builder.toSafeHtml().asString();
	}

	private void addPlayer(String username, int points, int position) {
		
		initialTableStatus.opponents[position] = username;
		initialTableStatus.playerScores[position + 1] = points;
		initialTableStatus.whoIsBot[position] = false;

		SafeHtmlBuilder builder = new SafeHtmlBuilder();
		builder.appendHtmlConstant("<b><big>");
		builder.appendEscaped(username);
		builder.appendHtmlConstant(" (");
		builder.append(points);
		builder.appendHtmlConstant(")</big></b>");

		String s = builder.toSafeHtml().asString();

		if (buttons.get(position) != null)
			remove(buttons.get(position));
		assert labels.get(position).getText().isEmpty();
		labels.get(position).setHTML(s);
		
		if (isTableFull())
			listener.onTableFull(initialTableStatus);
	}
	
	private boolean isTableFull() {
		return initialTableStatus.opponents[0] != null
			&& initialTableStatus.opponents[1] != null
			&& initialTableStatus.opponents[2] != null;
	}

	private void addBot(int position) {
		// The name is ignored, but it must not be null.
		initialTableStatus.opponents[position] = "";
		initialTableStatus.whoIsBot[position] = true;
		
		assert (buttons.get(position) != null);
		remove(buttons.get(position));
		buttons.set(position, null);
		
		assert labels.get(position).getText().isEmpty();
		labels.get(position).setHTML("<b><big>bot</big></b>");
		
		if (isTableFull())
			listener.onTableFull(initialTableStatus);
	}
	
	private void removePlayer(int player) {
		initialTableStatus.opponents[player] = null;
		
		labels.get(player).setText("");
		if (isOwner)
			addBotButton(player);
	}
	
	public void freeze() {
		for (PushButton button : buttons)
			if (button != null)
				button.setEnabled(false);
		frozen  = true;
	}

	public void handleGameEnded(int[] matchPoints, int[] playersTotalPoints) {
		if (frozen) {
			System.out.println("Client: notice: received a GameEnded notification while frozen, ignoring it.");
			return;
		}
		listener.onGameEnded();
	}

	public void handleNewPlayerJoined(String name, boolean isBot, int points,
			int position) {
		if (frozen) {
			System.out.println("Client: notice: received a NewPlayerJoined notification while frozen, ignoring it.");
			return;
		}
		if (isBot)
			addBot(position);
		else
			addPlayer(name, points, position);
	}

	public void handlePlayerLeft(String player) {
		if (frozen) {
			System.out.println("Client: notice: received a PlayerLeft notification while frozen, ignoring it.");
			return;
		}
		for (int i = 0; i < 3; i++) {
			if (initialTableStatus.opponents[i] == null)
				continue;
			if (initialTableStatus.whoIsBot[i])
				continue;
			if (initialTableStatus.opponents[i].equals(player)) {
				removePlayer(i);
				return;
			}
		}
		System.out.println("Client: received an invalid PlayerLeft notification: the user does not exist.");
	}
}
