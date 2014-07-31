package server;

import static spark.Spark.get;
import static spark.Spark.post;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import spark.Request;
import utilities.Messages;
import utilities.Settings;

import com.dropbox.core.DbxException;

import database.Database;
import dropbox.DropboxManager;

/**
 * Contains main method. Initializes the database connections, sets up db schema
 * and Dropbox Connections
 * 
 * @author ROHIT
 *
 */
public class Server {
	private Database db;

	public static void main(String args[]) throws Exception {
		// Initialize Dropbox
		Server server = new Server();
		Database db = server.getDb();
		server.initializeRoutes();
		DropboxManager.authorize();
	}

	public Server() {
		db = new Database();
	}

	public Database getDb() {
		return db;
	}

	/**
	 * Performs major routing duties, calls necessary handlers. Extracted into
	 * it's own method for extensibility and testing purposes
	 */
	public void initializeRoutes() {
		get("/status", (request, response) -> "Server is running");
		get("/find/", (request, response) -> this.getHandler(request));
		post("/modify/", (request, response) -> this.modifyHandler(request));
		post("/open/", (request, response) -> this.openHandler(request));
		post("/move/", (request, response) -> this.moveHandler(request));
		post("/rename/", (request, response) -> this.renameHandler(request));
	}

	/**
	 * Handles open events sent by the Transient Cloud Client
	 * 
	 * @param request
	 * @return
	 */
	public String openHandler(Request request) {
		Database db = getDb();
		SimpleDateFormat formatter = new SimpleDateFormat("d/M/y h:m:s a");
		try {

			Date date = new Date(formatter.parse(request.queryParams("date"))
					.getTime());
			db.insertNewEvent("open", request.queryParams("file_name"),
					request.queryParams("file_path"), date);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		db.deleteHandler(new Date(Calendar.getInstance().getTimeInMillis()));
		return Messages.POST_SUCCESSFUL;
	}

	/**
	 * Handles modify events sent by the Transient Cloud Client
	 * 
	 * @param request
	 * @return
	 */
	public String modifyHandler(Request request) {
		System.out.println("Received modify event for "
				+ request.queryParams(("name")));
		SimpleDateFormat formatter = new SimpleDateFormat("d/M/y h:m:s a");
		db.deleteHandler(new Date(Calendar.getInstance().getTimeInMillis()));
		String size = request.queryParams("file_size");
		long numericSize = Long.valueOf(size).longValue();
		// check if there is enough space and delete files otherwise
		while (!enoughSpaceAvailable(numericSize)) {
			System.out.println("There isn't enough space available");
			db.deleteLeastRecentlyUsedFile();
		}

		long expirationPeriod = Settings.baseRetentionTime;
		try {
			Date date = new Date(formatter.parse(
					request.queryParams("expiration_date")).getTime());
			Date expirationDate = new Date(formatter.parse(
					request.queryParams("expiration_date")).getTime()
					+ expirationPeriod);
			db.insertNewFile(request.queryParams("name"),
					request.queryParams("path"),
					request.queryParams("identifier"),
					request.queryParams("file_size"), expirationDate);
			db.insertNewEvent("modify", request.queryParams("name"),
					request.queryParams("path"), date);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return Messages.POST_SUCCESSFUL;
	}

	/**
	 * Handles Move Requests from Transient Cloud Client
	 * 
	 * @param request
	 * @return
	 */
	public String moveHandler(Request request) {
		System.out.println("Inside move handler");
		db.updateFile("path", request.queryParams("old_path"),
				request.queryParams("new_path"));
		return Messages.PUT_SUCCESSFUL;
	}

	/**
	 * Handles Rename Requests from Transient Cloud Client
	 * 
	 * @param request
	 * @return
	 */
	public String renameHandler(Request request) {
		System.out.println("Inside rename handler");
		db.updateFile("path", request.queryParams("old_path"),
				request.queryParams("new_path"));
		db.updateFile("name", request.queryParams("old_name"),
				request.queryParams("new_name"));
		return Messages.PUT_SUCCESSFUL;
	}

	public String getHandler(Request request) {
		return Messages.GET_SUCCESSFUL;
	}

	/**
	 * Checks if the user's dropbox folder has enough room for a file of given
	 * size
	 * 
	 * @param size
	 * @return
	 */
	public boolean enoughSpaceAvailable(long size) {
		try {
			long freeSpace = DropboxManager.getFreeDropboxSpace();
			System.out.println("Free Space in Dropbox: " + freeSpace);
			return (freeSpace - size >= Settings.FREE_SPACE_THRESHOLD);
		} catch (DbxException e) {
			return false;
		}
	}
}