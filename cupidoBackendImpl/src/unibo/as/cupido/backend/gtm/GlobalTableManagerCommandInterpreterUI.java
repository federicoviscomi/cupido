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

package unibo.as.cupido.backend.gtm;

import jargs.gnu.CmdLineParser;
import jargs.gnu.CmdLineParser.IllegalOptionValueException;
import jargs.gnu.CmdLineParser.Option;
import jargs.gnu.CmdLineParser.UnknownOptionException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.util.Collection;

import unibo.as.cupido.backend.gtm.LTMSwarm.Triple;
import unibo.as.cupido.common.structures.TableInfoForClient;

/**
 * A command interpreter console interface for running and managing a GTM.
 * 
 * The list of commands follows:
 * <table border="1">
 * <tr>
 * <td>SINTAX</td>
 * <td>SEMANTIC</td>
 * </tr>
 * <tr>
 * <td>exit</td>
 * <td>Shutdown the server and exit</td>
 * </tr>
 * <tr>
 * <td>start</td>
 * <td>Start the server</td>
 * </tr>
 * <tr>
 * <td>list -l | --localManagers</td>
 * <td>Shows a list of all the local table managers currently active</td>
 * </tr>
 * <tr>
 * <td>list -t | --table</td>
 * <td>Shows a list of all the local table managers currently active</td>
 * </tr>
 * 
 * </table>
 * 
 */
public class GlobalTableManagerCommandInterpreterUI {

	private static final String FORMAT = "%-20.20s %-3.3s %-10.10s %-30.30s\n";
	private static final String USAGE = String.format(FORMAT, "COMMAND", "OPT",
			"LONG_OPT", "DESCRIPTION")
			+ String.format(FORMAT, "start", "", "", "start the GTM server")
			+ String.format(FORMAT, "exit", "", "", "shutdown the GTM server")
			+ String.format(FORMAT, "list", "-l", "--localManagers",
					"list all LTM managed by this GTM server")
			+ String.format(FORMAT, "list", "-t", "--table",
					"list all table managed by this GTM server");

	public static void main(String[] args) throws UnknownHostException,
			RemoteException, AlreadyBoundException {
		if (args.length > 1 && "start".equals(args[1])) {
			new GlobalTableManagerCommandInterpreterUI(true).runInterpreter();
		} else {
			new GlobalTableManagerCommandInterpreterUI(false).runInterpreter();

		}
	}

	/** stores a reference to the GTM */
	private GlobalTableManager globalTableManager = null;
	/** current input line */
	private String currentInputLine;
	/**
	 * <tt>true</tt> if <tt>parseCommand()</tt> finds error input current input
	 * line; <tt>false</tt> otherwise.
	 */
	private boolean error;
	/** parsed commands */
	private String[] command;
	/** the options parser tool */
	private CmdLineParser parser;
	/** tells if -l option is present input current input line */
	private Option listLocalManagersOtion;
	/** tells if -t option is present input current input line */
	private Option listTableOption;
	/** the input is read from this */
	private BufferedReader in;
	/**
	 * <tt>true</tt> after execution of command exit or end of input file;
	 * <tt>false</tt> otherwise
	 */
	private boolean exit;
	/** stores all known commands */
	private static final String[] allCommands = { "start", "exit", "list" };

	/**
	 * Creates a command interpreter for a gtm
	 * 
	 * @param startGTM
	 *            if <tt>true</tt> this creates also a gtm; otherwise it does
	 *            not create a gtm
	 * @throws RemoteException
	 * @throws UnknownHostException
	 * @throws AlreadyBoundException
	 */
	public GlobalTableManagerCommandInterpreterUI(boolean startGTM)
			throws RemoteException, UnknownHostException, AlreadyBoundException {
		parser = new CmdLineParser();
		listLocalManagersOtion = parser.addBooleanOption('l', "localManagers");
		listTableOption = parser.addBooleanOption('t', "table");
		in = new BufferedReader(new InputStreamReader(System.in));
		if (startGTM) {
			globalTableManager = new GlobalTableManager();
		}
	}

	/**
	 * Switch the right command
	 */
	private void execute() {
		if (command[0].equals("start")) {
			try {
				executeStart();
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (AlreadyBoundException e) {
				e.printStackTrace();
			}
		} else if (command[0].equals("exit")) {
			executeExit(0);
		} else if (globalTableManager == null) {
			System.out.println("start GTM first!");
			System.out.println(USAGE);
		} else if (command[0].equals("list")) {
			try {
				executeList();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Execute command exit
	 * 
	 * @param exitStatus
	 */
	private void executeExit(int exitStatus) {
		if (globalTableManager != null) {
			globalTableManager.shutDown();
		}
		System.out.println("bye!");
		System.out.close();
		exit = true;
	}

	/**
	 * Execute command list
	 * 
	 * @throws RemoteException
	 */
	private void executeList() throws RemoteException {
		boolean listLTM = (parser.getOptionValue(listLocalManagersOtion) == null ? false
				: true);
		boolean listTables = (parser.getOptionValue(listTableOption) == null ? false
				: true);
		if (listLTM) {
			Triple[] allLocalServer = globalTableManager.getAllLTM();
			System.out.format("\n list af all local server follows:");
			for (Triple localServer : allLocalServer) {
				System.out.format("\n %25s", localServer);
			}
		}
		if (listTables) {
			Collection<TableInfoForClient> tableList = globalTableManager
					.getTableList();
			System.out.format("\n list af all tables follows:");
			for (TableInfoForClient table : tableList) {
				System.out.format("\n %25s", table);
			}
		}
		if (!listLTM && !listTables) {
			System.out.println(" nothing to list?");
		}
	}

	/**
	 * Execute command start
	 * 
	 * @throws RemoteException
	 * @throws UnknownHostException
	 * @throws AlreadyBoundException
	 */
	private void executeStart() throws RemoteException, UnknownHostException,
			AlreadyBoundException {
		if (globalTableManager == null) {
			globalTableManager = new GlobalTableManager();
		} else {
			System.out.println("GTM already started!");
		}
	}

	/**
	 * Reads an input line, parses it and checks if input command is one of
	 * <tt>allCommands</tt>
	 */
	private void parseCommand() {
		try {
			currentInputLine = in.readLine();
			System.out.println(currentInputLine);
			System.out.flush();
			if (currentInputLine == null) {
				executeExit(0);
				return;
			}
			currentInputLine = currentInputLine.trim();
			parser.parse(currentInputLine.split("\\s+"));
			command = parser.getRemainingArgs();
			if (command.length < 1) {
				error = true;
				System.out.println("\n1 sintax error\n " + USAGE);
				System.out.flush();
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
						System.out.println(command[0] + ": command not found");
						System.out.println(USAGE);
						error = true;
					}
				}
			}
		} catch (IllegalOptionValueException e) {
			error = true;
			System.out.println("\n2 sintax error\n " + USAGE);
			System.out.flush();
		} catch (UnknownOptionException e) {
			error = true;
			System.out.println("\n3 sintax error\n " + USAGE);
			System.out.flush();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * Print a prompt message in the console
	 */
	private void prompt() {
		System.out.print("\n#: ");
		System.out.flush();
	}

	/**
	 * Run the command interpreter.
	 * 
	 * @throws AlreadyBoundException
	 */
	public void runInterpreter() {
		while (!exit) {
			do {
				error = false;
				prompt();
				parseCommand();
			} while (error);
			execute();
		}
	}
}
