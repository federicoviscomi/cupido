package unibo.as.cupido.client;

import java.util.List;

import unibo.as.cupido.backendInterfaces.common.ChatMessage;
import unibo.as.cupido.client.GlobalChatWidget.ChatListener;
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
	private ChatListener listener;
	
	public interface ChatListener {
		public void sendMessage(String message);
	}

	public GlobalChatWidget(final String username, ChatListener listener) {

		this.username = username;
		this.listener = listener;

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
		if (messageField.getText().equals(""))
			return;

		listener.sendMessage(messageField.getText());
		
		messageField.setText("");
		messageField.setFocus(true);
	}

	public void setLastMessages(ChatMessage[] messages) {
		SafeHtmlBuilder x = new SafeHtmlBuilder();
		
		for (ChatMessage message : messages) {
			x.appendHtmlConstant("<p><b>");
			x.appendEscaped(message.userName);
			x.appendHtmlConstant("</b>: ");
			x.appendEscaped(message.message);
			x.appendHtmlConstant("</p>");
		}
		
		messageList.setHTML(x.toSafeHtml().asString());
		messagesPanel.scrollToBottom();
	}
}
