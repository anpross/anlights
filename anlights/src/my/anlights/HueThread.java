package my.anlights;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.Queue;

import my.anlights.data.HueMessage;

import org.json.JSONObject;

import android.util.Log;

public class HueThread {

	private static final String TAG = Constants.LOGGING_TAG;


	
	private Queue<HueMessage> lowQueue;
	private Queue<HueMessage> highQueue;
	private int requestsPerSec = 30;
	private int maxPackageAgeMs = 500;
	private int slotDuration = 1000 / requestsPerSec;
	private int lowQueueCapacity = maxPackageAgeMs / slotDuration;
	private long lastRun = 0;
	
	private Thread t;
	private HueRunnable run;
	
	JSONObject result;
	
	public HueThread() {
		lowQueue = new LinkedList<HueMessage>(); 
		highQueue = new LinkedList<HueMessage>(); 

		run = new HueRunnable();

	}
	 
	
	public JSONObject pushMessage(HueMessage message) {
		Log.d(TAG,"process message:"+message);
		
		
		long currTime = Calendar.getInstance().getTimeInMillis();
		long currSlotEnd = lastRun + slotDuration;
		lastRun = currTime;
		if(currSlotEnd > currTime){
			long currSlotRemaining = currSlotEnd - currTime;
			try {
				Thread.sleep(currSlotRemaining);
				Log.d(TAG,"sleeping for "+currSlotRemaining+"ms");
			} catch (InterruptedException e) {
				Log.e(TAG,"problem waiting for current message slot to end",e);
			}
		}

		
		result = null;
		run.setMessage(message);
		t = new Thread(run);
			t.start();
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			result = run.getResult();
			Log.d(TAG,"return result:"+result);
		return result;
	}
	
	
	public JSONObject getResult() {
		return result;
	}
	
}
