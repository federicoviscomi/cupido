package unibo.as.cupido.client;

import com.google.gwt.user.client.ui.AbsolutePanel;

public class CupidoTableScreen extends AbsolutePanel {

	/**
	 *  The width of the chat sidebar.
	 */
	public static final int chatWidth = 200;

	public CupidoTableScreen(ScreenSwitcherInterface screenSwitcher,
			String username) {
		setHeight(Cupido.height + "px");
		setWidth(Cupido.width + "px");

		HeartsTableWidget tableWidget = new HeartsTableWidget(username);
		tableWidget.setHeight(Cupido.height + "px");
		tableWidget.setWidth((Cupido.width - chatWidth) + "px");
		add(tableWidget, 0, 0);
		
		LocalChatWidget chatWidget = new LocalChatWidget(username);
		chatWidget.setHeight(Cupido.height + "px");
		chatWidget.setWidth(chatWidth + "px");
		add(chatWidget, Cupido.width - chatWidth, 0);
	}
}
