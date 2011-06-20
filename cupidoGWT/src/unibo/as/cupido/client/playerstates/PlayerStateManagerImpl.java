package unibo.as.cupido.client.playerstates;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.InitialTableStatus;
import unibo.as.cupido.common.structures.ObservedGameStatus;
import unibo.as.cupido.common.structures.PlayerStatus;
import unibo.as.cupido.client.CardsGameWidget;
import unibo.as.cupido.client.CardsGameWidget.CardRole.State;
import unibo.as.cupido.client.screens.ScreenManager;
import unibo.as.cupido.shared.cometNotification.CardPassed;
import unibo.as.cupido.shared.cometNotification.CardPlayed;
import unibo.as.cupido.shared.cometNotification.GameEnded;
import unibo.as.cupido.shared.cometNotification.GameStarted;
import unibo.as.cupido.shared.cometNotification.NewPlayerJoined;
import unibo.as.cupido.shared.cometNotification.PlayerLeft;

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
	 * The (ordered) list of cards dealt in the current trick.
	 */
	private List<Card> dealtCards = new ArrayList<Card>();
	
	private String username;

	/**
	 * Initialize the state manager. The current user is a player, and his hand
	 * cards are `cards'.
	 */
	public PlayerStateManagerImpl(int tableSize, ScreenManager screenManager,
			InitialTableStatus initialTableStatus, Card[] cards, String username) {
		this.username = username;
		this.screenManager = screenManager;

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
		observedGameStatus.playerStatus[0].score = initialTableStatus.playerScores[0];

		// Left player
		observedGameStatus.playerStatus[1] = new PlayerStatus();
		observedGameStatus.playerStatus[1].isBot = initialTableStatus.whoIsBot[0];
		observedGameStatus.playerStatus[1].name = initialTableStatus.opponents[0];
		observedGameStatus.playerStatus[1].numOfCardsInHand = 13;
		observedGameStatus.playerStatus[1].playedCard = null;
		observedGameStatus.playerStatus[1].score = initialTableStatus.playerScores[1];

		// Top player
		observedGameStatus.playerStatus[2] = new PlayerStatus();
		observedGameStatus.playerStatus[2].isBot = initialTableStatus.whoIsBot[1];
		observedGameStatus.playerStatus[2].name = initialTableStatus.opponents[1];
		observedGameStatus.playerStatus[2].numOfCardsInHand = 13;
		observedGameStatus.playerStatus[2].playedCard = null;
		observedGameStatus.playerStatus[2].score = initialTableStatus.playerScores[2];

		// Right player
		observedGameStatus.playerStatus[3] = new PlayerStatus();
		observedGameStatus.playerStatus[3].isBot = initialTableStatus.whoIsBot[2];
		observedGameStatus.playerStatus[3].name = initialTableStatus.opponents[2];
		observedGameStatus.playerStatus[3].numOfCardsInHand = 13;
		observedGameStatus.playerStatus[3].playedCard = null;
		observedGameStatus.playerStatus[3].score = initialTableStatus.playerScores[3];

		this.cardsGameWidget = new CardsGameWidget(tableSize,
				observedGameStatus, cards, new VerticalPanel(),
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

		List<Card> handCards = new ArrayList<Card>();

		for (Card card : cards)
			handCards.add(card);

		transitionToCardPassing(handCards);
	}

	@Override
	public void transitionToCardPassing(List<Card> hand) {
		currentState = new CardPassingState(cardsGameWidget, this, hand);
		sendPendingNotifications();
	}

	@Override
	public void transitionToCardPassingWaiting(List<Card> hand) {
		currentState = new CardPassingWaitingState(cardsGameWidget, this, hand);
		sendPendingNotifications();
	}

	@Override
	public void transitionToEndOfTrick(List<Card> hand) {
		currentState = new EndOfTrickState(cardsGameWidget, this, hand);
		sendPendingNotifications();
	}

	@Override
	public void transitionToFirstDealer(List<Card> hand) {
		currentState = new FirstDealerState(cardsGameWidget, this, hand);
		sendPendingNotifications();
	}

	@Override
	public void transitionToWaitingDeal(List<Card> hand) {
		currentState = new WaitingDealState(cardsGameWidget, this, hand);
		sendPendingNotifications();
	}

	@Override
	public void transitionToWaitingFirstDeal(List<Card> hand) {
		currentState = new WaitingFirstDealState(cardsGameWidget, this, hand);
		sendPendingNotifications();
	}

	@Override
	public void transitionToYourTurn(List<Card> hand) {
		currentState = new YourTurnState(cardsGameWidget, this, hand);
		sendPendingNotifications();
	}

	@Override
	public void transitionToGameEnded() {
		currentState = new GameEndedState(cardsGameWidget, this);
		sendPendingNotifications();
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
		if (card.suit == Card.Suit.HEARTS)
			heartsBroken = true;
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
	public boolean areHeartsBroken() {
		return heartsBroken;
	}
	
	@Override
	public void disableControls() {
		currentState.disableControls();
	}

	@Override
	public void handleCardPassed(Card[] cards) {
		boolean handled = currentState.handleCardPassed(cards);
		if (!handled)
			pendingNotifications.add(new CardPassed(cards));
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
	public void handleGameStarted(Card[] myCards) {
		boolean handled = currentState.handleGameStarted(myCards);
		if (!handled)
			pendingNotifications.add(new GameStarted(myCards));
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
				
			} else if (x instanceof PlayerLeft) {
				PlayerLeft message = (PlayerLeft) x;
				handlePlayerLeft(message.player);
				
			} else {
				assert false;
			}
		}
	}
}
