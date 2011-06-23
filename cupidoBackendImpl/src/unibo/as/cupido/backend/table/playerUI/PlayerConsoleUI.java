package unibo.as.cupido.backend.table.playerUI;

import jargs.gnu.CmdLineParser;
import jargs.gnu.CmdLineParser.IllegalOptionValueException;
import jargs.gnu.CmdLineParser.UnknownOptionException;

import java.io.BufferedReader;
import java.io.FileInputStream;
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

import unibo.as.cupido.backend.ltm.LocalTableManager;
import unibo.as.cupido.common.exception.AllLTMBusyException;
import unibo.as.cupido.common.exception.PlayerNotFoundException;
import unibo.as.cupido.common.interfaces.GlobalTableManagerInterface;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.InitialTableStatus;
import unibo.as.cupido.common.structures.TableInfoForClient;

public class PlayerConsoleUI {
	private static final String FORMAT = "%-20.20s %-3.3s %-10.10s %-30.30s\n";

	private static final String USAGE = String.format(FORMAT + FORMAT + FORMAT
			+ FORMAT + FORMAT + FORMAT + FORMAT + FORMAT + FORMAT + FORMAT
			+ FORMAT + FORMAT + FORMAT, "COMMAND", "OPT", "LONG_OPT",
			"DESCRIPTION", "create", "", "", "create a new table", "exit", "",
			"", "", "list", "-p", "--players", "list all player in the table",
			"list", "-c", "--cards", "list this player cards", "list", "-t",
			"--tables", "list all tables", "login NAME PASSWORD", "", "", "",
			"pass", "-a", "--arbitrary", "pass arbitrary cards",
			"pass CARD1 CARD2 CARD3", "-c", "--card", "pass specified cards",
			"play", "-a", "--arbitrary", "play an arbitrary card",
			"addbot POSITION", "", "",
			"add a bot in specified absolute position", "join", "", "",
			"join an arbitrary table", "help", "", "", "print this help",
			"leave", "", "", "leave the table(if any)");

	private static final String[] allCommands = { "create", "join", "list",
			"login", "pass", "play", "addbot", "help", "exit", "sleep", "leave" };

	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			new PlayerConsoleUI().execute();
		} else if (args.length == 1) {
			new PlayerConsoleUI(args[0]).execute();
		} else {
			new PlayerConsoleUI(args[0], args[1]).execute();
		}
	}

	private LocalTableManager localTableManager = null;
	private final GlobalTableManagerInterface gtm;
	private String playerName;
	private final BufferedReader in;
	private final PrintWriter out;
	private boolean logged = false;
	private Bot botNotification;
	private RemoteBot remoteBot;

	private boolean createdATable = false;
	private boolean joinedATable = false;
	private boolean viewedATable = false;

	public PlayerConsoleUI() throws Exception {
		this(new BufferedReader(new InputStreamReader(System.in)),
				new PrintWriter(System.out));
	}

	public PlayerConsoleUI(BufferedReader in, PrintWriter out)
			throws AccessException, RemoteException, NotBoundException {
		this.in = in;
		this.out = out;
		gtm = (GlobalTableManagerInterface) LocateRegistry.getRegistry()
				.lookup(GlobalTableManagerInterface.globalTableManagerName);
	}

	public PlayerConsoleUI(String inputFileName) throws Exception {
		this(new BufferedReader(new InputStreamReader(new FileInputStream(
				inputFileName))), new PrintWriter(System.out));
	}

	public PlayerConsoleUI(String inputFileName, String outputFileName)
			throws Exception {
		this(new BufferedReader(new InputStreamReader(new FileInputStream(
				inputFileName))), new PrintWriter(new FileOutputStream(
				outputFileName)));
	}

	public void execute() throws IOException {
		CmdLineParser parser = new CmdLineParser();
		CmdLineParser.Option cardsOption = parser
				.addBooleanOption('c', "cards");
		CmdLineParser.Option listPlayersOption = parser.addBooleanOption('p',
				"players");
		CmdLineParser.Option listTablesOption = parser.addBooleanOption('t',
				"tables");
		CmdLineParser.Option arbitraryCardsOption = parser.addBooleanOption(
				'a', "arbitrary");

		String nextCommandLine;

		while (true) {
			boolean error;
			String[] command = null;
			do {
				error = false;
				out.print("\n#: ");
				out.flush();
				nextCommandLine = in.readLine();
				out.println(nextCommandLine);
				out.flush();
				if (nextCommandLine == null) {
					exit(0);
				}
				try {
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
				}
			} while (error);

			if (command[0].equals("help")) {
				out.println(USAGE);
				out.flush();
			} else if (command[0].equals("exit")) {
				exit(0);
			} else if (command[0].equals("sleep")) {
				out.flush();
				try {
					Thread.sleep(Integer.parseInt(command[1]));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (logged) {
				if (command[0].equals("leave")) {
					try {
						if (remoteBot.singleTableManager != null) {
							remoteBot.singleTableManager.leaveTable(playerName);
						} else {
							out.println("there is no table to leave!");
						}
					} catch (PlayerNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (command[0].equals("join")) {
					try {
						TableInfoForClient tableInfo = gtm.getTableList()
								.iterator().next();
						remoteBot.singleTableManager = gtm.getLTMInterface(
								tableInfo.tableDescriptor.ltmId).getTable(
								tableInfo.tableDescriptor.id);
						remoteBot.initialTableStatus = remoteBot.singleTableManager
								.joinTable(playerName, remoteBot);
						out.println("successfully joined " + tableInfo);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (command[0].equals("pass")) {
					boolean specifiedCards = (parser
							.getOptionValue(cardsOption) == null ? false : true);
					boolean arbitraryCards = (parser
							.getOptionValue(arbitraryCardsOption) == null ? false
							: true);
					if (arbitraryCards) {
						remoteBot.passCards();
					} else if (specifiedCards) {
						throw new UnsupportedOperationException();
					} else {
						out.println("\nmissing option");
						out.flush();
					}
				} else if (command[0].equals("play")) {
					boolean arbitraryCards = (parser
							.getOptionValue(arbitraryCardsOption) == null ? false
							: true);
					if (arbitraryCards) {
						remoteBot.playNextCard();
					} else {
						throw new UnsupportedOperationException();
					}
				} else if (command[0].equals("login")) {
					out.println("\n already logged in " + playerName);
				} else if (command[0].equals("create")) {
					try {
						remoteBot.singleTableManager = gtm.createTable(
								playerName, botNotification);
						out.println("successfully created table "
								+ remoteBot.singleTableManager);
					} catch (AllLTMBusyException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (command[0].equals("list")) {
					boolean listTables = (parser
							.getOptionValue(listTablesOption) == null ? false
							: true);
					boolean listPlayers = (parser
							.getOptionValue(listPlayersOption) == null ? false
							: true);
					boolean listCards = (parser.getOptionValue(cardsOption) == null ? false
							: true);
					if (listTables) {
						out.println(Arrays.toString(gtm.getTableList().toArray(
								new TableInfoForClient[1])));
					} else if (listPlayers) {
						out.println(remoteBot.initialTableStatus);
					} else if (listCards) {
						out.print("\n"
								+ Arrays.toString(remoteBot.cards
										.toArray(new Card[13]))
								+ ". \nround cards: "
								+ Arrays.toString(remoteBot.playedCard));
					}
					out.flush();
				} else if (command[0].equals("addbot")) {
					try {
						if (command.length < 2) {
							out.println("missin bot position");
							out.flush();
						} else {
							int position = Integer.parseInt(command[1]);
							remoteBot.singleTableManager.addBot(playerName,
									position);
							position--;
							remoteBot.addBot(position);
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					throw new Error();
				}
			} else {// not logged
				if (command[0].equals("login")) {
					if (command.length < 2) {
						out.println("missing user name");
						out.flush();
					} else {
						logged = true;
						playerName = command[1];
						// TODO really check the database
						remoteBot = new RemoteBot(new InitialTableStatus(
								new String[3], new int[3], new boolean[3]),
								null, playerName);
						botNotification = (Bot) UnicastRemoteObject
								.exportObject(remoteBot);
						out.println("successfully logged in " + playerName);
						out.flush();
					}
				} else {
					out.println("log first!");
					out.println(USAGE);
					out.flush();
				}
			}
			out.flush();
		}
	}

	private void exit(int exitStatus) {
		try {
			if (remoteBot != null) {
				remoteBot.singleTableManager.leaveTable(playerName);
			}
			in.close();
		} catch (Exception e) {
			//
		}
		out.close();
		System.exit(exitStatus);
	}

}
