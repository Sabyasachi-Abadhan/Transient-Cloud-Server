package tests;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.Test;

public class ServerTest {

	@Test
	public void serverIsRunningTest() throws IOException {
		String url = "http://localhost:4567/status";
		URL urlObject = new URL(url);
		HttpURLConnection connection = (HttpURLConnection) urlObject
				.openConnection();
		connection.setRequestMethod("GET");
		connection.setRequestProperty("User-Agent", "Junit Test Suite");
		int responseCode = connection.getResponseCode();
		assertTrue(responseCode >= 200 & responseCode <= 299);
	}
}
