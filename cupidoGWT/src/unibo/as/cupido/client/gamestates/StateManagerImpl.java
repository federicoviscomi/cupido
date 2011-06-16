package unibo.as.cupido.client.gamestates;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.VerticalPanel;

import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.InitialTableStatus;
import unibo.as.cupido.backendInterfaces.common.ObservedGameStatus;
import unibo.as.cupido.backendInterfaces.common.PlayerStatus;
import unibo.as.cupido.client.CardsGameWidget;
import unibo.as.cupido.client.CardsGameWidget.CardRole.State;
import unibo.as.cupido.client.screens.ScreenSwitcher;

public class StateManagerImpl implements StateManager {
	
	private Object currentState = null;
	private ScreenSwitcher screenSwitcher;
	private CardsGameWidget cardsGameWidget;
	private int firstPlayerInTrick = -1;
	
	// FIXME: This is guaranteed to be correct for players only.
	boolean heartsBroken = false;
		
	/**
	 * Some information about the players.
	 * The first element refers to the bottom player, and the other players
	 * follow in clockwise order.
	 */
	private List<PlayerInfo> players;
		
	/**
	 * The (ordered) list of cards dealt in the current trick.
	 */
	private List<Card> dealtCards = new ArrayList<Card>();
	
	/**
	 * Initialize the state manager. The current user is a viewer.
	 */
	public StateManagerImpl(int tableSize, ScreenSwitcher screenSwitcher, ObservedGameStatus observedGameStatus) {
		
		this.screenSwitcher = screenSwitcher;
		this.cardsGameWidget = new CardsGameWidget(tableSize, observedGameStatus,
				null, new VerticalPanel(),
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
		for (PlayerStatus playerStatus : observedGameStatus.ogs) {
			PlayerInfo playerInfo = new PlayerInfo();
			playerInfo.isBot = playerStatus.isBot;
			playerInfo.name = playerStatus.name;
			players.add(playerInfo);
		}
		
		transitionToCardPassingAsViewer();
	}

	/**
	 * Initialize the state manager. The current user is a player, and his hand cards are `cards'.
	 */
	public StateManagerImpl(int tableSize, ScreenSwitcher screenSwitcher,
			InitialTableStatus initialTableStatus, Card[] cards, String username) {
		this.screenSwitcher = screenSwitcher;
		
		for (String opponent : initialTableStatus.opponents)
			assert opponent != null;
		
		ObservedGameStatus observedGameStatus = new ObservedGameStatus();
		observedGameStatus.ogs = new PlayerStatus[4];
		
		// Bottom player
		observedGameStatus.ogs[0] = new PlayerStatus();
		observedGameStatus.ogs[0].isBot = false;
		observedGameStatus.ogs[0].name = username;
		observedGameStatus.ogs[0].numOfCardsInHand = 13;
		observedGameStatus.ogs[0].playedCard = null;
		observedGameStatus.ogs[0].score = initialTableStatus.playerPoints[0];

		// Left player
		observedGameStatus.ogs[1] = new PlayerStatus();
		observedGameStatus.ogs[1].isBot = initialTableStatus.whoIsBot[0];
		observedGameStatus.ogs[1].name = initialTableStatus.opponents[0];
		observedGameStatus.ogs[1].numOfCardsInHand = 13;
		observedGameStatus.ogs[1].playedCard = null;
		observedGameStatus.ogs[1].score = initialTableStatus.playerPoints[1];

		// Top player
		observedGameStatus.ogs[2] = new PlayerStatus();
		observedGameStatus.ogs[2].isBot = initialTableStatus.whoIsBot[1];
		observedGameStatus.ogs[2].name = initialTableStatus.opponents[1];
		observedGameStatus.ogs[2].numOfCardsInHand = 13;
		observedGameStatus.ogs[2].playedCard = null;
		observedGameStatus.ogs[2].score = initialTableStatus.playerPoints[2];

		// Right player
		observedGameStatus.ogs[3] = new PlayerStatus();
		observedGameStatus.ogs[3].isBot = initialTableStatus.whoIsBot[2];
		observedGameStatus.ogs[3].name = initialTableStatus.opponents[2];
		observedGameStatus.ogs[3].numOfCardsInHand = 13;
		observedGameStatus.ogs[3].playedCard = null;
		observedGameStatus.ogs[3].score = initialTableStatus.playerPoints[3];
		
		this.cardsGameWidget = new CardsGameWidget(tableSize, observedGameStatus,
				cards, new VerticalPanel(),
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
		for (PlayerStatus playerStatus : observedGameStatus.ogs) {
			PlayerInfo playerInfo = new PlayerInfo();
			playerInfo.isBot = playerStatus.isBot;
			playerInfo.name = playerStatus.name;
			players.add(playerInfo);
		}
		
		List<Card> handCards = new ArrayList<Card>();
		
		for (Card card : cards)
			handCards.add(card);
		
		transitionToCardPassingAsPlayer(handCards);
	}

	@Override
	public void transitionToCardPassingAsPlayer(List<Card> hand) {
		currentState = new CardPassingAsPlayer(cardsGameWidget, this, hand);
	}

	@Override
	public void transitionToCardPassingAsViewer() {
		currentState = new CardPassingAsViewer(cardsGameWidget, this);
	}

	@Override
	public void transitionToCardPassingWaitingAsPlayer(List<Card> hand) {
		currentState = new CardPassingWaitingAsPlayer(cardsGameWidget, this, hand);
	}

	@Override
	public void transitionToEndOfTrickAsPlayer(List<Card> hand) {
		currentState = new EndOfTrickAsPlayer(cardsGameWidget, this, hand);
	}

	@Override
	public void transitionToEndOfTrickAsViewer() {
		currentState = new EndOfTrickAsViewer(cardsGameWidget, this);
	}

	@Override
	public void transitionToFirstDealer(List<Card> hand) {
		currentState = new FirstDealer(cardsGameWidget, this, hand);
	}

	@Override
	public void transitionToWaitingDealAsPlayer(List<Card> hand) {
		currentState = new WaitingDealAsPlayer(cardsGameWidget, this, hand);
	}

	@Override
	public void transitionToWaitingFirstDealAsPlayer(List<Card> hand) {
		currentState = new WaitingFirstDealAsPlayer(cardsGameWidget, this, hand);
	}

	@Override
	public void transitionToWaitingFirstDealAsViewer() {
		currentState = new WaitingFirstDealAsViewer(cardsGameWidget, this);
	}

	@Override
	public void transitionToYourTurn(List<Card> hand) {
		currentState = new YourTurn(cardsGameWidget, this, hand);
	}

	@Override
	public void transitionToGameEndedAsPlayer() {
		currentState = new GameEndedAsPlayer(cardsGameWidget, this);
	}
	
	@Override
	public void transitionToGameEndedAsViewer() {
		currentState = new GameEndedAsViewer(cardsGameWidget, this);
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
	 * @param cards An ordered list containing the cards in the current trick.
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
}
