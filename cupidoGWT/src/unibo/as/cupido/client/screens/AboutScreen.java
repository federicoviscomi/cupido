package unibo.as.cupido.client.screens;

import unibo.as.cupido.client.Cupido;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;

public class AboutScreen extends VerticalPanel implements Screen {

	private PushButton menuButton;

	public AboutScreen(final ScreenManager screenManager, final String username) {

		// Set an empty listener (one that handles no messages).
		screenManager.setListener(new CometMessageListener());

		setHeight((Cupido.height - 80) + "px");
		setWidth((Cupido.width - 120) + "px");

		setHorizontalAlignment(ALIGN_CENTER);

		DOM.setStyleAttribute(getElement(), "marginLeft", "60px");
		DOM.setStyleAttribute(getElement(), "marginTop", "60px");

		String message = "" + "<h1>Informazioni su Cupido</h1> "
				+ "<big><p>Questo gioco online di Hearts &egrave; stato "
				+ "progettato e realizzato da Lorenzo Belli, "
				+ "Marco Poletti e Federico Viscomi, per il progetto "
				+ "del corso Architetture Software all'Universit&agrave; "
				+ "di Bologna, nell'anno accademico 2010/2011.</p>"
				+ "<p>Questo progetto &egrave; open-source; i sorgenti "
				+ "sono stati rilasciati sotto la licenza GPLv2, e "
				+ "sono disponibili all'indirizzo "
				+ "<a href=\"http://gitorious.org/cupido\">"
				+ "http://gitorious.org/cupido</a>.</p></big>";

		add(new HTML(message));

		menuButton = new PushButton("Torna al menu");
		menuButton.setWidth("100px");
		menuButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				screenManager.displayMainMenuScreen(username);
			}
		});
		add(menuButton);
	}

	@Override
	public void prepareRemoval() {
	}

	@Override
	public void freeze() {
		menuButton.setEnabled(false);
	}
}
