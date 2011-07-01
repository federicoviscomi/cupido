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
import unibo.as.cupido.client.CupidoInterfaceAsync;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class LoginScreen extends VerticalPanel implements Screen {

	private CupidoInterfaceAsync cupidoService;
	private boolean frozen = false;

	private PushButton okButton;
	private PasswordTextBox passwordBox;
	private PushButton registerButton;
	private ScreenManager screenManager;

	private TextBox usernameBox;

	public LoginScreen(final ScreenManager screenManager,
			CupidoInterfaceAsync cupidoService) {

		this.screenManager = screenManager;
		this.cupidoService = cupidoService;

		// Set an empty listener (one that handles no messages).
		screenManager.setListener(new CometMessageListener());

		setHeight((Cupido.height - 200) + "px");
		setWidth((Cupido.width - 200) + "px");

		setHorizontalAlignment(ALIGN_CENTER);

		DOM.setStyleAttribute(getElement(), "marginLeft", "100px");
		DOM.setStyleAttribute(getElement(), "marginTop", "100px");

		add(new HTML("<h1>Login</h1>"));

		Grid grid = new Grid(2, 2);
		grid.setCellSpacing(20);

		HTML usernameLabel = new HTML("Nome utente:");
		usernameLabel.setHorizontalAlignment(ALIGN_RIGHT);
		grid.setWidget(0, 0, usernameLabel);

		HTML passwordLabel = new HTML("Password:");
		passwordLabel.setHorizontalAlignment(ALIGN_RIGHT);
		grid.setWidget(1, 0, passwordLabel);

		usernameBox = new TextBox();
		usernameBox.setWidth("200px");
		usernameBox.setHeight("20px");
		usernameBox.addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER)
					tryLogin();
			}
		});
		grid.setWidget(0, 1, usernameBox);

		passwordBox = new PasswordTextBox();
		passwordBox.setWidth("200px");
		passwordBox.setHeight("20px");
		passwordBox.addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER)
					tryLogin();
			}
		});
		grid.setWidget(1, 1, passwordBox);

		add(grid);

		HorizontalPanel bottomPanel = new HorizontalPanel();
		bottomPanel.setHorizontalAlignment(ALIGN_CENTER);
		bottomPanel.setSpacing(50);
		add(bottomPanel);

		registerButton = new PushButton("Registrati");
		registerButton.setWidth("100px");
		registerButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				screenManager.displayRegistrationScreen();
			}
		});
		bottomPanel.add(registerButton);

		okButton = new PushButton("OK");
		okButton.setWidth("100px");
		okButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				tryLogin();
			}
		});
		bottomPanel.add(okButton);
	}

	@Override
	public void freeze() {
		usernameBox.setEnabled(false);
		passwordBox.setEnabled(false);
		okButton.setEnabled(false);
		registerButton.setEnabled(false);
		frozen = true;
	}

	@Override
	public void prepareRemoval() {
	}

	private void tryLogin() {
		final String username = usernameBox.getText();
		cupidoService.login(username, passwordBox.getText(),
				new AsyncCallback<Boolean>() {
					@Override
					public void onFailure(Throwable caught) {
						if (frozen)
							return;
						screenManager.displayGeneralErrorScreen(caught);
					}

					@Override
					public void onSuccess(Boolean successful) {
						if (frozen)
							return;
						if (successful)
							screenManager.displayMainMenuScreen(username);
						else {
							// Remove the focus, so if the user dismisses the
							// alert with Enter, it
							// won't be fired again.
							usernameBox.setFocus(false);
							passwordBox.setFocus(false);
							okButton.setFocus(false);
							Window.alert("La password che hai inserito non \350 corretta. Riprova.");
						}
					}
				});
	}
}
