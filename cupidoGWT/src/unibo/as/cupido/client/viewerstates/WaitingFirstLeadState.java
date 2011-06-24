package unibo.as.cupido.client.viewerstates;

import unibo.as.cupido.client.CardsGameWidget;
import unibo.as.cupido.client.GWTAnimation;
import unibo.as.cupido.common.structures.Card;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;

public class WaitingFirstLeadState implements ViewerState {

	// FIXME: Remove this button when the servlet is ready.
	private PushButton continueButton;
	private PushButton exitButton;
	private ViewerStateManager stateManager;
	private CardsGameWidget cardsGameWidget;

	private boolean frozen = false;

	public WaitingFirstLeadState(CardsGameWidget cardsGameWidget,
			final ViewerStateManager stateManager) {

		this.cardsGameWidget = cardsGameWidget;
		this.stateManager = stateManager;

		VerticalPanel panel = new VerticalPanel();
		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

		final HTML text = new HTML("Attendi l'inizio del gioco.");
		text.setWidth("120px");
		text.setWordWrap(true);
		panel.add(text);

		// FIXME: Remove this button when the servlet is ready.
		continueButton = new PushButton("[DEBUG] Continua");
		continueButton.setEnabled(false);
		continueButton.setWidth("80px");
		continueButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				int player = Random.nextInt(4);
				Card card = new Card();
				card.suit = Card.Suit.CLUBS;
				card.value = 2;

				handleCardPlayed(card, player);
			}
		});
		panel.add(continueButton);

		exitButton = new PushButton("Esci");
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
	}

	@Override
	public void activate() {
	}

	@Override
	public void freeze() {
		continueButton.setEnabled(false);
		exitButton.setEnabled(false);
		frozen = true;
	}

	@Override
	public void handleAnimationStart() {
		if (frozen) {
			System.out
					.println("Client: notice: the handleAnimationStart() method was called while frozen, ignoring it.");
			return;
		}
		continueButton.setEnabled(false);
		exitButton.setEnabled(false);
	}

	@Override
	public void handleAnimationEnd() {
		if (frozen) {
			System.out
					.println("Client: notice: the handleAnimationEnd() method was called while frozen, ignoring it.");
			return;
		}
		continueButton.setEnabled(true);
		exitButton.setEnabled(true);
	}

	@Override
	public boolean handleCardPlayed(Card card, int playerPosition) {
		if (frozen) {
			System.out
					.println("Client: notice: the CardPlayed event was received while frozen, deferring it.");
			return false;
		}
		stateManager.addPlayedCard(playerPosition, card);

		cardsGameWidget.revealCoveredCard(playerPosition, card);
		cardsGameWidget.playCard(playerPosition, card);
		cardsGameWidget.runPendingAnimations(2000,
				new GWTAnimation.AnimationCompletedListener() {
					@Override
					public void onComplete() {
						stateManager.transitionToWaitingPlayedCard();
					}
				});
		return true;
	}

	@Override
	public boolean handleGameEnded(int[] matchPoints, int[] playersTotalPoints) {
		if (frozen) {
			System.out
					.println("Client: notice: the GameEnded event was received while frozen, deferring it.");
			return false;
		}
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean handlePlayerLeft(String player) {
		if (frozen) {
			System.out
					.println("Client: notice: the PlayerLeft event was received while frozen, deferring it.");
			return false;
		}
		// TODO Auto-generated method stub
		return false;
	}
}
