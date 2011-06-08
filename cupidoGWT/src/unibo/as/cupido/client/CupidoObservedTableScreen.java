package unibo.as.cupido.client;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

public class CupidoObservedTableScreen extends AbsolutePanel {

	public CupidoObservedTableScreen(ScreenSwitcher screenSwitcher,
			String username) {
		setHeight(Cupido.height + "px");
		setWidth(Cupido.width + "700px");
		Label label = new HTML("<b>Observed table screen (TODO)</b>");
		add(label, 300, 320);
	}

}
