package server;

import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.put;
import models.FileMetaData;
import models.Storage;

import org.apache.log4j.BasicConfigurator;

import spark.Request;

public class Server {
	private static Storage dataStore;

	public Server() {
		dataStore = new Storage();
		BasicConfigurator.configure();
	}

	public static void main(String args[]) {
		get("/find/:name", (request, response) -> getHandler(request));
		post("/:hash/:name/:timeStamp/:path",
				(request, response) -> postHandler(request));
		put("/:hash/:name/:timeStamp/:path",
				(request, response) -> putHandler(request));
		delete("/:hash", (request, response) -> deleteHandler(request));
	}

	public static String postHandler(Request request) {
		FileMetaData newFile = new FileMetaData(request.params(":name"),
				request.params(":timestamp"), request.params(":path"));
		dataStore.post(request.params(":hash"), newFile);
		return Messages.POST_SUCCESSFUL;
	}

	public static String putHandler(Request request) {
		FileMetaData newFile = new FileMetaData(request.params(":name"),
				request.params(":timestamp"), request.params(":path"));
		dataStore.put(request.params(":hash"), newFile);
		return Messages.PUT_SUCCESSFUL;
	}

	public static String deleteHandler(Request request) {
		dataStore.delete(request.params(":hash"));
		return Messages.DELETE_SUCCESSFUL;
	}

	public static String getHandler(Request request) {
		return "Logic to find " + request.params(":name") + " coming soon";
	}
}