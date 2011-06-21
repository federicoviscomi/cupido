package unibo.as.cupido.client.screens;

import unibo.as.cupido.client.Cupido;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;

public class LoadingScreen extends VerticalPanel implements Screen {

	public LoadingScreen(ScreenManager screenManager) {
		setHeight(Cupido.height + "px");
		setWidth(Cupido.width + "px");
		
		// Set an empty listener (one that handles no messages).
		screenManager.setListener(new CometMessageListener());

		this.setHorizontalAlignment(ALIGN_CENTER);
		this.setVerticalAlignment(ALIGN_MIDDLE);

		add(new HTML("Caricamento in corso, attendere..."));
	}

	@Override
	public void prepareRemoval() {
	}

	@Override
	public void freeze() {
	}
}
