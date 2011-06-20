package unibo.as.cupido.client.playerstates;

import java.util.List;

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

public class WaitingFirstDealState implements PlayerState {

	// FIXME: Remove this button when the servlet is ready.
	private PushButton continueButton;
	
	private PushButton exitButton;

	private HTML text;

	private CardsGameWidget cardsGameWidget;

	private PlayerStateManager stateManager;

	private List<Card> hand;

	public WaitingFirstDealState(final CardsGameWidget cardsGameWidget,
			final PlayerStateManager stateManager, List<Card> hand) {
		
		this.cardsGameWidget = cardsGameWidget;
		this.stateManager = stateManager;
		this.hand = hand;
		
		VerticalPanel panel = new VerticalPanel();
		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

		text = new HTML(
				"Attendi che il giocatore che ha il due di picche lo giochi.");
		text.setWidth("120px");
		text.setWordWrap(true);
		panel.add(text);

		// FIXME: Remove this button when the servlet is ready.
		continueButton = new PushButton("[DEBUG] Continua");
		continueButton.setWidth("80px");
		continueButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// FIXME: don't generate this data, get them from the servlet
				// instead.
				final int player = Random.nextInt(3) + 1;
				Card card = new Card(2, Card.Suit.CLUBS);
				
				handleCardPlayed(card, player);
			}
		});
		panel.add(continueButton);

		exitButton = new PushButton("Esci");
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
	public void handleCardPassed(Card[] cards) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleCardPlayed(Card card, final int playerPosition) {
		text.setText("");

		stateManager.addDealtCard(playerPosition, card);

		cardsGameWidget.revealCoveredCard(playerPosition, card);

		cardsGameWidget.dealCard(playerPosition, card);
		cardsGameWidget.runPendingAnimations(2000,
				new GWTAnimation.AnimationCompletedListener() {

					@Override
					public void onComplete() {
						if (playerPosition == 3)
							stateManager.transitionToYourTurn(hand);
						else
							stateManager.transitionToWaitingDeal(hand);
					}
				});
	}

	@Override
	public void handleGameEnded(int[] matchPoints, int[] playersTotalPoints) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleGameStarted(Card[] myCards) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handlePlayerLeft(String player) {
		// TODO Auto-generated method stub
		
	}
}
