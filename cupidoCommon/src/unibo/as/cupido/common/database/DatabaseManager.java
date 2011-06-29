/*  Cupido - An online Hearts game.
 *  Copyright (C) 2011 Lorenzo Belli, Marco Poletti, Federico Viscomi
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package unibo.as.cupido.common.database;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

import unibo.as.cupido.common.structures.RankingEntry;
import unibo.as.cupido.common.exception.DuplicateUserNameException;
import unibo.as.cupido.common.exception.NoSuchUserException;
import unibo.as.cupido.common.interfaces.DatabaseInterface;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

public class DatabaseManager implements DatabaseInterface {

	public static void main(String[] args) throws SQLException,
			DuplicateUserNameException, NoSuchUserException {
		DatabaseManager databaseManager = new DatabaseManager();
		databaseManager.test();
		// System.out.println(databaseManager.login("cane", "bau"));
	}

	private String userDB = "root";

	private String passDB = "cupido";
	private String host = "localhost";
	private Statement statement;
	private Connection connection;
	private final int NUMLOCALRANKENTRIES = DatabaseInterface.NUMLOCALRANKENTRIES;
	
	public DatabaseManager() {
		try {
			Class.forName("org.gjt.mm.mysql.Driver");
			String url = "jdbc:mysql://" + host + "/" + database;
			connection = (Connection) DriverManager.getConnection(url, userDB,
					passDB);
			System.out.println(" Database connection established to " + url
					+ ".");
			statement = (Statement) connection.createStatement();
		} catch (ClassNotFoundException e) {
			System.out.println("DBManager: on DatabaseManager() catched ClassNotFoundException");
			e.printStackTrace();
		} catch (SQLException e) {
			System.out.println("DBManager: on DatabaseManager() catched SQLException");
			e.printStackTrace();
		}

	}

	@Override
	public void addNewUser(String userName, String password)
			throws SQLException, DuplicateUserNameException {
		if (userName == null || password == null)
			throw new IllegalArgumentException();
		if (this.contains(userName))
			throw new DuplicateUserNameException(userName);
		statement.executeUpdate("INSERT INTO User VALUE ('" + userName + "', '"
				+ password + "', 0);");
	}

	@Override
	public void close() {
		try {
			statement.close();
		} catch (SQLException e) {
			System.out.println("DBManager: on close() catched SQLException");
			e.printStackTrace();
		}
		try {
			connection.close();
		} catch (SQLException e) {
			System.out.println("DBManager: on close() catched SQLException");
			e.printStackTrace();
		}
	}

	@Override
	public boolean contains(String userName) throws SQLException {
		return statement.executeQuery(
				"SELECT name FROM User WHERE name = '" + userName + "'").next();
	}

	/*
	 * Query is:
	 * select * from (SELECT @rank:=@rank+1 AS rank, name, score from
	 * User USE INDEX (scoreIndex) ORDER BY score DESC)AS globalList limit 2,9
	 * 
	 * @see
	 * unibo.as.cupido.common.interfaces.DatabaseInterface#getLocalRank(java
	 * .lang.String)
	 */
	@Override
	public ArrayList<RankingEntry> getLocalRank(String userName)
			throws SQLException, NoSuchUserException {
		if (userName == null)
			throw new IllegalArgumentException();
		int userRank = getUserRank(userName).rank;
		int from = (userRank - NUMLOCALRANKENTRIES/2);
		from = (from >= 0 ? from : 0);
		statement.executeUpdate("SET @rank=0;");
		ResultSet chunk = statement
				.executeQuery("SELECT * FROM " +
						"(SELECT @rank:=@rank+1 AS rank, name, score from User USE INDEX (scoreIndex) ORDER BY score DESC)" +
						"AS globalList LIMIT "+ from + ", " + NUMLOCALRANKENTRIES + " ;");

		ArrayList<RankingEntry> rank = new ArrayList<RankingEntry>(10);
		while (chunk.next()) {
			rank.add(new RankingEntry(chunk.getString(2), chunk.getInt(1),
					chunk.getInt(3)));
		}
		return rank;
	}

	@Override
	public int getPlayerScore(String userName) throws NoSuchUserException {
		if (userName == null)
			throw new IllegalArgumentException();
		try {
			ResultSet res = statement
			.executeQuery("SELECT score FROM User WHERE name = '"
					+ userName + "' LIMIT 1;");
			if (res.next())
				return res.getInt(1);
			throw new NoSuchUserException();
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}


	@Override
	public ArrayList<RankingEntry> getTopRank(int size) {
		if (size <= 0)
			throw new IllegalArgumentException();
		try {
			statement.executeUpdate("SET @rank=0;");
			ResultSet topChunk = statement
					.executeQuery("SELECT @rank:=@rank+1 AS rank, name, score  "
							+ " FROM User "
							+ " USE INDEX (scoreIndex) ORDER BY score DESC LIMIT "
							+ size + " ;");
			ArrayList<RankingEntry> rank = new ArrayList<RankingEntry>(size);
			while (topChunk.next()) {
				rank.add(new RankingEntry(topChunk.getString(2),
						topChunk.getInt(1), topChunk.getInt(3)));
			}
			return rank;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	
	/*
	 * Query is:
	 * SELECT * FROM (SELECT @rank:=@rank+1 AS rank, name, score FROM
	 * User USE INDEX (scoreIndex) ORDER BY score DESC) AS ranking WHERE
	 * name='echo';
	 */
	@Override
	public RankingEntry getUserRank(String userName) throws NoSuchUserException {
		if (userName == null)
			throw new IllegalArgumentException();

		try {
			if (!this.contains(userName))
				throw new NoSuchUserException(userName);
			statement.executeUpdate("SET @rank=0;");
			ResultSet chunk = statement
					.executeQuery("SELECT * FROM "
							+ "(SELECT @rank:=@rank+1 AS rank, name, score FROM User USE INDEX (scoreIndex) ORDER BY score DESC)"
							+ " AS ranking  where name='" + userName + "';");
			chunk.next();
			return new RankingEntry(userName, chunk.getInt(1), chunk.getInt(3));
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean login(String userName, String password) throws NoSuchUserException {
		if (userName == null || password == null)
			throw new IllegalArgumentException();
		try {
			if (!this.contains(userName))
				throw new NoSuchUserException(userName);
			ResultSet res = statement
					.executeQuery("SELECT * FROM User WHERE name = '" + userName
							+ "' AND password ='" + password + "' LIMIT 1;");
			return res.next();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	

	private void print() {
		try {
			ResultSet all = statement.executeQuery("SELECT * FROM User");
			ResultSetMetaData metaData = all.getMetaData();
			System.out.format("\n%16.16s %8.8s %8.8s\n",
					metaData.getColumnName(1), metaData.getColumnName(2),
					metaData.getColumnName(3));
			while (all.next()) {
				System.out.format("%16.16s %8.8s %8.8s\n", all.getString(1),
						all.getString(2), all.getInt(3));
			}

		} catch (SQLException e) {
			System.out.println("DBManager: on print() catched SQLException");
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
			addNewUser("rosa", "rosa");
		} catch (DuplicateUserNameException e) {
			//
		}
		/*ResultSet res = statement.executeQuery("SELECT * FROM User;");
		System.out.println();
		while (res.next()) {
			System.out.println(res.getString(1) + " " + res.getString(2) + " "
					+ res.getInt(3));
		}
		this.updateScore("rosa", 34);
		this.updateScore("cane", 34);
		System.out.println("Update");
		ArrayList<RankingEntry> globalRank = this.getLocalRank("cane");
		for (RankingEntry p : globalRank) {
			System.out.println(":" + p.username+" "+p.rank+" "+p.points);
		}
		*/
		for (RankingEntry p : this.getLocalRank("foxtrot")){
			System.out.println(p.rank+" "+p.username+" "+p.points);
		}
		System.out.println("---");
		for (RankingEntry p : this.getLocalRank("golf")){
			System.out.println(p.rank+" "+p.username+" "+p.points);
		}
		System.out.println("---");
		for (RankingEntry p : this.getTopRank(13)){
			System.out.println(p.rank+" "+p.username+" "+p.points);
		}
	}

	@Override
	public void updateScore(String userName, int score) throws NoSuchUserException {
		if (userName == null)
			throw new IllegalArgumentException();
		try {
			if (!this.contains(userName))
				throw new NoSuchUserException(userName);
			statement.executeUpdate("UPDATE User SET score = " + score
					+ " WHERE name = '" + userName + "';");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
