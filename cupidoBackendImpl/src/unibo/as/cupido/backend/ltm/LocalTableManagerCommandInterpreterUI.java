/*  Cupido - An online Hearts game.
 *  Copyright (C) 2011 Lorenzo Belli, Marco Poletti, Federico Viscomi
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed input the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package unibo.as.cupido.backend.ltm;

import jargs.gnu.CmdLineParser;
import jargs.gnu.CmdLineParser.IllegalOptionValueException;
import jargs.gnu.CmdLineParser.Option;
import jargs.gnu.CmdLineParser.UnknownOptionException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * A command interpreter console interface for an LTM.
 * <p>
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
 * <td>create -o creatorName</td>
 * <td>Create a new Table with specified creatorName</td>
 * </tr>
 * <tr>
 * <td>list</td>
 * <td>List all the tables managed by this LTM</td>
 * </tr>
 * </table>
 * 
 */
public class LocalTableManagerCommandInterpreterUI {

	private static final String FORMAT = "%-7.7s %-3.3s %-10.10s %-20.20s\n";
	private static final String USAGE = String.format(FORMAT, "COMMAND", "OPT",
			"LONG_OPT", "DESCRIPTION")
			+ String.format(FORMAT, "start", "", "", "start the LTM server")
			+ String.format(FORMAT, "exit", "", "",
					"shutdown the LTM server and exit")
			+ String.format(FORMAT, "list", "-t", "--table",
					"list all table managed by this LTM server")
			+ String.format(FORMAT, "create", "-c", "--creator",
					"create a new table with specified creator");

	public static void main(String[] args) throws UnknownHostException {
		if (args.length > 1 && "start".equals(args[1])) {
			try {
				new LocalTableManagerCommandInterpreterUI(true)
						.runInterpreter();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				new LocalTableManagerCommandInterpreterUI(false)
						.runInterpreter();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/** stores a reference to an LTM */
	private LocalTableManager localTableManager = null;
	/**
	 * <tt>true</tt> if <tt>parseCommand()</tt> finds error input current input
	 * line; <tt>false</tt> otherwise.
	 */
	private boolean error;
	/** the options parser tool */
	private CmdLineParser parser;
	/** tells if -c option is present input current input line */
	private Option tableCreatorOption;
	/** tells if -t option is present input current input line */
	private Option listTablesOption;
	/** the input is read from this */
	private BufferedReader input;
	/** current input line */
	private String currentInputLine;
	/** parsed commands */
	private String[] command;
	/** <tt>true</tt> after execution of command exit; <tt>false</tt> otherwise */
	private boolean exit;
	/** stores all known commands */
	private static final String[] allCommands = { "start", "exit", "list",
			"create" };

	public LocalTableManagerCommandInterpreterUI(boolean startLTM)
			throws RemoteException, UnknownHostException {
		parser = new CmdLineParser();
		tableCreatorOption = parser.addStringOption('c', "creator");
		listTablesOption = parser.addBooleanOption('t', "tables");
		input = new BufferedReader(new InputStreamReader(System.in));
		if (startLTM) {
			try {
				localTableManager = new LocalTableManager();
			} catch (NotBoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Runs the command interpreter
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

	/**
	 * Switch the right command
	 */
	private void execute() {
		if (command[0].equals("start")) {
			try {
				executeStart();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NotBoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (command[0].equals("exit")) {
			executeExit();
		} else if (localTableManager == null) {
			System.out.println("start LTM first!");
			System.out.println(USAGE);
		} else if (command[0].equals("create")) {
			try {
				executeCreate();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (command[0].equals("list")) {
			executeList();
		} else {
			throw new Error();
		}
	}

	/**
	 * Execute command list
	 */
	private void executeList() {
		boolean listTables = (parser.getOptionValue(listTablesOption) == null ? false
				: true);
		if (listTables) {
			// TODO
		}
	}

	/**
	 * Execute command create
	 * 
	 * @throws RemoteException
	 * 
	 */
	private void executeCreate() throws RemoteException {
		String creator = (String) parser.getOptionValue(tableCreatorOption);
		localTableManager.createTable(creator, null);
	}

	/**
	 * Execute command start
	 * 
	 * @throws RemoteException
	 * @throws UnknownHostException
	 * @throws NotBoundException
	 */
	private void executeStart() throws RemoteException, UnknownHostException,
			NotBoundException {
		if (localTableManager != null) {
			System.out.println("LTM already started!");
		} else {
			localTableManager = new LocalTableManager();
		}
	}

	/**
	 * Reads an input line, parses it and checks if input command is one of
	 * <tt>allCommands</tt>
	 */
	private void parseCommand() {
		try {
			currentInputLine = input.readLine();
			System.out.println(currentInputLine);
			System.out.flush();
			if (currentInputLine == null) {
				executeExit();
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
	 * Execute command exit
	 */
	private void executeExit() {
		if (localTableManager != null) {
			localTableManager.shutDown();
		}
		exit = true;
	}

}
