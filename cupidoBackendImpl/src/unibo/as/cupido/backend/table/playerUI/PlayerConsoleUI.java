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

package unibo.as.cupido.backend.table.playerUI;

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
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import unibo.as.cupido.backend.ltm.LocalTableManager;
import unibo.as.cupido.backend.table.bot.LocalBot;
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
import unibo.as.cupido.common.interfaces.GlobalTableManagerInterface;
import unibo.as.cupido.common.interfaces.LocalTableManagerInterface;
import unibo.as.cupido.common.interfaces.ServletNotificationsInterface;
import unibo.as.cupido.common.structures.ChatMessage;
import unibo.as.cupido.common.structures.InitialTableStatus;
import unibo.as.cupido.common.structures.TableInfoForClient;

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
			+ String.format(FORMAT, "passTODO s1 v1 s2 v2 s3 v3", "-c",
					"--card", "pass specified cards cards ")
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
			+ String.format(FORMAT, "TODOjoin TABLE", "", "",
					"joins specified table")
			+ String.format(FORMAT, "TODOview TABLE", "", "",
					"views specified table")
			+ String.format(FORMAT, "chat MESSAGE", "", "",
					"send specified message to the local chat");
	private static final String[] allCommands = { "create", "join", "list",
			"login", "pass", "play", "addbot", "help", "exit", "sleep",
			"leave", "view", "chat" };

	public static void main(String[] args) throws IOException {
		if (args.length == 0) {
			new PlayerConsoleUI().runInterpreter();
		} else if (args.length == 1) {
			new PlayerConsoleUI(args[0]).runInterpreter();
		} else {
			new PlayerConsoleUI(args[0], args[1]).runInterpreter();
		}
	}

	private LocalTableManager localTableManager = null;
	private GlobalTableManagerInterface gtm;
	private String playerName;
	private final BufferedReader in;
	private final PrintWriter out;
	private boolean logged = false;
	private ServletNotificationsInterface botNotification;
	private RemoteBot remoteBot;
	private RemoteViewerUI remoteViewer;

	private boolean creatingATable = false;
	private boolean joiningATable = false;
	private boolean viewingATable = false;

	private boolean error;
	private CmdLineParser parser;
	private Option cardsOption;
	private Option listPlayersOption;
	private Option listTablesOption;
	private Option arbitraryCardsOption;
	private String[] command;

	private boolean exit;

	private String nextCommandLine;

	private LocalBot bot;

	public PlayerConsoleUI() {
		this(new BufferedReader(new InputStreamReader(System.in)),
				new PrintWriter(System.out));
	}

	public PlayerConsoleUI(BufferedReader in, PrintWriter out) {
		this.in = in;
		this.out = out;

		parser = new CmdLineParser();
		cardsOption = parser.addBooleanOption('c', "cards");
		listPlayersOption = parser.addBooleanOption('p', "players");
		listTablesOption = parser.addBooleanOption('t', "tables");
		arbitraryCardsOption = parser.addBooleanOption('a', "arbitrary");

		try {
			gtm = (GlobalTableManagerInterface) LocateRegistry.getRegistry()
					.lookup(GlobalTableManagerInterface.DEFAULT_GTM_NAME);
		} catch (AccessException e) {
			out.println("cannot connect to gtm");
		} catch (RemoteException e) {
			out.println("cannot connect to gtm");
		} catch (NotBoundException e) {
			out.println("cannot connect to gtm");
		}
	}

	public PlayerConsoleUI(String inputFileName) throws FileNotFoundException {
		this(new BufferedReader(new InputStreamReader(new FileInputStream(
				inputFileName))), new PrintWriter(System.out));
	}

	public PlayerConsoleUI(String inputFileName, String outputFileName)
			throws FileNotFoundException {
		this(new BufferedReader(new InputStreamReader(new FileInputStream(
				inputFileName))), new PrintWriter(new FileOutputStream(
				outputFileName)));
	}

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FullPositionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotCreatorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GameInterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void executeCreate() {
		try {
			// TODO really check the database
			// TODO is really necessary RemoteBot or can be used instead
			// LocalBot
			remoteBot = new RemoteBot(new InitialTableStatus(new String[3],
					new int[3], new boolean[3]), null, playerName);
			botNotification = (ServletNotificationsInterface) UnicastRemoteObject
					.exportObject(remoteBot.getServletNotificationsInterface());

			remoteBot.singleTableManager = gtm.createTable(playerName,
					botNotification);

			creatingATable = true;
			out.println("successfully created table "
					+ remoteBot.singleTableManager);
		} catch (AllLTMBusyException e) {
			out.println("cannot create a table rigth now because all LTM are busy, try again later");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void executeExit() {
		// exit = true;
		try {
			if (remoteBot != null) {
				remoteBot.singleTableManager.leaveTable(playerName);
			} else if (remoteViewer != null) {
				remoteViewer.singleTableManager.leaveTable(playerName);
			}
		} catch (Exception e) {
			//
		}
		try {
			in.close();
		} catch (IOException e) {
			//
		}
		out.println("bye!");
		out.close();
		System.exit(0);
	}

	private void executeHelp() {
		out.println(USAGE);
		out.flush();
	}

	private void executeJoin() {
		try {
			// TODO really check the database
			remoteBot = new RemoteBot(new InitialTableStatus(new String[3],
					new int[3], new boolean[3]), null, playerName);
			botNotification = (ServletNotificationsInterface) UnicastRemoteObject
					.exportObject(remoteBot.getServletNotificationsInterface());

			TableInfoForClient tableInfo = gtm.getTableList().iterator().next();
			remoteBot.singleTableManager = gtm.getLTMInterface(
					tableInfo.tableDescriptor.ltmId).getTable(
					tableInfo.tableDescriptor.id);
			remoteBot.initialTableStatus = remoteBot.singleTableManager
					.joinTable(playerName, botNotification);
			joiningATable = true;
			out.println("successfully joined " + tableInfo);
		} catch (NoSuchElementException e) {
			out.println("no table to join!");
		} catch (NoSuchTableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchLTMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FullTableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DuplicateUserNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchUserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GameInterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GameInterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void executeList() throws RemoteException {
		boolean listTables = (parser.getOptionValue(listTablesOption) == null ? false
				: true);
		boolean listPlayers = (parser.getOptionValue(listPlayersOption) == null ? false
				: true);
		boolean listCards = (parser.getOptionValue(cardsOption) == null ? false
				: true);
		if (listTables) {
			out.println("tables list follows:");
			Iterator<TableInfoForClient> list = gtm.getTableList().iterator();
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

	private void executeLogin() {
		if (command.length < 2) {
			out.println("missing user name");
			out.flush();
		} else {
			playerName = command[1];
			if (gtm == null) {
				try {
					gtm = (GlobalTableManagerInterface) LocateRegistry
							.getRegistry()
							.lookup(GlobalTableManagerInterface.DEFAULT_GTM_NAME);
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
		}
	}

	private void executePass() throws RemoteException {
		boolean specifiedCards = (parser.getOptionValue(cardsOption) == null ? false
				: true);
		boolean arbitraryCards = (parser.getOptionValue(arbitraryCardsOption) == null ? false
				: true);
		if (arbitraryCards) {
			remoteBot.passCards();
		} else if (specifiedCards) {
			// TODO
			throw new UnsupportedOperationException();
		} else {
			out.println("\nmissing option");
			out.flush();
		}
	}

	private void executePlay() throws RemoteException {
		boolean arbitraryCards = (parser.getOptionValue(arbitraryCardsOption) == null ? false
				: true);
		if (arbitraryCards) {
			try {
				remoteBot.playNextCard();
			} catch (GameEndedException e) {
				out.println("cannot play a card: game ended");
			}
		} else {
			throw new UnsupportedOperationException();
		}
	}

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

	private void executeView() {
		try {
			// TODO really check the database

			TableInfoForClient tableInfo = gtm.getTableList().iterator().next();
			LocalTableManagerInterface ltmInterface = gtm
					.getLTMInterface(tableInfo.tableDescriptor.ltmId);

			remoteViewer = new RemoteViewerUI(playerName, ltmInterface,
					tableInfo);

			out.flush();
			viewingATable = true;
			Thread.sleep(200);
			System.in.read();
		} catch (NoSuchElementException e) {
			out.println("there is no table to view");
			out.flush();
		} catch (NoSuchLTMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchTableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DuplicateViewerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WrongGameStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GameInterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void parseCommand() {
		try {
			nextCommandLine = in.readLine();
			out.println(nextCommandLine);
			out.flush();
			if (nextCommandLine == null) {
				executeExit();
				return;
			}
			nextCommandLine = nextCommandLine.trim();
			parser.parse(nextCommandLine.split("\\s+"));
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
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private void prompt() {
		out.print("\n#: ");
		out.flush();
	}

	public void runInterpreter() throws IOException {

		while (true) {
			do {
				error = false;
				prompt();
				parseCommand();
			} while (error);

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
			out.flush();
		}
	}

	private void executeChat() {
		try {
			ChatMessage message = new ChatMessage(playerName,
					nextCommandLine.substring("chat".length()));
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GameInterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
