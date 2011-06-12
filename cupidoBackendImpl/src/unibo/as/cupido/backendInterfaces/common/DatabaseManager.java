package unibo.as.cupido.backendInterfaces.common;

import java.sql.DriverManager;
import java.sql.SQLException;

import unibo.as.cupido.backendInterfaces.DatabaseInterface;

import com.mysql.jdbc.*;

public class DatabaseManager implements DatabaseInterface {

	public static void main(String[] args) {
		new DatabaseManager();
	}

	private String userDB = "root";
	private String passDB = "cupido";

	private String host = "localhost";

	public DatabaseManager() {
		try {
			// Class.forName("com.mysql.jdbc.Driver");
			System.out.println("Initializing Server... ");
			Class.forName("org.gjt.mm.mysql.Driver");
			System.out.println(" Driver Found.");
			String url = "jdbc:mysql://" + host + "/" + database;
			Connection connection = (Connection) DriverManager.getConnection(
					url, userDB, passDB);
			System.out.println(" Database connection established to " + url
					+ ".");
			Statement statement = (Statement) connection.createStatement();
			boolean execute = statement.execute("SELECT * FROM User");
			System.out.println(execute);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void addNewUser(String userName, String password) {
		// TODO Auto-generated method stub

	}

	@Override
	public Rank getGlobalRank() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getPlayerRank(String player) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getPlayerScore(String player) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean login(String userName, String password) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void updateScore(String userName, int score) {
		// TODO Auto-generated method stub

	}

}
