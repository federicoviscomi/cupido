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

import unibo.as.cupido.backend.table.bot.LocalBot;
import unibo.as.cupido.backend.table.bot.LocalBotInterface;
import unibo.as.cupido.common.database.DatabaseManager;
import unibo.as.cupido.common.exception.DuplicateUserNameException;
import unibo.as.cupido.common.exception.FullTableException;
import unibo.as.cupido.common.exception.NoSuchPlayerException;
import unibo.as.cupido.common.exception.NoSuchUserException;

import unibo.as.cupido.common.exception.NotCreatorException;
import unibo.as.cupido.common.exception.FullPositionException;
import unibo.as.cupido.common.interfaces.ServletNotificationsInterface;
import unibo.as.cupido.common.interfaces.TableInterface;
import unibo.as.cupido.common.interfaces.TableInterface.Positions;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.ChatMessage;
import unibo.as.cupido.common.structures.InitialTableStatus;
import unibo.as.cupido.common.structures.ObservedGameStatus;
import unibo.as.cupido.common.structures.PlayerStatus;

/**
 * Manages player in a single table.
 */
public class PlayersManager {

	/** store information for bot or human players */
	private static class PlayerInfo {

		/** this player name */
		final String name;

		/**
		 * player global score. Not score! This field is meaningful if and only
		 * if <code>isBot == false</code>
		 */
		int score;

		/**
		 * the notification interface for this player. This field is always
		 * meaningful.
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
		LocalBotInterface inactiveReplacementBot;

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

		/**
		 * Create info for a player.
		 * 
		 * @param name
		 *            player name
		 * @param score
		 *            player score
		 * @param playerNotificationInterface
		 *            player notification interface
		 * @param replacementBot
		 *            interface of local replacement bot of the player
		 */
		public PlayerInfo(String name, int score,
				ServletNotificationsInterface playerNotificationInterface,
				LocalBotInterface replacementBot) {

			if (name == null || playerNotificationInterface == null
					|| replacementBot == null)
				throw new IllegalArgumentException(name + " "
						+ playerNotificationInterface);

			this.name = name;
			this.score = score;
			this.playerNotificationInterface = playerNotificationInterface;
			this.inactiveReplacementBot = replacementBot;
			this.inactiveReplacementBotSNI = replacementBot
					.getServletNotificationsInterface();
			this.isBot = false;
			this.replaced = false;
		}

		/**
		 * Create new info for a bot
		 * 
		 * @param name
		 *            bot name
		 * @param bot
		 *            bot notification interface
		 */
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

	/**
	 * Create a deep copy of the given array. No reference are shared between
	 * the copy and the original.
	 * 
	 * @param cards
	 *            the array to clone
	 * @return a deep copy of the given array
	 */
	private static Card[] cloneCardArray(Card[] cards) {
		int n = cards.length;
		Card[] result = new Card[n];
		for (int i = 0; i < n; i++)
			result[i] = cards[i].clone();
		return result;
	}

	/** stores all players informations */
	private PlayerInfo[] players = new PlayerInfo[4];
	/** number of player */
	private int playersCount = 1;
	/** used to access the database */
	private final DatabaseManager databaseManager;
	/** controls action for sending notification to players and bots */
	private final ActionQueue controller;
	/** a deep copy of last received chat message */
	private ChatMessage clonedMessage;

	/**
	 * Create a new players manager and adds the specified creator.
	 * 
	 * @param creator
	 *            the creator of the table
	 * @param snf
	 *            the notification interface of the creator
	 * @param databaseManager
	 *            used to access the database
	 * @param controller
	 *            the action queue, used to send notification to players and
	 *            bots
	 * 
	 * @throws SQLException
	 *             if a deployment or implementation problem occurs.
	 * @throws NoSuchUserException
	 *             if there is no user with name <tt>creator</tt> in the
	 *             database
	 */
	public PlayersManager(String creator, ServletNotificationsInterface snf,
			DatabaseManager databaseManager, ActionQueue controller)
			throws SQLException, NoSuchUserException {

		if (creator == null || snf == null)
			throw new IllegalArgumentException();

		this.databaseManager = databaseManager;
		this.controller = controller;

		int score = databaseManager.getPlayerScore(creator);
		players[0] = new PlayerInfo(creator, score, snf, new LoggerBot(creator));
	}

	/**
	 * Add specified bot in the table
	 * 
	 * @param userName
	 *            the name of player issuing the call to this method
	 * @param position
	 *            the position of the bot to add
	 * @param botName
	 *            the name of the bot to add
	 * @param tableInterface
	 *            the interface of the table associated with this players
	 *            manager //TODO use a field instead of a parameter
	 * @throws FullPositionException
	 *             if table already has four players
	 * @throws NotCreatorException
	 *             if <tt>userName</tt> is not creator of this table
	 */
	public void addBot(String userName, int position, String botName,
			TableInterface tableInterface) throws FullPositionException,
			NotCreatorException {

		if (position < 1 || position > 3 || userName == null
				|| tableInterface == null)
			throw new IllegalArgumentException();
		if (players[position] != null)
			throw new FullPositionException(position);
		if (!userName.equals(players[Positions.OWNER.ordinal()].name))
			throw new NotCreatorException("Creator: "
					+ players[Positions.OWNER.ordinal()] + ". Current user: "
					+ userName);

		InitialTableStatus initialTableStatus = this
				.getInitialTableStatus(position);
		LocalBot bot = new LocalBot(botName, initialTableStatus, tableInterface);
		players[position] = new PlayerInfo(botName,
				bot.getServletNotificationsInterface());
		playersCount++;
	}

	/**
	 * Adds specified player to the table
	 * 
	 * @param playerName
	 *            name of player to add
	 * @param sni
	 *            notification interface of player to add
	 * @param score
	 *            score of player to add
	 * @return position of the player added
	 * @throws FullTableException
	 *             if this table already has four players
	 * @throws DuplicateUserNameException
	 *             if this table already has a player named <tt>playerName</tt>
	 */
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

		LocalBot replacementBot = new LocalBot(playerName,
				this.getInitialTableStatus(position));

		players[position] = new PlayerInfo(playerName, score, sni,
				replacementBot);
		playersCount++;

		return position;
	}

	/**
	 * Adds to <tt>observedGameStatus<tt> information for viewers of this table
	 * 
	 * @param observedGameStatus
	 *            the observed game status from the point of view of a viewer
	 */
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

	/**
	 * Get initial game status. This is used to get table information for new
	 * players.
	 * 
	 * @param position
	 *            position of the player who call this method.
	 * @return information on game status from the point of view of a player who
	 *         joins the table in specified position.
	 */
	public InitialTableStatus getInitialTableStatus(int position) {
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

	/**
	 * Gets name of player in specified position if any.
	 * 
	 * @param position
	 *            position of the player to get the name of
	 * @return name of player in specified position if any.
	 * @throws NoSuchPlayerException
	 *             if there is no player in specified position
	 */
	public String getPlayerName(int position) throws NoSuchPlayerException {
		if (players[position] != null)
			return players[position].name;
		throw new NoSuchPlayerException(position);
	}

	/**
	 * Get position of player with specified name
	 * 
	 * @param playerName
	 *            name of the player
	 * @return position of player with specified name if any.
	 * @throws NoSuchPlayerException
	 *             it there is no player with specified name
	 */
	public int getPlayerPosition(String playerName)
			throws NoSuchPlayerException {
		for (int i = 0; i < 4; i++) {
			if (players[i] != null && players[i].name.equals(playerName))
				return i;
		}
		throw new NoSuchPlayerException("\"" + playerName + "\"\n"
				+ Arrays.toString(players));
	}

	/**
	 * Returns number of players in the table.
	 * 
	 * @return number of players in the table.
	 */
	public int getPlayersCount() {
		return playersCount;
	}

	/**
	 * Returns number of non bot players remained in the table.
	 * 
	 * @return number of non bot players remained in the table.
	 */
	public int nonBotPlayersCount() {
		int nonBotPlayersCount = 4;
		for (int i = 0; i < 4; i++) {
			if (players[i] == null) {
				nonBotPlayersCount--;
			} else {
				if (players[i].isBot) {
					nonBotPlayersCount--;
				}
			}
		}
		return nonBotPlayersCount;
	}

	/**
	 * Notify every player but the one who is adding the bot and the bot itself
	 * that a bot joined.
	 * 
	 * @param botName
	 *            name of bot who joined
	 * @param position
	 *            position in which the bot is joining
	 */
	public void notifyBotJoined(final String botName, final int position) {
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

	/**
	 * Notify every player that game ended.
	 * 
	 * @param matchPoints
	 *            score of every player in this match
	 * @param playersTotalPoint
	 *            score of every player
	 */
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

	/**
	 * Notify every player that game ended prematurely
	 */
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

	/**
	 * Notify every player that game started.
	 * 
	 * @param cards
	 *            all cards dealt
	 */
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

	/**
	 * Notifiy every player that there is a new local chat message
	 * 
	 * @param message
	 *            the new local chat message
	 */
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

	/**
	 * Notify every other player that specified player played specified cards
	 * 
	 * @param userName
	 *            name of player who played
	 * @param card
	 *            card played by the player
	 * @throws NoSuchPlayerException
	 *             name of player who played
	 */
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

	/**
	 * Notify every other player that specified player joined.
	 * 
	 * @param playerName
	 *            name of player who joined
	 * @param score
	 *            score of player who joined
	 * @param position
	 *            position of player who joined
	 */
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

	/**
	 * Notify every player that specified player has left
	 * 
	 * @param playerName
	 *            the player who has left
	 */
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
	 * When player in position <tt>position</tt> passes cards <tt>cards</tt> to
	 * a player receiver, this last player got notified.
	 * 
	 * @param position
	 *            position of player who passes cards
	 * @param cards
	 *            cards passed
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

	/**
	 * Notify every player that specified player has been replaced.
	 * 
	 * @param playerLeftName
	 *            name of player who left.
	 * @param position
	 *            position of player who left.
	 * @throws EmptyPositionException
	 *             if there is no player in specified position
	 * @throws NoSuchPlayerException
	 *             if there is no player with name <tt>playerLeftName</tt>
	 */
	public void notifyPlayerReplaced(final String playerLeftName, int position)
			throws NoSuchPlayerException {
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

	/**
	 * Remove specified player.
	 * 
	 * @param playerName
	 *            name of player to remove.
	 * @throws NoSuchPlayerException
	 *             if there is no player named <tt>playerName</tt> in this table
	 */
	public void removePlayer(String playerName) throws NoSuchPlayerException {
		int position = getPlayerPosition(playerName);
		if (position == -1)
			throw new NoSuchPlayerException(playerName);
		if (playersCount < 1)
			throw new IllegalStateException();
		playersCount--;
		players[position] = null;
	}

	/**
	 * When a non bot player passes a card, his replacement bot has to mimic the
	 * non bot player. This is used to make the replacement bot in position
	 * <tt>position</tt> pass specified cards.
	 * 
	 * @param position
	 *            the position of replacement player
	 * @param cards
	 *            the card passed by player in specified position
	 *            <tt>position</tt>
	 * 
	 */
	public void replacementBotPassCards(int position, Card[] cards) {
		if (position < 0 || position > 3 || cards == null)
			throw new IllegalArgumentException();
		if (players[position] == null)
			throw new IllegalStateException();
		if (!players[position].isBot) {
			final Card[] clonedCards = cloneCardArray(cards);
			final LocalBotInterface inactiveReplacementBot = players[position].inactiveReplacementBot;
			controller.enqueue(new RemoteAction() {
				@Override
				public void onExecute() throws RemoteException {
					inactiveReplacementBot.passCards(clonedCards);
				}
			});
		}
	}

	/**
	 * When a non bot player plays a card, his replacement bot has to mimic the
	 * non bot player. This is used to make the replacement bot in position
	 * <tt>position</tt> play specified card.
	 * 
	 * @param position
	 *            the position of replacement player
	 * @param card
	 *            the card played by player in specified position
	 *            <tt>position</tt>
	 */
	public void replacementBotPlayCard(int position, Card card) {
		if (position < 0 || position > 3 || card == null)
			throw new IllegalArgumentException();
		if (players[position] == null)
			throw new IllegalStateException();
		if (!players[position].isBot) {
			final LocalBotInterface inactiveReplacementBot = players[position].inactiveReplacementBot;
			final Card cardClone = card.clone();
			controller.enqueue(new RemoteAction() {
				@Override
				public void onExecute() throws RemoteException {
					inactiveReplacementBot.playCard(cardClone);
				}
			});
		}
	}

	/**
	 * Replace specified player. This is accomplished by removing specified
	 * player and then activating his replacement bot.
	 * 
	 * @param playerName
	 *            name of player to be replaced
	 * @param position
	 *            position of player to be replaced
	 * @param tableInterface
	 *            this table interface
	 * @throws NoSuchPlayerException
	 *             if there is no player named <tt>playerName</tt>
	 */
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
		final LocalBotInterface inactiveReplacementBot = players[position].inactiveReplacementBot;
		controller.enqueue(new RemoteAction() {
			@Override
			public void onExecute() throws RemoteException {
				inactiveReplacementBot.activate(tableInterface);
			}
		});
		players[position].inactiveReplacementBot = null;
		players[position].inactiveReplacementBotSNI = null;
	}

	/**
	 * Calculates position of player <tt>absolutePosition1</tt> relative to
	 * player <tt>absolutePosition2</tt>
	 * 
	 * @param absolutePosition1
	 *            absolute position of first player
	 * @param absolutePosition2
	 *            absolute position of second player
	 * @return position of first player from the point of view of the second
	 *         player.
	 */
	private int toRelativePosition(int absolutePosition1, int absolutePosition2) {
		int res;
		if (absolutePosition2 < absolutePosition1)
			res = (absolutePosition1 - absolutePosition2) - 1;
		else
			res = (4 - absolutePosition2) + absolutePosition1 - 1;
		return res;
	}

	/**
	 * After the game ends, score of every player are updated according to match
	 * score.
	 * 
	 * @param matchPoints
	 *            score of every player in this match
	 * @return new score of every players
	 */
	public int[] updateScore(int[] matchPoints) {
		int min = matchPoints[0];
		for (int i = 1; i < 4; i++) {
			if (matchPoints[i] < min)
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
}
