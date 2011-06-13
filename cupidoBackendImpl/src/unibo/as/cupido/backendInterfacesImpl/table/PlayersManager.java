package unibo.as.cupido.backendInterfacesImpl.table;

import unibo.as.cupido.backendInterfaces.TableInterface.Positions;
import unibo.as.cupido.backendInterfaces.exception.FullTableException;
import unibo.as.cupido.backendInterfaces.exception.NotCreatorException;
import unibo.as.cupido.backendInterfaces.exception.PositionFullException;

public class PlayersManager {

	static class PlayerInfo {
		/**
		 * <code>true</code> if this player is a bot; <code>false</code>
		 * otherwise
		 */
		boolean isBot;

		/** this player name */
		String name;

		public PlayerInfo(String name, boolean isBot) {
			this.name = name;
			this.isBot = isBot;
		}

		@Override
		public String toString() {
			return "[is bot=" + isBot + ", name=" + name + "]";
		}
	}

	PlayerInfo[] players;
	int playersCount;

	public PlayersManager(String owner, boolean isBot) {
		playersCount = 0;
		players = new PlayerInfo[4];
		players[Positions.OWNER.ordinal()] = new PlayerInfo(owner, isBot);
		players[Positions.OWNER.ordinal()].name = owner;
		players[Positions.OWNER.ordinal()].isBot = isBot;
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
		players[position] = new PlayerInfo("_bot." + userName, true);
		playersCount++;
	}

	public void addPlayer(String playerName) throws FullTableException {
		if (playerName == null)
			throw new IllegalArgumentException();
		if (playersCount > 4) {
			throw new FullTableException();
		}
		int position = 1;
		while ((players[position] != null) && (position < 4))
			position++;
		players[position].name = playerName;
		playersCount++;
	}

	public int getPlayerPosition(String playerName) {
		for (int i = 0; i < 4; i++) {
			if (players[i] != null && players[i].name.equals(playerName))
				return i;
		}
		return -1;
	}

	public void removePlayer(String playerName) {
		int position = getPlayerPosition(playerName);
		if (position == -1)
			throw new IllegalArgumentException("player not found");
		playersCount--;
		players[position] = null;
	}

}
