/*  Cupido - An online Hearts game.
 *  Copyright (C) 2011 Lorenzo Belli, Marco Poletti, Federico Viscomi
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package unibo.as.cupido.client.viewerstates;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import unibo.as.cupido.client.screens.ScreenManager;
import unibo.as.cupido.client.widgets.CardsGameWidget;
import unibo.as.cupido.client.widgets.CardsGameWidget.CardRole.State;
import unibo.as.cupido.client.widgets.LocalChatWidget;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.ObservedGameStatus;
import unibo.as.cupido.common.structures.PlayerStatus;
import unibo.as.cupido.shared.cometNotification.CardPlayed;
import unibo.as.cupido.shared.cometNotification.GameEnded;

import com.google.gwt.user.client.ui.VerticalPanel;

public class ViewerStateManagerImpl implements ViewerStateManager {

	private ViewerState currentState = null;
	private ScreenManager screenManager;
	private CardsGameWidget cardsGameWidget;
	private int firstPlayerInTrick = -1;
	private int remainingTricks;

	private boolean frozen = false;

	private String username;

	private List<Serializable> pendingNotifications = new ArrayList<Serializable>();

	/**
	 * Some information about the players. The first element refers to the
	 * bottom player, and the other players follow in clockwise order.
	 */
	private List<PlayerInfo> players;

	/**
	 * The (ordered) list of cards played in the current trick.
	 */
	private List<Card> playedCards = new ArrayList<Card>();
	private LocalChatWidget chatWidget;

	/**
	 * Initialize the state manager. The current user is a viewer.
	 */
	public ViewerStateManagerImpl(int tableSize, ScreenManager screenManager,
			LocalChatWidget chatWidget, ObservedGameStatus observedGameStatus,
			String username) {

		this.username = username;
		this.screenManager = screenManager;
		this.chatWidget = chatWidget;
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
						// Nothing to do, viewers are not expected to click on
						// cards.
					}
				});

		players = new ArrayList<PlayerInfo>();
		for (PlayerStatus playerStatus : observedGameStatus.playerStatus) {
			PlayerInfo playerInfo = new PlayerInfo();
			playerInfo.isBot = playerStatus.isBot;
			playerInfo.name = playerStatus.name;
			players.add(playerInfo);
		}

		firstPlayerInTrick = observedGameStatus.firstDealerInTrick;

		if (firstPlayerInTrick != -1) {
			for (int i = 0; i < 4; i++) {
				Card card = observedGameStatus.playerStatus[(firstPlayerInTrick + i) % 4].playedCard;
				if (card != null)
					playedCards.add(card);
				else
					break;
			}
		}

		remainingTricks = observedGameStatus.playerStatus[0].numOfCardsInHand;
		if (observedGameStatus.playerStatus[0].playedCard != null)
			++remainingTricks;

		if (observedGameStatus.playerStatus[0].numOfCardsInHand == 13
				&& observedGameStatus.playerStatus[1].numOfCardsInHand == 13
				&& observedGameStatus.playerStatus[2].numOfCardsInHand == 13
				&& observedGameStatus.playerStatus[3].numOfCardsInHand == 13) {
			transitionToWaitingFirstLead();
			return;
		}

		int n = 0;

		for (int i = 0; i < 4; i++)
			if (observedGameStatus.playerStatus[i].playedCard != null)
				n++;

		if (n == 4)
			transitionToEndOfTrick();
		else
			transitionToWaitingPlayedCard();
	}

	private void transitionTo(ViewerState newState) {
		currentState = newState;
		currentState.activate();
		sendPendingNotifications();
	}

	@Override
	public void transitionToEndOfTrick() {
		if (frozen) {
			System.out
					.println("Client: notice: the transitionToEndOfTrick() method was called while frozen, ignoring it.");
			return;
		}
		transitionTo(new EndOfTrickState(cardsGameWidget, this));
	}

	@Override
	public void transitionToWaitingFirstLead() {
		if (frozen) {
			System.out
					.println("Client: notice: the transitionToWaitingFirstLead() method was called while frozen, ignoring it.");
			return;
		}
		transitionTo(new WaitingFirstLeadState(cardsGameWidget, this));
	}

	@Override
	public void transitionToGameEnded() {
		if (frozen) {
			System.out
					.println("Client: notice: the transitionToGameEnded() method was called while frozen, ignoring it.");
			return;
		}

		chatWidget.freeze();

		transitionTo(new GameEndedState(cardsGameWidget, this));
	}

	@Override
	public void transitionToWaitingPlayedCard() {
		if (frozen) {
			System.out
					.println("Client: notice: the transitionToWaitingPlayedCard() method was called while frozen, ignoring it.");
			return;
		}
		transitionTo(new WaitingPlayedCardState(cardsGameWidget, this));
	}

	@Override
	public int getFirstPlayerInTrick() {
		return firstPlayerInTrick;
	}

	@Override
	public List<Card> getPlayedCards() {
		return playedCards;
	}

	@Override
	public void addPlayedCard(int player, Card card) {
		if (frozen) {
			System.out
					.println("Client: notice: the addPlayedCard() method was called while frozen, ignoring it.");
			return;
		}
		if (firstPlayerInTrick == -1) {
			assert playedCards.size() == 0;
			firstPlayerInTrick = player;
		}
		assert (firstPlayerInTrick + playedCards.size()) % 4 == player;
		playedCards.add(card);
	}

	@Override
	public void goToNextTrick() {
		if (frozen) {
			System.out
					.println("Client: notice: the goToNextTrick() method was called while frozen, ignoring it.");
			return;
		}
		assert playedCards.size() == 4;
		firstPlayerInTrick += winnerCard(playedCards);
		firstPlayerInTrick = firstPlayerInTrick % 4;
		playedCards.clear();
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
	public void onFatalException(Throwable e) {
		screenManager.displayGeneralErrorScreen(e);
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
	public void freeze() {
		currentState.freeze();
		frozen = true;
	}

	@Override
	public void handleCardPlayed(Card card, int playerPosition) {
		if (frozen) {
			System.out
					.println("Client: notice: the handleCardPlayed() method was called while frozen, ignoring it.");
			return;
		}
		boolean handled = currentState.handleCardPlayed(card, playerPosition);
		if (!handled)
			pendingNotifications.add(new CardPlayed(card, playerPosition));
	}

	@Override
	public void handleGameEnded(int[] matchPoints, int[] playersTotalPoints) {
		if (frozen) {
			System.out
					.println("Client: notice: the handleGameEnded() method was called while frozen, ignoring it.");
			return;
		}
		boolean handled = currentState.handleGameEnded(matchPoints,
				playersTotalPoints);
		if (!handled)
			pendingNotifications.add(new GameEnded(matchPoints,
					playersTotalPoints));
	}

	@Override
	public void handlePlayerReplaced(String name, int position) {
		if (frozen) {
			System.out
					.println("Client: notice: the handlePlayerReplaced() method was called while frozen, ignoring it.");
			return;
		}
		PlayerInfo x = players.get(position);
		x.isBot = true;
		x.name = name;
		cardsGameWidget.setBot(position, name);
		currentState.handlePlayerReplaced(name, position);
	}

	private void sendPendingNotifications() {
		List<Serializable> list = pendingNotifications;
		// Note that this may be modified in the calls to handle*() methods
		// below.
		pendingNotifications = new ArrayList<Serializable>();

		for (Serializable x : list) {
			if (x instanceof CardPlayed) {
				CardPlayed message = (CardPlayed) x;
				handleCardPlayed(message.card, message.playerPosition);

			} else if (x instanceof GameEnded) {
				GameEnded message = (GameEnded) x;
				handleGameEnded(message.matchPoints, message.playersTotalPoints);

			} else {
				assert false;
			}
		}
	}
}
