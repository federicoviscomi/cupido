package unibo.as.cupido.backendInterfacesImpl;

import java.rmi.RemoteException;

import unibo.as.cupido.backendInterfaces.TableManagerInterface;

public class TableManager implements TableManagerInterface {

	public Table[] getTableList() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void notifyTableDestruction(TableDescriptor tableDescriptor)
			throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyTableJoin(TableDescriptor tableDescriptor)
			throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public TableDescriptor createTable(String owner,
			ServletNotifcationsInterface snf) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

}
