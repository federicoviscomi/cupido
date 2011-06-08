package unibo.as.cupido.backendInterfacesImpl;


import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import unibo.as.cupido.backendInterfaces.TableInterface;
import unibo.as.cupido.backendInterfaces.GlobalTableManagerInterface.ServletNotifcationsInterface;
import unibo.as.cupido.backendInterfaces.GlobalTableManagerInterface.Table;
import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.ChatMessage;
import unibo.as.cupido.backendInterfaces.common.FullTableException;
import unibo.as.cupido.backendInterfaces.common.IllegalMoveException;
import unibo.as.cupido.backendInterfaces.common.InitialTableStatus;
import unibo.as.cupido.backendInterfaces.common.NoSuchTableException;
import unibo.as.cupido.backendInterfaces.common.ObservedGameStatus;
import unibo.as.cupido.backendInterfaces.common.Pair;
import unibo.as.cupido.backendInterfaces.common.PlayerNotFoundException;
import unibo.as.cupido.backendInterfaces.common.PositionFullException;
import unibo.as.cupido.backendInterfaces.common.PlayerStatus;

/**
 * 
 * @author cane
 * 
 */

public class SingleTableManager implements TableInterface {

	public static void main(String[] args) {
		try {
			new SingleTableManager(null, null, new Table("ciao", 0, null));
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private final LocalTableManager localTableManager;

	private ToNotify toNotify;

	private static final int OWNER = 0;
	private static final int LEFT = 1;
	private static final int UP = 2;
	private static final int RIGHT = 3;

	private PlayerStatus[] players;

	private Card[][] cards = new Card[4][13];
	private int playersCount = 1;

	private int id;

	public SingleTableManager(ServletNotifcationsInterface snf, LocalTableManager localTableManager, Table table)
			throws RemoteException {
		this.localTableManager = localTableManager;
		players = new PlayerStatus[4];
		for (int i = 0; i < 4; i++)
			players[i] = new PlayerStatus(null, 0, null, 0, false);
		toNotify = new ToNotify();
		toNotify.add(table.owner, snf);
		players[OWNER].name = table.owner;

		daiCarte();
	}

	@Override
	public void addBot(String botName, int position) throws PositionFullException, FullTableException {
		if (playersCount > 4) {
			throw new FullTableException();
		}
		if (position < 1 || position > 3) {
			throw new IllegalArgumentException("position " + position + "out of bounds");
		}
		if (players[position].name != null) {
			throw new PositionFullException();
		}
		if (botName == null) {
			throw new IllegalArgumentException("invalid bot name");
		}
		players[position].isBot = true;
		players[position].name = botName;
		playersCount++;
	}

	private void daiCarte() {
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 13; j++) {
				cards[i][j] = new Card();
				cards[i][j].suit = Card.Suit.values()[i];
				cards[i][j].value = j + 1;
			}
		}

		Random random = new Random(System.currentTimeMillis());
		for (int i = 0; i < 26; i++) {
			int randPlayer1 = random.nextInt(4);
			int randPlayer2 = random.nextInt(4);
			int randCard1 = random.nextInt(13);
			int randCard2 = random.nextInt(13);
			Card temp = cards[randPlayer1][randCard1];
			cards[randPlayer1][randCard1] = cards[randPlayer2][randCard2];
			cards[randPlayer2][randCard2] = temp;
		}

		for (int i = 0; i < 4; i++) {
			java.util.Arrays.sort(cards[i], new Comparator<Card>() {
				@Override
				public int compare(Card o1, Card o2) {
					return (o1.suit.ordinal() * 13 + (o1.value == 1 ? 14 : o1.value))
							- (o2.suit.ordinal() * 13 + (o2.value == 1 ? 14 : o2.value));
				}
			});
		}

		for (int i = 0; i < 4; i++) {
			System.out.print("\n " + i + ": ");
			for (int j = 0; j < 13; j++) {
				System.out.print(cards[i][j] + " ");
			}
		}
	}

	private int getPlayerPosition(String userName) {
		for (int i = 0; i < 4; i++)
			if (players[i].equals(userName))
				return i;
		return -1;
	}

	@Override
	public InitialTableStatus joinTable(String playerName, ServletNotifcationsInterface snf) throws FullTableException,
			NoSuchTableException {
		if (playersCount > 4) {
			throw new FullTableException();
		}
		if (playerName == null || snf == null) {
			throw new IllegalArgumentException();
		}

		int position = 1;
		while (players[position].name != null)
			position++;

		players[position].name = playerName;

		/**
		 * Opponents are sorted clockwise (game is clockwise) opponents.lenght
		 * is always 3 opponents[i]==null means there is no i-th player
		 * opponents[0] is the player at your left, and so on...
		 */
		String[] opponents = new String[3];
		for (int i = 0; i < 3; i++)
			opponents[i] = players[(position + i + 1) % 4].name;

		/**
		 * (global) points of all the player playerPoints[0] are you,
		 * playerPoints[1] is the player at your left, and so on
		 */
		int[] playerPoints = new int[4];
		for (int i = 0; i < 4; i++)
			playerPoints[i] = players[(position + i) % 4].score;

		/**
		 * if opponents[i]==null then whoIsBot[i] has no meaning. if
		 * opponents[i]!= null then whoIsBot[i] is true if the player i is a
		 * bot, otherwise is false
		 */
		boolean[] whoIsBot = new boolean[4];
		for (int i = 0; i < 4; i++)
			whoIsBot[i] = whoIsBot[(position + i) % 4];

		toNotify.add(playerName, snf);
		return new InitialTableStatus(opponents, playerPoints, whoIsBot);
	}

	@Override
	public void leaveTable(String playerName) throws PlayerNotFoundException {
		if (playerName == null)
			throw new IllegalArgumentException();
		int position = getPlayerPosition(playerName);
		if (position == -1)
			throw new IllegalArgumentException("player not found");
		playersCount--;
		players[position].name = null;
		toNotify.remove(playerName);
	}

	@Override
	public void passCards(String userName, Card[] cards) throws IllegalMoveException {
		// TODO Auto-generated method stub
		toNotify.notifyCardPassed(cards, players[(getPlayerPosition(userName) + 1) % 4].name);
	}

	@Override
	public void playCard(String userName, Card card) throws IllegalMoveException {
		// TODO Auto-generated method stub
		toNotify.notifyCardPlayed(userName, card, getPlayerPosition(userName));
	}

	@Override
	public void sendMessage(ChatMessage message) {
		toNotify.notifyMessageSent(message);
	}

	@Override
	public ObservedGameStatus viewTable(String userName, ServletNotifcationsInterface snf) throws NoSuchTableException {
		toNotify.add(userName, snf);
		return new ObservedGameStatus(players);
	}
}
