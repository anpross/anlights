package my.anlights;

import my.anlights.data.HueMessage;
import my.anlights.util.MyLog;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.Queue;

public class HueThread {

	private static final String TAG = Constants.LOGGING_TAG;

	private int requestsPerSec = 30;
	private int maxPackageAgeMs = 500;
	private int slotDuration = 1000 / requestsPerSec;
	private int lowQueueCapacity = maxPackageAgeMs / slotDuration;
	private long lastRun = 0;

	private HueRunnable run;

	JSONObject result;

	public HueThread() {
		Queue<HueMessage> lowQueue = new LinkedList<HueMessage>();
		Queue<HueMessage> highQueue = new LinkedList<HueMessage>();

		run = new HueRunnable();

	}

	private static final String CLASS_NAME = HueThread.class.getCanonicalName();

	public JSONObject pushMessage(HueMessage message) {
		MyLog.entering(CLASS_NAME, "pushMessage", message);

		long currTime = Calendar.getInstance().getTimeInMillis();
		long currSlotEnd = lastRun + slotDuration;
		lastRun = currTime;
		if (currSlotEnd > currTime) {
			long currSlotRemaining = currSlotEnd - currTime;
			try {
				Thread.sleep(currSlotRemaining);
				MyLog.d("sleeping for " + currSlotRemaining + "ms");
			} catch (InterruptedException e) {
				MyLog.e("problem waiting for current message slot to end", e);
			}
		}


		result = null;
		run.setMessage(message);
		Thread t = new Thread(run);
		t.start();
		try {
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		result = run.getResult();
		MyLog.d("return result:" + result);

		MyLog.exiting(CLASS_NAME, "pushMessage", result);
		return result;
	}


	public JSONObject getResult() {
		return result;
	}

}
