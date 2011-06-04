package unibo.as.cupido.backendInterfacesImpl;

import jargs.gnu.CmdLineParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Set;

import unibo.as.cupido.backendInterfaces.LocalTableManagerInterface;

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
 * <td>list</td>
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

	private TableManager globalTableManager = null;

	public void execute() {
		CmdLineParser parser = new CmdLineParser();

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
								globalTableManager = new TableManager();
							} else if (command[0].equals("exit")) {
								exit(0);
							} else if (command[0].equals("list")) {
								Set<InetAddress> allLocalServer = globalTableManager.getAllLocalServer();
								System.out.format("\n list af all local server follows:");
								for (InetAddress localServer : allLocalServer) {
									System.out.format("\n %25s", localServer.getCanonicalHostName());
								}
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
		if (globalTableManager != null) {
			globalTableManager.shutDown();
		}
		System.exit(exitStatus);
	}

}
