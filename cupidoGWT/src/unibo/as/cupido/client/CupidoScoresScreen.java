package unibo.as.cupido.client;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

public class CupidoScoresScreen extends AbsolutePanel {

	public CupidoScoresScreen(ScreenSwitcherInterface screenSwitcher) {
		setHeight(Cupido.height + "px");
		setWidth(Cupido.width + "px");
		Label label = new HTML("<b>Scores scteen (TODO)</b>");
		add(label, 300, 320);
	}

}
