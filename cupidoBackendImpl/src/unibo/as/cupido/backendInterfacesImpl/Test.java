package unibo.as.cupido.backendInterfacesImpl;

import java.rmi.RemoteException;

import unibo.as.cupido.backendInterfaces.ServletNotificationsInterface;
import unibo.as.cupido.backendInterfaces.TableInterface;
import unibo.as.cupido.backendInterfaces.common.Pair;
import unibo.as.cupido.backendInterfaces.common.TableInfoForClient;
import unibo.as.cupido.backendInterfacesImpl.gtm.GlobalTableManager;
import unibo.as.cupido.backendInterfacesImpl.ltm.LocalTableManager;
import unibo.as.cupido.backendInterfacesImpl.table.DummyLoggerServletNotifyer;

public class Test {
	public static void main(String[] args) throws Exception {
		try {
			GlobalTableManager gtm = new GlobalTableManager();
			new LocalTableManager();
			ServletNotificationsInterface ownerSni = new DummyLoggerServletNotifyer(
					"Owner");
			TableInterface ti = gtm.createTable("Owner", ownerSni);
			ti.addBot("Owner", 1);
			ti.joinTable("Cane", new DummyLoggerServletNotifyer("Cane"));
			ti.joinTable("Gatto", new DummyLoggerServletNotifyer("Gatto"));
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
