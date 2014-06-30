package server;

import static spark.Spark.get;
import static spark.Spark.post;
import models.FileMetaData;
import models.Storage;
import spark.Request;
import database.DatabaseInitialization;

public class Server {
	private static Storage dataStore = new Storage();;

	public static void main(String args[]) {
		initializeRoutes();
	}

	/**
	 * Performs major routing duties, calls necessary handlers. Extracted into
	 * it's own method for extensibility and testing purposes
	 */
	public static void initializeRoutes() {
		get("/all", (request, reponse) -> showAll());
		get("/status", (request, response) -> "Server is running");
		get("/find/:name", (request, response) -> getHandler(request));
		post("/modify/", (request, response) -> modifyHandler(request));
		post("/open/", (request, response) -> openHandler(request));
		post("/put/", (request, response) -> putHandler(request));
		post("/delete/", (request, response) -> deleteHandler(request));

		// Initialize database

		DatabaseInitialization.setupServer();
	}

	private static String showAll() {
		return dataStore.print();
	}

	public static String openHandler(Request request) {
		// Store event in db events table
		return Messages.POST_SUCCESSFUL;
	}

	public static String modifyHandler(Request request) {
		System.out.println("Received modify event for "
				+ request.queryParams(("fileName")));
		// Replace with db queries for putting into file table and events table
		FileMetaData newFile = new FileMetaData(
				request.queryParams("fileName"),
				request.queryParams("fileLastModified"),
				request.queryParams("filePath"));
		dataStore.post(request.params("fileName"), newFile);
		return Messages.POST_SUCCESSFUL;
	}

	public static String putHandler(Request request) {
		System.out.println("Inside put handler");
		return Messages.PUT_SUCCESSFUL;
	}

	public static String deleteHandler(Request request) {
		System.out.println("Inside Delete Handler");
		return Messages.DELETE_SUCCESSFUL;
	}

	public static String getHandler(Request request) {
		return dataStore.search(request.params(":hash")).toString();
	}
}