package my.anlights.util;

/**
 * Created by Andreas on 06.06.13.
 */

import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import my.anlights.BuildConfig;
import my.anlights.Constants;

public class MyLog {

	private static String currClass;

	private static final boolean FILE_LOGGING = true;

	private static FileWriter writer;
	private static long nextLogfileEndTime = 0;

	public static void entering(String className, String methodName, Object... parameters) {

		currClass = className;
		// entry / exit is for debugging
		if (BuildConfig.DEBUG && isLoggable(className, Log.DEBUG)) {
			entryExit("entering ", className, methodName, parameters);
		}
	}

	public static void exiting(String className, String methodName, Object... parameters) {

		// entry / exit is for debugging
		if (BuildConfig.DEBUG && isLoggable(className, Log.DEBUG)) {
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

		if (FILE_LOGGING) {
			writeToFile(Log.DEBUG, sb.toString());
		}

		Log.d(Constants.LOGGING_TAG, sb.toString());
	}

	public static void d(String message) {
		if (BuildConfig.DEBUG && isLoggable(currClass, Log.DEBUG)) {
			if (FILE_LOGGING) {
				writeToFile(Log.DEBUG, message);
			}

			Log.d(Constants.LOGGING_TAG, "  " + currClass + ": " + message);
		}
	}

	public static void i(String message) {
		if (isLoggable(currClass, Log.INFO)) {
			if (FILE_LOGGING) {
				writeToFile(Log.INFO, message);
			}
			Log.i(Constants.LOGGING_TAG, "  " + currClass + ": " + message);
		}
	}

	public static void e(String message, Throwable e) {
		if (isLoggable(currClass, Log.ERROR)) {
			if (FILE_LOGGING) {
				writeToFile(Log.ERROR, message, e);
			}
			Log.e(Constants.LOGGING_TAG, message, e);
		}
	}

	public static void e(String message) {
		if (isLoggable(currClass, Log.ERROR)) {
			if (FILE_LOGGING) {
				writeToFile(Log.ERROR, message);
			}
			Log.e(Constants.LOGGING_TAG, message);
		}
	}

	private static void writeToFile(int error, String message) {
		writeToFile(error, message, null);
	}

	private static void writeToFile(int error, String message, Throwable excep) {

		if (writer == null) {
			initFileWriter();
		}

		try {
			writer.write(getLogLevelName(error));
			writer.write(": ");
			writer.write(message);
			if (excep != null) {
				writer.write("\nstacktrace:\n");
				excep.printStackTrace(new PrintWriter(writer));
			}
			if (error == Log.ERROR) {
				writer.flush();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String getLogLevelName(int level) {
		switch (level) {
			case Log.ERROR:
				return "ERROR";
			case Log.DEBUG:
				return "DEBUG";
			case Log.INFO:
				return "INFO";
			case Log.ASSERT:
				return "ASSERT";
			case Log.VERBOSE:
				return "VERBOSE";
			case Log.WARN:
				return "WARN";
			default:
				return "UNKNOWN";
		}
	}

	private static void initFileWriter() {
		if (writer == null) {
			try {
				writer = new FileWriter("sdcard/anlight_debug.log");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
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

	@Override
	protected void finalize() throws Throwable {
		writer.close();
		super.finalize();
	}
}