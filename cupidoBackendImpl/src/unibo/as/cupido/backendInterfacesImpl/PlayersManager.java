package unibo.as.cupido.backendInterfacesImpl;

import unibo.as.cupido.backendInterfaces.ServletNotifcationsInterface;
import unibo.as.cupido.backendInterfaces.common.FullTableException;
import unibo.as.cupido.backendInterfaces.common.InitialTableStatus;
import unibo.as.cupido.backendInterfaces.common.PositionFullException;

public class PlayersManager {

	static class PlayerInfo {
		boolean isBot;

		String name;
		int score;
		public PlayerInfo(String name, int score, boolean isBot) {
			this.name = name;
			this.score = score;
			this.isBot = isBot;
		}
	}

	private static enum Positions {
		LEFT, OWNER, RIGHT, UP
	}
	PlayerInfo[] players;
	private int playersCount;

	private int whoPlaysNext;

	public PlayersManager(ServletNotifcationsInterface snf, String owner) {
		playersCount = 0;
		players = new PlayerInfo[4];
		for (int i = 0; i < 4; i++)
			players[i] = new PlayerInfo(null, 0, false);
		players[Positions.OWNER.ordinal()].name = owner;
	}

	public void addBot(String botName, int position) throws FullTableException, IllegalArgumentException,
			PositionFullException {
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

	public InitialTableStatus addPlayer(String playerName) throws FullTableException {
		if (playersCount > 4) {
			throw new FullTableException();
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

		return new InitialTableStatus(opponents, playerPoints, whoIsBot);
	}

	public void addPoint(int winner, int points) {
		players[winner].score += points;
	}

	public String getPlayerName(int i) {
		return players[i].name;
	}

	int getPlayerPosition(String playerName) {
		for (int i = 0; i < 4; i++)
			if (players[i].name.equals(playerName))
				return i;
		return -1;
	}

	public int getPlayersCount() {
		return playersCount;
	}

	public int getScore(int i) {
		return players[i].score;
	}

	public boolean isBot(int i) {
		return players[i].isBot;
	}

	public void removePlayer(String playerName) {
		int position = getPlayerPosition(playerName);
		if (position == -1)
			throw new IllegalArgumentException("player not found");
		playersCount--;
		players[position].name = null;
	}

}
