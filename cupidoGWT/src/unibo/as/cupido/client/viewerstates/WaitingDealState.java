package unibo.as.cupido.client.viewerstates;

import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.client.CardsGameWidget;
import unibo.as.cupido.client.CardsGameWidget.CardRole.State;
import unibo.as.cupido.client.CardsGameWidget.GameEventListener;
import unibo.as.cupido.client.GWTAnimation;
import unibo.as.cupido.client.RandomCardGenerator;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;

public class WaitingDealState implements ViewerState {

	// FIXME: Remove this button when the servlet is ready.
	private PushButton continueButton;
	
	private PushButton exitButton;

	private ViewerStateManager stateManager;

	private CardsGameWidget cardsGameWidget;

	public WaitingDealState(final CardsGameWidget cardsGameWidget,
			final ViewerStateManager stateManager) {
		
		this.cardsGameWidget = cardsGameWidget;
		this.stateManager = stateManager;
		
		VerticalPanel panel = new VerticalPanel();
		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

		final int currentPlayer = (stateManager.getFirstPlayerInTrick() + stateManager
				.getDealtCards().size()) % 4;
		ViewerStateManager.PlayerInfo playerInfo = stateManager.getPlayerInfo()
				.get(currentPlayer);

		final HTML text;

		if (playerInfo.isBot)
			text = new HTML("Attendi che il bot giochi");
		else {
			SafeHtmlBuilder safeHtmlBuilder = new SafeHtmlBuilder();
			safeHtmlBuilder.appendHtmlConstant("Attendi che ");
			safeHtmlBuilder.appendEscaped(playerInfo.name);
			safeHtmlBuilder.appendHtmlConstant(" giochi.");
			text = new HTML(safeHtmlBuilder.toSafeHtml().asString());
		}

		text.setWidth("120px");
		text.setWordWrap(true);
		panel.add(text);

		// FIXME: Remove this button when the servlet is ready.
		continueButton = new PushButton("[DEBUG] Continua");
		continueButton.setWidth("80px");
		continueButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// FIXME: This data should come from the servlet.
				int player = currentPlayer;
				Card card = RandomCardGenerator.generateCard();
				
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
	public void handleCardPlayed(Card card, int playerPosition) {
		stateManager.addDealtCard(playerPosition, card);

		cardsGameWidget.revealCoveredCard(playerPosition, card);

		cardsGameWidget.dealCard(playerPosition, card);
		cardsGameWidget.runPendingAnimations(2000,
				new GWTAnimation.AnimationCompletedListener() {
					@Override
					public void onComplete() {
						if (stateManager.getDealtCards().size() == 4)
							stateManager.transitionToEndOfTrick();
						else
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
