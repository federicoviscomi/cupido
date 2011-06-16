package unibo.as.cupido.client.gamestates;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;

import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.client.CardsGameWidget;
import unibo.as.cupido.client.GWTAnimation;
import unibo.as.cupido.client.CardsGameWidget.GameEventListener;
import unibo.as.cupido.client.CardsGameWidget.CardRole.State;

public class WaitingFirstDealAsPlayer {

	public WaitingFirstDealAsPlayer(final CardsGameWidget cardsGameWidget, final StateManager stateManager, final List<Card> hand) {
		VerticalPanel panel = new VerticalPanel();
		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		
		final HTML text = new HTML("Attendi che il giocatore che ha il due di picche lo giochi.");
		text.setWidth("120px");
		text.setWordWrap(true);
		panel.add(text);
		
		// FIXME: Remove this button when the servlet is ready.
		final PushButton continueButton = new PushButton("[DEBUG] Continua");
		continueButton.setWidth("80px");
		continueButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// FIXME: don't generate this data, get them from the servlet instead.
				final int player = Random.nextInt(3) + 1;
				Card card = new Card(2, Card.Suit.CLUBS);
				
				text.setText("");
				
				stateManager.addDealtCard(player, card);
				
				cardsGameWidget.revealCoveredCard(player, card);
				
				cardsGameWidget.dealCard(player, card);
				cardsGameWidget.runPendingAnimations(2000, new GWTAnimation.AnimationCompletedListener() {
					
					@Override
					public void onComplete() {
						if (player == 3)
							stateManager.transitionToYourTurn(hand);
						else
							stateManager.transitionToWaitingDealAsPlayer(hand);
					}
				});
			}
		});
		panel.add(continueButton);
		
		final PushButton exitButton = new PushButton("Esci");
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
			}
		});
	}
}
