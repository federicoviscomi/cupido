package unibo.as.cupido.backendInterfacesImpl.gtm;

import jargs.gnu.CmdLineParser;
import jargs.gnu.CmdLineParser.Option;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import unibo.as.cupido.backendInterfaces.GlobalTableManagerInterface.Table;
import unibo.as.cupido.backendInterfaces.LocalTableManagerInterface;
import unibo.as.cupido.backendInterfaces.common.Pair;
import unibo.as.cupido.backendInterfacesImpl.table.LTMSwarm;
import unibo.as.cupido.backendInterfacesImpl.table.LTMSwarm.Triple;

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
 * 
 * @author cane
 * 
 */
public class GlobalTableManagerCommandInterpreterUI {

	public static void main(String[] args) {
		new GlobalTableManagerCommandInterpreterUI().execute();
	}

	private GlobalTableManager globalTableManager = null;

	public void execute() {
		CmdLineParser parser = new CmdLineParser();
		Option listLocalManagersOtion = parser.addBooleanOption('l',
				"localManagers");
		Option listTableOption = parser.addBooleanOption('t', "table");
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
								globalTableManager = new GlobalTableManager();
							} else if (command[0].equals("exit")) {
								exit(0);
							} else if (command[0].equals("list")) {
								boolean listLTM = (parser
										.getOptionValue(listLocalManagersOtion) == null ? false
										: true);
								boolean listTables = (parser
										.getOptionValue(listTableOption) == null ? false
										: true);
								if (listLTM) {
									Triple[] allLocalServer = globalTableManager
											.getAllLTM();
									System.out
											.format("\n list af all local server follows:");
									for (Triple localServer : allLocalServer) {
										System.out.format("\n %25s",
												localServer);
									}
								}
								if (listTables) {
									Collection<Pair<Table, LocalTableManagerInterface>> tableList = globalTableManager
											.getTableList();
									System.out
											.format("\n list af all tables follows:");
									for (Pair<Table, LocalTableManagerInterface> table : tableList) {
										System.out.format("\n %25s", table);
									}
								}
								if (!listLTM && !listTables) {
									System.out.println(" nothing to list?");
								}
							}
						} else {
							System.err.println("Syntax error");
						}
					} catch (CmdLineParser.OptionException e) {
						System.err.println(e.getLocalizedMessage());
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
		if (globalTableManager != null) {
			globalTableManager.shutDown();
		}
		System.exit(exitStatus);
	}

}
