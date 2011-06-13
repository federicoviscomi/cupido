package unibo.as.cupido.backendInterfacesImpl.table;

import java.rmi.RemoteException;
import java.sql.SQLException;

import unibo.as.cupido.backendInterfaces.GlobalTableManagerInterface;
import unibo.as.cupido.backendInterfaces.GlobalTableManagerInterface.Table;
import unibo.as.cupido.backendInterfaces.GlobalTableManagerInterface.TableDescriptor;
import unibo.as.cupido.backendInterfaces.ServletNotificationsInterface;
import unibo.as.cupido.backendInterfaces.TableInterface;
import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.ChatMessage;
import unibo.as.cupido.backendInterfaces.common.DatabaseManager;
import unibo.as.cupido.backendInterfaces.common.InitialTableStatus;
import unibo.as.cupido.backendInterfaces.common.ObservedGameStatus;
import unibo.as.cupido.backendInterfaces.common.PlayerStatus;
import unibo.as.cupido.backendInterfaces.exception.FullTableException;
import unibo.as.cupido.backendInterfaces.exception.IllegalMoveException;
import unibo.as.cupido.backendInterfaces.exception.NoSuchTableException;
import unibo.as.cupido.backendInterfaces.exception.NoSuchUserException;
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
			SingleTableManager stm = new SingleTableManager(sni, new Table(
					"Owner", 0, new TableDescriptor("servercane", 34453)), null);
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
	private BotManager botManager;
	private final Table table;
	

	public SingleTableManager(ServletNotificationsInterface snf, Table table,
			GlobalTableManagerInterface gtm) throws RemoteException {
		this.table = table;
		toNotify = new ToBeNotifyed();
		cardsManager = new CardsManager();
		playersManager = new PlayersManager(table.owner, false);
		botManager = new BotManager();
		toNotify.notifyPlayerJoined(table.owner, 0, snf);
		gameStatus = GameStatus.INIT;
	}

	@Override
	public void addBot(String userName, int position)
			throws PositionFullException, FullTableException,
			NotCreatorException {
		if (gameStatus.equals(GameStatus.ENDED)) {
			throw new IllegalStateException();
		}
		if (!playersManager.isCreator(userName))
			throw new NotCreatorException();
		playersManager.addBot(userName, position);
		toNotify.notifyBotJoined(userName, position, 0);
		botManager.addBot(position, cardsManager.cards[position]);
		if (playersManager.getPlayersCount() == 4) {
			if (gameStatus.equals(GameStatus.INIT)) {
				gameStatus = GameStatus.PASSING_CARDS;
				botPassCard();
			} else {
				if (gameStatus.equals(GameStatus.PASSING_CARDS)) {
					gameStatus = GameStatus.STARTED;
				}
				botPlayCard();
			}
		}
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
		if (cardsManager.allPlayerPassedCards()) {
			gameStatus = GameStatus.STARTED;
			cardsManager.passCards();
			toNotify.notifyGameStarted(null, null);
		}
	}

	private void botPlayCard() {
		try {
			int position = cardsManager.getCurrentPlayer();
			while (playersManager.isBot(position)) {
				Card card = botManager.chooseCardToPlay(position);
				cardsManager.playCard(position, card);
				toNotify.notifyCardPlayed("bot", card, position);
				if (cardsManager.allPlayerPlayedCards()) {
					if (gameStatus.equals(GameStatus.STARTED)) {
						int winner = cardsManager.getWinner();
						int points = cardsManager.getPoints();
						playersManager.addPoint(winner, points);
						gameStatus = GameStatus.STARTED;
					}
					if (cardsManager.gameEnded()) {
						// FIXME
						toNotify.notifyGameEnded(playersManager.getAllPoints(),
								playersManager.getAllPoints());
					}
				}
				position++;
			}
			if (gameStatus.ordinal() == GameStatus.STARTED.ordinal()) {
				int winner = cardsManager.getWinner();
				int points = cardsManager.getPoints();
				playersManager.addPoint(winner, points);
				gameStatus = GameStatus.STARTED;
			}
			if (cardsManager.gameEnded()) {
				// FIXME
				toNotify.notifyGameEnded(playersManager.getAllPoints(),
						playersManager.getAllPoints());
			}
		} catch (IllegalMoveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public InitialTableStatus joinTable(String playerName,
			ServletNotificationsInterface snf) throws FullTableException,
			NoSuchTableException {
		if (!gameStatus.equals(GameStatus.INIT)) {
			throw new IllegalStateException();
		}
		if (playerName == null || snf == null) {
			throw new IllegalArgumentException();
		}
		InitialTableStatus its = playersManager.addPlayer(playerName);
		toNotify.notifyPlayerJoined(playerName,
				playersManager.getPlayerPosition(playerName), snf);
		if (playersManager.getPlayersCount() == 4) {
			gameStatus = GameStatus.PASSING_CARDS;
			botPassCard();
		}
		return its;
	}

	@Override
	public void leaveTable(String playerName) throws PlayerNotFoundException {
		if (gameStatus.equals(GameStatus.ENDED)) {
			throw new IllegalStateException();
		}
		int position = playersManager.getPlayerPosition(playerName);
		if (gameStatus.equals(GameStatus.INIT)) {
			playersManager.removePlayer(playerName);
			toNotify.removePlayer(playerName);
		} else {
			try {
				this.addBot(
						playersManager.getPlayerName(Positions.OWNER.ordinal()),
						position);
			} catch (PositionFullException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FullTableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NotCreatorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void passCards(String userName, Card[] passedCards)
			throws IllegalArgumentException {
		if (gameStatus.ordinal() != GameStatus.PASSING_CARDS.ordinal())
			throw new IllegalStateException();
		if (userName == null || passedCards == null || passedCards.length != 3)
			throw new IllegalArgumentException();
		int position = playersManager.getPlayerPosition(userName);
		if (position == -1)
			throw new IllegalArgumentException("invalid user name");
		cardsManager.setCardPassing(position, passedCards);
		toNotify.notifyCardPassed(passedCards,
				playersManager.getPlayerName((position + 1) % 4));
		if (cardsManager.allPlayerPassedCards()) {
			gameStatus = GameStatus.STARTED;
			cardsManager.passCards();
			toNotify.notifyGameStarted(userName, passedCards);
		}
	}

	@Override
	public void playCard(String userName, Card card)
			throws IllegalMoveException {
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
				int runningPlayer = playersManager.getRunningPlayer();
				if (runningPlayer == -1) {

				} else {
					DatabaseManager databaseManager = new DatabaseManager();
					for (String player : playersManager.getPlayersName()) {
						try {
							databaseManager.updateScore(player, databaseManager.getPlayerScore(player));
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (NoSuchUserException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					databaseManager.close();
				}
				// FIXME
				toNotify.notifyGameEnded(playersManager.getAllPoints(),
						playersManager.getAllPoints());
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
	public ObservedGameStatus viewTable(String userName,
			ServletNotificationsInterface snf) throws NoSuchTableException,
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
			ps[i] = new PlayerStatus(playersManager.players[i].name,
					playersManager.players[i].points,
					cardsManager.cardPlayed[i], cardsManager.cards[i].size(),
					playersManager.players[i].isBot);
		return ogs;
	}

	@Override
	public Table getTable() throws RemoteException {
		return table;
	}
}
