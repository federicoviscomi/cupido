package unibo.as.cupido.client.playerstates;

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
import unibo.as.cupido.client.CardsGameWidget.GameEventListener;
import unibo.as.cupido.client.CardsGameWidget.CardRole.State;
import unibo.as.cupido.client.GWTAnimation;

public class EndOfTrickAsPlayer {

	public EndOfTrickAsPlayer(CardsGameWidget cardsGameWidget, final PlayerStateManager stateManager, final List<Card> hand) {
		VerticalPanel panel = new VerticalPanel();
		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		
		final HTML text = new HTML("");
		text.setWidth("120px");
		text.setWordWrap(true);
		panel.add(text);
		
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
				exitButton.setEnabled(false);
			}

			@Override
			public void onAnimationEnd() {
				exitButton.setEnabled(true);
			}

			@Override
			public void onCardClicked(int player, Card card, State state,
					boolean isRaised) {
			}
		});
		
		stateManager.goToNextTrick();
		
		final int player = stateManager.getFirstPlayerInTrick();
		
		cardsGameWidget.animateTrickTaking(player, 1500, 2000,
				new GWTAnimation.AnimationCompletedListener() {
			
			@Override
			public void onComplete() {
				if (hand.size() != 0) {
					if (player == 0)
						stateManager.transitionToYourTurn(hand);
					else
						stateManager.transitionToWaitingDealAsPlayer(hand);
				} else
					stateManager.transitionToGameEndedAsPlayer();
			}
		});
	}
}
