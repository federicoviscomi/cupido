package unibo.as.cupido.backendInterfacesImpl;

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
import unibo.as.cupido.backendInterfaces.common.PositionFullException;
import unibo.as.cupido.backendInterfaces.common.ObservedGameStatus.PlayerStatus;

/**
 * 
 * 
 * @author cane
 * 
 */

public class SingleTableThread implements TableInterface, Runnable {

	private final LocalTableManager localTableManager;
	private final ServletNotifcationsInterface snf;

	private static final int OWNER = 0;
	private static final int LEFT = 1;
	private static final int UP = 2;
	private static final int RIGHT = 3;

	private String[] playersName = new String[4];
	private int[] playersScore = new int[4];
	private boolean[] isBot = new boolean[4];
	private int playersCount = 1;
	private Card[][] cards = new Card[4][13];
	private Card[] playedCard = new Card[4];

	public SingleTableThread(ServletNotifcationsInterface snf, LocalTableManager localTableManager, Table table) {
		this.snf = snf;
		this.localTableManager = localTableManager;

		java.util.Arrays.fill(playersScore, 0);
		isBot[OWNER] = false;
		playersName[OWNER] = table.owner;

		daiCarte();
	}

	public static void main(String[] args) {
		Card[][] c = new Card[4][12];
		Card a = new Card();
	}

	private void daiCarte() {
		Card[] mazzo = new Card[52];
		// for(int i = 0;)

		for (int i = 0; i < 4; i++) {
			System.out.print("\n " + i + ": ");
			for (int j = 0; j < 13; j++)
				System.out.print(cards[i][j] + " ");
		}
	}

	@Override
	public void addBot(String botName, int position) throws PositionFullException, FullTableException {
		if (playersCount > 4) {
			throw new FullTableException();
		}
		if (position < 1 || position > 3) {
			throw new IllegalArgumentException("position " + position + "out of bounds");
		}
		if (playersName[position] != null) {
			throw new PositionFullException();
		}
		if (botName == null) {
			throw new IllegalArgumentException("invalid bot name");
		}
		isBot[position] = true;
		playersName[position] = botName;
		playersCount++;
	}

	public class E extends Exception{
		
	}
	
	@Override
	public InitialTableStatus joinTable(String playerName, ServletNotifcationsInterface snf) throws FullTableException,
			NoSuchTableException {
		
		
		
		if (playersCount > 4) {
			throw new FullTableException();
		}
		if (playerName == null) {
			throw new IllegalArgumentException("invalid player name");
		}
		int position = 1;
		while (playersName[position] != null)
			position++;
		playersName[position] = playerName;

		/**
		 * Opponents are sorted clockwise (game is clockwise) opponents.lenght
		 * is always 3 opponents[i]==null means there is no i-th player
		 * opponents[0] is the player at your left, and so on...
		 */
		String[] opponents = new String[3];
		for (int i = 0; i < 3; i++)
			opponents[i] = playersName[(position + i + 1) % 4];

		/**
		 * (global) points of all the player playerPoints[0] are you,
		 * playerPoints[1] is the player at your left, and so on
		 */
		int[] playerPoints = new int[4];
		for (int i = 0; i < 4; i++)
			playerPoints[i] = playersScore[(position + i) % 4];

		/**
		 * if opponents[i]==null then whoIsBot[i] has no meaning. if
		 * opponents[i]!= null then whoIsBot[i] is true if the player i is a
		 * bot, otherwise is false
		 */
		boolean[] whoIsBot = new boolean[4];
		for (int i = 0; i < 4; i++)
			whoIsBot[i] = whoIsBot[(position + i) % 4];

		
		return new InitialTableStatus(opponents, playerPoints, whoIsBot);
	}

	@Override
	public void leaveTable(String playerName) {
		if (playerName == null)
			throw new IllegalArgumentException();
		int position = -1;
		for (int i = 0; i < 4; i++)
			if (playersName[i].equals(playerName))
				position = i;
		if (position == -1)
			throw new IllegalArgumentException("player not found");
		playersCount--;
		playersName[position] = null;
	}

	@Override
	public void passCards(String userName, Card[] cards) throws IllegalMoveException {
		// TODO Auto-generated method stub

	}

	@Override
	public void playCard(String userName, Card card) throws IllegalMoveException {
		// TODO Auto-generated method stub

	}

	@Override
	public void run() {
		//
	}

	@Override
	public void sendMessage(ChatMessage message) {
		snf.notifyLocalChatMessage(message);
	}

	@Override
	public ObservedGameStatus viewTable(String userName, ServletNotifcationsInterface snf) throws NoSuchTableException {
		PlayerStatus[] players = new PlayerStatus[4];
		for (int i = 0; i < 4; i++)
			players[i] = new PlayerStatus(playersName[i], playersScore[i], playedCard[i], 0, isBot[i]);
		return new ObservedGameStatus(players);
	}

}
