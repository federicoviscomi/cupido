package unibo.as.cupido.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.PushButton;

public class BeforeGameWidget extends AbsolutePanel {

	// The width of the players' labels that contain usernames and scores.
	private static final int playerLabelWidth = 200;

	// The height of the players' labels that contain usernames and scores.
	private static final int playerLabelHeight = 20;
	
	HTML bottomLabel;
	HTML leftLabel;
	HTML topLabel;
	HTML rightLabel;
	
	/*
	 * This is not null only if there is no player in that position and
	 * the current user is the table owner.
	 */
	PushButton leftButton = null;

	/*
	 * This is not null only if there is no player in that position and
	 * the current user is the table owner.
	 */
	PushButton topButton = null;

	/*
	 * This is not null only if there is no player in that position and
	 * the current user is the table owner.
	 */
	PushButton rightButton = null;
	
	public interface Callback {
		/**
		 * @param position can be either 0, 1 or 2.
		 * When it is 0 it means the left-hand side position, and other positions
		 * follow in clockwise order.
		 */
		void onAddBot(int position);
	}

	/*
	 * The widget that represents the table before the actual game.
	 * 
	 * @param username is the bottom player's username (it may be the current user
	 * or not, depending whether the user is a player or just a viewer).
	 * 
	 * @points are is the total points of the bottom player.
	 * 
	 * @param isOwner specifies whether the current user is the owner of the table
	 *  (and thus also a player).
	 */
	public BeforeGameWidget(int tableSize, String username, int points,
			boolean isOwner, final Callback callback) {
		setWidth(tableSize + "px");
		setHeight(tableSize + "px");
		DOM.setStyleAttribute(getElement(), "background", "green");
		
		SafeHtmlBuilder builder = new SafeHtmlBuilder();
		builder.appendHtmlConstant("<b><big>");
		builder.appendEscaped(username);
		builder.appendHtmlConstant(" (");
		builder.append(points);
		builder.appendHtmlConstant(")</big></b>");
		
		bottomLabel = new HTML(builder.toSafeHtml().asString());
		
		leftLabel = new HTML();
		topLabel = new HTML();
		rightLabel = new HTML();
		
		bottomLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		leftLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		topLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		rightLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		
		bottomLabel.setWidth(playerLabelWidth + "px");
		leftLabel.setWidth(playerLabelWidth + "px");
		topLabel.setWidth(playerLabelWidth + "px");
		rightLabel.setWidth(playerLabelWidth + "px");
		
		bottomLabel.setHeight(playerLabelHeight + "px");
		leftLabel.setHeight(playerLabelHeight + "px");
		topLabel.setHeight(playerLabelHeight + "px");
		rightLabel.setHeight(playerLabelHeight + "px");
		
		add(bottomLabel, tableSize / 2 - playerLabelWidth/2, tableSize - 10 - playerLabelHeight);
		add(leftLabel, 10, tableSize / 2 - playerLabelHeight/2);
		add(topLabel, tableSize / 2 - playerLabelWidth/2, 10);
		add(rightLabel, tableSize - 10 - playerLabelWidth, tableSize / 2 - playerLabelHeight/2);
		
		if (isOwner) {
			leftButton = new PushButton("Aggiungi un bot");
			topButton = new PushButton("Aggiungi un bot");
			rightButton = new PushButton("Aggiungi un bot");
			
			final int buttonWidth = 95;
			final int buttonHeight = 15;
			
			leftButton.setHeight(buttonHeight + "px");
			topButton.setHeight(buttonHeight + "px");
			rightButton.setHeight(buttonHeight + "px");
			
			leftButton.setWidth(buttonWidth + "px");
			topButton.setWidth(buttonWidth + "px");
			rightButton.setWidth(buttonWidth + "px");
			
			add(leftButton, 30, tableSize / 2 - buttonHeight/2);
			add(topButton, tableSize / 2 - buttonWidth/2, 30);
			add(rightButton, tableSize - 40 - buttonWidth, tableSize / 2 - buttonHeight/2);
			
			leftButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					callback.onAddBot(0);
					addBot(0);
				}
			});
			
			topButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					callback.onAddBot(1);
					addBot(1);
				}
			});
			
			rightButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					callback.onAddBot(2);
					addBot(2);
				}
			});
		}
	}
	
	public void addPlayer(String username, int points, int position) {
		
		SafeHtmlBuilder builder = new SafeHtmlBuilder();
		builder.appendHtmlConstant("<b><big>");
		builder.appendEscaped(username);
		builder.appendHtmlConstant(" (");
		builder.append(points);
		builder.appendHtmlConstant(")</big></b>");
		
		String s = builder.toSafeHtml().asString();
		
		switch (position) {
		case 0:
			if (leftButton != null)
				remove(leftButton);
			assert leftLabel.getText().isEmpty();
			leftLabel.setHTML(s);
			break;
			
		case 1:
			if (topButton != null)
				remove(topButton);
			assert topLabel.getText().isEmpty();
			topLabel.setHTML(s);
			break;
			
		case 2:
			if (rightButton != null)
				remove(rightButton);
			assert rightLabel.getText().isEmpty();
			rightLabel.setHTML(s);
			break;
			
		default:
			assert false;	
		}		
	}
	
	public void addBot(int position) {
		switch (position) {
		case 0:
			if (leftButton != null);
				remove(leftButton);
			leftButton = null;
			assert leftLabel.getText().isEmpty();
			leftLabel.setHTML("<b><big>bot</big></b>");
			break;
			
		case 1:
			if (topButton != null);
				remove(topButton);
			topButton = null;
			assert topLabel.getText().isEmpty();
			topLabel.setHTML("<b><big>bot</big></b>");
			break;
			
		case 2:
			if (rightButton != null);
				remove(rightButton);
			rightButton = null;
			assert rightLabel.getText().isEmpty();
			rightLabel.setHTML("<b><big>bot</big></b>");
			break;
			
		default:
			assert false;	
		}
	}
}
