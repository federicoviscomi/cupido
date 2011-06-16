package unibo.as.cupido.client.playerstates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.client.CardsGameWidget;
import unibo.as.cupido.client.CardsGameWidget.CardRole.State;
import unibo.as.cupido.client.CardsGameWidget.GameEventListener;
import unibo.as.cupido.client.GWTAnimation;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;

public class CardPassingState {

	List<Card> raisedCards = new ArrayList<Card>();
	/**
	 * Whether the user has already confirmed to pass the selected cards.
	 */
	boolean confirmed = false;

	public CardPassingState(final CardsGameWidget cardsGameWidget,
			final PlayerStateManager stateManager, final List<Card> hand) {
		VerticalPanel cornerWidget = new VerticalPanel();
		cornerWidget.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		cornerWidget
				.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

		final HTML text = new HTML("Seleziona le tre carte da passare.");
		text.setWidth("120px");
		text.setWordWrap(true);
		cornerWidget.add(text);

		final PushButton okButton = new PushButton("OK");
		okButton.setEnabled(false);
		okButton.setWidth("80px");
		okButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				assert raisedCards.size() == 3;
				assert !confirmed;

				text.setText("");

				confirmed = true;

				// FIXME: Send `raisedCards' to the servlet here.

				for (Card card : raisedCards)
					cardsGameWidget.dealCard(0, card);

				cardsGameWidget.runPendingAnimations(1500,
						new GWTAnimation.AnimationCompletedListener() {
							@Override
							public void onComplete() {
								List<Card> sortedList = new ArrayList<Card>();
								for (Card card : raisedCards)
									sortedList.add(card);

								final Comparator<Card> cardComparator = CardsGameWidget
										.getCardComparator();
								Collections.sort(sortedList, cardComparator);

								// Uncover the cards in reverse order, to
								// respect the precondition
								// for CardsGameWidget.coverCard().
								for (int i = 0; i < 3; i++)
									cardsGameWidget.coverCard(0,
											sortedList.get(3 - i - 1));

								for (Card card : raisedCards) {
									boolean removedSomething = hand
											.remove(card);
									assert removedSomething;
								}

								stateManager
										.transitionToCardPassingWaiting(hand);
							}
						});
			}
		});
		cornerWidget.add(okButton);

		final PushButton exitButton = new PushButton("Esci");
		exitButton.setWidth("80px");
		exitButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				stateManager.exit();
			}
		});
		cornerWidget.add(exitButton);

		cardsGameWidget.setCornerWidget(cornerWidget);
		cardsGameWidget.setListener(new GameEventListener() {
			@Override
			public void onAnimationStart() {
				okButton.setEnabled(false);
				exitButton.setEnabled(false);
			}

			@Override
			public void onAnimationEnd() {
				okButton.setEnabled(!confirmed && raisedCards.size() == 3);
				exitButton.setEnabled(!confirmed);
			}

			@Override
			public void onCardClicked(int player, Card card, State state,
					boolean isRaised) {
				if (state == State.DEALT)
					return;
				if (player != 0 || card == null)
					return;

				if (isRaised) {
					boolean found = raisedCards.remove(card);
					assert found;
					cardsGameWidget.lowerRaisedCard(0, card);
				} else {
					if (raisedCards.size() == 3)
						// Never raise more than 3 cards at once.
						return;
					raisedCards.add(card);
					cardsGameWidget.raiseCard(0, card);
				}

				cardsGameWidget.runPendingAnimations(200,
						new GWTAnimation.AnimationCompletedListener() {
							@Override
							public void onComplete() {
							}
						});
			}
		});
	}
}
