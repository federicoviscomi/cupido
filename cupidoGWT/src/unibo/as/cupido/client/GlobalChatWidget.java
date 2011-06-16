package unibo.as.cupido.client;

import unibo.as.cupido.client.screens.CupidoMainMenuScreen;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;

public class GlobalChatWidget extends AbsolutePanel {

	private ScrollPanel messagesPanel;
	private HTML messageList;
	private TextBox messageField;
	private PushButton sendButton;
	private String username;

	public GlobalChatWidget(final String username) {

		this.username = username;

		int bottomRowHeight = 30;

		messagesPanel = new ScrollPanel();
		messagesPanel.setWidth((CupidoMainMenuScreen.chatWidth - 20) + "px");
		messagesPanel.setHeight((Cupido.height - bottomRowHeight) + "px");
		add(messagesPanel, 10, 0);

		messageList = new HTML("<p><i>Benvenuto nella chat</i></p>");
		messagesPanel.add(messageList);

		HorizontalPanel bottomRow = new HorizontalPanel();
		bottomRow.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

		int sendButtonWidth = 50;

		messageField = new TextBox();
		messageField
				.setWidth((CupidoMainMenuScreen.chatWidth - sendButtonWidth)
						+ "px");
		messageField.addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER)
					sendMessage();
			}
		});
		bottomRow.add(messageField);

		sendButton = new PushButton("Invia");
		sendButton.setWidth(sendButtonWidth + "px");
		sendButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				sendMessage();
			}
		});
		bottomRow.add(sendButton);

		bottomRow.setWidth(CupidoMainMenuScreen.chatWidth + "px");
		bottomRow.setHeight(bottomRowHeight + "px");
		add(bottomRow, 0, (Cupido.height - bottomRowHeight));
	}

	protected void sendMessage() {
		// FIXME: This is a place-holder implementation.
		// Rewrite this method when the servlet is ready.
		if (messageField.getText().equals(""))
			return;

		String messages = messageList.getHTML();

		SafeHtmlBuilder x = new SafeHtmlBuilder();
		x.appendHtmlConstant("<p><b>");
		x.appendEscaped(username);
		x.appendHtmlConstant("</b>: ");
		x.appendEscaped(messageField.getText());
		x.appendHtmlConstant("</p>");
		messages = messages + x.toSafeHtml().asString();
		messageList.setHTML(messages);
		messagesPanel.scrollToBottom();

		messageField.setText("");
		messageField.setFocus(true);
	}
}
