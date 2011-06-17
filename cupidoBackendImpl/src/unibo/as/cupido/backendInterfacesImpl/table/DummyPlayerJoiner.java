package unibo.as.cupido.backendInterfacesImpl.table;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;

import unibo.as.cupido.backendInterfaces.GlobalTableManagerInterface;
import unibo.as.cupido.backendInterfaces.LocalTableManagerInterface;
import unibo.as.cupido.backendInterfaces.ServletNotificationsInterface;
import unibo.as.cupido.backendInterfaces.common.TableInfoForClient;
import unibo.as.cupido.backendInterfaces.exception.DuplicateUserNameException;
import unibo.as.cupido.backendInterfaces.exception.FullTableException;
import unibo.as.cupido.backendInterfaces.exception.NoSuchLTMException;
import unibo.as.cupido.backendInterfaces.exception.NoSuchTableException;
import unibo.as.cupido.backendInterfaces.exception.NoSuchUserException;
import unibo.as.cupido.backendInterfacesImpl.table.bot.AbstractBot;

public class DummyPlayerJoiner extends AbstractBot {

	private static final long serialVersionUID = 5260369104316800600L;

	public static void main(String[] args) throws RemoteException,
			IllegalArgumentException, IllegalStateException,
			NoSuchLTMException, NoSuchTableException, FullTableException,
			SQLException, NoSuchUserException {
		String[] names = { "Cane", "Gatto", "Topo" };
		// DuplicateUserNameException e = null;
		for (int i = 0; i < names.length; i++) {
			try {
				DummyPlayerJoiner dummyPlayerJoiner = new DummyPlayerJoiner(
						names[i]);
				dummyPlayerJoiner.joinATable();
				return;
			} catch (DuplicateUserNameException e1) {
				System.err.println("duplicate user name " + names[i]);
			}
		}
	}

	private GlobalTableManagerInterface gtm;

	public DummyPlayerJoiner(String userName) {
		super(userName);
		try {
			Registry registry = LocateRegistry.getRegistry();
			gtm = (GlobalTableManagerInterface) registry
					.lookup(GlobalTableManagerInterface.globalTableManagerName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void joinATable() throws RemoteException, NoSuchLTMException,
			NoSuchTableException, IllegalArgumentException,
			IllegalStateException, FullTableException,
			DuplicateUserNameException, SQLException, NoSuchUserException {

		TableInfoForClient tifc = gtm.getTableList().iterator().next();
		LocalTableManagerInterface ltmInterface = gtm
				.getLTMInterface(tifc.tableDescriptor.ltmId);
		singleTableManager = ltmInterface.getTable(tifc.tableDescriptor.id);
		initialTableStatus = singleTableManager.joinTable(userName,
				(ServletNotificationsInterface) UnicastRemoteObject
						.exportObject(this));
		System.out.println("DummiPlayerJoiner: " + userName + " "
				+ initialTableStatus);
		System.out.flush();
	}
}
