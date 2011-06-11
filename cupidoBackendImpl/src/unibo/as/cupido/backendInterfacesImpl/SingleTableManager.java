package unibo.as.cupido.backendInterfacesImpl;

import java.rmi.RemoteException;

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

	/**
	 * Vi è anche un secondo modo di vincere la partita: un giocatore vince se
	 * riesce a prendere tutte le carte di cuori e la donna di picche lasciando
	 * gli avversari a zero punti. Regola aggiuntiva: un giocatore primo di mano
	 * può giocare una carta di cuori solo se prima nella mano qualcuno ha
	 * giocato una carta di cuori.
	 * 
	 * @author cane
	 * 
	 */

	static enum GameStatus {
		ENDED, FIRST_HAND, INIT, OTHER_HANDS, PASSING_CARDS
	}

	public static void main(String[] args) throws Exception {
		try {
			// TODO implement the following class
			ServletNotifcationsInterface sni = new ServletNotifcationsInterface() {
				@Override
				public void notifyGameEnded(int[] matchPoints, int[] playersTotalPoint) {
				}

				@Override
				public void notifyGameStarted(Card[] cards) {
				}

				@Override
				public void notifyLocalChatMessage(ChatMessage message) {
				}

				@Override
				public void notifyPassedCards(Card[] cards) {
				}

				@Override
				public void notifyPlayedCard(Card card, int playerPosition) {
				}

				@Override
				public void notifyPlayerJoined(String name, boolean isBot, int point, int position) {
				}

				@Override
				public void notifyPlayerLeft(String name) {
				}
			};

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
	private ToNotify toNotify;

	public SingleTableManager(ServletNotifcationsInterface snf, Table table, GlobalTableManagerInterface gtm)
			throws RemoteException {
		toNotify = new ToNotify();
		cardsManager = new CardsManager();
		playersManager = new PlayersManager(snf, table.owner);

		System.out.println(snf + " " + table + " " + gtm);
		playersManager = new PlayersManager(snf, table.owner);
		// toNotify = new ToNotify();
		toNotify.notifyPlayerJoined(table.owner, 0, snf);
		// cardsManager = new CardsManager();
		gameStatus = GameStatus.INIT;
	}

	@Override
	public void addBot(String botName, int position) throws PositionFullException, FullTableException {
		if (gameStatus.ordinal() != GameStatus.INIT.ordinal()) {
			throw new IllegalStateException();
		}
		playersManager.addBot(botName, position);
		toNotify.notifyBotJoined(botName, position, new BotNotification());
		if (playersManager.getPlayersCount() == 4) {
			gameStatus = GameStatus.PASSING_CARDS;
		}
	}

	@Override
	public InitialTableStatus joinTable(String playerName, ServletNotifcationsInterface snf) throws FullTableException,
			NoSuchTableException {
		if (gameStatus.ordinal() != GameStatus.INIT.ordinal()) {
			throw new IllegalStateException();
		}
		if (playerName == null || snf == null) {
			throw new IllegalArgumentException();
		}
		InitialTableStatus its = playersManager.addPlayer(playerName);
		toNotify.notifyPlayerJoined(playerName, playersManager.getPlayerPosition(playerName), snf);
		if (playersManager.getPlayersCount() == 4) {
			gameStatus = GameStatus.PASSING_CARDS;
		}
		return its;
	}

	@Override
	public void leaveTable(String playerName) throws PlayerNotFoundException {
		if (gameStatus.ordinal() == GameStatus.INIT.ordinal()) {
			playersManager.removePlayer(playerName);
			toNotify.remove(playerName);
		} else {
			//
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
			gameStatus = GameStatus.FIRST_HAND;
			cardsManager.passCards();
			toNotify.notifyGameStarted(userName);
		}
	}

	@Override
	public void playCard(String userName, Card card) throws IllegalMoveException {
		if (gameStatus.ordinal() != GameStatus.FIRST_HAND.ordinal()
				&& gameStatus.ordinal() != GameStatus.OTHER_HANDS.ordinal()) {
			throw new IllegalStateException();
		}
		int position = playersManager.getPlayerPosition(userName);
		cardsManager.playCard(position, card);
		toNotify.notifyCardPlayed(userName, card, position);
		if (cardsManager.allPlayerPlayedCards()) {
			if (gameStatus.ordinal() == GameStatus.FIRST_HAND.ordinal()) {
				int winner = cardsManager.getWinner();
				int points = cardsManager.getPoints();
				playersManager.addPoint(winner, points);
				gameStatus = GameStatus.OTHER_HANDS;
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
	public ObservedGameStatus viewTable(String userName, ServletNotifcationsInterface snf) throws NoSuchTableException {
		toNotify.viewerJoined(userName, snf);
		ObservedGameStatus ogs = new ObservedGameStatus();
		PlayerStatus[] ps = new PlayerStatus[4];
		for (int i = 0; i < 4; i++)
			ps[i] = new PlayerStatus(playersManager.players[i].name, playersManager.players[i].score,
					cardsManager.cardPlayed[i], cardsManager.cards[i].size(), playersManager.players[i].isBot);
		return ogs;
	}
}
