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

public class ChatWidget extends AbsolutePanel {

	public interface ChatListener {
		public void sendMessage(String message);
	}
	private static String constructMessageHtml(String username, String message) {
		SafeHtmlBuilder x = new SafeHtmlBuilder();
		x.appendHtmlConstant("<p><b>");
		x.appendEscaped(username);
		x.appendHtmlConstant("</b>: ");
		x.appendEscaped(message);
		x.appendHtmlConstant("</p>");
		return x.toSafeHtml().asString();
	}
	private boolean frozen = false;
	private ChatListener listener;
	private TextBox messageField;
	private HTML messageList;

	private ScrollPanel scrollPanel;

	private PushButton sendButton;

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
		messageField.setWidth((width - sendButtonWidth - 40)
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

		bottomRow.setWidth((width - 10) + "px");
		bottomRow.setHeight(bottomRowHeight + "px");
		add(bottomRow, 0, (height - bottomRowHeight - 10));
	}

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
	
	public void freeze() {
		messageField.setEnabled(false);
		sendButton.setEnabled(false);
		frozen = true;
	}
	
	private void sendMessage() {
		if (messageField.getText().equals(""))
			return;
		
		listener.sendMessage(messageField.getText());
		
		messageField.setText("");
		messageField.setFocus(true);
	}

	public void setLastMessages(ChatMessage[] list) {
		if (frozen) {
			System.out
					.println("Client: notice: setLastMessages() was called while frozen, ignoring it.");
			return;
		}

		String message = "";

		for (ChatMessage entry : list)
			message += constructMessageHtml(entry.userName, entry.message);

		messageList.setHTML(message);
		scrollPanel.scrollToBottom();
	}
}
