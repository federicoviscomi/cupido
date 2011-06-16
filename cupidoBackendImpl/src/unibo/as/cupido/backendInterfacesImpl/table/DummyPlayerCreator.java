package unibo.as.cupido.backendInterfacesImpl.table;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteStub;
import java.rmi.server.UnicastRemoteObject;
import unibo.as.cupido.backendInterfaces.GlobalTableManagerInterface;
import unibo.as.cupido.backendInterfaces.ServletNotificationsInterface;
import unibo.as.cupido.backendInterfaces.common.InitialTableStatus;
import unibo.as.cupido.backendInterfaces.exception.FullTableException;
import unibo.as.cupido.backendInterfaces.exception.NotCreatorException;
import unibo.as.cupido.backendInterfaces.exception.PositionFullException;
import unibo.as.cupido.backendInterfacesImpl.table.bot.AbstractBot;
import unibo.as.cupido.backendInterfacesImpl.table.bot.Bot;

public class DummyPlayerCreator extends AbstractBot implements Serializable,
		Bot, ServletNotificationsInterface {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8439829734552972246L;

	public static void main(String[] args) throws Exception {

		Bot bot = (Bot) UnicastRemoteObject
				.exportObject(new DummyPlayerCreator("Owner"));

		bot.createTable();
		// 1 refers to absolute position
		bot.addBot(1);
		// Thread.sleep(1000);
		bot.addBot(2);
		// Thread.sleep(1000);
		bot.addBot(3);
	}

	private GlobalTableManagerInterface gtm;

	public DummyPlayerCreator(String userName) {

		super(userName);
		initialTableStatus = new InitialTableStatus();
		initialTableStatus.opponents = new String[3];
		initialTableStatus.playerScores = new int[3];
		initialTableStatus.whoIsBot = new boolean[3];

		try {
			Registry registry = LocateRegistry.getRegistry();
			gtm = (GlobalTableManagerInterface) registry
					.lookup(GlobalTableManagerInterface.globalTableManagerName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public synchronized void addBot(int position) {
		try {
			singleTableManager.addBot(userName, position);
			position--;
			initialTableStatus.opponents[position] = "_bot." + userName + "."
					+ position;
			initialTableStatus.playerScores[position] = 0;
			initialTableStatus.whoIsBot[position] = true;
			System.out.println(" " + userName + ", " + initialTableStatus);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void createTable() {
		try {
			singleTableManager = gtm.createTable(userName, this);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
