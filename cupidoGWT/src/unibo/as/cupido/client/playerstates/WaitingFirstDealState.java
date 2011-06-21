package unibo.as.cupido.client.playerstates;

import java.util.List;

import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.client.CardsGameWidget;
import unibo.as.cupido.client.CardsGameWidget.CardRole.State;
import unibo.as.cupido.client.CardsGameWidget.GameEventListener;
import unibo.as.cupido.client.CupidoInterfaceAsync;
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

	private boolean frozen = false;

	private CupidoInterfaceAsync cupidoService;

	public WaitingFirstDealState(CardsGameWidget cardsGameWidget,
			final PlayerStateManager stateManager, List<Card> hand, CupidoInterfaceAsync cupidoService) {
		
		this.cardsGameWidget = cardsGameWidget;
		this.stateManager = stateManager;
		this.hand = hand;
		this.cupidoService = cupidoService;
		
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
	}
	
	@Override
	public void activate() {
	}

	@Override
	public void freeze() {
		continueButton.setEnabled(false);
		exitButton.setEnabled(false);
		frozen  = true;
	}
	
	@Override
	public void handleAnimationStart() {
		if (frozen) {
			System.out.println("Client: notice: the handleAnimationStart() event was received while frozen, ignoring it.");
			return;
		}
		continueButton.setEnabled(false);
		exitButton.setEnabled(false);
	}

	@Override
	public void handleAnimationEnd() {
		if (frozen) {
			System.out.println("Client: notice: the handleAnimationEnd() event was received while frozen, ignoring it.");
			return;
		}
		continueButton.setEnabled(true);
		exitButton.setEnabled(true);
	}

	@Override
	public void handleCardClicked(int player, Card card, State state,
			boolean isRaised) {
		if (frozen) {
			System.out.println("Client: notice: the handleCardClicked() event was received while frozen, ignoring it.");
			return;
		}
	}

	@Override
	public boolean handleCardPassed(Card[] cards) {
		if (frozen) {
			System.out.println("Client: notice: the handleCardPassed() event was received while frozen, deferring it.");
			return false;
		}
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean handleCardPlayed(Card card, final int playerPosition) {
		if (frozen) {
			System.out.println("Client: notice: the handleCardPlayed() event was received while frozen, deferring it.");
			return false;
		}
		
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
		return true;
	}

	@Override
	public boolean handleGameEnded(int[] matchPoints, int[] playersTotalPoints) {
		if (frozen) {
			System.out.println("Client: notice: the handleGameEnded() event was received while frozen, deferring it.");
			return false;
		}
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean handleGameStarted(Card[] myCards) {
		if (frozen) {
			System.out.println("Client: notice: the handleGameStarted() event was received while frozen, deferring it.");
			return false;
		}
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean handlePlayerLeft(String player) {
		if (frozen) {
			System.out.println("Client: notice: the handlePlayerLeft() event was received while frozen, deferring it.");
			return false;
		}
		// TODO Auto-generated method stub
		return false;
	}
}
