package server;

import static spark.Spark.get;
import static spark.Spark.post;

import java.sql.Date;
import java.sql.SQLException;

import models.FileMetaData;
import spark.Request;
import database.Database;

public class Server {
	private Database db;

	public static void main(String args[]) throws SQLException {
		Server server = new Server();
		Database db = server.getDb();
		server.initializeRoutes();
		db.insertNewEvent("testFile", "testFile", "testFile", new Date(0));
		db.insertNewFile("testFile", "testFile", "testFile", new Date(0));
		// need to fix the below query
		db.deleteFile(new Date(0));
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
		get("/find/:name", (request, response) -> this.getHandler(request));
		post("/modify/", (request, response) -> this.modifyHandler(request));
		post("/open/", (request, response) -> this.openHandler(request));
		post("/put/", (request, response) -> this.putHandler(request));
		post("/delete/", (request, response) -> this.deleteHandler(request));
	}

	public String openHandler(Request request) {
		// Store event in db events table
		Database db = getDb();
		// Find out how to convert string to java sql date
		// Date date = new Date(request.queryParams("date"));
		try {
			db.insertNewEvent("open", request.queryParams("file_name"),
					request.queryParams("file_path"), new Date(0));
		} catch (SQLException e) {
			System.out.println("Couldn't add open event to the database");
			e.printStackTrace();
		}
		return Messages.POST_SUCCESSFUL;
	}

	public String modifyHandler(Request request) {
		System.out.println("Received modify event for "
				+ request.queryParams(("fileName")));
		// Replace with db queries for putting into file table and events table
		FileMetaData newFile = new FileMetaData(
				request.queryParams("fileName"),
				request.queryParams("fileLastModified"),
				request.queryParams("filePath"));
		return Messages.POST_SUCCESSFUL;
	}

	public String putHandler(Request request) {
		System.out.println("Inside put handler");
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