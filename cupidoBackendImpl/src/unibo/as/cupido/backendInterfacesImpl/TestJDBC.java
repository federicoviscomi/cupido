package unibo.as.cupido.backendInterfacesImpl;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import com.mysql.jdbc.Connection;

public class TestJDBC {
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) throws InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			//Connection connection = (Connection) DriverManager.getConnection("jdbs:mysql://:localhost:3306/cupido","root", "cupido");
			Connection connection = (Connection) DriverManager.getConnection("jdbs:mysql://localhost:mysql/cupido",
					"root", "cupido");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("JDBC driver loaded");
	}
}
