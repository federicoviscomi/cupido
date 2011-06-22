package unibo.as.cupido.backend.table;

import java.rmi.RemoteException;
import java.sql.SQLException;

import unibo.as.cupido.backend.table.bot.BotNotificationInterface;
import unibo.as.cupido.common.database.DatabaseManager;
import unibo.as.cupido.common.exception.DuplicateUserNameException;
import unibo.as.cupido.common.exception.FullTableException;
import unibo.as.cupido.common.exception.NoSuchUserException;
import unibo.as.cupido.common.exception.NotCreatorException;
import unibo.as.cupido.common.exception.PlayerNotFoundException;
import unibo.as.cupido.common.exception.PositionFullException;
import unibo.as.cupido.common.interfaces.ServletNotificationsInterface;
import unibo.as.cupido.common.interfaces.TableInterface.Positions;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.ChatMessage;
import unibo.as.cupido.common.structures.InitialTableStatus;
import unibo.as.cupido.common.structures.ObservedGameStatus;

public class PlayersManager {

	private static class NonRemoteBotInfo {

		final String botName;
		final BotNotificationInterface bot;

		public NonRemoteBotInfo(String botName, BotNotificationInterface bot) {
			if (botName == null || bot == null)
				throw new IllegalArgumentException("null bot name or interface");
			this.botName = botName;
			this.bot = bot;
		}

		@Override
		public String toString() {
			return "[is bot=true, name=" + botName + "]";
		}
	}

	private static class PlayerInfo {
		/**
		 * <code>true</code> if this player is a bot; <code>false</code>
		 * otherwise
		 */
		private final boolean isBot;

		/** this player name */
		private final String name;

		/**
		 * player global score. Not points! This field is meaningful if and only
		 * if <code>isBot == false</code>
		 */
		private int score;

		/** the servlet notification interface for this player */
		private final ServletNotificationsInterface sni;

		public PlayerInfo(String name, boolean isBot, int score,
				ServletNotificationsInterface sni) {
			if (name == null || sni == null)
				throw new IllegalArgumentException(name + " " + sni);
			this.name = name;
			this.isBot = isBot;
			this.score = score;
			this.sni = sni;
		}

		@Override
		public String toString() {
			return "[is bot=" + isBot + ", name=" + name + "]";
		}
	}

	private PlayerInfo[] players = new PlayerInfo[4];
	private NonRemoteBotInfo[] nonRemoteBotsInfo = new NonRemoteBotInfo[4];
	private int playersCount = 1;
	private final RemovalThread removalThread;
	private final DatabaseManager databaseManager;

	public PlayersManager(String owner, ServletNotificationsInterface snf,
			int score, RemovalThread removalThread,
			DatabaseManager databaseManager) throws SQLException,
			NoSuchUserException {
		this.databaseManager = databaseManager;
		if (owner == null || snf == null || removalThread == null)
			throw new IllegalArgumentException();
		players[0] = new PlayerInfo(owner, false, score, snf);
		this.removalThread = removalThread;
		removalThread.start();
	}

	private void addBot(String userName, int position,
			ServletNotificationsInterface bot) throws FullTableException,
			IllegalArgumentException, PositionFullException,
			NotCreatorException {

		if (playersCount > 4)
			throw new FullTableException();
		if (position < 1 || position > 3 || userName == null)
			throw new IllegalArgumentException();
		if (players[position] != null)
			throw new PositionFullException();
		if (!userName.equals(players[Positions.OWNER.ordinal()].name))
			throw new NotCreatorException("Creator: "
					+ players[Positions.OWNER.ordinal()] + ". Current user: "
					+ userName);

		/*
		 * notify every players but the one who is adding the bot and the bot
		 * itself
		 */
		for (int i = 1; i < 4; i++) {
			if (i != position && players[i] != null
					&& !players[i].name.equals(userName)) {
				try {
					this.print();
					players[i].sni.notifyPlayerJoined("_bot." + userName + "."
							+ position, true, 0,
							toRelativePosition(position, i));
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		players[position] = new PlayerInfo("_bot." + userName + "." + position,
				true, 0, bot);
		playersCount++;
	}

	public void addNonRemoteBot(String userName, int position,
			BotNotificationInterface bot) throws FullTableException,
			PositionFullException, NotCreatorException {

		if (playersCount > 4)
			throw new FullTableException();
		if (position < 1 || position > 3 || userName == null)
			throw new IllegalArgumentException();
		if (players[position] != null)
			throw new PositionFullException();
		if (!userName.equals(players[Positions.OWNER.ordinal()].name))
			throw new NotCreatorException("Creator: "
					+ players[Positions.OWNER.ordinal()] + ". Current user: "
					+ userName);
		String botName = "_bot." + userName + "." + position;

		/*
		 * notify every players but the one who is adding the bot and the bot
		 * itself
		 */
		for (int i = 1; i < 4; i++) {
			if (i != position) {
				if (players[i] != null) {
					try {
						players[i].sni.notifyPlayerJoined("_bot." + userName
								+ "." + position, true, 0,
								toRelativePosition(position, i));
					} catch (RemoteException e) {
						System.err.println(" " + players[i].name
								+ " is unreachable. Removing from table");
						removalThread.addRemoval(i);
					}
				}
				if (nonRemoteBotsInfo[i] != null) {
					nonRemoteBotsInfo[i].bot.notifyPlayerJoined("_bot."
							+ userName + "." + position, true, 0,
							toRelativePosition(position, i));
				}
			}
		}

		nonRemoteBotsInfo[position] = new NonRemoteBotInfo(botName, bot);
		playersCount++;
		removalThread.remove();
	}

	public int addPlayer(String playerName, ServletNotificationsInterface sni,
			int score) throws FullTableException, SQLException,
			NoSuchUserException, DuplicateUserNameException {

		if (playerName == null)
			throw new IllegalArgumentException();
		if (playersCount > 4)
			throw new FullTableException();

		int position = 1;
		while ((players[position] != null) && (position < 4))
			position++;

		if (position == 4)
			throw new FullTableException();

		/* check for duplicate user name or servlet notification interface */
		for (int i = 0; i < 4; i++) {
			if (players[i] != null) {
				if (players[i].name.equals(playerName))
					throw new DuplicateUserNameException();
			}
			if (nonRemoteBotsInfo[i] != null) {
				if (nonRemoteBotsInfo[i].botName.equals(playerName))
					throw new IllegalArgumentException("Duplicate player name");
			}
		}

		/* notify every players but the one who is joining */
		for (int i = 0; i < 4; i++) {
			if (i != position) {
				if (players[i] != null) {
					try {
						players[i].sni.notifyPlayerJoined(playerName, false,
								score, toRelativePosition(position, i));
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						removalThread.addRemoval(i);
					}
				}
				if (nonRemoteBotsInfo[i] != null) {
					nonRemoteBotsInfo[i].bot.notifyPlayerJoined("_bot."
							+ playerName + "." + position, false, score,
							toRelativePosition(position, i));
				}
			}
		}

		players[position] = new PlayerInfo(playerName, false, score, sni);
		playersCount++;
		removalThread.remove();
		return position;
	}

	public void addPlayersInformationForViewers(
			ObservedGameStatus observedGameStatus) {
		for (int i = 0; i < 4; i++) {
			if (players[i] != null) {
				observedGameStatus.playerStatus[i].name = players[i].name;
				observedGameStatus.playerStatus[i].isBot = players[i].isBot;
				observedGameStatus.playerStatus[i].score = players[i].score;
			}
		}
		if (playersCount < 4)
			observedGameStatus.firstDealerInTrick = -1;
	}

	InitialTableStatus getInitialTableStatus(int position) {
		String[] opponents = new String[3];
		int[] playerPoints = new int[3];
		boolean[] whoIsBot = new boolean[3];
		for (int i = 0; i < 3; i++) {
			int next = (position + i + 1) % 4;

			if (players[next] != null) {
				PlayerInfo nextOpponent = players[next];
				opponents[i] = nextOpponent.name;
				playerPoints[i] = nextOpponent.score;
				whoIsBot[i] = false;
			} else if (nonRemoteBotsInfo[next] != null) {
				NonRemoteBotInfo nextOpponent = nonRemoteBotsInfo[next];
				opponents[i] = nextOpponent.botName;
				playerPoints[i] = 0;
				whoIsBot[i] = true;
			}

		}
		return new InitialTableStatus(opponents, playerPoints, whoIsBot);
	}

	public String getPlayerName(int i) {
		if (players[i] != null)
			return players[i].name;
		return nonRemoteBotsInfo[i].botName;
	}

	public int getPlayerPosition(String playerName) {
		for (int i = 0; i < 4; i++) {
			if (players[i] != null && players[i].name.equals(playerName))
				return i;
			if (nonRemoteBotsInfo[i] != null
					&& nonRemoteBotsInfo[i].botName.equals(playerName))
				return i;
		}
		return -1;
	}

	public void notifyGameEnded(int[] matchPoints, int[] playersTotalPoint) {
		for (int i = 0; i < 4; i++) {
			if (players[i] != null) {
				try {
					players[i].sni.notifyGameEnded(matchPoints,
							playersTotalPoint);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (nonRemoteBotsInfo[i] != null) {
				nonRemoteBotsInfo[i].bot.notifyGameEnded(matchPoints,
						playersTotalPoint);
			}
		}
	}

	public void notifyGameStarted(Card[][] cards) {
		for (int i = 0; i < 4; i++) {
			if (players[i] != null) {
				try {
					players[i].sni.notifyGameStarted(cards[i]);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (nonRemoteBotsInfo[i] != null) {
				nonRemoteBotsInfo[i].bot.notifyGameStarted(cards[i]);
			}
		}
	}

	public void notifyNewLocalChatMessage(ChatMessage message) {
		for (PlayerInfo pi : players) {
			if (pi != null && !pi.name.equals(message.userName)) {
				try {
					pi.sni.notifyLocalChatMessage(message);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/** send a notification to player in absolute position <code>position</code> */
	public void notifyPassedCards(int position, Card[] cards) {
		try {
			if (players[position] != null) {
				players[position].sni.notifyPassedCards(cards);
			} else {
				nonRemoteBotsInfo[position].bot.notifyPassedCards(cards);
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void notifyPlayedCard(String userName, Card card) {
		int position = getPlayerPosition(userName);
		for (int i = 0; i < 4; i++) {
			if (players[i] != null && !players[i].name.equals(userName)) {
				try {
					players[i].sni.notifyPlayedCard(card,
							toRelativePosition(position, i));
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (nonRemoteBotsInfo[i] != null
					&& !nonRemoteBotsInfo[i].botName.equals(userName)) {
				nonRemoteBotsInfo[i].bot.notifyPlayedCard(card,
						toRelativePosition(position, i));
			}
		}
	}

	public int playersCount() {
		return playersCount;
	}

	public void print() {
		for (int i = 0; i < 4; i++) {
			System.out.print(players[i]);
			// if (players[i] != null) {System.out.print(players[i]);} else
			// {System.out.print(nonRemoteBotsInfo[i]);}
		}
		System.out.print("\t");
		for (int i = 0; i < 4; i++) {
			System.out.print(nonRemoteBotsInfo[i]);
		}
	}

	public void removePlayer(String playerName) throws PlayerNotFoundException {
		int position = getPlayerPosition(playerName);
		if (position == -1)
			throw new PlayerNotFoundException();
		if (playersCount < 1)
			throw new IllegalStateException();
		playersCount--;
		players[position] = null;

		for (int i = 0; i < 4; i++) {
			if (players[i] != null) {
				try {
					players[i].sni.notifyPlayerLeft(playerName);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (nonRemoteBotsInfo[i] != null) {
				nonRemoteBotsInfo[i].bot.notifyPlayerLeft(playerName);
			}
		}
	}

	/* the notification is to be sent to 2 */
	private int toRelativePosition(int absolutePosition1, int absolutePosition2) {
		int res;
		if (absolutePosition2 < absolutePosition1)
			res = (absolutePosition1 - absolutePosition2) - 1;
		else
			res = (4 - absolutePosition2) + absolutePosition1 - 1;
		// System.err.println(" abs1 " + absolutePosition1 + " abs2 "+
		// absolutePosition2 + " res " + res);
		return res;
	}

	public int[] updateScore(int[] matchPoints) {
		int min = matchPoints[0];
		for (int i = 1; i < 4; i++) {
			if (min < matchPoints[i])
				min = matchPoints[i];
		}

		int[] newScore = new int[4];
		for (int i = 0; i < 4; i++) {
			if (players[i] != null) {
				if (matchPoints[i] == min) {
					players[i].score += 4;
				} else {
					players[i].score -= 1;
				}
				newScore[i] = players[i].score;
				try {
					databaseManager.updateScore(players[i].name,
							players[i].score);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchUserException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return newScore;
	}

}
