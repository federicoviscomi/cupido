package unibo.as.cupido.backend.table;

import java.rmi.RemoteException;

public abstract class RemoteAction implements Action {

	abstract public void onExecute() throws RemoteException;
	
	@Override
	public void execute() {
		try {
			onExecute();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}
