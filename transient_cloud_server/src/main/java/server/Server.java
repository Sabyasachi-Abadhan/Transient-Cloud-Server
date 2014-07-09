package server;

import static spark.Spark.get;
import static spark.Spark.post;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import spark.Request;
import database.Database;
import dropbox.DropboxManager;

public class Server {
	private Database db;

	public static void main(String args[]) throws Exception {
		// Initialize Dropbox
		DropboxManager.authorize();
		Server server = new Server();
		Database db = server.getDb();
		server.initializeRoutes();
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
		post("/delete/", (request, response) -> this.deleteHandler(request));
	}

	public String openHandler(Request request) {
		// Store event in db events table
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

	public String modifyHandler(Request request) {
		System.out.println("Received modify event for "
				+ request.queryParams(("name")));
		SimpleDateFormat formatter = new SimpleDateFormat("d/M/y h:m:s a");
		// check if there is enough space and delete files otherwise
		long expirationPeriod = 604800000;
		try {
			Date date = new Date(formatter.parse(
					request.queryParams("expiration_date")).getTime()
					+ expirationPeriod);
			db.insertNewFile(request.queryParams("name"),
					request.queryParams("path"),
					request.queryParams("identifier"),
					request.queryParams("hash"), date);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		db.deleteHandler(new Date(Calendar.getInstance().getTimeInMillis()));
		return Messages.POST_SUCCESSFUL;
	}

	public String moveHandler(Request request) {
		System.out.println("Inside move handler");
		db.updateFile("path", request.queryParams("old_path"),
				request.queryParams("new_path"));
		return Messages.PUT_SUCCESSFUL;
	}

	public String renameHandler(Request request) {
		System.out.println("Inside rename handler");
		db.updateFile("path", request.queryParams("old_path"),
				request.queryParams("new_path"));
		db.updateFile("name", request.queryParams("old_name"),
				request.queryParams("new_name"));
		return Messages.PUT_SUCCESSFUL;
	}

	public String deleteHandler(Request request) {
		System.out.println("Inside Delete Handler");
		return Messages.DELETE_SUCCESSFUL;
	}

	public String getHandler(Request request) {
		return Messages.GET_SUCCESSFUL;
	}
}