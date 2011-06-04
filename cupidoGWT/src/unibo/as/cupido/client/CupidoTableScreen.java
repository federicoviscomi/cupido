package unibo.as.cupido.client;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

public class CupidoTableScreen extends AbsolutePanel {

	public CupidoTableScreen(ScreenSwitcherInterface screenSwitcher,
			String username) {
		setHeight("700px");
		setWidth("700px");
		Label label = new HTML("<b>Table screen (TODO)</b>");
		add(label, 300, 320);
	}

}
