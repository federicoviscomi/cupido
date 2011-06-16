package unibo.as.cupido.client.gamestates;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;

import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.client.CardsGameWidget;
import unibo.as.cupido.client.CardsGameWidget.GameEventListener;
import unibo.as.cupido.client.CardsGameWidget.CardRole.State;

public class WaitingFirstDealAsViewer {

	public WaitingFirstDealAsViewer(CardsGameWidget cardsGameWidget, final StateManager stateManager) {
		VerticalPanel panel = new VerticalPanel();
		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		
		final HTML text = new HTML("TODO");
		text.setWidth("120px");
		text.setWordWrap(true);
		panel.add(text);
		
		// FIXME: Remove this button when the servlet is ready.
		final PushButton continueButton = new PushButton("[DEBUG] Continua");
		continueButton.setEnabled(false);
		continueButton.setWidth("80px");
		continueButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// TODO Auto-generated method stub				
			}
		});
		panel.add(continueButton);
		
		final PushButton exitButton = new PushButton("Esci");
		exitButton.setEnabled(false);
		exitButton.setWidth("80px");
		exitButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				stateManager.exit();
			}
		});
		panel.add(exitButton);
		
		cardsGameWidget.setCornerWidget(panel);
		cardsGameWidget.setListener(new GameEventListener() {
			@Override
			public void onAnimationStart() {
				continueButton.setEnabled(false);
				exitButton.setEnabled(false);
			}

			@Override
			public void onAnimationEnd() {
				continueButton.setEnabled(true);
				exitButton.setEnabled(true);
			}

			@Override
			public void onCardClicked(int player, Card card, State state,
					boolean isRaised) {
				// TODO Auto-generated method stub
			}
		});
	}
}
