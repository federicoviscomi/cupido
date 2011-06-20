package unibo.as.cupido.client.playerstates;

import java.util.List;

import unibo.as.cupido.common.structures.Card;
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

public class YourTurnState implements PlayerState {

	private PushButton exitButton;

	public YourTurnState(final CardsGameWidget cardsGameWidget,
			final PlayerStateManager stateManager, final List<Card> hand) {
		VerticalPanel panel = new VerticalPanel();
		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

		final HTML text = new HTML("Tocca a te giocare.");
		text.setWidth("120px");
		text.setWordWrap(true);
		panel.add(text);

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

			boolean dealtCard = false;

			@Override
			public void onAnimationStart() {
				exitButton.setEnabled(false);
			}

			@Override
			public void onAnimationEnd() {
				exitButton.setEnabled(true);
			}

			@Override
			public void onCardClicked(int player, Card card, State state,
					boolean isRaised) {
				if (dealtCard)
					return;
				if (player != 0)
					return;

				if (!canDealCard(card, stateManager.getDealtCards(), hand,
						stateManager.areHeartsBroken()))
					return;

				dealtCard = true;

				hand.remove(card);

				stateManager.addDealtCard(player, card);

				cardsGameWidget.dealCard(player, card);
				cardsGameWidget.runPendingAnimations(2000,
						new GWTAnimation.AnimationCompletedListener() {
							@Override
							public void onComplete() {
								if (stateManager.getDealtCards().size() == 4)
									stateManager.transitionToEndOfTrick(hand);
								else
									stateManager.transitionToWaitingDeal(hand);
							}
						});
			}
		});
	}

	private static boolean canDealCard(Card card, List<Card> dealtCards,
			List<Card> hand, boolean areHeartsBroken) {

		if (dealtCards.size() == 0) {
			// This is the first card in the trick.

			// If this was the first trick in the game, the active state
			// would be FirstDealer.
			assert hand.size() < 13;

			if (areHeartsBroken)
				// Any card will is ok.
				return true;

			// The player must not play hearts.
			return card.suit != Card.Suit.HEARTS;

		}

		// This is not the first card in a trick, so the player must play a card
		// with the
		// same suit, if possible.

		Card.Suit suit = dealtCards.get(0).suit;

		boolean hasSameSuit = false;

		for (Card handCard : hand)
			if (handCard.suit == suit) {
				hasSameSuit = true;
				break;
			}

		if (hasSameSuit)
			return card.suit == suit;

		// The player has no card with that suit, so he is allowed to deal any
		// card,
		// except in the first trick, where the player must not play a penalty
		// card, if possible.

		if (hand.size() != 13)
			// Not the first trick, the player can deal any card.
			return true;

		// First trick

		boolean mustPlayAPenaltyCard = true;

		for (Card handCard : hand)
			if (!isPenaltyCard(handCard)) {
				mustPlayAPenaltyCard = false;
				break;
			}

		if (mustPlayAPenaltyCard)
			return true;

		// The player has some not-penalty cards, so he must play one of them.
		return !isPenaltyCard(card);
	}

	private static boolean isPenaltyCard(Card card) {
		if (card.suit == Card.Suit.HEARTS)
			return true;
		if (card.suit == Card.Suit.SPADES && card.value == 12)
			return true;
		return false;
	}

	@Override
	public void disableControls() {
		exitButton.setEnabled(false);
	}

	@Override
	public boolean handleCardPassed(Card[] cards) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean handleCardPlayed(Card card, int playerPosition) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean handleGameEnded(int[] matchPoints, int[] playersTotalPoints) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean handleGameStarted(Card[] myCards) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean handlePlayerLeft(String player) {
		// TODO Auto-generated method stub
		return false;
	}
}
