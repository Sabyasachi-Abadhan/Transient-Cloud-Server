package models;

import java.sql.Timestamp;

import server.Server;

/**
 * Class wraps MetaData such as name, last modified time and path. It is
 * instantiated whenever a new file is tracked
 * 
 * @author ROHIT
 *
 */
public class FileMetaData extends Server {

	private String name;
	private String path;
	private Timestamp lastModified;

	public FileMetaData(String name, String path, String lastModified) {
		this.name = name;
		this.path = path;
		this.lastModified = Timestamp.valueOf(lastModified);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Timestamp getLastModified() {
		return lastModified;
	}

	public void setLastModified(Timestamp lastModified) {
		this.lastModified = lastModified;
	}

}