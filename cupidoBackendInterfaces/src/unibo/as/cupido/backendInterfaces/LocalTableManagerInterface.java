package unibo.as.cupido.backendInterfaces;

import java.rmi.Remote;

import unibo.as.cupido.backendInterfaces.TableManagerInterface.ServletNotifcationsInterface;
import unibo.as.cupido.backendInterfaces.TableManagerInterface.Table;

public interface LocalTableManagerInterface extends Remote {

	/**
	 * 
	 * @param owner
	 * @return the table id
	 */
	public int createTable(String owner, ServletNotifcationsInterface snf);

	/**
	 * 
	 * Returns the description of the table with id tableId
	 * 
	 * @param tableId
	 * 
	 * @return
	 */
	public Table getTable(int tableId);
}
