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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.zschech.gwt.comet.client.CometClient;
import net.zschech.gwt.comet.client.CometListener;
import net.zschech.gwt.comet.client.CometSerializer;
import unibo.as.cupido.client.CometMessageListener;
import unibo.as.cupido.client.Cupido;
import unibo.as.cupido.client.Cupido.CupidoCometSerializer;
import unibo.as.cupido.client.CupidoInterface;
import unibo.as.cupido.client.CupidoInterfaceAsync;
import unibo.as.cupido.common.structures.InitialTableStatus;
import unibo.as.cupido.common.structures.ObservedGameStatus;
import unibo.as.cupido.common.structures.RankingEntry;
import unibo.as.cupido.common.structures.TableInfoForClient;
import unibo.as.cupido.shared.cometNotification.CardPassed;
import unibo.as.cupido.shared.cometNotification.CardPlayed;
import unibo.as.cupido.shared.cometNotification.GameEnded;
import unibo.as.cupido.shared.cometNotification.GameStarted;
import unibo.as.cupido.shared.cometNotification.NewLocalChatMessage;
import unibo.as.cupido.shared.cometNotification.NewPlayerJoined;
import unibo.as.cupido.shared.cometNotification.PlayerLeft;
import unibo.as.cupido.shared.cometNotification.PlayerReplaced;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The global screen manager.
 * 
 * This implements the screen switching, by both changing the displayed screen
 * and redirecting the comet messages to the current screen.
 */
public class ScreenManagerImpl extends AbsolutePanel implements ScreenManager {

	/**
	 * The listener that handles comet messages.
	 * 
	 * This can be modified with the <code>setListener()</code> method. Each
	 * screen changes this when it's displayed.
	 */
	private CometMessageListener cometMessageListener;

	/**
	 * This is used to communicate with the servlet using RPC.
	 */
	private CupidoInterfaceAsync cupidoService = GWT
			.create(CupidoInterface.class);

	/**
	 * The current screen.
	 */
	private Screen currentScreen = null;

	/**
	 * The widget displaying the current screen.
	 */
	private Widget currentScreenWidget = null;

	/**
	 * This is used to check that no screen switches occur while switching
	 * screen.
	 */
	private boolean switchingScreen = false;

	/**
	 * The default constructor.
	 * 
	 * This displays the loading screen, opens a comet connection to the servlet
	 * and then displays the login screen.
	 */
	public ScreenManagerImpl() {
		setHeight(Cupido.height + "px");
		setWidth(Cupido.width + "px");

		cupidoService.openCometConnection(new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				System.out
						.println("cupidoService.openCometConnection() failed.");
			}

			@Override
			public void onSuccess(Void result) {
				System.out
						.println("cupidoService.openCometConnection() succeeded.");

				CometListener cometListener = new CometListener() {

					@Override
					public void onConnected(int heartbeat) {
					}

					@Override
					public void onDisconnected() {
					}

					@Override
					public void onError(Throwable exception, boolean connected) {
					}

					@Override
					public void onHeartbeat() {
					}

					@Override
					public void onMessage(List<? extends Serializable> messages) {
						for (Serializable message : messages) {
							if (message instanceof CardPassed) {
								CardPassed x = (CardPassed) message;
								cometMessageListener.onCardPassed(x.cards);
							} else if (message instanceof CardPlayed) {
								CardPlayed x = (CardPlayed) message;
								cometMessageListener.onCardPlayed(x.card,
										x.playerPosition);
							} else if (message instanceof GameEnded) {
								GameEnded x = (GameEnded) message;
								cometMessageListener.onGameEnded(x.matchPoints,
										x.playersTotalPoints);
							} else if (message instanceof GameStarted) {
								GameStarted x = (GameStarted) message;
								cometMessageListener.onGameStarted(x.myCards);
							} else if (message instanceof NewLocalChatMessage) {
								NewLocalChatMessage x = (NewLocalChatMessage) message;
								cometMessageListener.onNewLocalChatMessage(
										x.user, x.message);
							} else if (message instanceof NewPlayerJoined) {
								NewPlayerJoined x = (NewPlayerJoined) message;
								cometMessageListener.onNewPlayerJoined(x.name,
										x.isBot, x.points, x.position);
							} else if (message instanceof PlayerLeft) {
								PlayerLeft x = (PlayerLeft) message;
								cometMessageListener.onPlayerLeft(x.player);
							} else if (message instanceof PlayerReplaced) {
								PlayerReplaced x = (PlayerReplaced) message;
								cometMessageListener.onPlayerReplaced(x.name,
										x.position);
							} else {
								displayGeneralErrorScreen(new Exception(
										"Unhandled comet message: "
												+ message.toString()));
								break;
							}
						}
					}

					@Override
					public void onRefresh() {
					}
				};

				CometSerializer serializer = GWT
						.create(CupidoCometSerializer.class);

				CometClient cometClient = new CometClient(GWT
						.getModuleBaseURL() + "comet", serializer,
						cometListener);
				cometClient.start();

				System.out.println("Client: Comet client started ("
						+ GWT.getModuleBaseURL() + "comet).");

				displayLoginScreen();
			}
		});
	}

	@Override
	public void displayAboutScreen(String username) {
		assert !switchingScreen;
		switchingScreen = true;

		removeCurrentScreen();
		AboutScreen screen = new AboutScreen(this, username);
		currentScreen = screen;
		currentScreenWidget = screen;
		add(currentScreenWidget, 0, 0);

		switchingScreen = false;
	}

	@Override
	public void displayGeneralErrorScreen(Throwable e) {
		assert !switchingScreen;
		switchingScreen = true;

		removeCurrentScreen();
		GeneralErrorScreen screen = new GeneralErrorScreen(this, e);
		currentScreen = screen;
		currentScreenWidget = screen;
		add(currentScreenWidget, 0, 0);

		switchingScreen = false;
	}

	@Override
	public void displayLoadingScreen() {
		assert !switchingScreen;
		switchingScreen = true;

		removeCurrentScreen();
		LoadingScreen screen = new LoadingScreen(this);
		currentScreen = screen;
		currentScreenWidget = screen;
		add(currentScreenWidget, 0, 0);

		switchingScreen = false;
	}

	@Override
	public void displayLoginScreen() {
		assert !switchingScreen;
		switchingScreen = true;

		removeCurrentScreen();
		LoginScreen screen = new LoginScreen(this, cupidoService);
		currentScreen = screen;
		currentScreenWidget = screen;
		add(currentScreenWidget, 0, 0);

		switchingScreen = false;

	}

	@Override
	public void displayMainMenuScreen(String username) {
		assert !switchingScreen;
		switchingScreen = true;

		removeCurrentScreen();
		MainMenuScreen screen = new MainMenuScreen(this, username,
				cupidoService);
		currentScreen = screen;
		currentScreenWidget = screen;
		add(currentScreenWidget, 0, 0);

		switchingScreen = false;
	}

	@Override
	public void displayObservedTableScreen(String username,
			ObservedGameStatus observedGameStatus) {
		assert !switchingScreen;
		switchingScreen = true;

		removeCurrentScreen();
		ObservedTableScreen screen = new ObservedTableScreen(this, username,
				observedGameStatus, cupidoService);
		currentScreen = screen;
		currentScreenWidget = screen;
		add(currentScreenWidget, 0, 0);

		switchingScreen = false;
	}

	@Override
	public void displayRegistrationScreen() {
		assert !switchingScreen;
		switchingScreen = true;

		removeCurrentScreen();
		RegistrationScreen screen = new RegistrationScreen(this, cupidoService);
		currentScreen = screen;
		currentScreenWidget = screen;
		add(currentScreenWidget, 0, 0);

		switchingScreen = false;

	}

	@Override
	public void displayScoresScreen(String username,
			ArrayList<RankingEntry> topRanks, ArrayList<RankingEntry> localRanks) {
		assert !switchingScreen;
		switchingScreen = true;

		removeCurrentScreen();
		ScoresScreen screen = new ScoresScreen(this, username, topRanks,
				localRanks);
		currentScreen = screen;
		currentScreenWidget = screen;
		add(currentScreenWidget, 0, 0);

		switchingScreen = false;
	}

	@Override
	public void displayTableListScreen(String username,
			Collection<TableInfoForClient> tableCollection) {
		assert !switchingScreen;
		switchingScreen = true;

		removeCurrentScreen();
		TableListScreen screen = new TableListScreen(this, username,
				tableCollection, cupidoService);
		currentScreen = screen;
		currentScreenWidget = screen;
		add(currentScreenWidget, 0, 0);

		switchingScreen = false;
	}

	@Override
	public void displayTableScreen(String username, boolean isOwner,
			InitialTableStatus initialTableStatus, int userScore) {
		assert !switchingScreen;
		switchingScreen = true;

		removeCurrentScreen();
		TableScreen screen = new TableScreen(this, username, isOwner,
				initialTableStatus, userScore, cupidoService);
		currentScreen = screen;
		currentScreenWidget = screen;
		add(currentScreenWidget, 0, 0);

		switchingScreen = false;
	}

	@Override
	public void setListener(CometMessageListener listener) {
		cometMessageListener = listener;
	}

	/**
	 * A helper method used to remove the current screen.
	 */
	private void removeCurrentScreen() {
		if (currentScreen == null)
			return;
		currentScreen.prepareRemoval();
		remove(currentScreenWidget);
	}
}
