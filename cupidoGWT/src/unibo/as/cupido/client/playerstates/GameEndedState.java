package unibo.as.cupido.client.playerstates;

import unibo.as.cupido.common.exception.NoSuchTableException;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.client.CardsGameWidget;
import unibo.as.cupido.client.CardsGameWidget.CardRole.State;
import unibo.as.cupido.client.CardsGameWidget.GameEventListener;
import unibo.as.cupido.client.CupidoInterfaceAsync;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;

public class GameEndedState implements PlayerState {

	private PushButton exitButton;
	
	private boolean frozen = false;

	private CupidoInterfaceAsync cupidoService;

	public GameEndedState(CardsGameWidget cardsGameWidget,
			final PlayerStateManager stateManager, final CupidoInterfaceAsync cupidoService) {
		
		this.cupidoService = cupidoService;
		
		VerticalPanel panel = new VerticalPanel();
		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

		// FIXME: Display the scores in some way.
		final HTML text = new HTML("La partita &egrave; finita");
		text.setWidth("120px");
		text.setWordWrap(true);
		panel.add(text);

		exitButton = new PushButton("Esci");
		exitButton.setWidth("80px");
		exitButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// FIXME: Should cupidoService.leaveTable() be called here or not?
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
		exitButton.setEnabled(false);
		frozen = true;
	}

	@Override
	public void handleAnimationStart() {
		if (frozen) {
			System.out.println("Client: notice: the handleAnimationStart() event was received while frozen, ignoring it.");
			return;
		}
		exitButton.setEnabled(false);
	}

	@Override
	public void handleAnimationEnd() {
		if (frozen) {
			System.out.println("Client: notice: the handleAnimationEnd() event was received while frozen, ignoring it.");
			return;
		}
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
	public boolean handleCardPlayed(Card card, int playerPosition) {
		if (frozen) {
			System.out.println("Client: notice: the handleCardPlayed() event was received while frozen, deferring it.");
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
