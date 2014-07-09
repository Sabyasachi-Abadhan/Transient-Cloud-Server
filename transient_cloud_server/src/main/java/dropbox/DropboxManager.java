package dropbox;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Locale;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuthNoRedirect;

public class DropboxManager {

	private final static String APP_KEY = "6nhr1g62etidyki";
	private final static String APP_SECRET = "hotuqvzbb0dlvxb";
	private static String accessToken = "";
	private static DbxRequestConfig config = null;

	/***
	 * This static method is called inside initialize if the accessToken is not
	 * yet set for the server. The server does not persist the accessToken so it
	 * has to be authorized every time the server is restarted
	 * 
	 * @throws Exception
	 */
	public static void authorize() throws Exception {
		DbxAppInfo appInfo = new DbxAppInfo(APP_KEY, APP_SECRET);
		config = new DbxRequestConfig("TransientCloudServer/1.0", Locale
				.getDefault().toString());
		DbxWebAuthNoRedirect webAuth = new DbxWebAuthNoRedirect(config, appInfo);
		String authorizeUrl = webAuth.start();
		System.out.println("1. Go to: " + authorizeUrl);
		System.out
				.println("2. Click \"Allow\" (you might have to log in first)");
		System.out.println("3. Copy the authorization code.");
		String code = new BufferedReader(new InputStreamReader(System.in))
				.readLine().trim();
		DbxAuthFinish authFinish = webAuth.finish(code);
		setAccessToken(authFinish.accessToken);
		if (!isReady())
			return;
		DbxClient client = new DbxClient(config, getAccessToken());
		System.out.println("Linked account: "
				+ client.getAccountInfo().displayName);
	}

	public static void deleteFile(String filePath) throws DbxException {
		if (!isReady())
			return;
		DbxClient client = new DbxClient(config, getAccessToken());
		System.out.println("Linked account: "
				+ client.getAccountInfo().displayName);

	}

	public static String getAccessToken() {
		return accessToken;
	}

	public static void setAccessToken(String accessToken) {
		DropboxManager.accessToken = accessToken;
	}

	public static boolean isReady() {
		return (config != null && !getAccessToken().equals(""));
	}
}
