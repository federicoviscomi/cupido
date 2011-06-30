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

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Arrays;

import unibo.as.cupido.backend.table.bot.NonRemoteBot;
import unibo.as.cupido.common.database.DatabaseManager;
import unibo.as.cupido.common.exception.DuplicateUserNameException;
import unibo.as.cupido.common.exception.FullTableException;
import unibo.as.cupido.common.exception.NoSuchPlayerException;
import unibo.as.cupido.common.exception.NoSuchUserException;

import unibo.as.cupido.common.exception.NotCreatorException;
import unibo.as.cupido.common.exception.EmptyPositionException;
import unibo.as.cupido.common.exception.FullPositionException;
import unibo.as.cupido.common.interfaces.ServletNotificationsInterface;
import unibo.as.cupido.common.interfaces.TableInterface;
import unibo.as.cupido.common.interfaces.TableInterface.Positions;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.ChatMessage;
import unibo.as.cupido.common.structures.InitialTableStatus;
import unibo.as.cupido.common.structures.ObservedGameStatus;
import unibo.as.cupido.common.structures.PlayerStatus;

public class PlayersManager {

	/** store information for inactiveReplacementBot or human players */
	private static class PlayerInfo {

		/** this player name */
		final String name;

		/**
		 * player global score. Not points! This field is meaningful if and only
		 * if <code>isBot == false</code>
		 */
		int score;

		/**
		 * the notification interface for this player. This field is always
		 * meaningfull.
		 * <ul>
		 * <li>If <code>isBot==false</code> then this field is the notification
		 * interface of a client servlet.</li>
		 * <li>Otherwise if <code>isBot==true</code> then
		 * <ul>
		 * <li>if <code>replaced==false</code> then this field is the
		 * notification interface of a bot who has been added before the game
		 * start</li>
		 * <li>if <code>replaced==true</code> then this field is the
		 * notification interface of a bot who replaced a player after the game
		 * starts</li></li>
		 * </ul>
		 * </ul>
		 */
		ServletNotificationsInterface playerNotificationInterface;

		/**
		 * if <code>isBot==false</code> then this field is the bot who could
		 * replace this player; otherwise this field is <code>null<code>.
		 */
		NonRemoteBotInterface inactiveReplacementBot;

		/**
		 * if <code>isBot==false</code> then this field is the notification
		 * interface of the inactiveReplacementBot who could replace this
		 * player; otherwise this field is <code>null<code>.
		 */
		ServletNotificationsInterface inactiveReplacementBotSNI;

		/**
		 * <code>true</code> if this is a bot or an active
		 * inactiveReplacementBot; <code>false</code> otherwise
		 */
		boolean isBot;

		/**
		 * <code>true</code> if this player has been replaced, in this case
		 * <code>false</code> otherwise
		 */
		boolean replaced;

		public PlayerInfo(String name, int score,
				ServletNotificationsInterface notificationInterface,
				NonRemoteBotInterface replacementBot) {

			if (name == null || notificationInterface == null
					|| replacementBot == null)
				throw new IllegalArgumentException(name + " "
						+ notificationInterface);

			this.name = name;
			this.score = score;
			this.playerNotificationInterface = notificationInterface;
			this.inactiveReplacementBot = replacementBot;
			this.inactiveReplacementBotSNI = replacementBot
					.getServletNotificationsInterface();
			this.isBot = false;
			this.replaced = false;
		}

		public PlayerInfo(String name, ServletNotificationsInterface bot) {
			if (name == null || bot == null)
				throw new IllegalArgumentException();
			this.name = name;
			this.playerNotificationInterface = bot;
			this.inactiveReplacementBot = null;
			this.inactiveReplacementBotSNI = null;
			this.isBot = true;
			this.replaced = false;
		}

		@Override
		public String toString() {
			if (isBot) {
				if (replaced) {
					return "[name=" + name + ", bot replacer]";
				} else {
					return "[name=" + name + ", bot]";
				}
			} else {
				return "[name=" + name + ", score=" + score + ", human player]";
			}
		}
	}

	private static Card[] cloneCardArray(Card[] cards) {
		int n = cards.length;
		Card[] result = new Card[n];
		for (int i = 0; i < n; i++)
			result[i] = cards[i].clone();
		return result;
	}

	private PlayerInfo[] players = new PlayerInfo[4];
	private int playersCount = 1;
	private final DatabaseManager databaseManager;
	private final ActionQueue controller;

	private ChatMessage clonedMessage;

	public PlayersManager(String owner, ServletNotificationsInterface snf,
			DatabaseManager databaseManager, ActionQueue controller)
			throws SQLException, NoSuchUserException {

		if (owner == null || snf == null)
			throw new IllegalArgumentException();

		this.databaseManager = databaseManager;
		this.controller = controller;

		int score = databaseManager.getPlayerScore(owner);
		players[0] = new PlayerInfo(owner, score, snf, new LoggerBot(owner));
	}

	public void addBot(String userName, int position, String botName,
			TableInterface tableInterface) throws FullPositionException,
			NotCreatorException {

		if (position < 1 || position > 3 || userName == null
				|| tableInterface == null)
			throw new IllegalArgumentException();
		if (players[position] != null)
			throw new FullPositionException();
		if (!userName.equals(players[Positions.OWNER.ordinal()].name))
			throw new NotCreatorException("Creator: "
					+ players[Positions.OWNER.ordinal()] + ". Current user: "
					+ userName);

		InitialTableStatus initialTableStatus = this
				.getInitialTableStatus(position);
		NonRemoteBot bot = new NonRemoteBot(botName, initialTableStatus,
				tableInterface, position);
		players[position] = new PlayerInfo(botName,
				bot.getServletNotificationsInterface());
		playersCount++;
	}

	public int addPlayer(String playerName, ServletNotificationsInterface sni,
			int score) throws FullTableException, DuplicateUserNameException {

		if (playerName == null)
			throw new IllegalArgumentException();
		if (playersCount > 4)
			throw new FullTableException();

		int position = 1;
		while ((players[position] != null) && (position < 4))
			position++;

		if (position == 4)
			throw new FullTableException();

		/* check for duplicate user name */
		for (int i = 0; i < 4; i++) {
			if (players[i] != null && players[i].name.equals(playerName)) {
				throw new DuplicateUserNameException(playerName);
			}
		}

		NonRemoteBot replacementBot = new NonRemoteBot(playerName,
				this.getInitialTableStatus(position), position);

		players[position] = new PlayerInfo(playerName, score, sni,
				replacementBot);
		playersCount++;

		return position;
	}

	public void addPlayersInformationForViewers(
			ObservedGameStatus observedGameStatus) {
		for (int i = 0; i < 4; i++) {
			if (players[i] != null) {
				observedGameStatus.playerStatus[i] = new PlayerStatus();
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
				opponents[i] = players[next].name;
				playerPoints[i] = players[next].score;
				whoIsBot[i] = players[next].isBot;
			}
		}
		return new InitialTableStatus(opponents, playerPoints, whoIsBot);
	}

	public String getPlayerName(int i) throws NoSuchPlayerException {
		if (players[i] != null)
			return players[i].name;
		throw new NoSuchPlayerException();
	}

	public int getPlayerPosition(String playerName)
			throws NoSuchPlayerException {
		for (int i = 0; i < 4; i++) {
			if (players[i] != null && players[i].name.equals(playerName))
				return i;
		}
		throw new NoSuchPlayerException("\"" + playerName + "\"\n"
				+ Arrays.toString(players));
	}

	public void notifyBotJoined(final String botName, final int position) {
		/*
		 * notify every players but the one who is adding the bot and the bot
		 * itself
		 */
		for (int i = 1; i < 4; i++) {
			final PlayerInfo player = players[i];
			final int relativePosition = toRelativePosition(position, i);
			if (i != position && player != null) {
				controller.enqueue(new RemoteAction() {
					@Override
					public void onExecute() throws RemoteException {
						player.playerNotificationInterface.notifyPlayerJoined(
								botName, true, 0, relativePosition);
					}
				});
				if (!players[i].isBot) {
					controller.enqueue(new RemoteAction() {
						@Override
						public void onExecute() throws RemoteException {
							player.inactiveReplacementBotSNI
									.notifyPlayerJoined(botName, true, 0,
											relativePosition);
						}
					});
				}
			}
		}
	}

	public void notifyGameEnded(int[] matchPoints, int[] playersTotalPoint) {
		if (matchPoints == null || playersTotalPoint == null)
			throw new IllegalArgumentException();
		for (final PlayerInfo player : players) {
			final int[] matchPointsClone = matchPoints.clone();
			final int[] playersTotalPointClone = playersTotalPoint.clone();
			if (player == null)
				throw new IllegalStateException();
			controller.enqueue(new RemoteAction() {
				@Override
				public void onExecute() throws RemoteException {
					player.playerNotificationInterface.notifyGameEnded(
							matchPointsClone, playersTotalPointClone);
				}
			});
			if (!player.isBot) {
				final int[] matchPointsClone2 = matchPoints.clone();
				final int[] playersTotalPointClone2 = playersTotalPoint.clone();
				controller.enqueue(new RemoteAction() {
					@Override
					public void onExecute() throws RemoteException {
						player.inactiveReplacementBotSNI.notifyGameEnded(
								matchPointsClone2, playersTotalPointClone2);
					}
				});
			}
		}
	}

	public void notifyGameEndedPrematurely() {
		for (final PlayerInfo player : players) {
			if (player != null) {
				controller.enqueue(new RemoteAction() {
					@Override
					public void onExecute() throws RemoteException {
						player.playerNotificationInterface.notifyGameEnded(
								null, null);
					}
				});
				if (!player.isBot) {
					controller.enqueue(new RemoteAction() {
						@Override
						public void onExecute() throws RemoteException {
							player.inactiveReplacementBotSNI.notifyGameEnded(
									null, null);
						}
					});
				}
			}
		}
	}

	public void notifyGameStarted(Card[][] cards) {
		for (int i = 0; i < 4; i++) {
			final PlayerInfo player = players[i];
			final Card[] playerCards = cloneCardArray(cards[i]);
			if (player == null) {
				throw new IllegalStateException(
						"cannot start game: missing player " + i);
			}
			controller.enqueue(new RemoteAction() {
				@Override
				public void onExecute() throws RemoteException {
					player.playerNotificationInterface
							.notifyGameStarted(playerCards);
				}
			});
			final Card[] playerCards2 = cloneCardArray(cards[i]);
			if (!player.isBot) {
				controller.enqueue(new RemoteAction() {
					@Override
					public void onExecute() throws RemoteException {
						player.inactiveReplacementBotSNI
								.notifyGameStarted(playerCards2);
					}
				});
			}
		}
	}

	public void notifyNewLocalChatMessage(ChatMessage message) {
		for (final PlayerInfo player : players) {
			clonedMessage = message.clone();
			if (player != null && !player.isBot
					&& !player.name.equals(message.userName)) {
				controller.enqueue(new RemoteAction() {
					@Override
					public void onExecute() throws RemoteException {
						player.playerNotificationInterface
								.notifyLocalChatMessage(clonedMessage);
					}
				});
			}
		}
	}

	public void notifyPlayedCard(String userName, Card card)
			throws NoSuchPlayerException {
		final int position = getPlayerPosition(userName);
		for (int i = 0; i < 4; i++) {
			final PlayerInfo player = players[i];
			final int relativePosition = toRelativePosition(position, i);
			if (player == null) {
				throw new IllegalStateException("missing player " + i);
			}
			if (!player.name.equals(userName)) {
				final Card cardClone = card.clone();
				controller.enqueue(new RemoteAction() {
					@Override
					public void onExecute() throws RemoteException {
						player.playerNotificationInterface.notifyPlayedCard(
								cardClone, relativePosition);
					}
				});
				if (!player.isBot) {
					final Card cardClone2 = card.clone();
					controller.enqueue(new RemoteAction() {
						@Override
						public void onExecute() throws RemoteException {
							player.inactiveReplacementBotSNI.notifyPlayedCard(
									cardClone2, relativePosition);
						}
					});
				}
			}
		}
	}

	public void notifyPlayerJoined(final String playerName, final int score,
			int position) {
		/* notify every players but the one who is joining */
		for (int i = 0; i < 4; i++) {
			final PlayerInfo player = players[i];
			final int relativePosition = toRelativePosition(position, i);
			if (i != position && player != null) {
				controller.enqueue(new RemoteAction() {
					@Override
					public void onExecute() throws RemoteException {
						player.playerNotificationInterface.notifyPlayerJoined(
								playerName, false, score, relativePosition);
					}
				});
				if (!player.isBot) {
					controller.enqueue(new RemoteAction() {
						@Override
						public void onExecute() throws RemoteException {
							player.inactiveReplacementBotSNI
									.notifyPlayerJoined(playerName, false,
											score, relativePosition);
						}
					});
				}
			}
		}
	}

	public void notifyPlayerLeft(final String playerName) {
		for (final PlayerInfo player : players) {
			if (player != null && !player.name.equals(playerName)) {
				controller.enqueue(new RemoteAction() {
					@Override
					public void onExecute() throws RemoteException {
						player.playerNotificationInterface
								.notifyPlayerLeft(playerName);
					}
				});
				if (!player.isBot) {
					controller.enqueue(new RemoteAction() {
						@Override
						public void onExecute() throws RemoteException {
							player.inactiveReplacementBotSNI
									.notifyPlayerLeft(playerName);
						}
					});
				}
			}
		}
	}

	/**
	 * <code>position</code> is the position of player who passed cards
	 * 
	 * @param playerName
	 */
	public void notifyPlayerPassedCards(int position, Card[] cards) {
		int receiverIndex = (position + 5) % 4;
		final PlayerInfo receiver = players[receiverIndex];
		if (receiver == null) {
			throw new IllegalStateException(
					"cannot pass cards: missing player " + receiverIndex);
		}
		final Card[] cardsClone = cloneCardArray(cards);
		controller.enqueue(new RemoteAction() {
			@Override
			public void onExecute() throws RemoteException {
				receiver.playerNotificationInterface
						.notifyPassedCards(cardsClone);
			}
		});
		if (!receiver.isBot) {
			final Card[] cardsClone2 = cloneCardArray(cards);
			controller.enqueue(new RemoteAction() {
				@Override
				public void onExecute() throws RemoteException {
					receiver.inactiveReplacementBotSNI
							.notifyPassedCards(cardsClone2);
				}
			});
		}
	}

	public void notifyPlayerReplaced(final String playerLeftName, int position)
			throws FullPositionException, EmptyPositionException,
			NoSuchPlayerException {
		for (int i = 0; i < 4; i++) {
			if (i != position) {
				final PlayerInfo player = players[i];
				final int relativePosition = toRelativePosition(position, i);
				if (player == null) {
					throw new IllegalStateException("missing player " + i);
				}
				controller.enqueue(new RemoteAction() {
					@Override
					public void onExecute() throws RemoteException {
						player.playerNotificationInterface
								.notifyPlayerReplaced(playerLeftName,
										relativePosition);
					}
				});
				if (!player.isBot) {
					controller.enqueue(new RemoteAction() {
						@Override
						public void onExecute() throws RemoteException {
							player.inactiveReplacementBotSNI
									.notifyPlayerReplaced(playerLeftName,
											relativePosition);
						}
					});
				}
			}
		}
	}

	public int olayersCount() {
		return playersCount;
	}

	public void print() {
		System.out.println(Arrays.toString(players));
	}

	public void removePlayer(String playerName) throws NoSuchPlayerException {
		int position = getPlayerPosition(playerName);
		if (position == -1)
			throw new NoSuchPlayerException();
		if (playersCount < 1)
			throw new IllegalStateException();
		playersCount--;
		players[position] = null;
	}

	public void replacementBotPassCards(int position, Card[] cards) {
		if (position < 0 || position > 3 || cards == null)
			throw new IllegalArgumentException();
		if (players[position] == null)
			throw new IllegalStateException();
		if (!players[position].isBot) {
			final Card[] clonedCards = cloneCardArray(cards);
			final NonRemoteBotInterface inactiveReplacementBot = players[position].inactiveReplacementBot;
			controller.enqueue(new RemoteAction() {
				@Override
				public void onExecute() throws RemoteException {
					inactiveReplacementBot.passCards(clonedCards);
				}
			});
		}
	}

	public void replacementBotPlayCard(int position, Card card) {
		if (position < 0 || position > 3 || card == null)
			throw new IllegalArgumentException();
		if (players[position] == null)
			throw new IllegalStateException();
		if (!players[position].isBot) {
			final NonRemoteBotInterface inactiveReplacementBot = players[position].inactiveReplacementBot;
			final Card cardClone = card.clone();
			controller.enqueue(new RemoteAction() {
				@Override
				public void onExecute() throws RemoteException {
					inactiveReplacementBot.playCard(cardClone);
				}
			});
		}
	}

	public void replacePlayer(String playerName, int position,
			final TableInterface tableInterface) throws NoSuchPlayerException {
		if (playerName == null || tableInterface == null || position < 1
				|| position > 3)
			throw new IllegalArgumentException();
		if (players[position].isBot)
			throw new IllegalStateException("attemp to replace a bot!");

		players[position].isBot = true;
		players[position].replaced = true;
		players[position].playerNotificationInterface = players[position].inactiveReplacementBotSNI;
		final NonRemoteBotInterface inactiveReplacementBot = players[position].inactiveReplacementBot;
		controller.enqueue(new RemoteAction() {
			@Override
			public void onExecute() throws RemoteException {
				inactiveReplacementBot.activate(tableInterface);
			}
		});
		players[position].inactiveReplacementBot = null;
		players[position].inactiveReplacementBotSNI = null;
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
			if (players[i] == null) {
				throw new IllegalStateException("missing player " + i);
			}
			if (!players[i].isBot) {
				if (matchPoints[i] == min) {
					players[i].score += 4;
				} else {
					players[i].score -= 1;
				}
				newScore[i] = players[i].score;
				try {
					databaseManager.updateScore(players[i].name,
							players[i].score);
				} catch (NoSuchUserException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return newScore;
	}

	public int nonBotPlayersCount() {
		int nonBotPlayersCount = 4;
		for (int i = 0; i < 4; i++) {
			if (players[i].isBot) {
				nonBotPlayersCount--;
			}
		}
		return nonBotPlayersCount;
	}
}
