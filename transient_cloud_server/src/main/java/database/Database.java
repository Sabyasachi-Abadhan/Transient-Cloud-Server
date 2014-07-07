package database;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

	private void deleteFiles(Date date) {
		Connection connection = getConnection();
		PreparedStatement deleteStatement;
		try {
			deleteStatement = connection
					.prepareStatement("delete from files where expiration_date <= (?)");
			deleteStatement.setDate(1, date);
			deleteStatement.execute();
		} catch (SQLException e) {
			System.out.println("Couldn't delete the file, sorry");
		}
	}

	public void deleteHandler(Date date) {
		// Select the next expiration_date and then the thread expires,
		// recalculate for all files that expire and then sleep until the next
		// expiration date
		Connection connection = getConnection();
		PreparedStatement filesAboutToExpireQuery;
		try {
			// First get the files about to expire
			filesAboutToExpireQuery = connection
					.prepareStatement("select * from files where expiration_date <= (?)");
			filesAboutToExpireQuery.setDate(1, date);
			System.out.println(filesAboutToExpireQuery);
			ResultSet results = filesAboutToExpireQuery.executeQuery();
			// Calculate new expiration dates if applicable
			while (results.next()) {
				System.out.println("Calling setNewExpirationDate");
				setNewExpirationDate(results.getString("path"),
						results.getDate("expiration_date"));
			}
			// Delete the files which will expire anyway
			deleteFiles(date);
		} catch (SQLException e) {
			System.out.println("Couldn't execute deleteHandler, sorry");
			e.printStackTrace();
		}
	}

	private void setNewExpirationDate(String filePath, Date expirationDate) {
		// This kind of filtering doesn't work if the file has been
		// moved/renamed.
		// File hash is also not a good solution because file contents will
		// change
		String searchTerm = filePath.substring(filePath.lastIndexOf("Dropbox"));
		Connection connection = getConnection();
		try {
			PreparedStatement openEvents = connection
					.prepareStatement("select * from events where name = 'open' and file_name like ?");
			openEvents.setString(1, "%" + searchTerm + "%");
			System.out.println(openEvents);
			ResultSet results = openEvents.executeQuery();
			System.out.println("Calling calculateExpirationDate");
			Date newExpirationDate = calculateExpirationDate(results,
					expirationDate);
			PreparedStatement setNewDateStatement = connection
					.prepareStatement("update files set expiration_date = ? where path = ?");
			setNewDateStatement.setDate(1, newExpirationDate);
			setNewDateStatement.setString(2, filePath);
		} catch (SQLException e) {
			System.out.println("Couldn't set new expiration date");
			e.printStackTrace();
		}
	}

	private Date calculateExpirationDate(ResultSet results, Date expirationDate) {
		/*
		 * Algorithm Get Maximum number of millseconds elapsed (first entry) ->
		 * d For all the other entries of timestamp x, get (d - x)
		 */
		try {
			double totalMilliSeconds = 0.0;
			long maxMilliSeconds = 0;
			while (results.next()) {
				long currentMilliSeconds = results.getDate("date").getTime();
				if (currentMilliSeconds > maxMilliSeconds)
					maxMilliSeconds = currentMilliSeconds;
				totalMilliSeconds += currentMilliSeconds;
			}
			Date newExpirationDate = new Date(
					(long) (expirationDate.getTime() * Math
							.ceil(totalMilliSeconds * 1.0 / maxMilliSeconds)));
			System.out.println("New expiration date: "
					+ newExpirationDate.toString());
			return newExpirationDate;
		} catch (SQLException e) {
			System.out.println("Failed calculation");
			e.printStackTrace();
			return expirationDate;
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
