/*  Cupido - An online Hearts game.
 *  Copyright (C) 2011 Lorenzo Belli, Marco Poletti, Federico Viscomi
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package unibo.as.cupido.backend.table;

import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;

import unibo.as.cupido.backend.table.bot.BotNotificationInterface;
import unibo.as.cupido.backend.table.bot.NonRemoteBot;
import unibo.as.cupido.common.database.DatabaseManager;
import unibo.as.cupido.common.exception.DuplicateUserNameException;
import unibo.as.cupido.common.exception.FullTableException;
import unibo.as.cupido.common.exception.NoSuchUserException;
import unibo.as.cupido.common.exception.NotCreatorException;
import unibo.as.cupido.common.exception.PlayerNotFoundException;
import unibo.as.cupido.common.exception.PositionEmptyException;
import unibo.as.cupido.common.exception.PositionFullException;
import unibo.as.cupido.common.interfaces.ServletNotificationsInterface;
import unibo.as.cupido.common.interfaces.TableInterface;
import unibo.as.cupido.common.interfaces.TableInterface.Positions;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.ChatMessage;
import unibo.as.cupido.common.structures.InitialTableStatus;
import unibo.as.cupido.common.structures.ObservedGameStatus;
import unibo.as.cupido.common.structures.PlayerStatus;

public class PlayersManager {

	private static class NonRemoteBotInfo {

		final String botName;
		BotNotificationInterface bot;

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

		/** this player name */
		final String name;

		/**
		 * player global score. Not points! This field is meaningful if and only
		 * if <code>isBot == false</code>
		 */
		int score;

		/** the servlet notification interface for this player */
		final ServletNotificationsInterface sni;

		public PlayerInfo(String name, int score,
				ServletNotificationsInterface sni) {
			if (name == null || sni == null)
				throw new IllegalArgumentException(name + " " + sni);
			this.name = name;
			this.score = score;
			this.sni = sni;
		}

		@Override
		public String toString() {
			return "[is bot=false, name=" + name + "]";
		}
	}

	private PlayerInfo[] players = new PlayerInfo[4];
	private NonRemoteBotInfo[] nonRemoteBotsInfo = new NonRemoteBotInfo[4];
	private int playersCount = 1;
	private final RemovalThread removalThread;
	private final DatabaseManager databaseManager;
	private NonRemoteBotInfo[] botReplacement = new NonRemoteBotInfo[4];

	public PlayersManager(String owner, ServletNotificationsInterface snf,
			int score, RemovalThread removalThread,
			DatabaseManager databaseManager) throws SQLException,
			NoSuchUserException {
		if (owner == null || snf == null || removalThread == null)
			throw new IllegalArgumentException();

		this.databaseManager = databaseManager;
		this.removalThread = removalThread;

		players[0] = new PlayerInfo(owner, score, snf);
		removalThread.start();
	}

	public void addBot(String userName, int position,
			BotNotificationInterface bot, String botName)
			throws FullTableException, PositionFullException,
			NotCreatorException {

		if (playersCount >= 4)
			throw new FullTableException();
		if (position < 1 || position > 3 || userName == null)
			throw new IllegalArgumentException();
		if (players[position] != null || nonRemoteBotsInfo[position] != null)
			throw new PositionFullException();
		if (!userName.equals(players[Positions.OWNER.ordinal()].name))
			throw new NotCreatorException("Creator: "
					+ players[Positions.OWNER.ordinal()] + ". Current user: "
					+ userName);

		nonRemoteBotsInfo[position] = new NonRemoteBotInfo(botName, bot);
		botReplacement[position] = null;
		playersCount++;
	}

	public int addPlayer(String playerName, ServletNotificationsInterface sni,
			int score) throws FullTableException, SQLException,
			NoSuchUserException, DuplicateUserNameException {

		if (playerName == null)
			throw new IllegalArgumentException();
		if (playersCount > 4)
			throw new FullTableException();

		int position = 1;
		while ((players[position] != null || nonRemoteBotsInfo[position] != null)
				&& (position < 4))
			position++;

		if (position == 4)
			throw new FullTableException();

		/* check for duplicate user name or servlet notification interface */
		for (int i = 0; i < 4; i++) {
			if (players[i] != null) {
				if (players[i].name.equals(playerName))
					throw new DuplicateUserNameException(playerName);
				if (players[i].sni.equals(sni))
					throw new IllegalArgumentException("Duplicate sni");
			} else if (nonRemoteBotsInfo[i] != null) {
				if (nonRemoteBotsInfo[i].botName.equals(playerName))
					throw new DuplicateUserNameException(playerName);
			}
		}

		try {
			String fakeBotReplacement = SingleTableManager.botNames[position];
			botReplacement[position] = new NonRemoteBotInfo(fakeBotReplacement,
					new NonRemoteBot(fakeBotReplacement,
							this.getInitialTableStatus(position),
							FakeSingleTableManager.defaultInstance));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		players[position] = new PlayerInfo(playerName, score, sni);
		playersCount++;
		return position;
	}

	public void addPlayersInformationForViewers(
			ObservedGameStatus observedGameStatus) {
		for (int i = 0; i < 4; i++) {
			if (players[i] != null) {
				observedGameStatus.playerStatus[i] = new PlayerStatus();
				observedGameStatus.playerStatus[i].name = players[i].name;
				observedGameStatus.playerStatus[i].isBot = false;
				observedGameStatus.playerStatus[i].score = players[i].score;
			} else if (nonRemoteBotsInfo[i] != null) {
				observedGameStatus.playerStatus[i] = new PlayerStatus();
				observedGameStatus.playerStatus[i].name = nonRemoteBotsInfo[i].botName;
				observedGameStatus.playerStatus[i].isBot = true;
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

	public void notifyBotJoined(String botName, int position) {
		/*
		 * notify every players but the one who is adding the bot and the bot
		 * itself
		 */
		for (int i = 1; i < 4; i++) {
			if (i != position) {
				if (players[i] != null) {
					try {
						players[i].sni.notifyPlayerJoined(botName, true, 0,
								toRelativePosition(position, i));
					} catch (RemoteException e) {
						System.err.println(" " + players[i].name
								+ " is unreachable. Removing from table");
						removalThread.addRemoval(i);
					}
				} else if (nonRemoteBotsInfo[i] != null) {
					nonRemoteBotsInfo[i].bot.notifyPlayerJoined(botName, true,
							0, toRelativePosition(position, i));
				}
				if (botReplacement[i] != null) {
					botReplacement[i].bot.notifyPlayerJoined(botName, true, 0,
							toRelativePosition(position, i));
				}
			}
		}
	}

	public void notifyGameEnded(int[] matchPoints, int[] playersTotalPoint) {
		if (matchPoints == null || playersTotalPoint == null)
			throw new IllegalArgumentException();
		for (int i = 0; i < 4; i++) {
			if (players[i] != null) {
				try {
					players[i].sni.notifyGameEnded(matchPoints,
							playersTotalPoint);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (nonRemoteBotsInfo[i] != null) {
				nonRemoteBotsInfo[i].bot.notifyGameEnded(matchPoints,
						playersTotalPoint);
			}
			if (botReplacement[i] != null) {
				botReplacement[i].bot.notifyGameEnded(matchPoints,
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
			} else if (nonRemoteBotsInfo[i] != null) {
				nonRemoteBotsInfo[i].bot.notifyGameStarted(cards[i]);
			}
			if (botReplacement[i] != null) {
				botReplacement[i].bot.notifyGameStarted(cards[i]);
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
			if (botReplacement[position] != null) {
				botReplacement[position].bot.notifyPassedCards(cards);
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
			if (botReplacement[i] != null
					&& !botReplacement[i].botName.equals(userName)) {
				botReplacement[i].bot.notifyPlayedCard(card,
						toRelativePosition(position, i));
			}
		}
	}

	public void notifyPlayerJoined(String playerName, int score, int position) {
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
				} else if (nonRemoteBotsInfo[i] != null) {
					nonRemoteBotsInfo[i].bot.notifyPlayerJoined(playerName,
							false, score, toRelativePosition(position, i));
				}
				if (botReplacement[i] != null) {
					botReplacement[i].bot.notifyPlayerJoined(playerName, false,
							score, toRelativePosition(position, i));
				}
			}
		}
	}

	public void notifyPlayerLeft(String playerName) {
		for (int i = 0; i < 4; i++) {
			if (players[i] != null && !players[i].name.equals(playerName)) {
				try {
					players[i].sni.notifyPlayerLeft(playerName);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (nonRemoteBotsInfo[i] != null) {
				nonRemoteBotsInfo[i].bot.notifyPlayerLeft(playerName);
			}
			if (botReplacement[i] != null) {
				botReplacement[i].bot.notifyPlayerLeft(playerName);
			}
		}
	}

	public void notifyPlayerReplaced(String playerLeftName, String botName,
			int position) throws PositionFullException, PositionEmptyException {
		for (int i = 1; i < 4; i++) {
			if (i != position) {
				if (players[i] != null
						&& !players[i].name.equals(playerLeftName)) {
					try {
						players[i].sni.notifyPlayerReplaced(botName,
								toRelativePosition(position, i));
					} catch (RemoteException e) {
						//
					}
				} else if (nonRemoteBotsInfo[i] != null
						&& !nonRemoteBotsInfo[i].botName.equals(botName)) {
					nonRemoteBotsInfo[i].bot.notifyPlayerReplaced(botName,
							toRelativePosition(position, i));
				}
			}
			if (botReplacement[i] != null) {
				botReplacement[i].bot.notifyPlayerReplaced(botName,
						toRelativePosition(position, i));
			}
		}
	}

	public int playersCount() {
		return playersCount;
	}

	public void removePlayer(String playerName) throws PlayerNotFoundException {
		int position = getPlayerPosition(playerName);
		if (position == -1)
			throw new PlayerNotFoundException();
		if (playersCount < 1)
			throw new IllegalStateException();
		playersCount--;
		players[position] = null;
	}

	public void replacePlayer(String playerName, int position,
			TableInterface tableInterface) throws PlayerNotFoundException {
		try {
			this.removePlayer(playerName);
			botReplacement[position].bot.activate(tableInterface);
			this.addBot(players[Positions.OWNER.ordinal()].name, position,
					botReplacement[position].bot,
					botReplacement[position].botName);
			botReplacement[position] = null;
		} catch (FullTableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PositionFullException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotCreatorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/* the notification is to be sent to 2 */
	private int toRelativePosition(int absolutePosition1, int absolutePosition2) {
		int res;
		if (absolutePosition2 < absolutePosition1)
			res = (absolutePosition1 - absolutePosition2) - 1;
		else
			res = (4 - absolutePosition2) + absolutePosition1 - 1;
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

	public void replacementBotPlayCard(int position, Card card) {
		if (botReplacement[position] == null)
			return;
		NonRemoteBot nrb = (NonRemoteBot) botReplacement[position].bot;
		nrb.playCard(card);
	}

	public void replacementBotPassCards(int position, Card[] cards) {
		if (botReplacement[position] == null)
			return;
		NonRemoteBot nrb = (NonRemoteBot) botReplacement[position].bot;
		nrb.passCards(cards);
	}

	public void notifyGameEndedPrematurely() {
		for (int i = 1; i < 4; i++) {
			if (players[i] != null) {
				try {
					System.out
							.println("game ended prematurely. notifing player"
									+ i);
					players[i].sni.notifyGameEnded(null, null);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (nonRemoteBotsInfo[i] != null) {
				System.out
				.println("game ended prematurely. notifing bot"
						+ i);
				nonRemoteBotsInfo[i].bot.notifyGameEnded(null, null);
			}
			if (botReplacement[i] != null) {
				System.out
				.println("game ended prematurely. notifing bot replacement"
						+ i);
				botReplacement[i].bot.notifyGameEnded(null, null);
			}
		}
	}
}
