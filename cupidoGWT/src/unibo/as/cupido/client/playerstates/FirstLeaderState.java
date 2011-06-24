package unibo.as.cupido.client.playerstates;

import java.util.List;

import unibo.as.cupido.client.CardsGameWidget;
import unibo.as.cupido.client.CardsGameWidget.CardRole.State;
import unibo.as.cupido.client.CupidoInterfaceAsync;
import unibo.as.cupido.client.GWTAnimation;
import unibo.as.cupido.common.exception.NoSuchTableException;
import unibo.as.cupido.common.structures.Card;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;

public class FirstLeaderState implements PlayerState {

	private PushButton exitButton;
	private HTML text;
	private List<Card> hand;
	private CardsGameWidget cardsGameWidget;
	private PlayerStateManager stateManager;

	private boolean frozen = false;
	private CupidoInterfaceAsync cupidoService;
	private boolean playedCard = false;

	public FirstLeaderState(CardsGameWidget cardsGameWidget,
			final PlayerStateManager stateManager, List<Card> hand,
			final CupidoInterfaceAsync cupidoService) {

		this.cardsGameWidget = cardsGameWidget;
		this.stateManager = stateManager;
		this.hand = hand;
		this.cupidoService = cupidoService;

		VerticalPanel panel = new VerticalPanel();
		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

		text = new HTML("Sei il primo a giocare; devi giocare il due di fiori");
		text.setWidth("120px");
		text.setWordWrap(true);
		panel.add(text);

		exitButton = new PushButton("Esci");
		exitButton.setWidth("80px");
		exitButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				freeze();
				cupidoService.leaveTable(new AsyncCallback<Void>() {
					@Override
					public void onFailure(Throwable caught) {
						try {
							throw caught;
						} catch (NoSuchTableException e) {
							// The table has been destroyed in the meantime,
							// nothing to do.
						} catch (Throwable e) {
							stateManager.onFatalException(e);
						}
					}

					@Override
					public void onSuccess(Void result) {
						stateManager.exit();
					}
				});
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
		exitButton.setEnabled(false);
		frozen = true;
	}

	@Override
	public void handleAnimationStart() {
		if (frozen) {
			System.out
					.println("Client: notice: the handleAnimationStart() event was received while frozen, ignoring it.");
			return;
		}
		exitButton.setEnabled(false);
	}

	@Override
	public void handleAnimationEnd() {
		if (frozen) {
			System.out
					.println("Client: notice: the handleAnimationEnd() event was received while frozen, ignoring it.");
			return;
		}
		exitButton.setEnabled(true);
	}

	@Override
	public void handleCardClicked(int player, Card card, State state,
			boolean isRaised) {
		if (frozen) {
			System.out
					.println("Client: notice: the handleCardClicked() event was received while frozen, ignoring it.");
			return;
		}
		if (player != 0 || card == null)
			return;
		if (card.suit != Card.Suit.CLUBS || card.value != 2)
			return;
		
		if (playedCard )
			// The user has already played a card.
			return;
		
		playedCard = true;

		text.setText("");

		hand.remove(card);

		stateManager.addPlayedCard(0, card);

		cardsGameWidget.playCard(0, card);
		cardsGameWidget.runPendingAnimations(2000,
				new GWTAnimation.AnimationCompletedListener() {
					@Override
					public void onComplete() {
						stateManager.transitionToWaitingPlayedCard(hand);
					}
				});

		cupidoService.playCard(card, new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				try {
					throw caught;
				} catch (NoSuchTableException e) {
					// The table does not exist anymore, because the owner has
					// left.
					// Do nothing yet, this situation will be handled when the
					// GameEnded
					// notification arrives.
				} catch (Throwable e) {
					stateManager.onFatalException(e);
				}
			}

			@Override
			public void onSuccess(Void result) {
			}
		});
	}

	@Override
	public boolean handleCardPassed(Card[] cards) {
		if (frozen) {
			System.out
					.println("Client: notice: the handleCardPassed() event was received while frozen, deferring it.");
			return false;
		}
		// This notification should never arrive in this state. 
		freeze();
		stateManager.onFatalException(new Exception("The CardPassed notification was received when the client was in the FirstLeader state"));
		return true;
	}

	@Override
	public boolean handleCardPlayed(Card card, int playerPosition) {
		if (frozen) {
			System.out
					.println("Client: notice: the handleCardPlayed() event was received while frozen, deferring it.");
			return false;
		}
		if (playedCard) {
			// Let the next state handle this.
			return false;
		}
		// This notification should never arrive in this state. 
		freeze();
		stateManager.onFatalException(new Exception("The CardPlayed notification was received when the client was in the FirstLeader state"));
		return true;
	}

	@Override
	public boolean handleGameEnded(int[] matchPoints, int[] playersTotalPoints) {
		if (frozen) {
			System.out
					.println("Client: notice: the handleGameEnded() event was received while frozen, deferring it.");
			return false;
		}
		if (playedCard) {
			// Let the next state handle this.
			return false;
		}
		stateManager.exit();
		Window.alert("Il creatore del tavolo \350 uscito dalla partita, quindi la partita \350 stata interrotta.");
		return true;
	}

	@Override
	public boolean handleGameStarted(Card[] myCards) {
		if (frozen) {
			System.out
					.println("Client: notice: the handleGameStarted() event was received while frozen, deferring it.");
			return false;
		}
		// This notification should never arrive in this state. 
		freeze();
		stateManager.onFatalException(new Exception("The GameStarted notification was received when the client was in the FirstLeader state"));
		return true;
	}

	@Override
	public void handlePlayerLeft(int player) {
		if (frozen) {
			System.out
					.println("Client: notice: the handlePlayerLeft() event was received while frozen, ignoring it.");
			return;
		}
		// Nothing to do.
	}
}