package my.anlights.util;

/**
 * Created by Andreas on 06.06.13.
 */

import android.util.Log;
import my.anlights.Constants;

import java.util.Arrays;

public class MyLog {

	private static final int MAX_MESSAGE_LENGTH = 1024;

	public static void entering(String className, String methodName, Object... parameters) {
		//if (Log.isLoggable(Constants.LOGGING_TAG, Log.DEBUG)) {
		entryExit("entering ", className, methodName, parameters);
	}

	public static void exiting(String className, String methodName, Object... parameters) {
		//if (Log.isLoggable(Constants.LOGGING_TAG, Log.DEBUG)) {
		entryExit("exiting ", className, methodName, parameters);
		//}
	}

	private static void entryExit(String entryExit,String className, String methodName, Object... parameters){
		StringBuffer sb = new StringBuffer(entryExit);
		sb.append(className).append(":").append(methodName).append("(");

		if (parameters != null) {
			for (int i = 0; i < parameters.length; i++) {
				sb.append(parameters[i]);
				if ((i + 1) < parameters.length) {
					sb.append(", ");
				}
			}
		}
		sb.append(")");
		Log.d(Constants.LOGGING_TAG, sb.toString());
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
