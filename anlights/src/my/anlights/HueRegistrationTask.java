package my.anlights;

import android.os.AsyncTask;
import android.widget.ProgressBar;
import my.anlights.data.HueBridge;
import my.anlights.data.HueException;
import my.anlights.util.MyLog;

/**
 * Created by Andreas on 15.06.13.
 */
public class HueRegistrationTask extends AsyncTask<Void, Integer, Boolean> {

	private static final String CLASS_NAME = HueRegistrationTask.class.getCanonicalName();

	private ProgressBar progressBar;
	private HueBridge bridge;
	private CallbackListener dialogCallback;

	public HueRegistrationTask(ProgressBar progressBar, HueBridge bridge, CallbackListener dialogCallback) {
		MyLog.entering(CLASS_NAME, "HueRegistrationTask", progressBar, bridge);

		this.progressBar = progressBar;
		this.bridge = bridge;
		this.dialogCallback = dialogCallback;

		MyLog.exiting(CLASS_NAME, "HueRegistrationTask");
	}

	@Override
	protected Boolean doInBackground(Void... contexts) {
		MyLog.entering(CLASS_NAME, "doInBackground", contexts);

		int pollDuration = Constants.REGISTRATION_DURATION_S * 1000;
		int pollInterval = Constants.REGISTRATION_INTERVAL_MS;
		int pollCycles = pollDuration / pollInterval;

		boolean registrationSuccessful = false;
		int currPollCycle = 0;
		do {
			currPollCycle++;

			try {
				MyLog.d("waiting for time to pass");
				Thread.sleep(pollInterval);
			} catch (InterruptedException e) {
				MyLog.e("problem during sleep", e);
			}

			try {
				registrationSuccessful = bridge.registerUser();
			} catch (HueException e) {
				MyLog.e("still not registered", e);
			}

			publishProgress(pollCycles - currPollCycle, pollCycles);

		} while (currPollCycle < pollCycles && !registrationSuccessful && !this.isCancelled());

		MyLog.exiting(CLASS_NAME, "doInBackground", registrationSuccessful);
		return registrationSuccessful;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		MyLog.entering(CLASS_NAME, "onProgressUpdate", values);

		int progress = values[0];
		int maxValue = values[1];

		progressBar.setMax(maxValue);
		progressBar.setProgress(progress);
		MyLog.exiting(CLASS_NAME, "onProgressUpdate");
	}

	@Override
	protected void onPostExecute(Boolean aBoolean) {
		MyLog.entering(CLASS_NAME, "onPostExecute", aBoolean);
		dialogCallback.callback(aBoolean);
		progressBar.setProgress(0);
		MyLog.exiting(CLASS_NAME, "onPostExecute");
	}
}
