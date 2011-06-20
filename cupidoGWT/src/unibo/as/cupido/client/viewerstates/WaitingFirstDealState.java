package unibo.as.cupido.client.viewerstates;

import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.client.CardsGameWidget;
import unibo.as.cupido.client.CardsGameWidget.CardRole.State;
import unibo.as.cupido.client.CardsGameWidget.GameEventListener;
import unibo.as.cupido.client.GWTAnimation;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;

public class WaitingFirstDealState implements ViewerState {

	// FIXME: Remove this button when the servlet is ready.
	private PushButton continueButton;
	private PushButton exitButton;
	private ViewerStateManager stateManager;
	private CardsGameWidget cardsGameWidget;

	public WaitingFirstDealState(CardsGameWidget cardsGameWidget,
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

	@Override
	public void disableControls() {
		continueButton.setEnabled(false);
		exitButton.setEnabled(false);
	}

	@Override
	public void handleCardPlayed(Card card, int playerPosition) {
		stateManager.addDealtCard(playerPosition, card);

		cardsGameWidget.revealCoveredCard(playerPosition, card);
		cardsGameWidget.dealCard(playerPosition, card);
		cardsGameWidget.runPendingAnimations(2000,
				new GWTAnimation.AnimationCompletedListener() {
					@Override
					public void onComplete() {
						stateManager.transitionToWaitingDeal();
					}
				});
	}

	@Override
	public void handleGameEnded(int[] matchPoints, int[] playersTotalPoints) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleNewPlayerJoined(String name, boolean isBot, int points,
			int position) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handlePlayerLeft(String player) {
		// TODO Auto-generated method stub
		
	}
}
