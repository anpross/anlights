package my.anlights.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.getpebble.android.kit.util.PebbleTuple;
import my.anlights.Constants;
import my.anlights.PebbleConstants;
import my.anlights.UpdateHueAction;
import my.anlights.data.HueState;
import my.anlights.util.MyLog;
import org.json.JSONException;

import java.util.Iterator;
import java.util.UUID;

/**
 * Created by Andreas on 18.05.13.
 */


public class PebbleConnectionReceiver extends BroadcastReceiver {

	private static final String CLASS_NAME = PebbleConnectionReceiver.class.getCanonicalName();

	@Override
	public void onReceive(Context context, Intent intent) {
		MyLog.entering(CLASS_NAME, "onReceive", context, intent);

		final UUID receivedUuid = (UUID) intent.getSerializableExtra(com.getpebble.android.kit.Constants.APP_UUID);
		if (intent.getAction().equals(com.getpebble.android.kit.Constants.INTENT_APP_RECEIVE)) {
			onAppReceivce(context, intent);
			if (receivedUuid.equals(Constants.PEBBLE_UUID)) {

			}
		} else if (intent.getAction().equals(com.getpebble.android.kit.Constants.INTENT_PEBBLE_CONNECTED)) {
			onPebbleConnected(context, intent);
		} else if (intent.getAction().equals(com.getpebble.android.kit.Constants.INTENT_PEBBLE_DISCONNECTED)) {
			onPebbleDisconnected(context, intent);
		}
		MyLog.exiting(CLASS_NAME, "onReceive");
	}


	public void onAppReceivce(Context context, Intent intent) {
		MyLog.entering(CLASS_NAME, "onAppReceivce", context, intent);
		final int transactionId = intent.getIntExtra(com.getpebble.android.kit.Constants.TRANSACTION_ID, -1);
		final String jsonData = intent.getStringExtra(com.getpebble.android.kit.Constants.MSG_DATA);
		MyLog.i("got data from Pebble " + jsonData);
		try {
			final PebbleDictionary data = PebbleDictionary.fromJson(jsonData);
			HueState dataState = getHueStateForPebbleDictionary(data);

			UpdateHueAction update = new UpdateHueAction();
			update.updateHue(dataState);
			PebbleKit.sendAckToPebble(context, transactionId);
		} catch (JSONException e) {
			MyLog.e("failed reived -> dict" + e);
		}
		MyLog.exiting(CLASS_NAME, "onAppReceivce");
	}

	public HueState getHueStateForPebbleDictionary(PebbleDictionary data) {
		MyLog.entering(CLASS_NAME, "getHueStateForPebbleDictionary", data);
		HueState state = new HueState();
		Iterator<PebbleTuple> iData = data.iterator();
		while (iData.hasNext()) {
			PebbleTuple currTuple = iData.next();
			Integer currKey = (Integer) currTuple.key;
			Long currValue = (Long) currTuple.value;

			if (currKey == PebbleConstants.ONOFF_KEY) {
				if (currValue == PebbleConstants.ONOFF_VALUE_ON) {
					state.setOn(true);
				} else if (currValue == PebbleConstants.ONOFF_VALUE_OFF) {
					state.setOn(false);
				}
			} else if (currKey == PebbleConstants.BRIGHTNESS_KEY) {
				state.setBri((int) (currValue * 255 / 100));
			} else if (currKey == PebbleConstants.TEMP_KEY) {
				state.setCt((int) (154 + (currValue * 346 / 100)));
			}
		}
		MyLog.exiting(CLASS_NAME, "getHueStateForPebbleDictionary", state);
		return state;
	}

	public void onPebbleConnected(Context context, Intent intent) {
		MyLog.entering(CLASS_NAME, "onPebbleConnected", context, intent);
		final String pebbleAddress = intent.getStringExtra("address");

		MyLog.i("Pebble " + pebbleAddress + " connected");
		MyLog.exiting(CLASS_NAME, "onPebbleConnected");
	}

	public void onPebbleDisconnected(Context context, Intent intent) {
		MyLog.entering(CLASS_NAME, "onPebbleDisconnected", context, intent);
		MyLog.exiting(CLASS_NAME, "onPebbleDisconnected");
	}
}
