package unibo.as.cupido.client;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

public class CupidoGeneralErrorScreen extends AbsolutePanel {

	public CupidoGeneralErrorScreen(ScreenSwitcherInterface screenSwitcher, Exception e) {
		setHeight(Cupido.height + "px");
		setWidth(Cupido.width + "700px");
		Label label = new HTML("<b>General error screen (TODO)</b>");
		add(label, 300, 320);
	}

}
