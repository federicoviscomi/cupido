package unibo.as.cupido.client.playerstates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;

import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.client.CardsGameWidget;
import unibo.as.cupido.client.GWTAnimation;
import unibo.as.cupido.client.RandomCardGenerator;
import unibo.as.cupido.client.CardsGameWidget.GameEventListener;
import unibo.as.cupido.client.CardsGameWidget.CardRole.State;

public class CardPassingWaitingAsPlayer {

	public CardPassingWaitingAsPlayer(final CardsGameWidget cardsGameWidget, final PlayerStateManager stateManager, final List<Card> hand) {
		VerticalPanel panel = new VerticalPanel();
		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		
		final HTML text = new HTML("Aspetta che gli altri giocatori decidano quali carte passare.");
		text.setWidth("120px");
		text.setWordWrap(true);
		panel.add(text);
		
		// FIXME: Remove this button when the servlet is ready.
		final PushButton continueButton = new PushButton("[DEBUG] Continua");
		continueButton.setWidth("80px");
		continueButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				
				text.setText("");
				
				// FIXME: Remove this. This data should come from the servlet.
				List<Card> passedCards = new ArrayList<Card>();
				passedCards.add(RandomCardGenerator.generateCard());
				passedCards.add(RandomCardGenerator.generateCard());
				passedCards.add(RandomCardGenerator.generateCard());
				
				hand.addAll(passedCards);
				
				Collections.sort(passedCards, CardsGameWidget.getCardComparator());
				
				for (int i = 0; i < 3; i++)
					cardsGameWidget.revealCoveredCard(0, passedCards.get(3 - i - 1));

				for (Card card : passedCards)
					cardsGameWidget.pickCard(0, card);
				
				cardsGameWidget.runPendingAnimations(2000, new GWTAnimation.AnimationCompletedListener() {
					@Override
					public void onComplete() {
						boolean found = false;
						for (Card card : hand)
							if (card.suit == Card.Suit.CLUBS && card.value == 2) {
								found = true;
								break;
							}
						if (found)
							stateManager.transitionToFirstDealer(hand);
						else
							stateManager.transitionToWaitingFirstDealAsPlayer(hand);
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
