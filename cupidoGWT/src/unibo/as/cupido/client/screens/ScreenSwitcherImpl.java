package unibo.as.cupido.client.screens;

import java.io.Serializable;
import java.util.List;

import net.zschech.gwt.comet.client.CometClient;
import net.zschech.gwt.comet.client.CometListener;
import net.zschech.gwt.comet.client.CometSerializer;
import unibo.as.cupido.client.Cupido;
import unibo.as.cupido.client.Cupido.CupidoCometSerializer;
import unibo.as.cupido.client.CupidoCometListener;
import unibo.as.cupido.client.CupidoInterface;
import unibo.as.cupido.client.CupidoInterfaceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Widget;

public class ScreenSwitcherImpl extends AbsolutePanel implements ScreenSwitcher {

	Widget currentScreenWidget = null;
	Screen currentScreen = null;

	// This is used to check that no screen switches occur while switching
	// screen.
	boolean switchingScreen = false;

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

				displayLoginScreen();
			}
		});
	}

	private void removeCurrentScreen() {
		if (currentScreen == null)
			return;
		currentScreen.prepareRemoval();
		remove(currentScreenWidget);
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
	public void displayMainMenuScreen(String username) {
		assert !switchingScreen;
		switchingScreen = true;

		removeCurrentScreen();
		MainMenuScreen screen = new MainMenuScreen(this, username, cupidoService);
		currentScreen = screen;
		currentScreenWidget = screen;
		add(currentScreenWidget, 0, 0);

		switchingScreen = false;
	}

	@Override
	public void displayScoresScreen(String username) {
		assert !switchingScreen;
		switchingScreen = true;

		removeCurrentScreen();
		ScoresScreen screen = new ScoresScreen(this);
		currentScreen = screen;
		currentScreenWidget = screen;
		add(currentScreenWidget, 0, 0);

		switchingScreen = false;
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
	public void displayTableScreen(String username) {
		assert !switchingScreen;
		switchingScreen = true;

		removeCurrentScreen();
		TableScreen screen = new TableScreen(this, username, cupidoService,
				cometListener);
		currentScreen = screen;
		currentScreenWidget = screen;
		add(currentScreenWidget, 0, 0);

		switchingScreen = false;
	}

	@Override
	public void displayObservedTableScreen(String username) {
		assert !switchingScreen;
		switchingScreen = true;

		removeCurrentScreen();
		ObservedTableScreen screen = new ObservedTableScreen(this, username,
				cupidoService, cometListener);
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

	public void displayLoadingScreen() {
		assert !switchingScreen;
		switchingScreen = true;

		removeCurrentScreen();
		LoadingScreen screen = new LoadingScreen();
		currentScreen = screen;
		currentScreenWidget = screen;
		add(currentScreenWidget, 0, 0);

		switchingScreen = false;
	}
	
	public void disableControls() {
		currentScreen.disableControls();
	}
}
