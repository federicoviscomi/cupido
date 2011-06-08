package unibo.as.cupido.backendInterfacesImpl;

import java.nio.channels.IllegalSelectorException;
import java.rmi.RemoteException;

import unibo.as.cupido.backendInterfaces.GlobalTableManagerInterface;
import unibo.as.cupido.backendInterfaces.GlobalTableManagerInterface.ServletNotifcationsInterface;
import unibo.as.cupido.backendInterfaces.GlobalTableManagerInterface.Table;
import unibo.as.cupido.backendInterfaces.TableInterface;
import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.ChatMessage;
import unibo.as.cupido.backendInterfaces.common.FullTableException;
import unibo.as.cupido.backendInterfaces.common.IllegalMoveException;
import unibo.as.cupido.backendInterfaces.common.InitialTableStatus;
import unibo.as.cupido.backendInterfaces.common.NoSuchTableException;
import unibo.as.cupido.backendInterfaces.common.ObservedGameStatus;
import unibo.as.cupido.backendInterfaces.common.PlayerNotFoundException;
import unibo.as.cupido.backendInterfaces.common.PositionFullException;

/**
 * 
 * @author cane
 * 
 */

public class SingleTableManager implements TableInterface {

	public static void main(String[] args) throws Exception {
		try {
			ServletNotifcationsInterface sni = new ServletNotifcationsInterface() {

				@Override
				public void notifyGameEnded(int[] matchPoints, int[] playersTotalPoint) {
					// TODO Auto-generated method stub

				}

				@Override
				public void notifyGameStarted(Card[] cards) {
					// TODO Auto-generated method stub

				}

				@Override
				public void notifyLocalChatMessage(ChatMessage message) {
					// TODO Auto-generated method stub

				}

				@Override
				public void notifyPlassedCards(Card[] cards) {
					// TODO Auto-generated method stub

				}

				@Override
				public void notifyPlayedCard(Card card, int playerPosition) {
					// TODO Auto-generated method stub

				}

				@Override
				public void notifyPlayerJoined(String name, boolean isBot, int point, int position) {
					// TODO Auto-generated method stub

				}

				@Override
				public void notifyPlayerLeft(String name) {
					// TODO Auto-generated method stub

				}

			};

			SingleTableManager stm = new SingleTableManager(sni, new Table("Owner", 0, null), null);
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

	private void printGameStatus() {
		System.out.format("\n\nGame status: %s", gameStatus.toString());
		cardsManager.print(playersManager);
	}

	
	private ToNotify toNotify;

	private static enum GameStatus {
		INIT, PASSING_CARDS, FIRST_HAND, OTHER_HANDS, ENDED
	};

	private CardsManager cardsManager;
	private PlayersManager playersManager;
	private GameStatus gameStatus;

	public SingleTableManager(ServletNotifcationsInterface snf, Table table, GlobalTableManagerInterface gtm) throws RemoteException {
		playersManager = new PlayersManager(snf, table.owner);
		toNotify = new ToNotify();
		toNotify.notifyPlayerJoined(table.owner, 0, snf);
		cardsManager = new CardsManager();
		gameStatus = GameStatus.INIT;
	}

	@Override
	public void addBot(String botName, int position) throws PositionFullException, FullTableException {
		if (gameStatus.ordinal() != GameStatus.INIT.ordinal()) {
			throw new IllegalStateException();
		}
		playersManager.addBot(botName, position);
		toNotify.notifyBotJoined(botName, position);
		if (playersManager.getPlayersCount() == 4)
			gameStatus = GameStatus.PASSING_CARDS;
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
		if (playersManager.getPlayersCount() == 4)
			gameStatus = GameStatus.PASSING_CARDS;
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
		int position = playersManager.getPlayerPosition(userName);
		cardsManager.playCard(position, card);
		playersManager.playCard(position, card);
		toNotify.notifyCardPlayed(userName, card, position);
	}

	@Override
	public void sendMessage(ChatMessage message) {
		toNotify.notifyMessageSent(message);
	}

	@Override
	public ObservedGameStatus viewTable(String userName, ServletNotifcationsInterface snf) throws NoSuchTableException {
		toNotify.viewerJoined(userName, snf);
		return playersManager.getObservedGameStatus();
	}
}
