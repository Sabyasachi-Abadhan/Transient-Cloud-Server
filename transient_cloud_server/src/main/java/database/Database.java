package database;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.hsqldb.Server;

public class Database {

	private Server databaseServer;
	private Connection connection = null;

	public Database() {
		databaseServer = new Server();
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
			setupTables();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}

	public Connection getConnection() {
		return connection;
	}

	public void setupTables() {
		Connection connection = getConnection();
		try {
			connection
					.prepareStatement(
							"create table events (name varchar(255), file_name varchar(255), file_path varchar(255), date DATE)")
					.execute();
			connection
					.prepareStatement(
							"create table files (name varchar(255), path varchar(255), identifier varchar(255), expiration_date DATE)")
					.execute();
		} catch (SQLException e) {
			System.out.println("Failure to execute query, sorry");
			e.printStackTrace();
		}
	}

	public void insertNewEvent(String name, String filePath, String fileName,
			Date date) throws SQLException {
		Connection connection = getConnection();
		PreparedStatement insertStatement = connection
				.prepareStatement("insert into events (name, file_name, file_path, date) values (?,?,?,?)");
		insertStatement.setString(1, name);
		insertStatement.setString(2, fileName);
		insertStatement.setString(3, filePath);
		try {
			insertStatement.setDate(4, date);
			insertStatement.execute();
		} catch (SQLException e) {
			System.out.println("Could not insert new event");
			e.printStackTrace();
		}
	}

	public void insertNewFile(String name, String path, String identifier,
			Date date) throws SQLException {
		Connection connection = getConnection();
		PreparedStatement insertStatement = connection
				.prepareStatement("insert into files (name, path, identifier, expiration_date) values (?,?,?,?)");
		insertStatement.setString(1, name);
		insertStatement.setString(2, path);
		insertStatement.setString(3, identifier);
		try {
			insertStatement.setDate(4, date);
			insertStatement.execute();
		} catch (SQLException e) {
			System.out.println("Could not insert new file");
			e.printStackTrace();
		}
	}

	public void deleteFile(Date date) {
		Connection connection = getConnection();
		PreparedStatement deleteStatement;
		try {
			deleteStatement = connection
					.prepareStatement("delete from files where expiration_date < (?)");
			deleteStatement.setDate(1, date);
			deleteStatement.execute();
		} catch (SQLException e) {
			System.out.println("Couldn't delete file, sorry");
		}

	}
}
