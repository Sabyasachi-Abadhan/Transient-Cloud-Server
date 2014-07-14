package database;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hsqldb.Server;

import utilities.Settings;

import com.dropbox.core.DbxException;

import dropbox.DropboxManager;

/**
 * Class abstracts away all create/update operations and calculations involving
 * the database
 * 
 * @author ROHIT
 *
 */
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

	/**
	 * Getter method to facilitate the re - use of Connection to database
	 * 
	 * @return
	 */
	public Connection getConnection() {
		return connection;
	}

	/**
	 * Sets up the schema of the database if it hasn't already been created
	 */
	public void setupTables() {
		Connection connection = getConnection();
		try {
			connection
					.prepareStatement(
							"create table if not exists events (name varchar(255), file_name varchar(255), file_path varchar(255), date DATE)")
					.execute();
			connection
					.prepareStatement(
							"create table if not exists files (id identity, name varchar(255), path varchar(255), identifier varchar(255), size varchar(255), expiration_date DATE)")
					.execute();
		} catch (SQLException e) {
			System.out.println("Failure to execute query, sorry");
			e.printStackTrace();
		}
	}

	/**
	 * Inserts a new modify/open event into the events table
	 * 
	 * @param name
	 * @param filePath
	 * @param fileName
	 * @param date
	 * @throws SQLException
	 */
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

	/**
	 * Inserts a new file into the files table
	 * 
	 * @param name
	 * @param path
	 * @param identifier
	 * @param size
	 * @param date
	 * @throws SQLException
	 */
	public void insertNewFile(String name, String path, String identifier,
			String size, Date date) throws SQLException {
		Connection connection = getConnection();
		if (!fileExists(path)) {
			PreparedStatement insertStatement = connection
					.prepareStatement("insert into files (id, name, path, identifier, size, expiration_date) values (null, ?,?,?,?,?)");
			insertStatement.setString(1, name);
			insertStatement.setString(2, path);
			insertStatement.setString(3, identifier);
			insertStatement.setString(4, size);
			try {
				insertStatement.setDate(5, date);
				insertStatement.execute();
			} catch (SQLException e) {
				System.out.println("Could not insert new file");
				e.printStackTrace();
			}
		} else {
			System.out.println("File already existed so just updating");
			PreparedStatement insertStatement = connection
					.prepareStatement("update files set name = (?), identifier = (?), size = (?), expiration_date = (?) where path = (?)");
			insertStatement.setString(1, name);
			insertStatement.setString(2, identifier);
			insertStatement.setString(3, size);
			try {
				insertStatement.setDate(4, date);
				insertStatement.setString(5, path);
				insertStatement.execute();
			} catch (SQLException e) {
				System.out.println("Could not insert new file");
				e.printStackTrace();
			}
		}
	}

	private boolean fileExists(String path) {
		Connection connection = getConnection();
		int rowCount = 0;
		try {
			PreparedStatement search = connection
					.prepareStatement("select * from files where path = (?)");
			search.setString(1, path);
			ResultSet results = search.executeQuery();
			while (results.next())
				++rowCount;
			return (rowCount > 0);
		} catch (SQLException e) {
			return false;
		}

	}

	/**
	 * Deletes the least recently used file. The file with the oldest expiration
	 * date is the one that is least recently used We trigger a deleteHandler on
	 * purpose here so that if there is a chance to clean up, then we can
	 */
	public void deleteLeastRecentlyUsedFile() {
		Connection connection = getConnection();
		try {
			PreparedStatement getMinimumDate = connection
					.prepareStatement("select * from files where expiration_date = (select min(expiration_date) from files)");
			ResultSet files = getMinimumDate.executeQuery();
			String path = "", transientDirectoryName = "";
			while (files.next()) {
				path = files.getString("path");
				transientDirectoryName = files.getString("identifier");
			}
			PreparedStatement delete = connection
					.prepareStatement("delete from files where path=(?)");
			delete.setString(1, path);
			delete.execute();
			DropboxManager.deleteFile(path.substring(
					path.lastIndexOf(transientDirectoryName))
					.replace("\\", "/"));
		} catch (SQLException | DbxException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method is called whenever a move/rename operation is encountered. It
	 * replaces the oldValue of the column = columnName with newValue
	 * 
	 * @param columnName
	 * @param oldValue
	 * @param newValue
	 */
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

	/**
	 * deleteHandler is called every time there is a new open/modify event
	 * received by the server. It recalculates the expiration_date on files
	 * about to expire
	 * 
	 * @param date
	 */
	public void deleteHandler(Date date) {
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

	private void deleteFiles(Date date) {
		try {
			int numberOfFilesDeleted = deleteFilesFromDropbox(date);
			deleteFilesFromDatabase(date);
			if (numberOfFilesDeleted > 0)
				deleteOpenEventsFromDatabase(date);
			else
				System.out.println("Didn't delete from the events table");
		} catch (SQLException | DbxException e) {
			System.out.println("Couldn't delete the file, sorry");
			e.printStackTrace();
		}
	}

	private void deleteOpenEventsFromDatabase(Date date) throws SQLException {
		Connection connection = getConnection();
		PreparedStatement deleteOpenEventsStatement = connection
				.prepareStatement("delete from events where name = 'open' and date <= (?)");
		deleteOpenEventsStatement.setDate(1, date);
		deleteOpenEventsStatement.execute();
	}

	private void deleteFilesFromDatabase(Date date) throws SQLException {
		Connection connection = getConnection();
		PreparedStatement deleteStatement;
		deleteStatement = connection
				.prepareStatement("delete from files where expiration_date <= (?)");
		deleteStatement.setDate(1, date);
		deleteStatement.execute();
	}

	private int deleteFilesFromDropbox(Date date) throws SQLException,
			DbxException {
		Connection connection = getConnection();
		PreparedStatement getFilesToDeleteStatement;
		getFilesToDeleteStatement = connection
				.prepareStatement("Select * from files where expiration_date <= (?)");
		getFilesToDeleteStatement.setDate(1, date);
		ResultSet results = getFilesToDeleteStatement.executeQuery();
		return deleteFilesFromDropbox(results);
	}

	private int deleteFilesFromDropbox(ResultSet results) throws SQLException {
		int numberOfFilesDeleted = 0;
		while (results.next()) {
			String fullFilePath = results.getString("path");
			String transientFolderName = results.getString("identifier");
			String pathToDelete = fullFilePath.substring(fullFilePath
					.lastIndexOf(transientFolderName));
			pathToDelete = pathToDelete.replace("\\", "/");
			System.out.println("Deleting file: " + pathToDelete);
			try {
				DropboxManager.deleteFile(pathToDelete);
				++numberOfFilesDeleted;
			} catch (DbxException ex) {
				ex.printStackTrace();
			}
		}
		return numberOfFilesDeleted;
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
			setNewDateStatement.executeUpdate();
		} catch (SQLException e) {
			System.out.println("Couldn't set new expiration date");
			e.printStackTrace();
		}
	}

	private Date calculateExpirationDate(ResultSet results, Date expirationDate) {
		try {
			double totalMilliSeconds = 0.0;
			long maxMilliSeconds = 0;
			while (results.next()) {
				long currentMilliSeconds = results.getDate("date").getTime();
				if (currentMilliSeconds > maxMilliSeconds)
					maxMilliSeconds = currentMilliSeconds;
				totalMilliSeconds += currentMilliSeconds;
			}
			// multiply with base period and add to previous expiration_date
			long expirationExtensionPeriod = (long) Math.min(
					Settings.maxRetentionTime,
					Settings.baseRetentionTime
							* Math.ceil(totalMilliSeconds * 1.0
									/ maxMilliSeconds));
			Date newExpirationDate = new Date(
					(long) (expirationDate.getTime() + expirationExtensionPeriod));
			System.out.println("New expiration date: "
					+ newExpirationDate.toString());
			return newExpirationDate;
			// delete the open events
		} catch (SQLException e) {
			System.out.println("Failed calculation");
			e.printStackTrace();
			return expirationDate;
		}
	}
}
