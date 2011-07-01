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

import unibo.as.cupido.client.CupidoInterfaceAsync;
import unibo.as.cupido.client.screens.ScreenManager;
import unibo.as.cupido.client.widgets.CardsGameWidget;
import unibo.as.cupido.client.widgets.cardsgame.CardRole;
import unibo.as.cupido.client.widgets.cardsgame.GameEventListener;
import unibo.as.cupido.common.exception.GameEndedException;
import unibo.as.cupido.common.exception.GameInterruptedException;
import unibo.as.cupido.common.exception.NoSuchTableException;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.ObservedGameStatus;
import unibo.as.cupido.common.structures.PlayerStatus;
import unibo.as.cupido.shared.cometNotification.CardPlayed;
import unibo.as.cupido.shared.cometNotification.GameEnded;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ViewerStateManagerImpl implements ViewerStateManager {

	/**
	 * Decides whether a candidate card takes the previous one in Hearts.
	 * 
	 * @param candidate The candidate card.
	 * @param previous The previous card.
	 * @return true if candidate takes previous, false otherwise.
	 */
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

	/**
	 * Computes the index of the winning card in a trick.
	 * 
	 * @param cards
	 *            An ordered list containing the cards in the current trick.
	 * @return The index of the winning card.
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

	private CardsGameWidget cardsGameWidget;
	private CupidoInterfaceAsync cupidoService;
	private ViewerState currentState = null;

	private int firstPlayerInTrick = -1;

	/**
	 * Specifies whether the UI is frozen (i.e. does no longer react to events) or not.
	 */
	private boolean frozen = false;

	private List<Serializable> pendingNotifications = new ArrayList<Serializable>();

	/**
	 * The (ordered) list of cards played in the current trick.
	 */
	private List<Card> playedCards = new ArrayList<Card>();

	/**
	 * Some information about the players. The first element refers to the
	 * bottom player, and the other players follow in clockwise order.
	 */
	private List<PlayerInfo> players;
	private int remainingTricks;

	private ScreenManager screenManager;

	private String username;

	/**
	 * Initialize the state manager. The current user is a viewer.
	 */
	public ViewerStateManagerImpl(int tableSize, ScreenManager screenManager,
			ObservedGameStatus observedGameStatus, String username,
			CupidoInterfaceAsync cupidoService) {

		if (observedGameStatus.firstDealerInTrick == -1) {
			// No-one has played the two of clubs yet. The players may or may
			// not have passed cards. So the number of cards in each hand
			// may be 10 or 13, make sure that all players have 13 cards.
			for (PlayerStatus player : observedGameStatus.playerStatus)
				player.numOfCardsInHand = 13;
		}

		this.username = username;
		this.screenManager = screenManager;
		this.cupidoService = cupidoService;
		this.cardsGameWidget = new CardsGameWidget(tableSize,
				observedGameStatus, null, new VerticalPanel(),
				new GameEventListener() {
					@Override
					public void onAnimationEnd() {
						currentState.handleAnimationEnd();
					}

					@Override
					public void onAnimationStart() {
						currentState.handleAnimationStart();
					}

					@Override
					public void onCardClicked(int player, Card card,
							CardRole.State state, boolean isRaised) {
						// Nothing to do, viewers are not expected to click on
						// cards.
					}

					@Override
					public void onExit() {
						exit();
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

		if (firstPlayerInTrick == -1) {
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
	public void exit() {
		if (frozen) {
			System.out
					.println("Client: notice: the exit() method was called while frozen, ignoring it.");
			return;
		}

		// The current animation (if any) is stopped.
		freeze();

		// FIXME: Note that leaveTable() is called even if the game is already
		// finished, even if this is not needed.
		cupidoService.leaveTable(new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				try {
					throw caught;
				} catch (NoSuchTableException e) {
					// This can happen even if no problems occur.
				} catch (GameInterruptedException e) {
					// This can happen even if no problems occur.
				} catch (GameEndedException e) {
					// This can happen even if no problems occur.
				} catch (Throwable e) {
					// Can't call screenManager.displayGeneralErrorScreen()
					// because
					// the main screen has now the flow of control.
					System.out
							.println("Client: got a fatal exception in leaveTable().");
				}
			}

			@Override
			public void onSuccess(Void result) {
			}
		});

		screenManager.displayMainMenuScreen(username);
	}

	@Override
	public void freeze() {
		cardsGameWidget.freeze();
		currentState.freeze();
		frozen = true;
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
	public List<PlayerInfo> getPlayerInfo() {
		return players;
	}

	@Override
	public int getRemainingTricks() {
		return remainingTricks;
	}

	@Override
	public CardsGameWidget getWidget() {
		return cardsGameWidget;
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

	@Override
	public void onFatalException(Throwable e) {
		screenManager.displayGeneralErrorScreen(e);
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
	public void transitionToGameEnded() {
		if (frozen) {
			System.out
					.println("Client: notice: the transitionToGameEnded() method was called while frozen, ignoring it.");
			return;
		}

		transitionTo(new GameEndedState(cardsGameWidget, this));
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
	public void transitionToWaitingPlayedCard() {
		if (frozen) {
			System.out
					.println("Client: notice: the transitionToWaitingPlayedCard() method was called while frozen, ignoring it.");
			return;
		}
		transitionTo(new WaitingPlayedCardState(cardsGameWidget, this));
	}
}
