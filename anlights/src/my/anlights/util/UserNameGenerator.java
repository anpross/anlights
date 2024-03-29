package my.anlights.util;

import android.os.Build;
import android.util.Base64;
import my.anlights.Constants;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * generates a random name used to register the Application at the bridge.
 * <p/>
 * device name is added so you can tell what devices you have registered on the bridge.
 * <p/>
 * this is just for the unlikely case that this application will ever have more then one user.
 */
public class UserNameGenerator {

	private static final int RANDOM_BIT_LENGTH = 96;
	private static final int MAXIMUM_USERNAME_LENGTH = 40;


	private static final String CLASS_NAME = UserNameGenerator.class.getCanonicalName();

	public static String generateUserName() {
		MyLog.entering(CLASS_NAME, "generateUserName");
		SecureRandom random = new SecureRandom();

		String hash = Base64.encodeToString(new BigInteger(RANDOM_BIT_LENGTH, random).toByteArray(), Base64.URL_SAFE + Base64.NO_PADDING + Base64.NO_WRAP);
		hash = hash.replace("-", ""); // no - allowed
		hash = hash.replace("_", ""); // no _ allowed

		// removing spaces just because i don't like them
		String device = Build.MODEL.replace(" ", "");

		// cutting device name in case its too long
		if (Constants.LOGGING_TAG.length() + device.length() + hash.length() > MAXIMUM_USERNAME_LENGTH) {

			device = device.substring(0, MAXIMUM_USERNAME_LENGTH - Constants.LOGGING_TAG.length() - hash.length());
		}

		// sadly no dividers such as "-" or "_" are allowed
		StringBuilder sb = new StringBuilder(Constants.LOGGING_TAG);
		sb.append(device);
		sb.append(hash);

		String userName = sb.toString();

		MyLog.exiting(CLASS_NAME, "generateUserName", userName);
		return userName;
	}
}
