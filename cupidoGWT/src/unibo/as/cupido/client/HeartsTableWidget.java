package unibo.as.cupido.client;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

public class HeartsTableWidget extends AbsolutePanel {
	
	HeartsTableWidget(String username) {
		DOM.setStyleAttribute(getElement(), "background", "green");
		Label label = new HTML("<b>Table widget (TODO)</b>");
		add(label, 300, 320);
	}
}
