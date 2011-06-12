package unibo.as.cupido.backendInterfaces.common;

import java.rmi.RemoteException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import unibo.as.cupido.backendInterfaces.DatabaseInterface;
import unibo.as.cupido.backendInterfaces.exception.DuplicateUserNameException;
import unibo.as.cupido.backendInterfaces.exception.NoSuchUserException;

import com.mysql.jdbc.*;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

public class DatabaseManager implements DatabaseInterface {

	public static void main(String[] args) throws SQLException,
			DuplicateUserNameException, NoSuchUserException {
		new DatabaseManager().test();
	}

	private String userDB = "root";
	private String passDB = "cupido";
	private String host = "localhost";
	// TODO handle statement.close() problem
	private Statement statement;

	public DatabaseManager() {
		try {
			System.out.println("Initializing Server... ");
			Class.forName("org.gjt.mm.mysql.Driver");
			System.out.println(" Driver Found.");
			String url = "jdbc:mysql://" + host + "/" + database;
			Connection connection = (Connection) DriverManager.getConnection(
					url, userDB, passDB);
			System.out.println(" Database connection established to " + url
					+ ".");
			statement = (Statement) connection.createStatement();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void test() throws SQLException, NoSuchUserException {
		try {
			addNewUser("ciao", "bau");
		} catch (DuplicateUserNameException e) {
			//
		}
		try {
			addNewUser("cane", "miao");
		} catch (DuplicateUserNameException e) {
			//
		}
		try {
			addNewUser("rosa", "rosae");
		} catch (DuplicateUserNameException e) {
			//
		}
		ResultSet res = statement.executeQuery("SELECT * FROM User");
		System.out.println();
		while (res.next()) {
			System.out.println(res.getString(1) + " " + res.getString(2) + " "
					+ res.getInt(3));
		}
		this.updateScore("rosa", 34);
		this.updateScore("cane", 34);
		ArrayList<Pair<String, Integer>> globalRank = this.getLocalRank("cane");
		for (Pair<String, Integer> p : globalRank) {
			System.out.println(":" + p);
		}
	}

	private boolean contains(String userName) throws SQLException {
		return statement.executeQuery(
				"SELECT name FROM User WHERE name = '" + userName + "'").next();
	}

	@Override
	public void addNewUser(String userName, String password)
			throws SQLException, DuplicateUserNameException {
		if (userName == null || password == null)
			throw new IllegalArgumentException();
		if (this.contains(userName))
			throw new DuplicateUserNameException();
		// TODO is there any way to exploit return value of
		// statement.executeUpdate in order not to use this.contains?
		statement.executeUpdate("INSERT INTO User VALUE ('" + userName + "', '"
				+ password + "', 0);");
	}

	@Override
	public ArrayList<Pair<String, Integer>> getLocalRank(String userName)
			throws SQLException, NoSuchUserException {
		if (userName == null)
			throw new IllegalArgumentException();

		// FIXME don't know if this works

		int userRank = getUserRank(userName);

		statement.executeUpdate("SET @rank=0;");
		ResultSet chunk = statement
				.executeQuery("SELECT @rank:=@rank+1 AS rank, name, score  "
						+ " FROM User "
						+ " USE INDEX (scoreIndex) WHERE @rank >= "
						+ (userRank - 5) + " AND @rank <= " + (userRank + 4)
						+ " ORDER BY score DESC LIMIT 10;");
		ArrayList<Pair<String, Integer>> rank = new ArrayList<Pair<String, Integer>>(
				10);
		while (chunk.next()) {
			rank.add(new Pair<String, Integer>(chunk.getString(2), chunk
					.getInt(3)));
		}
		return rank;
	}

	@Override
	public int getUserRank(String userName) throws SQLException,
			NoSuchUserException {
		if (userName == null)
			throw new IllegalArgumentException();

		// FIXME does this really works?

		if (!this.contains(userName))
			throw new NoSuchUserException(userName);
		statement.executeUpdate("SET @rank=0;");
		ResultSet chunk = statement
				.executeQuery("SELECT @rank:=@rank+1 AS rank FROM User USE INDEX (scoreIndex) WHERE name = '"
						+ userName + "' ORDER BY score DESC LIMIT 1;");
		chunk.next();
		// FIXME doesn't work with getInt(1). could that be a problem?
		return (int) chunk.getLong(1);
	}

	@Override
	public int getPlayerScore(String userName) throws SQLException,
			NoSuchUserException {
		if (userName == null)
			throw new IllegalArgumentException();
		if (!this.contains(userName))
			throw new NoSuchUserException(userName);
		ResultSet res = statement
				.executeQuery("SELECT score FROM User WHERE name = '"
						+ userName + "' LIMIT 1;");
		if (res.next())
			return res.getInt(1);
		// TODO
		return 0;
	}

	@Override
	public boolean login(String userName, String password) throws SQLException,
			NoSuchUserException {
		if (userName == null || password == null)
			throw new IllegalArgumentException();
		if (!this.contains(userName))
			throw new NoSuchUserException(userName);
		ResultSet res = statement
				.executeQuery("SELECT * FROM User WHERE name = '" + userName
						+ "', password ='" + password + "' LIMIT 1;");
		return res.next();
	}

	@Override
	public void updateScore(String userName, int score) throws SQLException,
			NoSuchUserException {
		if (userName == null)
			throw new IllegalArgumentException();
		if (!this.contains(userName))
			throw new NoSuchUserException(userName);
		statement.executeUpdate("UPDATE User SET score = " + score
				+ " WHERE name = '" + userName + "';");
	}

	@Override
	public ArrayList<Pair<String, Integer>> getTopRank(int size)
			throws RemoteException, SQLException {
		if (size <= 0)
			throw new IllegalArgumentException();
		// FIXME does this really work?
		statement.executeUpdate("SET @rank=0;");
		ResultSet topChunk = statement
				.executeQuery("SELECT @rank:=@rank+1 AS rank, name, score  "
						+ " FROM User "
						+ " USE INDEX (scoreIndex) ORDER BY score DESC LIMIT "
						+ size + " ;");
		ArrayList<Pair<String, Integer>> rank = new ArrayList<Pair<String, Integer>>(
				size);
		while (topChunk.next()) {
			System.out.println(topChunk.getLong(1) + " "
					+ topChunk.getString(2) + " " + topChunk.getLong(3));
			rank.add(new Pair<String, Integer>(topChunk.getString(2), topChunk
					.getInt(3)));
		}
		return rank;
	}
}
