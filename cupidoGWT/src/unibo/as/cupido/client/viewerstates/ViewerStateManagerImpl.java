package unibo.as.cupido.client.viewerstates;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.ObservedGameStatus;
import unibo.as.cupido.common.structures.PlayerStatus;
import unibo.as.cupido.client.CardsGameWidget;
import unibo.as.cupido.client.CardsGameWidget.CardRole.State;
import unibo.as.cupido.client.screens.ScreenManager;
import unibo.as.cupido.shared.cometNotification.CardPassed;
import unibo.as.cupido.shared.cometNotification.CardPlayed;
import unibo.as.cupido.shared.cometNotification.GameEnded;
import unibo.as.cupido.shared.cometNotification.GameStarted;
import unibo.as.cupido.shared.cometNotification.PlayerLeft;

import com.google.gwt.user.client.ui.VerticalPanel;

public class ViewerStateManagerImpl implements ViewerStateManager {

	private ViewerState currentState = null;
	private ScreenManager screenManager;
	private CardsGameWidget cardsGameWidget;
	private int firstPlayerInTrick = -1;
	private int remainingTricks;
	
	String username;
	
	private List<Serializable> pendingNotifications = new ArrayList<Serializable>();

	/**
	 * Some information about the players. The first element refers to the
	 * bottom player, and the other players follow in clockwise order.
	 */
	private List<PlayerInfo> players;

	/**
	 * The (ordered) list of cards dealt in the current trick.
	 */
	private List<Card> dealtCards = new ArrayList<Card>();

	/**
	 * Initialize the state manager. The current user is a viewer.
	 */
	public ViewerStateManagerImpl(int tableSize, ScreenManager screenManager,
			ObservedGameStatus observedGameStatus, String username) {

		this.username = username;
		this.screenManager = screenManager;
		this.cardsGameWidget = new CardsGameWidget(tableSize,
				observedGameStatus, null, new VerticalPanel(),
				new CardsGameWidget.GameEventListener() {
					@Override
					public void onAnimationStart() {
						currentState.handleAnimationStart();
					}

					@Override
					public void onAnimationEnd() {
						currentState.handleAnimationEnd();
					}

					@Override
					public void onCardClicked(int player, Card card,
							State state, boolean isRaised) {
						// Nothing to do, viewers are not expected to click on cards.
					}
				});

		players = new ArrayList<PlayerInfo>();
		for (PlayerStatus playerStatus : observedGameStatus.playerStatus) {
			PlayerInfo playerInfo = new PlayerInfo();
			playerInfo.isBot = playerStatus.isBot;
			playerInfo.name = playerStatus.name;
			players.add(playerInfo);
		}

		// NOTE: It may be -1, but it's not an issue here.
		firstPlayerInTrick = observedGameStatus.firstDealerInTrick;

		for (int i = 0; i < 4; i++) {
			Card card = observedGameStatus.playerStatus[(firstPlayerInTrick + i) % 4].playedCard;
			if (card != null)
				dealtCards.add(card);
			else
				break;
		}

		remainingTricks = observedGameStatus.playerStatus[0].numOfCardsInHand;
		if (observedGameStatus.playerStatus[0].playedCard != null)
			++remainingTricks;

		if (observedGameStatus.playerStatus[0].numOfCardsInHand == 13
				&& observedGameStatus.playerStatus[1].numOfCardsInHand == 13
				&& observedGameStatus.playerStatus[2].numOfCardsInHand == 13
				&& observedGameStatus.playerStatus[3].numOfCardsInHand == 13) {
			transitionToWaitingFirstDeal();
			return;
		}

		int n = 0;

		for (int i = 0; i < 4; i++)
			if (observedGameStatus.playerStatus[i].playedCard != null)
				n++;

		if (n == 4)
			transitionToEndOfTrick();
		else
			transitionToWaitingDeal();
	}
	
	private void transitionTo(ViewerState newState) {
		currentState = newState;
		currentState.activate();
		sendPendingNotifications();
	}

	@Override
	public void transitionToEndOfTrick() {
		transitionTo(new EndOfTrickState(cardsGameWidget, this));
	}

	@Override
	public void transitionToWaitingFirstDeal() {
		transitionTo(new WaitingFirstDealState(cardsGameWidget, this));
	}

	@Override
	public void transitionToGameEnded() {
		transitionTo(new GameEndedState(cardsGameWidget, this));
	}

	@Override
	public void transitionToWaitingDeal() {
		transitionTo(new WaitingDealState(cardsGameWidget, this));
	}

	@Override
	public int getFirstPlayerInTrick() {
		return firstPlayerInTrick;
	}

	@Override
	public List<Card> getDealtCards() {
		return dealtCards;
	}

	@Override
	public void addDealtCard(int player, Card card) {
		if (firstPlayerInTrick == -1) {
			assert dealtCards.size() == 0;
			firstPlayerInTrick = player;
		}
		assert (firstPlayerInTrick + dealtCards.size()) % 4 == player;
		dealtCards.add(card);
	}

	@Override
	public void goToNextTrick() {
		assert dealtCards.size() == 4;
		firstPlayerInTrick += winnerCard(dealtCards);
		firstPlayerInTrick = firstPlayerInTrick % 4;
		dealtCards.clear();
		--remainingTricks;
	}

	/**
	 * Computes the index of the winning card in a trick.
	 * 
	 * @param cards
	 *            An ordered list containing the cards in the current trick.
	 */
	private static int winnerCard(List<Card> cards) {
		assert cards.size() == 4;
		for (Card card : cards)
			assert card != null;
		int winner = 0;
		for (int candidate = 1; candidate < 4; candidate++)
			if (cardTakes(cards.get(candidate), cards.get(winner)))
				winner = candidate;
		return winner;
	}

	private static boolean cardTakes(Card candidate, Card previous) {
		if (candidate.suit != previous.suit)
			return false;
		if (candidate.value == previous.value)
			return false;
		if (candidate.value == 1)
			return true;
		if (previous.value == 1)
			return false;
		return candidate.value > previous.value;
	}

	@Override
	public void exit() {
		screenManager.displayMainMenuScreen(username);
	}

	@Override
	public CardsGameWidget getWidget() {
		return cardsGameWidget;
	}

	public List<PlayerInfo> getPlayerInfo() {
		return players;
	}

	@Override
	public int getRemainingTricks() {
		return remainingTricks;
	}

	@Override
	public void disableControls() {
		currentState.disableControls();
	}

	@Override
	public void handleCardPlayed(Card card, int playerPosition) {
		boolean handled = currentState.handleCardPlayed(card, playerPosition);
		if (!handled)
			pendingNotifications.add(new CardPlayed(card, playerPosition));
	}

	@Override
	public void handleGameEnded(int[] matchPoints, int[] playersTotalPoints) {
		boolean handled = currentState.handleGameEnded(matchPoints, playersTotalPoints);
		if (!handled)
			pendingNotifications.add(new GameEnded(matchPoints, playersTotalPoints));
	}

	@Override
	public void handlePlayerLeft(String player) {
		boolean handled = currentState.handlePlayerLeft(player);
		if (!handled)
			pendingNotifications.add(new PlayerLeft(player));
	}
	
	private void sendPendingNotifications() {
		List<Serializable> list = pendingNotifications;
		// Note that this may be modified in the calls to handle*() methods below.
		pendingNotifications = new ArrayList<Serializable>();
		
		for (Serializable x : list) {
			if (x instanceof CardPlayed) {
				CardPlayed message = (CardPlayed) x;
				handleCardPlayed(message.card, message.playerPosition);
				
			} else if (x instanceof GameEnded) {
				GameEnded message = (GameEnded) x;
				handleGameEnded(message.matchPoints, message.playersTotalPoints);
				
			} else if (x instanceof PlayerLeft) {
				PlayerLeft message = (PlayerLeft) x;
				handlePlayerLeft(message.player);
				
			} else {
				assert false;
			}
		}
	}
}
