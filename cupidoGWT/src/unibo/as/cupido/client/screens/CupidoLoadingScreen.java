package unibo.as.cupido.client.screens;

import unibo.as.cupido.client.Cupido;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;

public class CupidoLoadingScreen extends VerticalPanel implements Screen {

	public CupidoLoadingScreen() {
		setHeight(Cupido.height + "px");
		setWidth(Cupido.width + "px");

		this.setHorizontalAlignment(ALIGN_CENTER);
		this.setVerticalAlignment(ALIGN_MIDDLE);

		add(new HTML("Caricamento in corso, attendere..."));
	}

	@Override
	public void prepareRemoval() {
	}
}
