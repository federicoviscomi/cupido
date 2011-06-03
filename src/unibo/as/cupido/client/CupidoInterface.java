/**
 * 
 */
package unibo.as.cupido.client;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.RemoteService;

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
	
	/*
	 * Returns the interface to manage the table
	 */
	void createTable();
	
	void joinTable(String server, int tableId);
	
	void viewTable(String server, int tableId);
}
