package unibo.as.cupido.backendInterfacesImpl;

import unibo.as.cupido.backendInterfaces.GlobalTableManagerInterface.TableDescriptor;
import unibo.as.cupido.backendInterfaces.TableInterface;
import unibo.as.cupido.backendInterfaces.GlobalTableManagerInterface;
import unibo.as.cupido.backendInterfaces.GlobalTableManagerInterface.ServletNotifcationsInterface;
import unibo.as.cupido.backendInterfaces.GlobalTableManagerInterface.Table;
import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.FullTableException;
import unibo.as.cupido.backendInterfaces.common.IllegalMoveException;
import unibo.as.cupido.backendInterfaces.common.InitialTableStatus;
import unibo.as.cupido.backendInterfaces.common.NoSuchTableException;
import unibo.as.cupido.backendInterfaces.common.ObservedGameStatus;
import unibo.as.cupido.backendInterfaces.common.PositionFullException;

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

	private String[] playersName;
	private int[] playersScore;
	private boolean[] isBot;
	private int playersCount = 1;

	public SingleTableThread(ServletNotifcationsInterface snf, LocalTableManager localTableManager, Table table) {
		this.snf = snf;
		this.localTableManager = localTableManager;
		playersName = new String[4];
		isBot = new boolean[4];
		playersScore = new int[4];

		java.util.Arrays.fill(playersScore, 0);
		isBot[OWNER] = false;
		playersName[OWNER] = table.owner;
	}

	@Override
	public void addBot(String botName, int position) throws PositionFullException {
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
		playersScore[position] = 0; // non e' necessario
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
	public void sendMessage(String userName, String message) {
		// TODO Auto-generated method stub

	}

	@Override
	public ObservedGameStatus viewTable(String userName, ServletNotifcationsInterface snf) throws NoSuchTableException {
		// TODO Auto-generated method stub
		ObservedGameStatus ogs = new ObservedGameStatus();
		return ogs;
	}

}
