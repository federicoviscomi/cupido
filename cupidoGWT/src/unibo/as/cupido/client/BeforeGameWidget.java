package unibo.as.cupido.client;

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
	HTML leftLabel;
	HTML topLabel;
	HTML rightLabel;

	/*
	 * This is not null only if there is no player in that position and the
	 * current user is the table owner.
	 */
	PushButton leftButton = null;

	/*
	 * This is not null only if there is no player in that position and the
	 * current user is the table owner.
	 */
	PushButton topButton = null;

	/*
	 * This is not null only if there is no player in that position and the
	 * current user is the table owner.
	 */
	PushButton rightButton = null;

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
		
		setWidth(tableSize + "px");
		setHeight(tableSize + "px");
		DOM.setStyleAttribute(getElement(), "background", "green");
		
		bottomLabel = new HTML();
		leftLabel = new HTML();
		topLabel = new HTML();
		rightLabel = new HTML();

		bottomLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		leftLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		topLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		rightLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

		bottomLabel.setWidth(playerLabelWidth + "px");
		leftLabel.setWidth(playerLabelWidth + "px");
		topLabel.setWidth(playerLabelWidth + "px");
		rightLabel.setWidth(playerLabelWidth + "px");

		bottomLabel.setHeight(playerLabelHeight + "px");
		leftLabel.setHeight(playerLabelHeight + "px");
		topLabel.setHeight(playerLabelHeight + "px");
		rightLabel.setHeight(playerLabelHeight + "px");

		add(bottomLabel, tableSize / 2 - playerLabelWidth / 2, tableSize - 10
				- playerLabelHeight);
		add(leftLabel, 10, tableSize / 2 - playerLabelHeight / 2);
		add(topLabel, tableSize / 2 - playerLabelWidth / 2, 10);
		add(rightLabel, tableSize - 10 - playerLabelWidth, tableSize / 2
				- playerLabelHeight / 2);
		
		bottomLabel.setHTML(constructLabelHtml(bottomUserName, initialTableStatus.playerScores[0]));

		if (initialTableStatus.opponents[0] != null) {
			leftLabel.setHTML(constructLabelHtml(initialTableStatus.opponents[0],
					initialTableStatus.playerScores[1]));
		} else {
			if (isOwner) {
				addBotButton(0);
			} else {
				// TODO: Decide what the label should show in this case.
				// When changing here, also update the removePlayer() method.
			}
		}

		if (initialTableStatus.opponents[1] != null) {
			topLabel.setHTML(constructLabelHtml(initialTableStatus.opponents[1],
					initialTableStatus.playerScores[2]));
		} else {
			if (isOwner) {
				addBotButton(1);
			} else {
				// TODO: Decide what the label should show in this case.
				// When changing here, also update the removePlayer() method.
			}
		}

		if (initialTableStatus.opponents[2] != null) {
			rightLabel.setHTML(constructLabelHtml(initialTableStatus.opponents[2],
					initialTableStatus.playerScores[3]));
		} else {
			if (isOwner) {
				addBotButton(2);
			} else {
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
	
	private void addBotButton(int position) {
		final int buttonWidth = 95;
		final int buttonHeight = 15;
		
		switch (position) {
		case 0:
			leftButton = new PushButton("Aggiungi un bot");
			leftButton.setHeight(buttonHeight + "px");
			leftButton.setWidth(buttonWidth + "px");
			add(leftButton, 30, tableSize / 2 - buttonHeight / 2);
			leftButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					addBot(0);
				}
			});
			break;
			
		case 1:
			topButton = new PushButton("Aggiungi un bot");
			topButton.setHeight(buttonHeight + "px");
			topButton.setWidth(buttonWidth + "px");
			add(topButton, tableSize / 2 - buttonWidth / 2, 30);
			topButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					addBot(1);
				}
			});
			break;
			
		case 2:
			rightButton = new PushButton("Aggiungi un bot");
			rightButton.setHeight(buttonHeight + "px");
			rightButton.setWidth(buttonWidth + "px");
			add(rightButton, tableSize - 40 - buttonWidth, tableSize / 2
					- buttonHeight / 2);
			rightButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					addBot(2);
				}
			});
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

		switch (position) {
		case 0:
			if (leftButton != null)
				remove(leftButton);
			assert leftLabel.getText().isEmpty();
			leftLabel.setHTML(s);
			break;

		case 1:
			if (topButton != null)
				remove(topButton);
			assert topLabel.getText().isEmpty();
			topLabel.setHTML(s);
			break;

		case 2:
			if (rightButton != null)
				remove(rightButton);
			assert rightLabel.getText().isEmpty();
			rightLabel.setHTML(s);
			break;

		default:
			assert false;
		}
		
		if (isTableFull())
			listener.onTableFull(initialTableStatus);
	}
	
	public boolean isTableFull() {
		return initialTableStatus.opponents[0] != null
			&& initialTableStatus.opponents[1] != null
			&& initialTableStatus.opponents[2] != null;
	}

	private void addBot(int position) {
		// The name is ignored, but it must not be null.
		initialTableStatus.opponents[position] = "";
		initialTableStatus.whoIsBot[position] = true;
		
		switch (position) {
		case 0:
			if (leftButton != null)
				;
			remove(leftButton);
			leftButton = null;
			assert leftLabel.getText().isEmpty();
			leftLabel.setHTML("<b><big>bot</big></b>");
			break;

		case 1:
			if (topButton != null)
				;
			remove(topButton);
			topButton = null;
			assert topLabel.getText().isEmpty();
			topLabel.setHTML("<b><big>bot</big></b>");
			break;

		case 2:
			if (rightButton != null)
				;
			remove(rightButton);
			rightButton = null;
			assert rightLabel.getText().isEmpty();
			rightLabel.setHTML("<b><big>bot</big></b>");
			break;

		default:
			assert false;
		}
		
		if (isTableFull())
			listener.onTableFull(initialTableStatus);
	}
	
	private void removePlayer(int player) {
		initialTableStatus.opponents[player] = null;
		
		switch (player) {
		case 0:
			
			leftLabel.setText("");
			if (isOwner)
				addBotButton(player);
			break;
			
		case 1:
			
			leftLabel.setText("");
			if (isOwner)
				addBotButton(player);
			break;
			
		case 2:
			
			leftLabel.setText("");
			if (isOwner)
				addBotButton(player);
			break;
		}
	}
	
	public void disableControls() {
		if (leftButton != null)
			leftButton.setEnabled(false);
		if (topButton != null)
			topButton.setEnabled(false);
		if (rightButton != null)
			rightButton.setEnabled(false);
	}

	public void handleGameEnded(int[] matchPoints, int[] playersTotalPoints) {
		listener.onGameEnded();
	}

	public void handleNewPlayerJoined(String name, boolean isBot, int points,
			int position) {
		if (isBot)
			addBot(position);
		else
			addPlayer(name, points, position);
	}

	public void handlePlayerLeft(String player) {
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
