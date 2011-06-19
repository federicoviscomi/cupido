package unibo.as.cupido.client;

import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.InitialTableStatus;
import unibo.as.cupido.client.playerstates.PlayerStateManager;
import unibo.as.cupido.client.playerstates.PlayerStateManagerImpl;
import unibo.as.cupido.client.screens.ScreenSwitcher;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;

public class HeartsTableWidget extends AbsolutePanel {

	private ScreenSwitcher screenSwitcher;
	private BeforeGameWidget beforeGameWidget;
	private CardsGameWidget cardsGameWidget;
	private int tableSize;

	private PlayerStateManager stateManager;

	/**
	 * 
	 * @param tableSize
	 *            The size of the table (width and height) in pixels.
	 * @param username
	 */
	public HeartsTableWidget(int tableSize, final String username,
			final ScreenSwitcher screenSwitcher) {
		this.tableSize = tableSize;
		this.screenSwitcher = screenSwitcher;

		setWidth(tableSize + "px");
		setHeight(tableSize + "px");

		beforeGameWidget = new BeforeGameWidget(tableSize, "pippo", 1234, true,
				new BeforeGameWidget.Callback() {
					private int numPlayers = 1;

					@Override
					public void onAddBot(int position) {
						numPlayers++;
						if (numPlayers == 4)
							startGame(username);
					}
				});
		add(beforeGameWidget, 0, 0);
	}

	public void startGame(final String username) {

		remove(beforeGameWidget);

		// FIXME: Initialize the widget with the correct values.
		// These values are only meant for debugging purposes.

		InitialTableStatus initialTableStatus = new InitialTableStatus();

		initialTableStatus.opponents = new String[3];
		initialTableStatus.opponents[0] = "Pinco";
		initialTableStatus.opponents[1] = "This must *not* be displayed";
		initialTableStatus.opponents[2] = "Pallo";

		initialTableStatus.playerScores = new int[4];
		initialTableStatus.playerScores[0] = 1234;
		initialTableStatus.playerScores[1] = 5678;
		initialTableStatus.playerScores[2] = 9012;
		initialTableStatus.playerScores[3] = 3456;

		initialTableStatus.whoIsBot = new boolean[3];
		initialTableStatus.whoIsBot[0] = false;
		initialTableStatus.whoIsBot[1] = true;
		initialTableStatus.whoIsBot[2] = false;

		Card[] bottomPlayerCards = new Card[13];

		for (int i = 0; i < 13; i++)
			bottomPlayerCards[i] = RandomCardGenerator.generateCard();

		stateManager = new PlayerStateManagerImpl(tableSize, screenSwitcher,
				initialTableStatus, bottomPlayerCards, username);

		final VerticalPanel controllerPanel = new VerticalPanel();
		controllerPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		controllerPanel
				.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

		final PushButton exitButton = new PushButton("Esci dal gioco");
		exitButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				screenSwitcher.displayMainMenuScreen(username);
			}
		});
		controllerPanel.add(exitButton);

		cardsGameWidget = stateManager.getWidget();
		add(cardsGameWidget, 0, 0);
	}
}
