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

import unibo.as.cupido.client.Cupido;
import unibo.as.cupido.client.CupidoInterfaceAsync;
import unibo.as.cupido.common.exception.DuplicateUserNameException;
import unibo.as.cupido.common.exception.FatalException;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
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

public class RegistrationScreen extends VerticalPanel implements Screen {

	private CupidoInterfaceAsync cupidoService;
	private ScreenManager screenManager;

	private TextBox usernameBox;
	private PasswordTextBox passwordBox;
	private PasswordTextBox passwordConfirmBox;

	private PushButton okButton;
	private PushButton abortButton;

	private HTML checkUsernameAvailabilityLabel;
	private PushButton checkUsernameAvailability;

	private boolean frozen = false;

	public RegistrationScreen(final ScreenManager screenManager,
			final CupidoInterfaceAsync cupidoService) {
		setHeight((Cupido.height - 280) + "px");
		setWidth(Cupido.width + "px");

		this.screenManager = screenManager;
		this.cupidoService = cupidoService;

		// Set an empty listener (one that handles no messages).
		screenManager.setListener(new CometMessageListener());

		setHorizontalAlignment(ALIGN_CENTER);

		DOM.setStyleAttribute(getElement(), "marginTop", "100px");

		add(new HTML("<h1>Registrazione a Cupido</h1>"));

		Grid grid = new Grid(3, 4);

		HTML usernameLabel = new HTML("Nome utente:");
		usernameLabel.setWidth("200px");
		usernameLabel.setHorizontalAlignment(ALIGN_RIGHT);
		grid.setWidget(0, 0, usernameLabel);

		HTML passwordLabel = new HTML("Password:");
		passwordLabel.setWidth("200px");
		passwordLabel.setHorizontalAlignment(ALIGN_RIGHT);
		grid.setWidget(1, 0, passwordLabel);

		HTML passwordConfirmLabel = new HTML("Conferma password:");
		passwordConfirmLabel.setWidth("200px");
		passwordConfirmLabel.setHorizontalAlignment(ALIGN_RIGHT);
		grid.setWidget(2, 0, passwordConfirmLabel);

		usernameBox = new TextBox();
		usernameBox.setWidth("200px");
		usernameBox.addKeyUpHandler(new KeyUpHandler() {
			private String lastContent = "";

			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (usernameBox.getText().equals(lastContent))
					return;
				lastContent = usernameBox.getText();
				okButton.setEnabled(false);
				checkUsernameAvailability.setEnabled(true);
				checkUsernameAvailabilityLabel.setText("");
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER)
					checkUsername();
			}
		});
		grid.setWidget(0, 1, usernameBox);

		passwordBox = new PasswordTextBox();
		passwordBox.setWidth("200px");
		grid.setWidget(1, 1, passwordBox);

		passwordConfirmBox = new PasswordTextBox();
		passwordConfirmBox.setWidth("200px");
		passwordConfirmBox.addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					if (okButton.isEnabled())
						tryRegistering();
				}
			}
		});
		grid.setWidget(2, 1, passwordConfirmBox);

		checkUsernameAvailability = new PushButton();
		checkUsernameAvailability.setHTML("Controlla disponibilit&agrave;");
		checkUsernameAvailability.setEnabled(false);
		checkUsernameAvailability.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				checkUsername();
			}
		});
		grid.setWidget(0, 2, checkUsernameAvailability);

		checkUsernameAvailabilityLabel = new HTML();
		checkUsernameAvailabilityLabel.setWidth("200px");
		grid.setWidget(0, 3, checkUsernameAvailabilityLabel);

		add(grid);

		HorizontalPanel bottomPanel = new HorizontalPanel();
		bottomPanel.setSpacing(50);
		add(bottomPanel);

		abortButton = new PushButton("Annulla");
		abortButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				screenManager.displayLoginScreen();
			}
		});
		bottomPanel.add(abortButton);

		okButton = new PushButton("OK");
		okButton.setEnabled(false);
		okButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				tryRegistering();
			}
		});
		bottomPanel.add(okButton);
	}

	private void checkUsername() {

		usernameBox.setEnabled(false);
		checkUsernameAvailability.setEnabled(false);
		checkUsernameAvailabilityLabel.setText("Controllo in corso...");
		assert !okButton.isEnabled();
		cupidoService.isUserRegistered(usernameBox.getText(),
				new AsyncCallback<Boolean>() {
					@Override
					public void onFailure(Throwable caught) {
						if (frozen) {
							System.out
									.println("Client: notice: the onFailure() event was received while frozen, ignoring it.");
							return;
						}
						screenManager.displayGeneralErrorScreen(caught);
					}

					@Override
					public void onSuccess(Boolean isRegistered) {
						if (frozen) {
							System.out
									.println("Client: notice: the onSuccess() event was received while frozen, ignoring it.");
							return;
						}
						checkUsernameAvailability.setEnabled(false);
						usernameBox.setEnabled(true);
						if (isRegistered) {
							checkUsernameAvailabilityLabel.setText("");
							// Remove the focus, so if the user dismisses the
							// alert with Enter, it
							// won't be fired again.
							usernameBox.setFocus(false);
							passwordBox.setFocus(false);
							passwordConfirmBox.setFocus(false);
							okButton.setFocus(false);

							Window.alert("Il nome utente che hai scelto \350 gi\340 stato usato; provane un altro.");
						} else {
							checkUsernameAvailabilityLabel
									.setText("Disponibile");
							okButton.setEnabled(true);
						}
					}
				});
	}

	private void tryRegistering() {
		if (usernameBox.getText().isEmpty()) {
			// Remove the focus, so if the user dismisses the alert with Enter,
			// it
			// won't be fired again.
			usernameBox.setFocus(false);
			passwordBox.setFocus(false);
			passwordConfirmBox.setFocus(false);
			okButton.setFocus(false);

			Window.alert("Non hai inserito il nome utente. Inseriscilo e riprova.");
			return;
		}
		if (passwordBox.getText().isEmpty()) {
			// Remove the focus, so if the user dismisses the alert with Enter,
			// it
			// won't be fired again.
			usernameBox.setFocus(false);
			passwordBox.setFocus(false);
			passwordConfirmBox.setFocus(false);
			okButton.setFocus(false);

			Window.alert("Non hai inserito la password. Inseriscila e riprova.");
			return;
		}
		if (!passwordBox.getText().equals(passwordConfirmBox.getText())) {
			// Remove the focus, so if the user dismisses the alert with Enter,
			// it
			// won't be fired again.
			usernameBox.setFocus(false);
			passwordBox.setFocus(false);
			passwordConfirmBox.setFocus(false);
			okButton.setFocus(false);

			Window.alert("Le password inserite non corrispondono, riprova ad inserirle.");
			return;
		}

		usernameBox.setEnabled(false);
		passwordBox.setEnabled(false);
		passwordConfirmBox.setEnabled(false);
		okButton.setEnabled(false);
		abortButton.setEnabled(false);

		cupidoService.registerUser(usernameBox.getText(),
				passwordBox.getText(), new AsyncCallback<Void>() {

					@Override
					public void onFailure(Throwable caught) {
						if (frozen) {
							System.out
									.println("Client: notice: the onFailure() event was received while frozen, ignoring it.");
							return;
						}
						try {
							throw caught;
						} catch (DuplicateUserNameException e) {
							Window.alert("L'username che hai scelto non \350 pi\371 disponibile, scegline un altro.");
							usernameBox.setEnabled(true);
							passwordBox.setEnabled(true);
							passwordConfirmBox.setEnabled(true);
							okButton.setEnabled(false);
							abortButton.setEnabled(true);
							usernameBox.setFocus(true);
							checkUsernameAvailabilityLabel.setText("");
						} catch (Throwable e) {
							screenManager.displayGeneralErrorScreen(e);
						}
					}

					@Override
					public void onSuccess(Void result) {
						if (frozen) {
							System.out
									.println("Client: notice: the onSuccess() event was received while frozen, ignoring it.");
							return;
						}

						final String username = usernameBox.getText();

						cupidoService.login(username, passwordBox.getText(),
								new AsyncCallback<Boolean>() {
									@Override
									public void onFailure(Throwable caught) {
										if (frozen) {
											System.out
													.println("Client: notice: the onFailure() event was received while frozen, ignoring it.");
											return;
										}
										screenManager
												.displayGeneralErrorScreen(caught);
									}

									@Override
									public void onSuccess(Boolean successful) {
										if (frozen) {
											System.out
													.println("Client: notice: the onSuccess() event was received while frozen, ignoring it.");
											return;
										}
										if (successful)
											screenManager
													.displayMainMenuScreen(username);
										else
											screenManager
													.displayGeneralErrorScreen(new FatalException(
															"The very same username-password pair used for registering didn't work for logging in."));
									}
								});
					}

				});
	}

	@Override
	public void prepareRemoval() {
	}

	@Override
	public void freeze() {
		usernameBox.setEnabled(false);
		passwordBox.setEnabled(false);
		passwordConfirmBox.setEnabled(false);

		okButton.setEnabled(false);
		abortButton.setEnabled(false);

		checkUsernameAvailability.setEnabled(false);

		frozen = true;
	}
}
