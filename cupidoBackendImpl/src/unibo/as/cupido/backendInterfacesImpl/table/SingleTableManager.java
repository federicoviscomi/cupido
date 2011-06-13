package unibo.as.cupido.backendInterfacesImpl.table;

import java.rmi.RemoteException;
import unibo.as.cupido.backendInterfaces.GlobalTableManagerInterface;
import unibo.as.cupido.backendInterfaces.ServletNotificationsInterface;
import unibo.as.cupido.backendInterfaces.TableInterface;
import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.ChatMessage;
import unibo.as.cupido.backendInterfaces.common.InitialTableStatus;
import unibo.as.cupido.backendInterfaces.common.ObservedGameStatus;
import unibo.as.cupido.backendInterfaces.common.PlayerStatus;
import unibo.as.cupido.backendInterfaces.common.TableDescriptor;
import unibo.as.cupido.backendInterfaces.common.TableInfoForClient;
import unibo.as.cupido.backendInterfaces.exception.FullTableException;
import unibo.as.cupido.backendInterfaces.exception.IllegalMoveException;
import unibo.as.cupido.backendInterfaces.exception.NoSuchTableException;
import unibo.as.cupido.backendInterfaces.exception.NotCreatorException;
import unibo.as.cupido.backendInterfaces.exception.PlayerNotFoundException;
import unibo.as.cupido.backendInterfaces.exception.PositionFullException;
import unibo.as.cupido.backendInterfacesImpl.DummyLoggerServletNotifyer;

/**
 * 
 * @author cane
 * 
 */
public class SingleTableManager implements TableInterface {

	public static void main(String[] args) throws Exception {
		try {
			// TODO implement the following class
			ServletNotificationsInterface sni = new DummyLoggerServletNotifyer();
			SingleTableManager stm = new SingleTableManager(sni,
					new TableInfoForClient("Owner", 0, new TableDescriptor(
							"servercane", 34453)), null);
			stm.addBot("bot", 1);
			stm.joinTable("Cane", sni);
			stm.joinTable("Gatto", sni);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private CheckGameStatus checkGameStatus;
	private CardsManager cardsManager;
	private PlayersManager playersManager;
	private ToBeNotifyed toNotify;
	private BotManager botManager;
	private final TableInfoForClient table;

	public SingleTableManager(ServletNotificationsInterface snf,
			TableInfoForClient table, GlobalTableManagerInterface gtm)
			throws RemoteException {
		this.table = table;
		toNotify = new ToBeNotifyed();
		checkGameStatus = new CheckGameStatus();
		cardsManager = new CardsManager();
		playersManager = new PlayersManager(table.owner, false);
		botManager = new BotManager();
		toNotify.notifyPlayerJoined(table.owner, 0, snf);
	}

	@Override
	public void addBot(String userName, int position)
			throws PositionFullException, FullTableException,
			NotCreatorException {
		checkGameStatus.checkAddBot();
		playersManager.addBot(userName, position);
		toNotify.notifyBotJoined(userName, position, 0);
		botManager.addBot(position, cardsManager.cards[position]);
		updateGameStatus();
	}

	private void botPassCard() {
		for (int i = 1; i < 4; i++) {
			if (playersManager.isBot(i)) {
				Card[] cardsToPass = botManager.chooseCardsToPass(i);
				cardsManager.setCardPassing(i, cardsToPass);
				toNotify.notifyCardPassed(cardsToPass,
						playersManager.getPlayerName((i + 1) % 4));
			}
		}
	}

	private void botPlayCard() {
		try {
			int position = cardsManager.getCurrentPlayer();
			while (playersManager.isBot(position)) {
				Card card = botManager.chooseCardToPlay(position);
				cardsManager.playCard(position, card);
				toNotify.notifyCardPlayed("bot", card, position);
				position++;
			}
		} catch (IllegalMoveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void endGame() {
		playersManager.updateScore(cardsManager.getWinners());
		toNotify.notifyGameEnded(cardsManager.points,
				playersManager.getScores());
	}

	@Override
	public TableInfoForClient getTable() throws RemoteException {
		return table;
	}

	@Override
	public InitialTableStatus joinTable(String playerName,
			ServletNotificationsInterface snf) throws FullTableException,
			NoSuchTableException {
		if (playerName == null || snf == null) {
			throw new IllegalArgumentException();
		}
		InitialTableStatus its = playersManager.addPlayer(playerName);
		toNotify.notifyPlayerJoined(playerName,
				playersManager.getPlayerPosition(playerName), snf);
		return its;
	}

	@Override
	public void leaveTable(String playerName) throws PlayerNotFoundException {

	}

	private void updateGameStatus() {
		switch (checkGameStatus.gameStatus) {
		case INIT: {
			if (playersManager.getPlayersCount() == 4)
				checkGameStatus.gameStatus = GameStatus.PASSING_CARDS;
			break;
		}
		case PASSING_CARDS: {
			if (cardsManager.allPlayerPassedCards())
				checkGameStatus.gameStatus = GameStatus.STARTED;
			break;
		}
		case STARTED: {
			if (cardsManager.gameEnded())
				checkGameStatus.gameStatus = GameStatus.ENDED;
			break;
		}
		case ENDED: {
			//
		}
		}
	}

	@Override
	public void passCards(String userName, Card[] passedCards)
			throws IllegalArgumentException {
		if (userName == null || passedCards == null || passedCards.length != 3)
			throw new IllegalArgumentException();
		int position = playersManager.getPlayerPosition(userName);
		if (position == -1)
			throw new IllegalArgumentException("invalid user name");
		cardsManager.setCardPassing(position, passedCards);
		toNotify.notifyCardPassed(passedCards,
				playersManager.getPlayerName((position + 1) % 4));
	}

	@Override
	public void playCard(String userName, Card card)
			throws IllegalMoveException {
		int position = playersManager.getPlayerPosition(userName);
		cardsManager.playCard(position, card);
		toNotify.notifyCardPlayed(userName, card, position);

		if (cardsManager.gameEnded()) {
			endGame();
		}

	}

	@Override
	public void sendMessage(ChatMessage message) {
		toNotify.notifyMessageSent(message);
	}

	@Override
	public ObservedGameStatus viewTable(String userName,
			ServletNotificationsInterface snf) throws NoSuchTableException,
			IllegalArgumentException, IllegalStateException {
		if (userName == null || snf == null)
			throw new IllegalArgumentException();
		toNotify.viewerJoined(userName, snf);
		ObservedGameStatus ogs = new ObservedGameStatus();
		PlayerStatus[] ps = new PlayerStatus[4];
		for (int i = 0; i < 4; i++)
			ps[i] = new PlayerStatus(playersManager.players[i].name,
					playersManager.players[i].score,
					cardsManager.cardPlayed[i], cardsManager.cards[i].size(),
					playersManager.players[i].isBot);
		return ogs;
	}
}
