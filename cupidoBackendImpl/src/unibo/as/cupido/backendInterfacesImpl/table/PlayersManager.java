package unibo.as.cupido.backendInterfacesImpl.table;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;

import unibo.as.cupido.backendInterfaces.ServletNotificationsInterface;
import unibo.as.cupido.backendInterfaces.TableInterface.Positions;
import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.ChatMessage;
import unibo.as.cupido.backendInterfaces.common.InitialTableStatus;
import unibo.as.cupido.backendInterfaces.common.PlayerStatus;
import unibo.as.cupido.backendInterfaces.exception.FullTableException;
import unibo.as.cupido.backendInterfaces.exception.NoSuchUserException;
import unibo.as.cupido.backendInterfaces.exception.NotCreatorException;
import unibo.as.cupido.backendInterfaces.exception.PlayerNotFoundException;
import unibo.as.cupido.backendInterfaces.exception.PositionFullException;
import unibo.as.cupido.backendInterfacesImpl.database.DatabaseManager;
import unibo.as.cupido.backendInterfacesImpl.table.bot.Bot;

public class PlayersManager {

	static class PlayerInfo {
		/**
		 * <code>true</code> if this player is a bot; <code>false</code>
		 * otherwise
		 */
		final boolean isBot;

		/** this player name */
		String name;

		/**
		 * player global score. Not points! This field is meaningful if and only
		 * if <code>isBot == true</code>
		 */
		int score;

		/** the servlet notification interface for this player */
		final ServletNotificationsInterface sni;

		public PlayerInfo(String name, boolean isBot, int score,
				ServletNotificationsInterface sni) {
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
	private int playersCount = 1;
	private final CardsManager cardsManager;

	public PlayersManager(String owner, ServletNotificationsInterface snf,
			int score, CardsManager cardsManager) throws SQLException,
			NoSuchUserException {
		this.cardsManager = cardsManager;
		players[Positions.OWNER.ordinal()] = new PlayerInfo(owner, false,
				score, snf);
	}

	public void addBot(String userName, int position,
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

		/* notify every players but the one who is adding the bot */
		for (PlayerInfo pi : players) {
			if (pi != null && !pi.name.equals(userName)) {
				try {
					pi.sni.notifyPlayerJoined("_bot." + userName, true, 0,
							position);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		players[position] = new PlayerInfo("_bot." + userName, true, 0, bot);
		playersCount++;

	}

	public int addPlayer(String playerName, ServletNotificationsInterface sni,
			int score) throws FullTableException, SQLException,
			NoSuchUserException {
		if (playerName == null)
			throw new IllegalArgumentException();
		if (playersCount > 4) {
			throw new FullTableException();
		}
		int position = 1;
		while ((players[position] != null) && (position < 4))
			position++;

		/* notify every players but the one who is joining */
		for (PlayerInfo pi : players) {
			if (pi != null) {
				try {
					pi.sni.notifyPlayerJoined(playerName, false, score,
							position);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		players[position] = new PlayerInfo(playerName, false, score, sni);
		playersCount++;

		if (playersCount == 4) {
			for (int i = 0; i < 4; i++) {
				try {
					players[i].sni.notifyGameStarted(cardsManager
							.getPlayerCards(i));
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

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

		for (PlayerInfo pi : players) {
			try {
				pi.sni.notifyPlayerLeft(playerName);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void notifyPassedCards(int position, Card[] cards) {
		try {
			players[position].sni.notifyPassedCards(cards);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void notifyPlayedCard(String userName, Card card) {
		for (PlayerInfo pi : players) {
			if (!pi.name.equals(userName)) {
				try {
					pi.sni.notifyPlayedCard(card, getPlayerPosition(userName));
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public void notifyNewLocalChatMessage(ChatMessage message) {
		for (PlayerInfo pi : players) {
			if (!pi.name.equals(message.userName)) {
				try {
					pi.sni.notifyLocalChatMessage(message);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public void addPlayersInformationForViewers(PlayerStatus[] playerStatus) {
		for (int i = 0; i < 4; i++) {
			if (players[i] != null) {
				playerStatus[i].name = players[i].name;
				playerStatus[i].isBot = players[i].isBot;
				playerStatus[i].score = players[i].score;
			}
		}
	}

	InitialTableStatus getInitialTableStatus(int position) {
		String[] opponents = new String[3];
		int[] playerPoints = new int[3];
		boolean[] whoIsBot = new boolean[3];
		for (int i = 0; i < 3; i++) {
			PlayerInfo nextOpponent = players[(position + i + 1) % 4];
			if (nextOpponent != null) {
				opponents[i] = nextOpponent.name;
				playerPoints[i] = nextOpponent.score;
				whoIsBot[i] = nextOpponent.isBot;
			}
		}
		return new InitialTableStatus(opponents, playerPoints, whoIsBot);
	}

}
