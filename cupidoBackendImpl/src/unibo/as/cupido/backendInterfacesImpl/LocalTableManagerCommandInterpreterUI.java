package unibo.as.cupido.backendInterfacesImpl;

import jargs.gnu.CmdLineParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
 * <td>start [-a global server address]</td>
 * <td>Start the server whit an optiona global server address</td>
 * </tr>
 * </table>
 * 
 * 
 * @author cane
 * 
 */
public class LocalTableManagerCommandInterpreterUI {

	public static void main(String[] args) {
		new LocalTableManagerCommandInterpreterUI().execute();
	}

	/**
	 * for simplicity this command line interpreter user interface can handle
	 * just one local table manager server
	 */
	private LocalTableManager localTableManager = null;

	public void execute() {
		CmdLineParser parser = new CmdLineParser();
		CmdLineParser.Option globalServerAddress = parser.addStringOption('a', "adrress");
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
								localTableManager = new LocalTableManager(
										(String) parser.getOptionValue(globalServerAddress));

							} else if (command[0].equals("exit")) {
								exit(0);
							}
						} else {
							System.out.println("Syntax error");
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
