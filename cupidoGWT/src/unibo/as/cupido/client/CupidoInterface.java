/**
 * 
 */
package unibo.as.cupido.client;

import java.io.Serializable;

import unibo.as.cupido.backendInterfaces.common.FullTableException;
import unibo.as.cupido.backendInterfaces.common.InitialTableStatus;
import unibo.as.cupido.backendInterfaces.common.NoSuchTableException;
import unibo.as.cupido.backendInterfaces.common.ObservedGameStatus;

import com.google.gwt.user.client.rpc.RemoteService;

/**
 * @author Lorenzo Belli
 * 
 */
public interface CupidoInterface extends RemoteService {

	public boolean login(String username, String password);

	/**
	 * 
	 * @param username
	 * 
	 * @param password
	 * @return
	 */
	public boolean registerUser(String username, String password);

	public boolean isUserRegistered(String username);

	public void logout();

	/*
	 * Identify a single table
	 */
	public class TableData implements Serializable {

		private static final long serialVersionUID = 1L;
		public String owner;
		public int vacantSeats;
		public String server;
		public int tableId;
	}

	/*
	 * Return all the tables
	 */
	public TableData[] getTableList();

	public InitialTableStatus createTable();

	public InitialTableStatus joinTable(String server, int tableId)
			throws FullTableException, NoSuchTableException;

	public ObservedGameStatus viewTable(String server, int tableId)
			throws NoSuchTableException;

}
