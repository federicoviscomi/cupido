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

package unibo.as.cupido.backend.playerUI;

import jargs.gnu.CmdLineParser;
import jargs.gnu.CmdLineParser.IllegalOptionValueException;
import jargs.gnu.CmdLineParser.Option;
import jargs.gnu.CmdLineParser.UnknownOptionException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.rmi.AccessException;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import unibo.as.cupido.common.database.DatabaseManager;
import unibo.as.cupido.common.exception.AllLTMBusyException;
import unibo.as.cupido.common.exception.DuplicateUserNameException;
import unibo.as.cupido.common.exception.DuplicateViewerException;
import unibo.as.cupido.common.exception.FullPositionException;
import unibo.as.cupido.common.exception.FullTableException;
import unibo.as.cupido.common.exception.GameEndedException;
import unibo.as.cupido.common.exception.GameInterruptedException;
import unibo.as.cupido.common.exception.NoSuchLTMException;
import unibo.as.cupido.common.exception.NoSuchPlayerException;
import unibo.as.cupido.common.exception.NoSuchTableException;
import unibo.as.cupido.common.exception.NoSuchUserException;
import unibo.as.cupido.common.exception.NotCreatorException;
import unibo.as.cupido.common.exception.WrongGameStateException;
import unibo.as.cupido.common.interfaces.DatabaseInterface;
import unibo.as.cupido.common.interfaces.GlobalTableManagerInterface;
import unibo.as.cupido.common.interfaces.LocalTableManagerInterface;
import unibo.as.cupido.common.interfaces.ServletNotificationsInterface;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.Card.Suit;
import unibo.as.cupido.common.structures.ChatMessage;
import unibo.as.cupido.common.structures.InitialTableStatus;
import unibo.as.cupido.common.structures.TableInfoForClient;

/**
 * This is a command line user interface for a player of Cupido.
 * 
 * The list of commands follows:
 * <table border="1">
 * <tr>
 * <td>SINTAX</td>
 * <td>SEMANTIC</td>
 * </tr>
 * <tr>
 * <td>create</td>
 * <td>create a new table</td>
 * </tr>
 * <tr>
 * <td>join</td>
 * <td>join an arbitrary table</td>
 * </tr>
 * <tr>
 * <td>list -p --players</td>
 * <td>list all players in current table</td>
 * </tr>
 * *
 * <tr>
 * <td>list -c --cards</td>
 * <td>list cards owned by this player</td>
 * </tr>
 * *
 * <tr>
 * <td>list -t --tables</td>
 * <td>list all tables in the gtm</td>
 * </tr>
 * <tr>
 * <td>login USERNAME</td>
 * <td>log into cupido with specified user name</td>
 * </tr>
 * <tr>
 * <td>pass -a --arbitrary</td>
 * <td>pass arbitrary sound cards</td>
 * </tr>
 * <tr>
 * <td>play -a --arbitrary</td>
 * <td>play arbitrary sound cards</td>
 * </tr>
 * <tr>
 * <td>addbot POSITION</td>
 * <td>add a bot in specified position</td>
 * </tr>
 * <tr>
 * <td>help</td>
 * <td>print an help message</td>
 * </tr>
 * <tr>
 * <td>exit</td>
 * <td>exit</td>
 * </tr>
 * <tr>
 * <td>sleep MILLIS</td>
 * <td>suspend the console for specified milliseconds</td>
 * </tr>
 * <tr>
 * <td>leave</td>
 * <td>leave the table(if any)</td>
 * </tr>
 * <tr>
 * <td>view</td>
 * <td>view an arbitrary table</td>
 * </tr>
 * <tr>
 * <td>chat MESSAGE</td>
 * <td>sends specified message to local chat(if any)</td>
 * </tr>
 * <tr>
 * <td></td>
 * </table>
 * <p>
 * 
 * Commands can be read from an input file thus making an automatic player.
 */
public class PlayerConsoleUI {
	private static final String FORMAT = "%-20.20s %-3.3s %-10.10s %-30.30s\n";

	private static final String USAGE = String.format(FORMAT, "COMMAND", "OPT",
			"LONG_OPT", "DESCRIPTION")
			+ String.format(FORMAT, "create", "", "", "create a new table")
			+ String.format(FORMAT, "exit", "", "", "")
			+ String.format(FORMAT, "list", "-p", "--players",
					"list all player in the table")
			+ String.format(FORMAT, "list", "-c", "--cards",
					"list this player cards")
			+ String.format(FORMAT, "list", "-t", "--tables", "list all tables")
			+ String.format(FORMAT, "login NAME PASSWORD", "", "", "")
			+ String.format(FORMAT, "pass", "-a", "--arbitrary",
					"pass arbitrary cards")
			+ String.format(FORMAT, "pass s1 v1 s2 v2 s3 v3", "-c", "--card",
					"pass specified cards cards ")
			+ String.format(FORMAT, "play", "-a", "--arbitrary",
					"play an arbitrary card")
			+ String.format(FORMAT, "play suit value", "", "",
					"play specified card")
			+ String.format(FORMAT, "addbot POSITION", "", "",
					"add a bot in specified absolute position")
			+ String.format(FORMAT, "join", "", "", "join an arbitrary table")
			+ String.format(FORMAT, "help", "", "", "print this help")
			+ String.format(FORMAT, "leave", "", "", "leave the table(if any)")
			+ String.format(FORMAT, "view", "", "",
					"view an arbitrary table in the GTM")
			+ String.format(FORMAT, "join TABLE_POS", "", "",
					"join the table listed in specified position")
			+ String.format(FORMAT, "view TABLE_POS", "", "",
					"views specified table")
			+ String.format(FORMAT, "chat MESSAGE", "", "",
					"send specified message to the local chat");
	private static final String[] allCommands = { "create", "join", "list",
			"login", "pass", "play", "addbot", "help", "exit", "sleep",
			"leave", "view", "chat" };

	/**
	 * Starts a console player interface. If no arguments are given then input
	 * and output are respectively <tt>System.in</tt> and <tt>System.out</tt>.
	 * If one arguments is given then it is name of an input file. If two
	 * arguments are given then the first is name of an input file and second is
	 * name of an output file.
	 * 
	 * @param args
	 * @throws IOException
	 * @throws SQLException
	 */
	public static void main(String[] args) throws IOException, SQLException {
		if (args.length == 0) {
			new PlayerConsoleUI().runInterpreter();
		} else if (args.length == 1) {
			new PlayerConsoleUI(args[0]).runInterpreter();
		} else {
			new PlayerConsoleUI(args[0], args[1]).runInterpreter();
		}
	}

	/** the gtm */
	private GlobalTableManagerInterface gtm;
	/** this player name */
	private String playerName;
	/** reads input from here */
	private final BufferedReader in;
	/** prints output here */
	private final PrintWriter out;
	/** <tt>true</tt> if a player correctly logged in */
	private boolean logged = false;
	/** this player notification are sent to this field */
	private ServletNotificationsInterface botNotification;
	/**
	 * mimic servlet operation on stm. This is meaningful only if player joined
	 * or created a table
	 */
	private AutomaticServlet remoteBot;
	/** viewer ui. This is meaningful only if this player is a viewer */
	private ViewerUI remoteViewer;
	/** <tt>true</tt> if this player created a table; <tt>false</tt> otherwise */
	private boolean creatingATable = false;
	/** <tt>true</tt> if this player joined a table; <tt>false</tt> otherwise */
	private boolean joiningATable = false;
	/** <tt>true</tt> if this player viewed a table; <tt>false</tt> otherwise */
	private boolean viewingATable = false;
	/**
	 * <tt>true</tt> if {@link #parseCommand()} found an error in current
	 * command line; <tt>false</tt> otherwise.
	 */
	private boolean error;
	/** the options parser tool */
	private CmdLineParser parser;
	/** tells if -c --cards option is present input current input line */
	private final Option cardsOption;
	/** tells if -p --players option is present input current input line */
	private final Option listPlayersOption;
	/** tells if -l --tables option is present input current input line */
	private final Option listTablesOption;
	/** tells if -a --arbitrary option is present input current input line */
	private final Option arbitraryCardsOption;
	/** current command line parsed */
	private String[] command;
	/**
	 * <tt>true</tt> after execution of command exit or end of input file;
	 * <tt>false</tt> otherwise
	 */
	private boolean exit = false;
	/** current input line */
	private String currentInputLine;

	private ServletNotificationsInterface servletNotificationsInterface;

	private DatabaseManager databaseManager;

	/**
	 * Creates a player command line user interface which reads from
	 * <tt>System.in</tt> and writes to <tt>System.out</tt>
	 * 
	 * @throws SQLException
	 */
	public PlayerConsoleUI() throws SQLException {
		this(new BufferedReader(new InputStreamReader(System.in)),
				new PrintWriter(System.out));
	}

	/**
	 * Creates a player command line user interface which reads from <tt>in</tt>
	 * and writes to file <tt>out</tt>
	 * 
	 * @throws SQLException
	 */
	public PlayerConsoleUI(BufferedReader in, PrintWriter out)
			throws SQLException {
		this.in = in;
		this.out = out;

		parser = new CmdLineParser();
		cardsOption = parser.addBooleanOption('c', "cards");
		listPlayersOption = parser.addBooleanOption('p', "players");
		listTablesOption = parser.addBooleanOption('t', "tables");
		arbitraryCardsOption = parser.addBooleanOption('a', "arbitrary");

		// databaseManager = new
		// DatabaseManager(DatabaseInterface.DEFAULT_DATABASE_ADDRESS);

		try {
			gtm = (GlobalTableManagerInterface) LocateRegistry.getRegistry()
					.lookup(GlobalTableManagerInterface.GTM_RMI_NAME);
		} catch (AccessException e) {
			out.println("cannot connect to gtm");
		} catch (RemoteException e) {
			out.println("cannot connect to gtm");
		} catch (NotBoundException e) {
			out.println("cannot connect to gtm");
		}
	}

	/**
	 * Creates a player command line user interface which reads from file named
	 * <tt>inputFileName</tt> and writes to <tt>System.out</tt>
	 * 
	 * @throws SQLException
	 */
	public PlayerConsoleUI(String inputFileName) throws FileNotFoundException,
			SQLException {
		this(new BufferedReader(new InputStreamReader(new FileInputStream(
				inputFileName))), new PrintWriter(System.out));
	}

	/**
	 * Creates a player command line user interface which reads from file named
	 * <tt>inputFileName</tt> and writes to file named <tt>outputFileName</tt>
	 * 
	 * @throws SQLException
	 */
	public PlayerConsoleUI(String inputFileName, String outputFileName)
			throws FileNotFoundException, SQLException {
		this(new BufferedReader(new InputStreamReader(new FileInputStream(
				inputFileName))), new PrintWriter(new FileOutputStream(
				outputFileName)));
	}

	/**
	 * Switch the right command
	 */
	private void execute() throws RemoteException {
		if (command[0].equals("help")) {
			executeHelp();
		} else if (command[0].equals("exit")) {
			executeExit();
		} else if (command[0].equals("sleep")) {
			executeSleep();
		} else if (logged) {
			if (command[0].equals("chat")) {
				executeChat();
			} else if (command[0].equals("view")) {
				executeView();
			} else if (command[0].equals("leave")) {
				executeLeave();
			} else if (command[0].equals("join")) {
				executeJoin();
			} else if (command[0].equals("pass")) {
				executePass();
			} else if (command[0].equals("play")) {
				executePlay();
			} else if (command[0].equals("login")) {
				out.println("\n already logged in " + playerName);
			} else if (command[0].equals("create")) {
				executeCreate();
			} else if (command[0].equals("list")) {
				executeList();
			} else if (command[0].equals("addbot")) {
				executeAddbot();
			} else {
				throw new Error();
			}
		} else {
			if (command[0].equals("login")) {
				executeLogin();
			} else {
				out.println("log first!");
				out.flush();
			}
		}
	}

	/**
	 * Execute addbot command
	 * 
	 * @throws RemoteException
	 */
	private void executeAddbot() throws RemoteException {
		try {
			if (command.length < 2) {
				out.println("missin bot position");
				out.flush();
			} else {
				int position = Integer.parseInt(command[1]);
				remoteBot.singleTableManager.addBot(playerName, position);
				position--;
				remoteBot.addBot(position);
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (FullPositionException e) {
			e.printStackTrace();
		} catch (NotCreatorException e) {
			e.printStackTrace();
		} catch (GameInterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Execute chat command
	 */
	private void executeChat() {
		try {
			ChatMessage message = new ChatMessage(playerName,
					currentInputLine.substring("chat".length()));
			if (creatingATable || joiningATable) {
				out.println("sent message " + message);
				remoteBot.singleTableManager.sendMessage(message);
			} else if (viewingATable) {
				out.println("sent message " + message);
				remoteViewer.singleTableManager.sendMessage(message);
			} else {
				out.println("cannot send a chat message rigth now");
			}
		} catch (RemoteException e) {

			e.printStackTrace();
		} catch (GameInterruptedException e) {

			e.printStackTrace();
		}
	}

	/**
	 * Execute create command
	 */
	private void executeCreate() {
		try {

			remoteBot = new AutomaticServlet(new InitialTableStatus(
					new String[3], new int[3], new boolean[3]), null,
					playerName);
			servletNotificationsInterface = remoteBot
					.getServletNotificationsInterface();
			botNotification = (ServletNotificationsInterface) UnicastRemoteObject
					.exportObject(servletNotificationsInterface);

			remoteBot.singleTableManager = gtm.createTable(playerName,
					botNotification);

			creatingATable = true;
			out.println("successfully created table "
					+ remoteBot.singleTableManager);
		} catch (AllLTMBusyException e) {
			out.println("cannot create a table rigth now because all LTM are busy, try again later");
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	/**
	 * Execute exit command
	 */
	private void executeExit() {
		exit = true;

		if (remoteBot != null) {
			remoteBot.actionQueue.killConsumer();
			//try {
				//UnicastRemoteObject.unexportObject(servletNotificationsInterface, true);
			//} catch (NoSuchObjectException e) {
				//
			//}
			try {
				remoteBot.singleTableManager.leaveTable(playerName);
			} catch (Exception e) {
				//
			}
		} else if (remoteViewer != null) {
			remoteBot.actionQueue.killConsumer();
			//try {
				//UnicastRemoteObject.unexportObject(servletNotificationsInterface, true);
			//} catch (NoSuchObjectException e) {
				//
			//}
			try {
				remoteViewer.singleTableManager.leaveTable(playerName);
			} catch (Exception e) {
				//
			}
		}
		try {
			in.close();
		} catch (IOException e) {
			//
		}
	}

	/**
	 * Execute help command
	 */
	private void executeHelp() {
		out.println(USAGE);
		out.flush();
	}

	/**
	 * Execute join command
	 */
	private void executeJoin() {
		try {
			remoteBot = new AutomaticServlet(new InitialTableStatus(
					new String[3], new int[3], new boolean[3]), null,
					playerName);
			botNotification = (ServletNotificationsInterface) UnicastRemoteObject
					.exportObject(remoteBot.getServletNotificationsInterface());

			Iterator<TableInfoForClient> iterator = gtm.getTableList()
					.iterator();
			TableInfoForClient tableInfo = null;
			if (command.length < 2) {
				tableInfo = iterator.next();
			} else {
				int tableNumber = Integer.parseInt(command[1]);
				if (tableNumber <= 0) {
					out.println("wrong table number " + tableNumber);
					return;
				}
				for (int i = 0; i < tableNumber; i++) {
					tableInfo = iterator.next();
				}
			}
			remoteBot.singleTableManager = gtm.getLTMInterface(
					tableInfo.tableDescriptor.ltmId).getTable(
					tableInfo.tableDescriptor.id);
			remoteBot.initialTableStatus = remoteBot.singleTableManager
					.joinTable(playerName, botNotification);
			joiningATable = true;
			out.println("successfully joined "
					+ tableInfo);
		} catch (NoSuchElementException e) {
			e.printStackTrace();
		} catch (NoSuchTableException e) {
			e.printStackTrace();
		} catch (NoSuchLTMException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (FullTableException e) {
			e.printStackTrace();
		} catch (DuplicateUserNameException e) {
			e.printStackTrace();
		} catch (NoSuchUserException e) {
			e.printStackTrace();
		} catch (GameInterruptedException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Execute leave command
	 */
	private void executeLeave() {
		try {
			if (remoteBot != null) {
				remoteBot.singleTableManager.leaveTable(playerName);
				out.println("table " + remoteBot.singleTableManager + " left");
				remoteBot = null;
			} else if (remoteViewer != null) {
				remoteViewer.singleTableManager.leaveTable(playerName);
				out.println("table " + remoteViewer.singleTableManager
						+ " left");
				remoteViewer = null;
			} else {
				out.println("there is no table to leave!");
			}
			creatingATable = joiningATable = viewingATable = false;
		} catch (NoSuchPlayerException e) {

			e.printStackTrace();
		} catch (GameInterruptedException e) {

			e.printStackTrace();
		} catch (RemoteException e) {

			e.printStackTrace();
		} catch (IllegalArgumentException e) {

			e.printStackTrace();
		}
	}

	/**
	 * Execute list command
	 */
	private void executeList() throws RemoteException {
		boolean listTables = (parser.getOptionValue(listTablesOption) == null ? false
				: true);
		boolean listPlayers = (parser.getOptionValue(listPlayersOption) == null ? false
				: true);
		boolean listCards = (parser.getOptionValue(cardsOption) == null ? false
				: true);
		if (listTables) {
			out.println("tables list follows:");
			Collection<TableInfoForClient> tableList = gtm.getTableList();
			Iterator<TableInfoForClient> list = tableList.iterator();
			while (list.hasNext()) {
				out.println(list.next());
			}
		} else if (listPlayers) {
			out.println("players list follows:");
			out.println(remoteBot.initialTableStatus);
		} else if (listCards) {
			if (remoteBot.cards == null) {
				out.println("you don't have any cards");
			} else {
				out.print("\nplayer cards " + remoteBot.cards.toString()
						+ ". \nround cards: "
						+ Arrays.toString(remoteBot.playedCard));
			}
		} else {
			out.println("what to list?");
		}
		out.flush();
	}

	/**
	 * Execute login command
	 */
	private void executeLogin() {
		if (command.length < 2) {
			out.println("missing user name");
			out.flush();
			return;
		}
		playerName = command[1];
		// try {
		// databaseManager.login(playerName, command[2]);
		if (gtm == null) {
			try {
				gtm = (GlobalTableManagerInterface) LocateRegistry
						.getRegistry().lookup(
								GlobalTableManagerInterface.GTM_RMI_NAME);
			} catch (AccessException e) {
				out.println("cannot connect to gtm");
			} catch (RemoteException e) {
				out.println("cannot connect to gtm");
			} catch (NotBoundException e) {
				out.println("cannot connect to gtm");
			}
		}
		if (gtm != null) {
			logged = true;
			out.println("successfully logged in " + playerName);
		}
		// } catch (NoSuchUserException e1) {
		// out.println("inavlid user name or password: " + playerName + " "
		// + command[2]);
		// }
	}

	/**
	 * Execute pass command
	 */
	private void executePass() throws RemoteException {
		boolean specifiedCards = (parser.getOptionValue(cardsOption) == null ? false
				: true);
		boolean arbitraryCards = (parser.getOptionValue(arbitraryCardsOption) == null ? false
				: true);
		if (arbitraryCards) {
			remoteBot.passCards(null);
		} else if (specifiedCards) {
			if (command.length < 8) {
				out.println("some argument is missing ");
			} else {
				Card[] cardsToPass = new Card[3];
				cardsToPass[1] = parseCard(command[2], command[3]);
				cardsToPass[2] = parseCard(command[4], command[5]);
				cardsToPass[3] = parseCard(command[6], command[7]);
				remoteBot.passCards(cardsToPass);
			}
		} else {
			out.println("\nmissing option");
			out.flush();
		}
	}

	private Card parseCard(String suit, String value) {
		Card.Suit parsedSuit = null;
		for (int i = 0; i < 4; i++) {
			if (Card.Suit.values()[i].toString().equals(suit)) {
				parsedSuit = Card.Suit.values()[i];
			}
		}
		if (parsedSuit == null) {
			out.println("wrong suit " + suit);
			return null;
		}
		int parsedValue = Integer.parseInt(value);
		if (parsedValue < 1 || parsedValue > 13) {
			out.println("value out of range " + value);
			return null;
		}
		return new Card(parsedValue, parsedSuit);
	}

	/**
	 * Execute play command
	 */
	private void executePlay() throws RemoteException {
		boolean arbitraryCards = (parser.getOptionValue(arbitraryCardsOption) == null ? false
				: true);
		if (arbitraryCards) {
			try {
				remoteBot.playNextCard();
			} catch (GameEndedException e) {
				out.println("cannot play a card: game ended");
			}
			return;
		}
		if (command.length < 3) {
			out.println("missing suit or value");
			return;
		}
		Card.Suit suit = null;
		for (int i = 0; i < 4; i++) {
			if (Card.Suit.values()[i].toString().equals(command[1])) {
				suit = Card.Suit.values()[i];
			}
		}
		if (suit == null) {
			out.println("wrong suit " + command[1]);
			return;
		}
		int value = Integer.parseInt(command[2]);
		if (value < 1 || value > 13) {
			out.println("value out of range " + value);
			return;
		}
		remoteBot.playCard(new Card(value, suit));
	}

	/**
	 * Execute sleep command
	 */
	private void executeSleep() {
		try {
			int sleepMillis = Integer.parseInt(command[1]);
			out.println("zzz");
			out.flush();
			Thread.sleep(sleepMillis);
		} catch (NumberFormatException e) {
			out.println("invalid millis " + command[1]);
			return;
		} catch (InterruptedException e) {
			//
		}
	}

	/**
	 * Execute view command
	 */
	private void executeView() {
		try {
			Iterator<TableInfoForClient> iterator = gtm.getTableList()
					.iterator();
			TableInfoForClient tableInfo = null;
			if (command.length > 1) {
				int tablePosition = Integer.parseInt(command[1]);
				if (tablePosition <= 0) {
					out.println("wrong table number " + tablePosition);
					return;
				}
				for (int i = 0; i < tablePosition; i++) {
					tableInfo = iterator.next();
				}
			} else {
				tableInfo = iterator.next();
			}
			LocalTableManagerInterface ltmInterface = gtm
					.getLTMInterface(tableInfo.tableDescriptor.ltmId);

			remoteViewer = new ViewerUI(playerName, ltmInterface, tableInfo);

			out.flush();
			viewingATable = true;
			Thread.sleep(200);
			System.in.read();
		} catch (NoSuchElementException e) {
			out.println("wrong table number or no table");
			out.flush();
		} catch (NoSuchLTMException e) {
			e.printStackTrace();
		} catch (NoSuchTableException e) {
			e.printStackTrace();
		} catch (DuplicateViewerException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (WrongGameStateException e) {
			e.printStackTrace();
		} catch (GameInterruptedException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Read and parse a line of input
	 */
	private void parseCommand() throws IOException {
		try {
			currentInputLine = in.readLine();
			out.println(currentInputLine);
			out.flush();
			if (currentInputLine == null) {
				executeExit();
				return;
			}
			currentInputLine = currentInputLine.trim();
			parser.parse(currentInputLine.split("\\s+"));
			command = parser.getRemainingArgs();
			if (command.length < 1) {
				error = true;
				out.println("\n1 sintax error\n " + USAGE);
				out.flush();
			} else {
				command[0] = command[0].trim();
				if (command[0].length() == 0) {
					error = true;
				} else {
					boolean recognizedCommand = false;
					for (String c : allCommands) {
						if (c.equals(command[0])) {
							recognizedCommand = true;
						}
					}
					if (!recognizedCommand) {
						out.println(command[0] + ": command not found");
						out.println(USAGE);
						error = true;
					}
				}
			}
		} catch (IllegalOptionValueException e) {
			error = true;
			out.println("\n2 sintax error\n " + USAGE);
			out.flush();
		} catch (UnknownOptionException e) {
			error = true;
			out.println("\n3 sintax error\n " + USAGE);
			out.flush();
		}
	}

	/**
	 * Print a prompt message
	 */
	private void prompt() {
		out.print("\n#: ");
		out.flush();
	}

	/**
	 * Run the command interpreter.
	 * 
	 * @throws IOException
	 */
	public void runInterpreter() throws IOException {
		while (!exit) {
			do {
				error = false;
				prompt();
				parseCommand();
			} while (error);
			if (!exit) {
				execute();
				out.flush();
			}
		}
		System.out.println("bye!");
	}
}
