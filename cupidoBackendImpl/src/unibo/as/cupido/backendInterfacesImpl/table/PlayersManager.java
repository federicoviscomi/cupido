package unibo.as.cupido.backendInterfacesImpl.table;

import java.util.ArrayList;
import java.util.Arrays;
import unibo.as.cupido.backendInterfaces.TableInterface.Positions;
import unibo.as.cupido.backendInterfaces.common.InitialTableStatus;
import unibo.as.cupido.backendInterfaces.exception.FullTableException;
import unibo.as.cupido.backendInterfaces.exception.NotCreatorException;
import unibo.as.cupido.backendInterfaces.exception.PositionFullException;

public class PlayersManager {

	static class PlayerInfo {
		boolean isBot;

		String name;
		/** total score of this player. Not points of this round! */
		int score;

		public PlayerInfo(String name, int score, boolean isBot) {
			this.name = name;
			this.score = score;
			this.isBot = isBot;
		}

		@Override
		public Object clone() {
			return new PlayerInfo(name, score, isBot);
		}

		@Override
		public String toString() {
			return "[is bot=" + isBot + ", name=" + name + ", score=" + score
					+ "]";
		}
	}

	/**
	 * If a players name is <code>null</code> then then the position is empty;
	 * otherwise the position is full. There is no regard on the value of isBot
	 */
	PlayerInfo[] players;
	private int playersCount;
	private int whoPlaysNext;

	public PlayersManager(String owner, boolean isBot) {
		playersCount = 0;
		players = new PlayerInfo[4];
		for (int i = 0; i < 4; i++)
			players[i] = new PlayerInfo(null, 0, false);
		players[Positions.OWNER.ordinal()].name = owner;
		players[Positions.OWNER.ordinal()].isBot = isBot;
	}

	public void addBot(String userName, int position)
			throws FullTableException, IllegalArgumentException,
			PositionFullException, NotCreatorException {
		if (playersCount > 4)
			throw new FullTableException();
		if (position < 1 || position > 3)
			throw new IllegalArgumentException("position " + position
					+ "out of bounds");
		if (players[position].name != null)
			throw new PositionFullException();
		if (userName == null)
			throw new IllegalArgumentException("invalid bot name");
		if (!userName.equals(players[Positions.OWNER.ordinal()]))
			throw new NotCreatorException();
		players[position].isBot = true;
		players[position].name = "_bot." + userName;
		playersCount++;
	}

	public InitialTableStatus addPlayer(String playerName)
			throws FullTableException {
		if (playersCount > 4) {
			throw new FullTableException();
		}
		int position = 1;
		while ((players[position].name != null) && (position < 4))
			position++;
		players[position].name = playerName;
		return getTableStatus(position);
	}

	public boolean botIsWinner() {
		// TODO Auto-generated method stub
		return false;
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

	public Iterable<String> getPlayersName() {
		return Arrays.asList(players[0].name, players[1].name, players[2].name,
				players[3].name);
	}

	/**
	 * @return the position of the running player if there exists such a player;
	 *         otherwise -1
	 */
	public String getRunningPlayer() {
		for (int i = 0; i < 4; i++) {
			if (players[i].score == 26) {
				if (!players[i].isBot)
					return players[i].name;
			}
		}
		return null;
	}

	public int getScore(int i) {
		return players[i].score;
	}

	// TODO remove score from PlayerInfo?
	public int[] getScores() {
		int[] scores = new int[4];
		for (int i = 0; i < 4; i++)
			scores[i] = players[i].score;
		return scores;
	}

	public InitialTableStatus getTableStatus(int position) {
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

	public boolean isBot(int i) {
		return players[i].isBot;
	}

	public boolean isCreator(String userName) {
		return players[Positions.OWNER.ordinal()].equals(userName);
	}

	public void removePlayer(String playerName) {
		int position = getPlayerPosition(playerName);
		if (position == -1)
			throw new IllegalArgumentException("player not found");
		playersCount--;
		players[position].name = null;
	}

	public void updateScore(ArrayList<Integer> winners) {
		for (int i = 0; i < 4; i++) {
			if (winners.contains(i))
				players[i].score += 4;
			else
				players[i].score--;
		}
	}

}
