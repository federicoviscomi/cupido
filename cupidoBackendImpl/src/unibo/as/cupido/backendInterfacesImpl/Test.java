package unibo.as.cupido.backendInterfacesImpl;

import java.rmi.RemoteException;

import unibo.as.cupido.backendInterfaces.ServletNotificationsInterface;
import unibo.as.cupido.backendInterfaces.TableInterface;
import unibo.as.cupido.backendInterfaces.common.Pair;
import unibo.as.cupido.backendInterfaces.common.TableInfoForClient;
import unibo.as.cupido.backendInterfacesImpl.gtm.GlobalTableManager;
import unibo.as.cupido.backendInterfacesImpl.ltm.LocalTableManager;
import unibo.as.cupido.backendInterfacesImpl.table.DummyPlayerCreator;
import unibo.as.cupido.backendInterfacesImpl.table.DummyPlayerJoiner;
import unibo.as.cupido.backendInterfacesImpl.table.bot.DummyLoggerBotNotifyer;

public class Test {
	public static void main(String[] args) throws Exception {
		try {
			new GlobalTableManager();
			new LocalTableManager();
			new DummyPlayerCreator("Owner").start();
			
			
			new DummyPlayerJoiner("Cane").start();
			new DummyPlayerJoiner("Gatto").start();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
