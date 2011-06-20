package unibo.as.cupido.backendInterfacesImpl.table.playerUI;

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
import unibo.as.cupido.backendInterfacesImpl.ltm.LocalTableManager;
import unibo.as.cupido.common.exception.AllLTMBusyException;
import unibo.as.cupido.common.exception.FullTableException;
import unibo.as.cupido.common.exception.NotCreatorException;
import unibo.as.cupido.common.exception.PositionFullException;
import unibo.as.cupido.common.interfaces.GlobalTableManagerInterface;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.InitialTableStatus;
import unibo.as.cupido.common.structures.TableInfoForClient;

public class PlayerConsoleUI {
	private static final String FORMAT = "%-20.20s %-3.3s %-10.10s %-30.30s\n";

	private static final String USAGE = String.format(FORMAT + FORMAT + FORMAT
			+ FORMAT + FORMAT + FORMAT + FORMAT + FORMAT + FORMAT, "COMMAND",
			"OPT", "LONG_OPT", "DESCRIPTION", "create", "", "",
			"create a new table", "exit", "", "", "", "list", "-p",
			"--players", "list all player in the table", "list", "-c",
			"--cards", "list this player cards", "list", "-t", "--tables",
			"list all tables", "login NAME PASSWORD", "", "", "", "pass", "-a",
			"--arbitrary", "pass arbitrary cards", "pass CARD1 CARD2 CARD3",
			"-c", "--card", "pass specified cards", "play", "-a",
			"--arbitrary", "play an arbitrary card");

	public static void main(String[] args) throws Exception {

		new PlayerConsoleUI(
				"/home/cane/workspace/cupido/cupidoBackendImpl/test/createTableAndAddThreeBot")
				.execute();
	}

	private LocalTableManager localTableManager = null;
	private final GlobalTableManagerInterface gtm;
	private String playerName;
	private TableInfoForClient table;
	private final BufferedReader in;
	private final PrintWriter out;
	private boolean logged = false;
	private Bot botNotification;
	private RemoteBot abstractBot;

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
				nextCommandLine = in.readLine();
				out.println(nextCommandLine);

				if (nextCommandLine == null) {
					exit(0);
				}
				try {
					parser.parse(nextCommandLine.split("\\s+"));
					command = parser.getRemainingArgs();
					if (command.length < 1) {
						error = true;
						out.println("\n1 sintax error\n " + USAGE);
					}
				} catch (IllegalOptionValueException e) {
					error = true;
					out.println("\n2 sintax error\n " + USAGE);
				} catch (UnknownOptionException e) {
					error = true;
					out.println("\n3 sintax error\n " + USAGE);
				}
			} while (error);
			if (command[0].equals("exit")) {
				exit(0);
			} else if (!logged && !command[0].equals("login")) {
				out.println("log first!");
				out.println(USAGE);
			} else if (command[0].equals("sleep")) {
				out.flush();
				try {
					Thread.sleep(Integer.parseInt(command[1]));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (command[0].equals("pass")) {
				boolean specifiedCards = (parser.getOptionValue(cardsOption) == null ? false
						: true);
				boolean arbitraryCards = (parser
						.getOptionValue(arbitraryCardsOption) == null ? false
						: true);
				if (arbitraryCards) {
					abstractBot.passCards();
				} else if (specifiedCards) {
					throw new UnsupportedOperationException();
				} else {
					out.println("\nmissing option");
				}
			} else if (command[0].equals("play")) {
				boolean arbitraryCards = (parser
						.getOptionValue(arbitraryCardsOption) == null ? false
						: true);
				if (arbitraryCards) {
					abstractBot.playNextCard();
				} else {
					throw new UnsupportedOperationException();
				}
			} else if (command[0].equals("login")) {
				logged = true;
				playerName = command[1];
				// TODO
				abstractBot = new RemoteBot(new InitialTableStatus(
						new String[3], new int[3], new boolean[3]), null,
						playerName);
				botNotification = (Bot) UnicastRemoteObject
						.exportObject(abstractBot);
				out.println("successfully logged in " + playerName);
			} else if (command[0].equals("create")) {
				try {
					abstractBot.singleTableManager = gtm.createTable(
							playerName, botNotification);
				} catch (AllLTMBusyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (command[0].equals("list")) {
				boolean listTables = (parser.getOptionValue(listTablesOption) == null ? false
						: true);
				boolean listPlayers = (parser.getOptionValue(listPlayersOption) == null ? false
						: true);
				boolean listCards = (parser.getOptionValue(cardsOption) == null ? false
						: true);
				if (listTables) {
					out.println(Arrays.toString(gtm.getTableList().toArray(
							new TableInfoForClient[1])));
				} else if (listPlayers) {
					out.println(abstractBot.initialTableStatus);
				} else if (listCards) {
					out.print("\n"
							+ Arrays.toString(abstractBot.cards
									.toArray(new Card[13]))
							+ ". \nround cards: "
							+ Arrays.toString(abstractBot.playedCard));
				}
			} else if (command[0].equals("addbot")) {
				try {
					int position = Integer.parseInt(command[1]);
					abstractBot.singleTableManager.addBot(playerName, position);
					position--;
					abstractBot.addBot(position);
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (PositionFullException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FullTableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NotCreatorException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				out.println(command[0] + ": command not found");
				out.println(USAGE);
			}
		}
	}

	private void exit(int exitStatus) {
		try {
			in.close();
		} catch (IOException e) {
		}
		out.close();
		try {
			if (abstractBot.singleTableManager != null) {
				gtm.notifyTableDestruction(table.tableDescriptor,
						localTableManager);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(exitStatus);
	}

}
