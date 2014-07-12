package dropbox;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Locale;

import utilities.Settings;

import com.dropbox.core.DbxAccountInfo.Quota;
import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuthNoRedirect;

/**
 * A singleton (well, a class with only static methods/fields really) that
 * manages all Dropbox operations. Authorize has to be called before other
 * methods
 * 
 * @author ROHIT
 *
 */
public class DropboxManager {

	private final static String APP_KEY = Settings.APP_KEY;
	private final static String APP_SECRET = Settings.APP_SECRET;
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
		if (!isReady()) {
			DbxWebAuthNoRedirect webAuth = new DbxWebAuthNoRedirect(config,
					appInfo);
			String authorizeUrl = webAuth.start();
			System.out.println(Settings.AUTH_PROMPT_1 + authorizeUrl);
			System.out.println(Settings.AUTH_PROMPT_2);
			System.out.println(Settings.AUTH_PROMPT_3);
			String code = new BufferedReader(new InputStreamReader(System.in))
					.readLine().trim();
			DbxAuthFinish authFinish = webAuth.finish(code);
			setAccessToken(authFinish.accessToken);
		}
		DbxClient client = new DbxClient(config, getAccessToken());
		System.out.println("Linked account: "
				+ client.getAccountInfo().displayName);
	}

	/**
	 * Deletes file from Dropbox with the given path
	 * 
	 * @param filePath
	 * @throws DbxException
	 */
	public static void deleteFile(String filePath) throws DbxException {
		if (!isReady())
			return;
		DbxClient client = new DbxClient(config, getAccessToken());
		filePath = new String("/").concat(filePath);
		client.delete(filePath);
	}

	/**
	 * Getter method for accessToken
	 * 
	 * @return String acessToken
	 */
	public static String getAccessToken() {
		return accessToken;
	}

	/**
	 * Setter method for accessToken
	 * 
	 */
	public static void setAccessToken(String accessToken) {
		DropboxManager.accessToken = accessToken;
	}

	/**
	 * Checks whether the authentication step has been completed and if the
	 * configuration has been initialized
	 * 
	 * @return
	 */
	public static boolean isReady() {
		return (config != null && !getAccessToken().equals(""));
	}

	/**
	 * Returns the amount of space free on the user's Dropbox account Dropbox
	 * doesn't have a direct method for this so we perform the following
	 * operation: Free Space = (Total Quota - Shared Folders Used - Normal
	 * Folders Used)
	 * 
	 * @return
	 * @throws DbxException
	 */
	public static long getFreeDropboxSpace() throws DbxException {
		if (!isReady())
			return 0L;
		DbxClient client = new DbxClient(config, getAccessToken());
		Quota quota = client.getAccountInfo().quota;
		return (quota.total - quota.shared - quota.normal);
	}
}
