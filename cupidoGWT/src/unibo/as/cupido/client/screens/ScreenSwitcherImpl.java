package unibo.as.cupido.client.screens;

import java.io.Serializable;
import java.util.List;

import unibo.as.cupido.client.Cupido;
import unibo.as.cupido.client.CupidoCometListener;
import unibo.as.cupido.client.CupidoInterface;
import unibo.as.cupido.client.CupidoInterfaceAsync;
import unibo.as.cupido.client.Cupido.CupidoCometSerializer;

import net.zschech.gwt.comet.client.CometClient;
import net.zschech.gwt.comet.client.CometListener;
import net.zschech.gwt.comet.client.CometSerializer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Widget;

public class ScreenSwitcherImpl extends AbsolutePanel implements ScreenSwitcher {

	Widget currentScreen = null;

	// This is used to check that no screen switches occur while switching
	// screen.
	boolean switchingScreen = false;

	// / This is null when the user is not logged in.
	String username = null;

	CupidoInterfaceAsync cupidoService = GWT.create(CupidoInterface.class);

	CupidoCometListener cometListener;

	public ScreenSwitcherImpl() {
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

				cometListener = new CupidoCometListener(new CometListener() {

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
					public void onRefresh() {
					}

					@Override
					public void onMessage(List<? extends Serializable> messages) {
						System.out
								.println("Client: received Comet message while loading.");
					}

				});

				CometSerializer serializer = GWT
						.create(CupidoCometSerializer.class);

				CometClient cometClient = new CometClient(GWT
						.getModuleBaseURL() + "comet", serializer,
						cometListener);
				cometClient.start();

				System.out.println("Client: Comet client started ("
						+ GWT.getModuleBaseURL() + "comet).");

				displayMainMenuScreen();
			}
		});
	}

	private void removeCurrentScreen() {
		if (currentScreen == null)
			return;
		if (currentScreen instanceof CupidoMainMenuScreen)
			// Update the `username' field on login and logout.
			username = ((CupidoMainMenuScreen) currentScreen).getUsername();
		remove(currentScreen);
	}

	@Override
	public void displayMainMenuScreen() {
		assert !switchingScreen;
		switchingScreen = true;

		removeCurrentScreen();
		currentScreen = new CupidoMainMenuScreen(this, username);
		add(currentScreen, 0, 0);

		switchingScreen = false;
	}

	@Override
	public void displayScoresScreen() {
		assert !switchingScreen;
		switchingScreen = true;

		removeCurrentScreen();
		assert username != null;
		remove(currentScreen);
		currentScreen = new CupidoScoresScreen(this);
		add(currentScreen, 0, 0);

		switchingScreen = false;
	}

	@Override
	public void displayAboutScreen() {
		assert !switchingScreen;
		switchingScreen = true;

		removeCurrentScreen();
		assert username != null;
		remove(currentScreen);
		currentScreen = new CupidoAboutScreen(this);
		add(currentScreen, 0, 0);

		switchingScreen = false;
	}

	@Override
	public void displayTableScreen() {
		assert !switchingScreen;
		switchingScreen = true;

		removeCurrentScreen();
		assert username != null;
		currentScreen = new CupidoTableScreen(this, username, cupidoService,
				cometListener);
		add(currentScreen, 0, 0);

		switchingScreen = false;
	}

	@Override
	public void displayObservedTableScreen() {
		assert !switchingScreen;
		switchingScreen = true;

		removeCurrentScreen();
		assert username != null;
		currentScreen = new CupidoObservedTableScreen(this, username);
		add(currentScreen, 0, 0);

		switchingScreen = false;
	}

	@Override
	public void displayGeneralErrorScreen(Exception e) {
		assert !switchingScreen;
		switchingScreen = true;

		removeCurrentScreen();
		currentScreen = new CupidoGeneralErrorScreen(this, e);
		add(currentScreen, 0, 0);

		switchingScreen = false;
	}

	public void displayLoadingScreen() {
		assert !switchingScreen;
		switchingScreen = true;

		removeCurrentScreen();
		currentScreen = new CupidoLoadingScreen();
		add(currentScreen, 0, 0);

		switchingScreen = false;
	}
}
