package unibo.as.cupido.client.viewerstates;

import java.util.ArrayList;
import java.util.List;

import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.ObservedGameStatus;
import unibo.as.cupido.backendInterfaces.common.PlayerStatus;
import unibo.as.cupido.client.CardsGameWidget;
import unibo.as.cupido.client.CardsGameWidget.CardRole.State;
import unibo.as.cupido.client.screens.ScreenSwitcher;

import com.google.gwt.user.client.ui.VerticalPanel;

public class ViewerStateManagerImpl implements ViewerStateManager {

	private Object currentState = null;
	private ScreenSwitcher screenSwitcher;
	private CardsGameWidget cardsGameWidget;
	private int firstPlayerInTrick = -1;
	private int remainingTricks;

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
	public ViewerStateManagerImpl(int tableSize, ScreenSwitcher screenSwitcher,
			ObservedGameStatus observedGameStatus) {

		this.screenSwitcher = screenSwitcher;
		this.cardsGameWidget = new CardsGameWidget(tableSize,
				observedGameStatus, null, new VerticalPanel(),
				new CardsGameWidget.GameEventListener() {
					@Override
					public void onAnimationStart() {
					}

					@Override
					public void onAnimationEnd() {
					}

					@Override
					public void onCardClicked(int player, Card card,
							State state, boolean isRaised) {
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

	@Override
	public void transitionToEndOfTrick() {
		currentState = new EndOfTrickState(cardsGameWidget, this);
	}

	@Override
	public void transitionToWaitingFirstDeal() {
		currentState = new WaitingFirstDealState(cardsGameWidget, this);
	}

	@Override
	public void transitionToGameEnded() {
		currentState = new GameEndedState(cardsGameWidget, this);
	}

	@Override
	public void transitionToWaitingDeal() {
		currentState = new WaitingDealState(cardsGameWidget, this);
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
		screenSwitcher.displayMainMenuScreen();
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
}
