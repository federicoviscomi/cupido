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

package unibo.as.cupido.client.playerstates;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import unibo.as.cupido.client.CardsGameWidget;
import unibo.as.cupido.client.CardsGameWidget.CardRole.State;
import unibo.as.cupido.client.CupidoInterfaceAsync;
import unibo.as.cupido.client.screens.ScreenManager;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.InitialTableStatus;
import unibo.as.cupido.common.structures.ObservedGameStatus;
import unibo.as.cupido.common.structures.PlayerStatus;
import unibo.as.cupido.shared.cometNotification.CardPassed;
import unibo.as.cupido.shared.cometNotification.CardPlayed;
import unibo.as.cupido.shared.cometNotification.GameEnded;
import unibo.as.cupido.shared.cometNotification.GameStarted;

import com.google.gwt.user.client.ui.VerticalPanel;

public class PlayerStateManagerImpl implements PlayerStateManager {

	private PlayerState currentState = null;
	private ScreenManager screenManager;
	private CardsGameWidget cardsGameWidget;
	private int firstPlayerInTrick = -1;

	boolean heartsBroken = false;

	List<Serializable> pendingNotifications = new ArrayList<Serializable>();

	/**
	 * Some information about the players. The first element refers to the
	 * bottom player, and the other players follow in clockwise order.
	 */
	private List<PlayerInfo> players;

	/**
	 * The (ordered) list of cards played in the current trick.
	 */
	private List<Card> playedCards = new ArrayList<Card>();

	private String username;

	private boolean frozen = false;
	private CupidoInterfaceAsync cupidoService;

	/**
	 * Initialize the state manager. The current user is a player, and his hand
	 * cards are `cards'.
	 * 
	 * @param scores The four users' scores, starting from the bottom
	 *               player and in clockwise order. The scores in initialTableStatus
	 *               are ignored.
	 */
	public PlayerStateManagerImpl(int tableSize, ScreenManager screenManager,
			InitialTableStatus initialTableStatus, int[] scores, Card[] cards,
			String username, CupidoInterfaceAsync cupidoService) {
		this.username = username;
		this.screenManager = screenManager;
		this.cupidoService = cupidoService;

		for (String opponent : initialTableStatus.opponents)
			assert opponent != null;

		ObservedGameStatus observedGameStatus = new ObservedGameStatus();
		observedGameStatus.playerStatus = new PlayerStatus[4];

		// Bottom player
		observedGameStatus.playerStatus[0] = new PlayerStatus();
		observedGameStatus.playerStatus[0].isBot = false;
		observedGameStatus.playerStatus[0].name = username;
		observedGameStatus.playerStatus[0].numOfCardsInHand = 13;
		observedGameStatus.playerStatus[0].playedCard = null;
		observedGameStatus.playerStatus[0].score = scores[0];

		// Left player
		observedGameStatus.playerStatus[1] = new PlayerStatus();
		observedGameStatus.playerStatus[1].isBot = initialTableStatus.whoIsBot[0];
		observedGameStatus.playerStatus[1].name = initialTableStatus.opponents[0];
		observedGameStatus.playerStatus[1].numOfCardsInHand = 13;
		observedGameStatus.playerStatus[1].playedCard = null;
		observedGameStatus.playerStatus[1].score = scores[1];

		// Top player
		observedGameStatus.playerStatus[2] = new PlayerStatus();
		observedGameStatus.playerStatus[2].isBot = initialTableStatus.whoIsBot[1];
		observedGameStatus.playerStatus[2].name = initialTableStatus.opponents[1];
		observedGameStatus.playerStatus[2].numOfCardsInHand = 13;
		observedGameStatus.playerStatus[2].playedCard = null;
		observedGameStatus.playerStatus[2].score = scores[2];

		// Right player
		observedGameStatus.playerStatus[3] = new PlayerStatus();
		observedGameStatus.playerStatus[3].isBot = initialTableStatus.whoIsBot[2];
		observedGameStatus.playerStatus[3].name = initialTableStatus.opponents[2];
		observedGameStatus.playerStatus[3].numOfCardsInHand = 13;
		observedGameStatus.playerStatus[3].playedCard = null;
		observedGameStatus.playerStatus[3].score = scores[3];

		this.cardsGameWidget = new CardsGameWidget(tableSize,
				observedGameStatus, cards, new VerticalPanel(),
				new CardsGameWidget.GameEventListener() {
					@Override
					public void onAnimationStart() {
						if (frozen) {
							System.out
									.println("Client: notice: the onAnimationStart() event was received while frozen, ignoring it.");
							return;
						}

						currentState.handleAnimationStart();
					}

					@Override
					public void onAnimationEnd() {
						if (frozen) {
							System.out
									.println("Client: notice: the onAnimationEnd() event was received while frozen, ignoring it.");
							return;
						}

						currentState.handleAnimationEnd();
					}

					@Override
					public void onCardClicked(int player, Card card,
							State state, boolean isRaised) {
						if (frozen) {
							System.out
									.println("Client: notice: the onCardClicked() event was received while frozen, ignoring it.");
							return;
						}

						currentState.handleCardClicked(player, card, state,
								isRaised);
					}
				});

		players = new ArrayList<PlayerInfo>();
		for (PlayerStatus playerStatus : observedGameStatus.playerStatus) {
			PlayerInfo playerInfo = new PlayerInfo();
			playerInfo.isBot = playerStatus.isBot;
			playerInfo.name = playerStatus.name;
			players.add(playerInfo);
		}

		List<Card> handCards = new ArrayList<Card>();

		for (Card card : cards)
			handCards.add(card);

		transitionToCardPassing(handCards);
	}

	private void transitionTo(PlayerState newState) {
		currentState = newState;
		currentState.activate();
		sendPendingNotifications();
	}

	@Override
	public void transitionToCardPassing(List<Card> hand) {
		if (frozen) {
			System.out
					.println("Client: notice: the transitionToCardPassing() method was called while frozen, ignoring it.");
			return;
		}

		transitionTo(new CardPassingState(cardsGameWidget, this, hand,
				cupidoService));
	}

	@Override
	public void transitionToCardPassingWaiting(List<Card> hand) {
		if (frozen) {
			System.out
					.println("Client: notice: the transitionToCardPassingWaiting() method was called while frozen, ignoring it.");
			return;
		}

		transitionTo(new CardPassingWaitingState(cardsGameWidget, this, hand,
				cupidoService));
	}

	@Override
	public void transitionToEndOfTrick(List<Card> hand) {
		if (frozen) {
			System.out
					.println("Client: notice: the transitionToEndOfTrick() method was called while frozen, ignoring it.");
			return;
		}

		transitionTo(new EndOfTrickState(cardsGameWidget, this, hand,
				cupidoService));
	}

	@Override
	public void transitionToFirstLeader(List<Card> hand) {
		if (frozen) {
			System.out
					.println("Client: notice: the transitionToFirstLeader() method was called while frozen, ignoring it.");
			return;
		}

		transitionTo(new FirstLeaderState(cardsGameWidget, this, hand,
				cupidoService));
	}

	@Override
	public void transitionToWaitingPlayedCard(List<Card> hand) {
		if (frozen) {
			System.out
					.println("Client: notice: the transitionToWaitingPlayedCard() method was called while frozen, ignoring it.");
			return;
		}

		transitionTo(new WaitingPlayedCardState(cardsGameWidget, this, hand,
				cupidoService));
	}

	@Override
	public void transitionToWaitingFirstLead(List<Card> hand) {
		if (frozen) {
			System.out
					.println("Client: notice: the transitionToWaitingFirstLead() method was called while frozen, ignoring it.");
			return;
		}

		transitionTo(new WaitingFirstLeadState(cardsGameWidget, this, hand,
				cupidoService));
	}

	@Override
	public void transitionToYourTurn(List<Card> hand) {
		if (frozen) {
			System.out
					.println("Client: notice: the transitionToYourTurn() method was called while frozen, ignoring it.");
			return;
		}

		transitionTo(new YourTurnState(cardsGameWidget, this, hand,
				cupidoService));
	}

	@Override
	public void transitionToGameEnded() {
		if (frozen) {
			System.out
					.println("Client: notice: the transitionToGameEnded() method was called while frozen, ignoring it.");
			return;
		}

		transitionTo(new GameEndedState(cardsGameWidget, this, cupidoService));
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

		if (card.suit == Card.Suit.HEARTS)
			heartsBroken = true;
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
		if (frozen) {
			System.out
					.println("Client: notice: the exit() method was called while frozen, ignoring it.");
			return;
		}

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
	public boolean areHeartsBroken() {
		return heartsBroken;
	}

	@Override
	public void freeze() {
		currentState.freeze();
	}

	@Override
	public void handleCardPassed(Card[] cards) {
		if (frozen) {
			System.out
					.println("Client: notice: the handleCardPassed() event was received while frozen, ignoring it.");
			return;
		}

		boolean handled = currentState.handleCardPassed(cards);
		if (!handled)
			pendingNotifications.add(new CardPassed(cards));
	}

	@Override
	public void handleCardPlayed(Card card, int playerPosition) {
		if (frozen) {
			System.out
					.println("Client: notice: the handleCardPlayed() event was received while frozen, ignoring it.");
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
					.println("Client: notice: the handleGameEnded() event was received while frozen, ignoring it.");
			return;
		}
		boolean handled = currentState.handleGameEnded(matchPoints,
				playersTotalPoints);
		if (!handled)
			pendingNotifications.add(new GameEnded(matchPoints,
					playersTotalPoints));
	}

	@Override
	public void handleGameStarted(Card[] myCards) {
		if (frozen) {
			System.out
					.println("Client: notice: the handleGameStarted() event was received while frozen, ignoring it.");
			return;
		}
		boolean handled = currentState.handleGameStarted(myCards);
		if (!handled)
			pendingNotifications.add(new GameStarted(myCards));
	}

	@Override
	public void handlePlayerLeft(String player) {
		if (frozen) {
			System.out
					.println("Client: notice: the handlePlayerLeft() event was received while frozen, ignoring it.");
			return;
		}
		int i = 1;
		while (i < 4 && players.get(i).name.equals(player))
			i++;
		if (i == 4) {
			onFatalException(new Exception("An invalid PlayerLeft notification was received."));
			return;
		}
		PlayerInfo x = players.get(i);
		x.isBot = true;
		x.name = null;
		// TODO: Update cardsGameWidget with the new player information.
		currentState.handlePlayerLeft(i);
	}

	private static void printCards(Card[] cards) {
		boolean first = true;
		for (Card card : cards) {
			if (!first)
				System.out.print(", ");
			first = false;
			System.out.println(card.toString());
		}
	}

	private void printPendingNotifications() {
		System.out.print("{ ");
		boolean first = true;
		for (Serializable x : pendingNotifications) {
			if (!first)
				System.out.println(",");
			System.out.print("  ");
			first = false;
			if (x instanceof CardPassed) {
				CardPassed message = (CardPassed) x;
				System.out.print("CardPassed(");
				printCards(message.cards);
				System.out.print(")");

			} else if (x instanceof CardPlayed) {
				CardPlayed message = (CardPlayed) x;
				System.out.print("CardPlayed(");
				System.out.print(message.card + ", " + message.playerPosition);
				System.out.print(")");

			} else if (x instanceof GameEnded) {
				System.out.print("GameEnded(...)");

			} else if (x instanceof GameStarted) {
				GameStarted message = (GameStarted) x;
				System.out.print("GameStarted(");
				printCards(message.myCards);
				System.out.print(")");

			} else {
				assert false;
			}
		}
		System.out.println("}");
	}

	private void sendPendingNotifications() {
		// TODO: Remove these lines.
		// System.out.println("Client: PlayerStateManagerImpl: in sendPendingNotifications(): "
		// + pendingNotifications.size() + " notifications pending.");
		// printPendingNotifications();

		List<Serializable> list = pendingNotifications;
		// Note that this may be modified in the calls to handle*() methods
		// below.
		pendingNotifications = new ArrayList<Serializable>();

		for (Serializable x : list) {
			if (x instanceof CardPassed) {
				CardPassed message = (CardPassed) x;
				handleCardPassed(message.cards);

			} else if (x instanceof CardPlayed) {
				CardPlayed message = (CardPlayed) x;
				handleCardPlayed(message.card, message.playerPosition);

			} else if (x instanceof GameEnded) {
				GameEnded message = (GameEnded) x;
				handleGameEnded(message.matchPoints, message.playersTotalPoints);

			} else if (x instanceof GameStarted) {
				GameStarted message = (GameStarted) x;
				handleGameStarted(message.myCards);

			} else {
				assert false;
			}
		}
		// TODO: Remove these lines.
		// System.out.println("Client: PlayerStateManagerImpl: exiting from sendPendingNotifications(): "
		// + pendingNotifications.size() + " notifications are still pending.");
		// printPendingNotifications();
	}

	@Override
	public void onFatalException(Throwable e) {
		screenManager.displayGeneralErrorScreen(e);
	}
}
