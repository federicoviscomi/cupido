package unibo.as.cupido.backendInterfacesImpl.table.bot;

import jargs.gnu.CmdLineParser;
import jargs.gnu.CmdLineParser.IllegalOptionValueException;
import jargs.gnu.CmdLineParser.UnknownOptionException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import unibo.as.cupido.backendInterfaces.GlobalTableManagerInterface;
import unibo.as.cupido.backendInterfaces.ServletNotificationsInterface;
import unibo.as.cupido.backendInterfaces.TableInterface;
import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.InitialTableStatus;
import unibo.as.cupido.backendInterfaces.common.TableDescriptor;
import unibo.as.cupido.backendInterfaces.common.TableInfoForClient;
import unibo.as.cupido.backendInterfaces.exception.AllLTMBusyException;
import unibo.as.cupido.backendInterfaces.exception.FullTableException;
import unibo.as.cupido.backendInterfaces.exception.NoSuchLTMInterfaceException;
import unibo.as.cupido.backendInterfaces.exception.NotCreatorException;
import unibo.as.cupido.backendInterfaces.exception.PositionFullException;
import unibo.as.cupido.backendInterfacesImpl.ltm.LocalTableManager;
import unibo.as.cupido.backendInterfacesImpl.ltm.LocalTableManagerCommandInterpreterUI;
import unibo.as.cupido.backendInterfacesImpl.table.LTMSwarm;

public class PlayerConsoleUI {
	private static final String FORMAT = "%-7.7s %-3.3s %-10.10s %-20.20s\n";
	private static final String USAGE = String.format(FORMAT + FORMAT + FORMAT
			+ FORMAT + FORMAT, "COMMAND", "OPT", "LONG_OPT", "DESCRIPTION",
			"create", "", "", "craete a new table", "exit", "", "", "", "list",
			"-p", "--players", "list all player in the table", "list", "-c",
			"--cards", "list this player cards", "list", "-t", "--tables",
			"list all tables", "login USER_NAME PASSWORD", "", "", "");

	public static void main(String[] args) throws Exception {
		new PlayerConsoleUI(
				"/home/cane/workspace/test/createTableAndAddThreeBot")
				.execute();
	}

	private LocalTableManager localTableManager = null;
	private final GlobalTableManagerInterface gtm;
	private TableInterface tableInterface;
	private String playerName;
	private ServletNotificationsInterface sni;
	private TableInfoForClient table;
	private InitialTableStatus initialTableStatus;
	private ArrayList<Card> cards;
	private Card[] roundCards;
	private final BufferedReader in;
	private final PrintWriter out;
	private boolean logged = false;

	public PlayerConsoleUI() throws Exception {
		gtm = (GlobalTableManagerInterface) LocateRegistry.getRegistry()
				.lookup(GlobalTableManagerInterface.globalTableManagerName);
		in = new BufferedReader(new InputStreamReader(System.in));
		out = new PrintWriter(System.out);
	}

	public PlayerConsoleUI(String inputFileName) throws Exception {
		gtm = (GlobalTableManagerInterface) LocateRegistry.getRegistry()
				.lookup(GlobalTableManagerInterface.globalTableManagerName);
		in = new BufferedReader(new InputStreamReader(new FileInputStream(
				inputFileName)));
		out = new PrintWriter(System.out);
	}

	public PlayerConsoleUI(String inputFileName, String outputFileName)
			throws Exception {
		gtm = (GlobalTableManagerInterface) LocateRegistry.getRegistry()
				.lookup(GlobalTableManagerInterface.globalTableManagerName);
		in = new BufferedReader(new InputStreamReader(new FileInputStream(
				inputFileName)));
		out = new PrintWriter(new FileOutputStream(outputFileName));
	}

	public void execute() throws IOException {
		CmdLineParser parser = new CmdLineParser();
		CmdLineParser.Option listCardsOption = parser.addStringOption('c',
				"cards");
		CmdLineParser.Option listPlayersOption = parser.addStringOption('p',
				"players");
		CmdLineParser.Option listTablesOption = parser.addStringOption('t',
				"tables");
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
						out.println("\nsintax error\n " + USAGE);
					}
				} catch (IllegalOptionValueException e) {
					error = true;
					out.println("\nsintax error\n " + USAGE);
				} catch (UnknownOptionException e) {
					error = true;
					out.println("\nsintax error\n " + USAGE);
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
			} else if (command[0].equals("login")) {
				logged = true;
				playerName = command[1];
				// TODO
				out.println("successfully logged in " + playerName);
			} else if (command[0].equals("create")) {
				try {
					tableInterface = gtm.createTable(playerName, sni);
				} catch (AllLTMBusyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (command[0].equals("list")) {
				boolean listTables = (parser.getOptionValue(listTablesOption) == null ? false
						: true);
				boolean listPlayers = (parser.getOptionValue(listPlayersOption) == null ? false
						: true);
				boolean listCards = (parser.getOptionValue(listCardsOption) == null ? false
						: true);
				if (listTables) {
					out.println(Arrays.toString(gtm.getTableList().toArray(
							new TableInfoForClient[1])));
				} else if (listPlayers) {
					out.println(initialTableStatus);
				} else if (listCards) {
					out.print("\n"
							+ Arrays.toString(cards.toArray(new Card[13]))
							+ ". \nround cards: " + Arrays.toString(roundCards));
				}
			} else if (command[0].equals("addbot")) {
				try {
					tableInterface.addBot(playerName,
							Integer.parseInt(command[1]));
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
			if (tableInterface != null) {
				gtm.notifyTableDestruction(table.tableDescriptor,
						localTableManager);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(exitStatus);
	}

}
