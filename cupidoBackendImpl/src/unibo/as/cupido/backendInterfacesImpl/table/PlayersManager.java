package unibo.as.cupido.backendInterfacesImpl.table;

import java.sql.SQLException;
import java.util.ArrayList;

import unibo.as.cupido.backendInterfaces.TableInterface.Positions;
import unibo.as.cupido.backendInterfaces.exception.FullTableException;
import unibo.as.cupido.backendInterfaces.exception.NoSuchUserException;
import unibo.as.cupido.backendInterfaces.exception.NotCreatorException;
import unibo.as.cupido.backendInterfaces.exception.PlayerNotFoundException;
import unibo.as.cupido.backendInterfaces.exception.PositionFullException;
import unibo.as.cupido.backendInterfacesImpl.database.DatabaseManager;

public class PlayersManager {

	static class PlayerInfo {
		/**
		 * <code>true</code> if this player is a bot; <code>false</code>
		 * otherwise
		 */
		boolean isBot;

		/** this player name */
		String name;

		/** player global score. Not points! */
		int score;

		public PlayerInfo(String name, boolean isBot, int score) {
			this.name = name;
			this.isBot = isBot;
			this.score = score;
		}

		@Override
		public String toString() {
			return "[is bot=" + isBot + ", name=" + name + "]";
		}
	}

	PlayerInfo[] players = new PlayerInfo[4];
	int playersCount = 0;

	private final DatabaseManager databaseManager;

	public PlayersManager(String owner, DatabaseManager databaseManager)
			throws SQLException, NoSuchUserException {
		this.databaseManager = databaseManager;
		players[Positions.OWNER.ordinal()] = new PlayerInfo(owner, false,
				databaseManager.getPlayerScore(owner));
	}

	public void addBot(String userName, int position)
			throws FullTableException, IllegalArgumentException,
			PositionFullException, NotCreatorException {
		if (playersCount > 4)
			throw new FullTableException();
		if (position < 1 || position > 3 || userName == null)
			throw new IllegalArgumentException();
		if (players[position] != null)
			throw new PositionFullException();
		if (!userName.equals(players[Positions.OWNER.ordinal()]))
			throw new NotCreatorException();
		players[position] = new PlayerInfo("_bot." + userName, true, 0);
		playersCount++;
	}

	public int addPlayer(String playerName) throws FullTableException,
			SQLException, NoSuchUserException {
		if (playerName == null)
			throw new IllegalArgumentException();
		if (playersCount > 4) {
			throw new FullTableException();
		}
		int position = 1;
		while ((players[position] != null) && (position < 4))
			position++;
		players[position] = new PlayerInfo(playerName, false,
				databaseManager.getPlayerScore(playerName));
		playersCount++;
		return position;
	}

	public int getPlayerPosition(String playerName) {
		for (int i = 0; i < 4; i++) {
			if (players[i] != null && players[i].name.equals(playerName))
				return i;
		}
		return -1;
	}

	public void removePlayer(String playerName) throws PlayerNotFoundException {
		int position = getPlayerPosition(playerName);
		if (position == -1)
			throw new PlayerNotFoundException();
		playersCount--;
		players[position] = null;
	}

	public ArrayList<String> nonBotPlayersName() {
		ArrayList<String> nbpn = new ArrayList<String>();
		for (int i = 0; i < 4; i++)
			if (players[i] != null && !players[i].isBot)
				nbpn.add(players[i].name);
		return nbpn;
	}

}
