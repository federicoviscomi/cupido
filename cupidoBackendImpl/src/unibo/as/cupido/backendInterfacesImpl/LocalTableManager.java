package unibo.as.cupido.backendInterfacesImpl;

import unibo.as.cupido.backendInterfaces.LocalTableManagerInterface;
import unibo.as.cupido.backendInterfaces.TableManagerInterface.ServletNotifcationsInterface;
import unibo.as.cupido.backendInterfaces.TableManagerInterface.Table;

public class LocalTableManager implements LocalTableManagerInterface {

	public void notifyTableDestruction(int tableId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int createTable(String owner, ServletNotifcationsInterface snf) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Table getTable(int tableId) {
		// TODO Auto-generated method stub
		return null;
	}

}
