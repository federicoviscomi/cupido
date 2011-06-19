package unibo.as.cupido.client.screens;

import unibo.as.cupido.backendInterfaces.exception.DuplicateUserNameException;
import unibo.as.cupido.backendInterfaces.exception.FatalException;
import unibo.as.cupido.client.Cupido;
import unibo.as.cupido.client.CupidoInterfaceAsync;
import com.google.gwt.user.client.Window;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.DOM;
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
	private ScreenSwitcher screenSwitcher;
	
	private TextBox usernameBox;
	private PasswordTextBox passwordBox;
	private PasswordTextBox passwordConfirmBox;

	private PushButton okButton;
	private PushButton abortButton;

	private HTML checkUsernameAvailabilityLabel;
	private PushButton checkUsernameAvailability;

	public RegistrationScreen(final ScreenSwitcher screenSwitcher,
			final CupidoInterfaceAsync cupidoService) {
		setHeight((Cupido.height - 280) + "px");
		setWidth(Cupido.width + "px");
		
		this.screenSwitcher = screenSwitcher;
		this.cupidoService = cupidoService;
		
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
			@Override
			public void onKeyUp(KeyUpEvent event) {
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
				if (okButton.isEnabled())
					tryRegistering();
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
		grid.setWidget(0, 3, checkUsernameAvailabilityLabel);
		
		add(grid);
		
		HorizontalPanel bottomPanel = new HorizontalPanel();
		bottomPanel.setSpacing(50);
		add(bottomPanel);

		abortButton = new PushButton("Annulla");
		abortButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				screenSwitcher.displayLoginScreen();
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
		cupidoService.isUserRegistered(usernameBox.getText(), new AsyncCallback<Boolean>() {
			@Override
			public void onFailure(Throwable caught) {
				screenSwitcher.displayGeneralErrorScreen(caught);
			}

			@Override
			public void onSuccess(Boolean isRegistered) {
				checkUsernameAvailability.setEnabled(false);
				usernameBox.setEnabled(true);
				if (isRegistered) {
					checkUsernameAvailabilityLabel.setText("");
					// Remove the focus, so if the user dismisses the alert with Enter, it
					// won't be fired again.
					usernameBox.setFocus(false);
					passwordBox.setFocus(false);
					passwordConfirmBox.setFocus(false);
					okButton.setFocus(false);
					
					Window.alert("Il nome utente che hai scelto \350 gi\340 stato usato; provane un altro.");
				} else {
					checkUsernameAvailabilityLabel.setText("Disponibile");
					okButton.setEnabled(true);
				}
			}
		});
	}
	
	private void tryRegistering() {
		if (usernameBox.getText().isEmpty()) {
			// Remove the focus, so if the user dismisses the alert with Enter, it
			// won't be fired again.
			usernameBox.setFocus(false);
			passwordBox.setFocus(false);
			passwordConfirmBox.setFocus(false);
			okButton.setFocus(false);
			
			Window.alert("Non hai inserito il nome utente. Inseriscilo e riprova.");
			return;
		}
		if (passwordBox.getText().isEmpty()) {
			// Remove the focus, so if the user dismisses the alert with Enter, it
			// won't be fired again.
			usernameBox.setFocus(false);
			passwordBox.setFocus(false);
			passwordConfirmBox.setFocus(false);
			okButton.setFocus(false);
			
			Window.alert("Non hai inserito la password. Inseriscila e riprova.");
			return;
		}
		if (!passwordBox.getText().equals(passwordConfirmBox.getText())) {
			// Remove the focus, so if the user dismisses the alert with Enter, it
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
		
		cupidoService.registerUser(usernameBox.getText(), passwordBox.getText(),
				new AsyncCallback<Void>() {

			@Override
			public void onFailure(Throwable caught) {
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
					screenSwitcher.displayGeneralErrorScreen(e);
				}
			}

			@Override
			public void onSuccess(Void result) {
				final String username = usernameBox.getText();
				
				cupidoService.login(username, passwordBox.getText(), new AsyncCallback<Boolean>() {
					@Override
					public void onFailure(Throwable caught) {
						screenSwitcher.displayGeneralErrorScreen(caught);
					}

					@Override
					public void onSuccess(Boolean successful) {
						if (successful)
							screenSwitcher.displayMainMenuScreen(username);
						else
							screenSwitcher.displayGeneralErrorScreen(new FatalException("The very same username-password pair used for registering didn't work for logging in."));
					}
				});
			}
			
		});
	}

	@Override
	public void prepareRemoval() {
	}

	@Override
	public void disableControls() {
		usernameBox.setEnabled(false);
		passwordBox.setEnabled(false);
		passwordConfirmBox.setEnabled(false);

		okButton.setEnabled(false);
		abortButton.setEnabled(false);

		checkUsernameAvailability.setEnabled(false);
	}
}
