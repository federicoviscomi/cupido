/*  Cupido - An online Hearts game.
 *  Copyright (C) 2011 Lorenzo Belli, Marco Poletti, Federico Viscomi
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package unibo.as.cupido.client.widgets;

import unibo.as.cupido.common.structures.ChatMessage;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * A widget that manages a chat.
 */
public class ChatWidget extends AbsolutePanel {

	/**
	 * A listener used by this class to notify the user code when the user sends
	 * a message.
	 */
	public interface ChatListener {
		/**
		 * This is called when the user sends a message.
		 * 
		 * @param message
		 *            The message entered by the user.
		 */
		public void sendMessage(String message);
	}

	/**
	 * Specifies whether the UI is frozen (i.e. does no longer react to events)
	 * or not.
	 */
	private boolean frozen = false;

	/**
	 * This listener is used to notify the client code when the user sends a new
	 * message.
	 */
	private ChatListener listener;

	/**
	 * The widget that allows the user to enter a new message.
	 */
	private TextBox messageField;

	/**
	 * The list of displayed messages.
	 */
	private HTML messageList;

	/**
	 * A panel that contains the messageList and displays horizontal and/or
	 * vertical scrollbars if needed.
	 */
	private ScrollPanel scrollPanel;

	/**
	 * The button that allows the user to send a new message.
	 */
	private PushButton sendButton;

	/**
	 * @param width
	 *            The width of the widget, in pixels.
	 * @param height
	 *            The height of the widget, in pixels.
	 * @param listener
	 *            This listener is used to notify the client code when the user
	 *            sends a new message.
	 */
	public ChatWidget(int width, int height, ChatListener listener) {

		this.listener = listener;

		setWidth(width + "px");
		setHeight(height + "px");

		final int bottomRowHeight = 30;

		scrollPanel = new ScrollPanel();
		scrollPanel.setWidth((width - 20) + "px");
		scrollPanel.setHeight((height - bottomRowHeight - 15) + "px");
		add(scrollPanel, 10, 0);

		VerticalPanel panel = new VerticalPanel();
		scrollPanel.add(panel);

		panel.add(new HTML("<p><i>Benvenuto nella chat</i></p>"));

		messageList = new HTML();
		panel.add(messageList);

		HorizontalPanel bottomRow = new HorizontalPanel();
		bottomRow.setSpacing(5);
		bottomRow.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		bottomRow.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

		int sendButtonWidth = 30;

		messageField = new TextBox();
		messageField.setWidth((width - sendButtonWidth - 40) + "px");
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

		bottomRow.setWidth((width - 10) + "px");
		bottomRow.setHeight(bottomRowHeight + "px");
		add(bottomRow, 0, (height - bottomRowHeight - 10));
	}

	/**
	 * Adds the specified message to the list of the displayed messages.
	 * 
	 * @param username
	 *            The user who sent the message.
	 * @param message
	 *            The actual message.
	 */
	public void displayMessage(String username, String message) {
		if (frozen) {
			System.out
					.println("Client: notice: displayMessage() was called while frozen, ignoring it.");
			return;
		}

		String messages = messageList.getHTML();

		messages += constructMessageHtml(username, message);

		messageList.setHTML(messages);
		scrollPanel.scrollToBottom();
	}

	/**
	 * When this is called, the widget stops responding to events and disables
	 * all user controls.
	 */
	public void freeze() {
		messageField.setEnabled(false);
		sendButton.setEnabled(false);
		frozen = true;
	}

	/**
	 * Replaces the displayed messages with those in the provided list.
	 * 
	 * @param list
	 *            The list containing the messages to be displayed.
	 */
	public void setMessages(ChatMessage[] list) {
		if (frozen) {
			System.out
					.println("Client: notice: setLastMessages() was called while frozen, ignoring it.");
			return;
		}

		String message = "";

		for (ChatMessage entry : list)
			message += constructMessageHtml(entry.userName, entry.message);

		// Don't scroll if no changes occurred.
		if (!messageList.getHTML().equals(message)) {
			messageList.setHTML(message);
			scrollPanel.scrollToBottom();
		}
	}

	/**
	 * This is called when the user confirms the entered message.
	 */
	private void sendMessage() {
		if (messageField.getText().equals(""))
			return;

		listener.sendMessage(messageField.getText());

		messageField.setText("");
		messageField.setFocus(true);
	}

	/**
	 * A helper method to generate the HTML for a chat message.
	 * 
	 * @param username
	 *            The user that posted the message.
	 * @param message
	 *            The actual message.
	 * 
	 * @return A string containing the HTML for the message.
	 */
	private static String constructMessageHtml(String username, String message) {
		SafeHtmlBuilder x = new SafeHtmlBuilder();
		x.appendHtmlConstant("<p><b>");
		x.appendEscaped(username);
		x.appendHtmlConstant("</b>: ");
		x.appendEscaped(message);
		x.appendHtmlConstant("</p>");
		return x.toSafeHtml().asString();
	}
}
