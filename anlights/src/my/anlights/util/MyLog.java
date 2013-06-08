package my.anlights.util;

/**
 * Created by Andreas on 06.06.13.
 */

import android.util.Log;
import my.anlights.Constants;

public class MyLog {

	private static final String TEMPLATE_ENTERING = "entering %1:%2(%3)";
	private static final String TEMPLATE_EXITING = "exiting %1:%2(%3)";

	private static final int MAX_MESSAGE_LENGTH = 1024;

	public static void entering(String className, String methodName, Object... parameters) {
		if (Log.isLoggable(Constants.LOGGING_TAG, Log.DEBUG)) {
			Log.d(Constants.LOGGING_TAG, String.format(TEMPLATE_ENTERING, className, methodName, parameters));
		}
	}

	public static void exiting(String className, String methodName, Object... returnValue) {
		if (Log.isLoggable(Constants.LOGGING_TAG, Log.DEBUG)) {
			Log.d(Constants.LOGGING_TAG, String.format(TEMPLATE_EXITING, className, methodName, returnValue));
		}
	}

	public static void d(String message) {
		Log.d(Constants.LOGGING_TAG, message);
	}

	public static void i(String message) {
		Log.i(Constants.LOGGING_TAG, message);
	}

	public static void e(String message, Throwable e) {
		Log.e(Constants.LOGGING_TAG, message, e);
	}

	public static void e(String message) {
		Log.e(Constants.LOGGING_TAG, message);
	}
}
