package my.anlights.data;

import my.anlights.util.MyLog;

/**
 * Created by Andreas on 09.06.13.
 */
public class HueException extends Exception {

	HueError error;

	private static final String CLASS_NAME = HueException.class.getCanonicalName();

	public HueException(HueError error) {
		this.error = error;
	}

	@Override
	public String getMessage() {
		return "problem occurred during hue communication, hue error: "+error.toString();
	}

	public boolean isAuthProblem() {
		MyLog.entering(CLASS_NAME,"isAuthProblem");
		boolean isAuthProb = false;
		if(error != null && error.getType() == HueError.ERROR_UNAUTHORIZED){
			isAuthProb = true;
		}
		MyLog.exiting(CLASS_NAME, "isAuthProblem", isAuthProb);
		return isAuthProb;
	}
}
