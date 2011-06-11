package unibo.as.cupido.backendInterfacesImpl;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.Collections;

import unibo.as.cupido.backendInterfaces.GlobalTableManagerInterface;
import unibo.as.cupido.backendInterfaces.GlobalTableManagerInterface.Table;
import unibo.as.cupido.backendInterfaces.GlobalTableManagerInterface.TableDescriptor;
import unibo.as.cupido.backendInterfaces.ServletNotifcationsInterface;
import unibo.as.cupido.backendInterfaces.TableInterface;
import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.ChatMessage;
import unibo.as.cupido.backendInterfaces.common.FullTableException;
import unibo.as.cupido.backendInterfaces.common.IllegalMoveException;
import unibo.as.cupido.backendInterfaces.common.InitialTableStatus;
import unibo.as.cupido.backendInterfaces.common.NoSuchTableException;
import unibo.as.cupido.backendInterfaces.common.ObservedGameStatus;
import unibo.as.cupido.backendInterfaces.common.PlayerNotFoundException;
import unibo.as.cupido.backendInterfaces.common.PlayerStatus;
import unibo.as.cupido.backendInterfaces.common.PositionFullException;

/**
 * 
 * @author cane
 * 
 */
public class SingleTableManager implements TableInterface {

	public static void main(String[] args) throws Exception {
		try {
			// TODO implement the following class
			ServletNotifcationsInterface sni = new DummyLoggerServletNotifyer();
			SingleTableManager stm = new SingleTableManager(sni, new Table("Owner", 0, new TableDescriptor(
					"servercane", 34453)), null);
			stm.printGameStatus();
			stm.addBot("bot", 1);
			stm.printGameStatus();
			stm.joinTable("Cane", sni);
			stm.printGameStatus();
			stm.joinTable("Gatto", sni);
			stm.printGameStatus();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private CardsManager cardsManager;

	/* posso spostarlo in CardsManagers? */
	private GameStatus gameStatus;

	private PlayersManager playersManager;
	private ToBeNotifyed toNotify;

	public SingleTableManager(ServletNotifcationsInterface snf, Table table, GlobalTableManagerInterface gtm)
			throws RemoteException {
		toNotify = new ToBeNotifyed();
		cardsManager = new CardsManager();
		playersManager = new PlayersManager(table.owner, false);

		System.out.println(snf + " " + table + " " + gtm);

		// toNotify = new ToNotify();
		toNotify.notifyPlayerJoined(table.owner, 0, snf);
		// cardsManager = new CardsManager();
		gameStatus = GameStatus.INIT;
	}

	@Override
	public void addBot(String botName, int position) throws PositionFullException, FullTableException {
		if (gameStatus.equals(GameStatus.ENDED)) {
			throw new IllegalStateException();
		}
		playersManager.addBot(botName, position);
		toNotify.notifyBotJoined(botName, position, 0);
		if (playersManager.getPlayersCount() == 4) {
			if (gameStatus.equals(GameStatus.INIT)) {
				if (playersManager.getPlayersCount() == 4) {
					gameStatus = GameStatus.PASSING_CARDS;
					botPassCard();
				}
			} else if (gameStatus.equals(GameStatus.PASSING_CARDS)) {
				gameStatus = GameStatus.STARTED;
				botPlayCard();
			} else if (gameStatus.equals(GameStatus.STARTED)) {
				botPlayCard();
			}
		}
	}

	private void botPlayCard() {
		// TODO Auto-generated method stub

	}

	@Override
	public InitialTableStatus joinTable(String playerName, ServletNotifcationsInterface snf) throws FullTableException,
			NoSuchTableException {
		if (!gameStatus.equals(GameStatus.INIT)) {
			throw new IllegalStateException();
		}
		if (playerName == null || snf == null) {
			throw new IllegalArgumentException();
		}
		InitialTableStatus its = playersManager.addPlayer(playerName);
		toNotify.notifyPlayerJoined(playerName, playersManager.getPlayerPosition(playerName), snf);
		if (playersManager.getPlayersCount() == 4) {
			gameStatus = GameStatus.PASSING_CARDS;
			botPassCard();
		}
		return its;
	}

	private void botPassCard() {
		// TODO
		if (cardsManager.allPlayerPassedCards()) {
			gameStatus = GameStatus.STARTED;
			cardsManager.passCards();
			toNotify.notifyGameStarted(null, null);
		}
	}

	@Override
	public void leaveTable(String playerName) throws PlayerNotFoundException {
		if (gameStatus.equals(GameStatus.ENDED)) {
			throw new IllegalStateException();
		}
		int position = playersManager.getPlayerPosition(playerName);
		if (gameStatus.equals(GameStatus.INIT)) {
			playersManager.removePlayer(playerName);
			toNotify.remove(playerName);
		} else {
			try {
				this.addBot("_bot." + playerName, position);
			} catch (PositionFullException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FullTableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void passCards(String userName, Card[] passedCards) throws IllegalArgumentException {
		if (gameStatus.ordinal() != GameStatus.PASSING_CARDS.ordinal())
			throw new IllegalStateException();
		if (userName == null || passedCards == null || passedCards.length != 3)
			throw new IllegalArgumentException();
		int position = playersManager.getPlayerPosition(userName);
		if (position == -1)
			throw new IllegalArgumentException("invalid user name");
		cardsManager.setCardPassing(position, passedCards);
		toNotify.notifyCardPassed(passedCards, playersManager.getPlayerName((position + 1) % 4));
		if (cardsManager.allPlayerPassedCards()) {
			gameStatus = GameStatus.STARTED;
			cardsManager.passCards();
			toNotify.notifyGameStarted(userName, passedCards);
		}
	}

	@Override
	public void playCard(String userName, Card card) throws IllegalMoveException {
		if (gameStatus.ordinal() != GameStatus.STARTED.ordinal()
				&& gameStatus.ordinal() != GameStatus.STARTED.ordinal()) {
			throw new IllegalStateException();
		}
		int position = playersManager.getPlayerPosition(userName);
		cardsManager.playCard(position, card);
		toNotify.notifyCardPlayed(userName, card, position);
		if (cardsManager.allPlayerPlayedCards()) {
			if (gameStatus.ordinal() == GameStatus.STARTED.ordinal()) {
				int winner = cardsManager.getWinner();
				int points = cardsManager.getPoints();
				playersManager.addPoint(winner, points);
				gameStatus = GameStatus.STARTED;
			}
			if (cardsManager.gameEnded()) {
				// FIXME
				toNotify.notifyGameEnded(playersManager.getAllPoints(), playersManager.getAllPoints());
			}
		}
	}

	private void printGameStatus() {
		System.out.format("\n\nGame status: %s", gameStatus.toString());
		cardsManager.print(playersManager);
	}

	@Override
	public void sendMessage(ChatMessage message) {
		toNotify.notifyMessageSent(message);
	}

	@Override
	public ObservedGameStatus viewTable(String userName, ServletNotifcationsInterface snf) throws NoSuchTableException,
			IllegalArgumentException, IllegalStateException {
		if (gameStatus.equals(GameStatus.ENDED)) {
			throw new IllegalStateException();
		}
		if (userName == null || snf == null)
			throw new IllegalArgumentException();
		toNotify.viewerJoined(userName, snf);
		ObservedGameStatus ogs = new ObservedGameStatus();
		PlayerStatus[] ps = new PlayerStatus[4];
		for (int i = 0; i < 4; i++)
			ps[i] = new PlayerStatus(playersManager.players[i].name, playersManager.players[i].score,
					cardsManager.cardPlayed[i], cardsManager.cards[i].size(), playersManager.players[i].isBot);
		return ogs;
	}
}
