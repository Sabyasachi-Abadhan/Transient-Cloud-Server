package models;

import java.util.HashMap;

/**
 * Initial storage memory will be in memory, later will be extracted into a
 * database.For now, using a HashMap which maps file hashes to FileMetaData
 * 
 * @author ROHIT
 *
 */
public class Storage {

	private HashMap<String, FileMetaData> storage;

	public Storage() {
		storage = new HashMap<String, FileMetaData>();
	}

	public void post(String hash, FileMetaData meta) {
		storage.put(hash, meta);
	}

	public void put(String hash, FileMetaData meta) {
		delete(hash);
		post(hash, meta);
	}

	public void delete(String hash) {
		storage.remove(hash);
	}

	public FileMetaData search(String hash) {
		return storage.get(hash);
	}
}
