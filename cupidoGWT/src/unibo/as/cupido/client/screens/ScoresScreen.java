package unibo.as.cupido.client.screens;

import unibo.as.cupido.client.Cupido;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

public class ScoresScreen extends AbsolutePanel implements Screen {

	public ScoresScreen(ScreenSwitcher screenSwitcher) {
		setHeight(Cupido.height + "px");
		setWidth(Cupido.width + "px");
		Label label = new HTML("<b>Scores scteen (TODO)</b>");
		add(label, 300, 320);
	}

	@Override
	public void prepareRemoval() {
	}
}
