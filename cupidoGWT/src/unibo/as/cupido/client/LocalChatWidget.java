package unibo.as.cupido.client;

import unibo.as.cupido.client.screens.TableScreen;

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

public class LocalChatWidget extends AbsolutePanel {

	private ScrollPanel messagesPanel;
	private HTML messageList;
	private TextBox messageField;
	private PushButton sendButton;
	private String username;
	
	private boolean frozen = false;

	private MessageSender messageSender;

	public interface MessageSender {
		public void sendMessage(String message);
	}

	public LocalChatWidget(final String username, MessageSender messageSender) {

		this.username = username;
		this.messageSender = messageSender;

		int bottomRowHeight = 30;

		messagesPanel = new ScrollPanel();
		messagesPanel.setWidth(TableScreen.chatWidth + "px");
		messagesPanel.setHeight((Cupido.height - bottomRowHeight) + "px");
		add(messagesPanel, 0, 0);

		messageList = new HTML("<p><i>Benvenuto nella chat del tavolo</i></p>");
		messagesPanel.add(messageList);

		HorizontalPanel bottomRow = new HorizontalPanel();
		bottomRow.setWidth(TableScreen.chatWidth + "px");
		bottomRow.setHeight(bottomRowHeight + "px");
		bottomRow.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

		int sendButtonWidth = 50;

		messageField = new TextBox();
		messageField.setWidth((TableScreen.chatWidth - sendButtonWidth)
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

		add(bottomRow, 0, (Cupido.height - bottomRowHeight));
	}

	private void sendMessage() {
		// FIXME: This is a place-holder implementation.
		// Rewrite this method when the servlet is ready.
		if (messageField.getText().equals(""))
			return;

		String message = messageField.getText();

		messageSender.sendMessage(message);

		// TODO: Check if this should be called or not.
		// displayMessage(username, message);

		messageField.setText("");
		messageField.setFocus(true);
	}

	public void displayMessage(String username, String message) {
		
		if (frozen) {
			System.out.println("Client: notice: displayMessage() was called while frozen, ignoring it.");
			return;
		}
		
		String messages = messageList.getHTML();

		SafeHtmlBuilder x = new SafeHtmlBuilder();
		x.appendHtmlConstant("<p><b>");
		x.appendEscaped(username);
		x.appendHtmlConstant("</b>: ");
		x.appendEscaped(message);
		x.appendHtmlConstant("</p>");
		messages = messages + x.toSafeHtml().asString();
		messageList.setHTML(messages);
		messagesPanel.scrollToBottom();
	}
	
	public void freeze() {
		messageField.setEnabled(false);
		sendButton.setEnabled(false);
		frozen = true;
	}
}
