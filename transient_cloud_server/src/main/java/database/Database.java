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
							"create table if not exists events (name varchar(255), file_name varchar(255), file_path varchar(255), date DATE)")
					.execute();
			connection
					.prepareStatement(
							"create table if not exists files (name varchar(255), path varchar(255), identifier varchar(255), hash varchar(255), expiration_date DATE)")
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
			String hash, Date date) throws SQLException {
		Connection connection = getConnection();
		PreparedStatement insertStatement = connection
				.prepareStatement("insert into files (name, path, identifier, hash, expiration_date) values (?,?,?,?,?)");
		insertStatement.setString(1, name);
		insertStatement.setString(2, path);
		insertStatement.setString(3, identifier);
		insertStatement.setString(4, hash);
		try {
			insertStatement.setDate(5, date);
			insertStatement.execute();
		} catch (SQLException e) {
			System.out.println("Could not insert new file");
			e.printStackTrace();
		}
	}

	public void deleteFile(Date date) {
		// Select the next expiration_date and then the thread expires,
		// recalculate for all files that expire and then sleep until the next
		// expiration date
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

	public void updateFile(String columnName, String oldValue, String newValue) {
		Connection connection = getConnection();
		PreparedStatement replaceStatement;
		try {
			replaceStatement = connection.prepareStatement("update files set "
					+ columnName + " =? where " + columnName + " = ? ");
			replaceStatement.setString(1, newValue);
			replaceStatement.setString(2, oldValue);
			System.out.println(replaceStatement);
			replaceStatement.executeUpdate();
		} catch (SQLException e) {
			System.out.println("Couldn't update entry, sorry");
			e.printStackTrace();
		}
	}
}
