package unibo.as.cupido.backendInterfacesImpl.ltm;

import jargs.gnu.CmdLineParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;

/**
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
 * <td>create -o owner</td>
 * <td>Create a new Table with specified owner</td>
 * </tr>
 * <tr>
 * <td>list</td>
 * <td>List all the tables managed by this LTM</td>
 * </tr>
 * </table>
 * 
 * 
 * @author cane
 * 
 */
public class LocalTableManagerCommandInterpreterUI {

	private static final String FORMAT = "%-7.7s %-3.3s %-10.10s %-20.20s\n";
	private static final String USAGE = String.format(FORMAT + FORMAT + FORMAT
			+ FORMAT + FORMAT, "COMMAND", "OPT", "LONG_OPT", "DESCRIPTION",
			"start", "", "", "start the LTM server", "exit", "", "",
			"shutdown the LTM server and exit", "list", "-t", "--table",
			"list all table managed by this LTM server", "create", "-o",
			"--owner", "create a new table with specified owner");

	public static void main(String[] args) {
		if (args.length > 1 && "start".equals(args[1])) {
			try {
				new LocalTableManagerCommandInterpreterUI(true).execute();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				new LocalTableManagerCommandInterpreterUI(false).execute();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * for simplicity this command line interpreter user interface can handle
	 * just one local table manager server
	 */
	private LocalTableManager localTableManager = null;

	public LocalTableManagerCommandInterpreterUI(boolean startLTM)
			throws RemoteException {
		if (startLTM) {
			localTableManager = new LocalTableManager();
		}
	}

	public void execute() {
		CmdLineParser parser = new CmdLineParser();
		CmdLineParser.Option globalServerAddress = parser.addStringOption('a',
				"adrress");
		CmdLineParser.Option tableOwnerOption = parser.addStringOption('o',
				"owner");
		CmdLineParser.Option listTablesOption = parser.addBooleanOption('o',
				"owner");
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String nextCommandLine;
		try {
			while (true) {
				System.out.print("\n#: ");
				nextCommandLine = in.readLine();
				if (nextCommandLine == null) {
					exit(0);
				} else {
					try {
						parser.parse(nextCommandLine.split("\\s+"));
						String[] command = parser.getRemainingArgs();
						if (command.length == 1) {
							if (command[0].equals("start")) {
								if (localTableManager != null) {
									System.out.println("LTM already started!");
								} else {
									try {
										localTableManager = new LocalTableManager();
									} catch (RemoteException e) {
										e.printStackTrace();
									}
								}
							} else {
								if (command[0].equals("exit")) {
									exit(0);
								} else if (localTableManager == null) {
									System.out.println("start LTM first!");
									System.out.println(USAGE);
								} else {
									if (command[0].equals("create")) {
										String owner = (String) parser
												.getOptionValue(tableOwnerOption);
										localTableManager.createTable(owner,
												null);
									} else if (command[0].equals("list")) {
										boolean listTables = (parser
												.getOptionValue(listTablesOption) == null ? false
												: true);
										if (listTables) {
											// TODO
										}
									} else {
										System.out.println(command[0]
												+ ": command not found");
										System.out.println(USAGE);
									}
								}
							}
						} else {
							System.out.println("Syntax error");
							System.out.println(USAGE);
						}
					} catch (CmdLineParser.OptionException e) {
						e.printStackTrace();
						exit(2);
					}
				}
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			exit(2);
		}
	}

	private void exit(int exitStatus) {
		if (localTableManager != null) {
			localTableManager.shutDown();
		}
		System.exit(exitStatus);
	}

}
