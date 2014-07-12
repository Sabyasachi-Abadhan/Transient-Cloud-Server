package utilities;

/**
 * Class stores database and DropboxManager related messages, constants and
 * credentials
 */
public class Settings {
	private static long oneDayInMilliseconds = 24 * 60 * 60 * 1000;
	public static long baseRetentionTime = 7 * oneDayInMilliseconds;
	public static long maxRetentionTime = 180 * oneDayInMilliseconds;

	// DropboxManager Specific Settings
	public final static String APP_KEY = "6nhr1g62etidyki";
	public final static String APP_SECRET = "hotuqvzbb0dlvxb";
	public final static String AUTH_PROMPT_1 = "1. Go to: ";
	public final static String AUTH_PROMPT_2 = "2. Click \"Allow\" (you might have to log in first)";
	public final static String AUTH_PROMPT_3 = "3. Copy the authorization code.";
	// 10 MB of free space reserved for temporary files/indexes
	public static final long FREE_SPACE_THRESHOLD = 10485760;
}
