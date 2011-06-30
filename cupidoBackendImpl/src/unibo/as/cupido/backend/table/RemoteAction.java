package unibo.as.cupido.backend.table;

import java.rmi.RemoteException;

public abstract class RemoteAction implements Action {

	@Override
	public void execute() {
		try {
			onExecute();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	abstract public void onExecute() throws RemoteException;
}
