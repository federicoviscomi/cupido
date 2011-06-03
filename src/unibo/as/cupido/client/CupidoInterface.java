/**
 * 
 */
package unibo.as.cupido.client;

import java.io.Serializable;

import unibo.as.cupido.shared.FullTableException;
import unibo.as.cupido.shared.InitialTableStatus;
import unibo.as.cupido.shared.NoSuchTableException;
import unibo.as.cupido.shared.ObservedGameStatus;

import com.google.gwt.user.client.rpc.RemoteService;

/**
 * @author Lorenzo Belli
 *
 */
public interface CupidoInterface extends RemoteService {

	boolean login(String username, String password);

	boolean registerUser(String username, String password);

	boolean isUserRegistered(String username);

	void logout();

	/*
	 * Identify a single table
	 */
	public class TableData implements Serializable {

		private static final long serialVersionUID = 1L;
		String owner;
		int vacantSeats;
		String server;
		int tableId;
	}

	/*
	 * Return all the tables
	 */
	TableData[] getTableList();

	InitialTableStatus createTable();

	InitialTableStatus joinTable(String server, int tableId) throws FullTableException, NoSuchTableException;

	ObservedGameStatus viewTable(String server, int tableId) throws NoSuchTableException;
	
	void leaveTable();
}
