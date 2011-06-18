package unibo.as.cupido.client.screens;

import unibo.as.cupido.client.Cupido;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;

public class AboutScreen extends VerticalPanel implements Screen {

	public AboutScreen(final ScreenSwitcher screenSwitcher, final String username) {
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

		PushButton button = new PushButton("Torna al menu");
		button.setWidth("100px");
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				screenSwitcher.displayMainMenuScreen(username);
			}
		});
		add(button);
	}

	@Override
	public void prepareRemoval() {
	}
}