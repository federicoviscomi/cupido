package unibo.as.cupido.backend.table;

import java.rmi.RemoteException;

/**
 * This is a helper class that can be used instead of the Action
 * interface, when the action involves RMI calls.
 * 
 * Using this class, no try-catch block is needed around the RMI call.
 */
public abstract class RemoteAction implements Action {

	@Override
	public void execute() {
		try {
			onExecute();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Derived classes must implement this instead of execute(),
	 * thus simplifying the code.
	 * 
	 * @throws RemoteException
	 *      If there is an error during communications.
	 */
	abstract public void onExecute() throws RemoteException;
}
