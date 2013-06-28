package my.anlights.util;

/**
 * Created by Andreas on 06.06.13.
 */

import android.util.Log;

import my.anlights.Constants;

public class MyLog {

    private static final int MAX_MESSAGE_LENGTH = 1024;

    private static String currClass;

    public static void entering(String className, String methodName, Object... parameters) {

        currClass = className;
        // entry / exit is for debugging
        if (isLoggable(className, Log.DEBUG)) {
            entryExit("entering ", className, methodName, parameters);
        }
    }

    public static void exiting(String className, String methodName, Object... parameters) {

        // entry / exit is for debugging
        if (isLoggable(className, Log.DEBUG)) {
            entryExit("exiting ", className, methodName, parameters);
        }
    }

    private static void entryExit(String entryExit, String className, String methodName, Object... parameters) {
        StringBuilder sb = new StringBuilder(entryExit);
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
        if (isLoggable(currClass, Log.DEBUG)) {
            Log.d(Constants.LOGGING_TAG, "  " + currClass + ": " + message);
        }
    }

    public static void i(String message) {
        if (isLoggable(currClass, Log.INFO)) {
            Log.i(Constants.LOGGING_TAG, "  " + currClass + ": " + message);
        }
    }

    public static void e(String message, Throwable e) {
        if (isLoggable(currClass, Log.ERROR)) {
            Log.e(Constants.LOGGING_TAG, message, e);
        }
    }

    public static void e(String message) {
        if (isLoggable(currClass, Log.ERROR)) {
            Log.e(Constants.LOGGING_TAG, message);
        }
    }

    /**
     * log no gui stuff but all errors
     *
     * @param className
     * @param severity
     * @return
     */
    private static boolean isLoggable(String className, int severity) {
        if (severity == Log.ERROR) {
            return true;
        } else if (className.startsWith("my.anlights.gui")) {
            return false;
        } else {
            return true;
        }
    }
}