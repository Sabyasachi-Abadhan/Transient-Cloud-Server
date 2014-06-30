package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.hsqldb.Server;

public class DatabaseInitialization {

	public static void setupServer() {
		Server databaseServer = new Server();
		Connection connection = null;
		// Turning off the logging done by hsqldb for a bit
		databaseServer.setLogWriter(null);
		databaseServer.setSilent(true);
		databaseServer.setDatabaseName(0, "TransientCloudServerdb");
		databaseServer.setDatabasePath(0, "file: TransientCloudServerdb");
		databaseServer.start();
		try {
			Class.forName("org.hsqldb.jdbcDriver");
			connection = DriverManager.getConnection(
					"jdbc:hsqldb:hsql://localhost/TransientCloudServerdb",
					"sa", "");
			setupEventsTable(connection);

		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}

	}

	public static void setupEventsTable(Connection connection) {
		try {
			connection
					.prepareStatement(
							"create table events (name varchar(255), file_name VARCHAR(255), date DATE)")
					.execute();
			connection
					.prepareStatement("insert into events (name) values ('test_name')");
		} catch (SQLException e) {
			System.out.println("Failure to execute query, sorry");
			e.printStackTrace();
		}
	}
}
