package unibo.as.cupido.client.playerstates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.client.CardsGameWidget;
import unibo.as.cupido.client.CardsGameWidget.CardRole.State;
import unibo.as.cupido.client.CardsGameWidget.GameEventListener;
import unibo.as.cupido.client.CupidoInterfaceAsync;
import unibo.as.cupido.client.GWTAnimation;
import unibo.as.cupido.client.RandomCardGenerator;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;

public class CardPassingWaitingState implements PlayerState {

	// FIXME: Remove this button when the servlet is ready.
	private PushButton continueButton;
	
	private PushButton exitButton;

	private CardsGameWidget cardsGameWidget;

	private PlayerStateManager stateManager;

	private List<Card> hand;
	
	private boolean frozen = false;

	private CupidoInterfaceAsync cupidoService;

	public CardPassingWaitingState(CardsGameWidget cardsGameWidget,
			final PlayerStateManager stateManager, List<Card> hand, CupidoInterfaceAsync cupidoService) {

		this.cardsGameWidget = cardsGameWidget;
		this.stateManager = stateManager;
		this.hand = hand;
		this.cupidoService = cupidoService;
		
		VerticalPanel panel = new VerticalPanel();
		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

		final HTML text = new HTML(
				"Aspetta che gli altri giocatori decidano quali carte passare.");
		text.setWidth("120px");
		text.setWordWrap(true);
		panel.add(text);

		// FIXME: Remove this button when the servlet is ready.
		continueButton = new PushButton("[DEBUG] Continua");
		continueButton.setWidth("80px");
		continueButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {

				text.setText("");

				// FIXME: Remove this. This data should come from the servlet.
				Card[] passedCards = new Card[3];
				passedCards[0] = RandomCardGenerator.generateCard();
				passedCards[1] = RandomCardGenerator.generateCard();
				passedCards[2] = RandomCardGenerator.generateCard();
				
				handleCardPassed(passedCards);
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
		frozen = false;
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
	public boolean handleCardPassed(Card[] passedCards) {
		if (frozen) {
			System.out.println("Client: notice: the handleCardPassed() event was received while frozen, deferring it.");
			return false;
		}
		List<Card> cards = new ArrayList<Card>();
		
		for (Card card : passedCards)
			cards.add(card);
		
		hand.addAll(cards);

		Collections.sort(cards,
				CardsGameWidget.getCardComparator());

		for (int i = 0; i < 3; i++)
			cardsGameWidget.revealCoveredCard(0,
					cards.get(3 - i - 1));

		for (Card card : passedCards)
			cardsGameWidget.pickCard(0, card);

		cardsGameWidget.runPendingAnimations(2000,
				new GWTAnimation.AnimationCompletedListener() {
					@Override
					public void onComplete() {
						boolean found = false;
						for (Card card : hand)
							if (card.suit == Card.Suit.CLUBS
									&& card.value == 2) {
								found = true;
								break;
							}
						if (found)
							stateManager.transitionToFirstDealer(hand);
						else
							stateManager
									.transitionToWaitingFirstDeal(hand);
					}
				});
		
		return true;
	}

	@Override
	public boolean handleCardPlayed(Card card, int playerPosition) {
		if (frozen) {
			System.out.println("Client: notice: the handleCardPassed() event was received while frozen, deferring it.");
			return false;
		}
		// TODO Auto-generated method stub
		return false;
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
