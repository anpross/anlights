package my.anlights;

import android.content.Context;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.getpebble.android.kit.util.PebbleTuple;

import java.util.UUID;

import my.anlights.data.HueState;
import my.anlights.util.MyLog;

/**
 * Created by user on 7/7/13.
 */
public class AnLightPebbleDataReceiver extends PebbleKit.PebbleDataReceiver {
	private final String CLASS_NAME = PebbleKit.PebbleDataReceiver.class.getCanonicalName();

	AnLightPebbleDataReceiver(UUID uuid) {
		super(uuid);

	}

	@Override
	public void receiveData(final Context context, final int transactionId, final PebbleDictionary data) {
		MyLog.entering(CLASS_NAME, "receiveData", context, transactionId, data);

		MyLog.i("got data:" + data);

		HueState state = new HueState();
		for (PebbleTuple currTuple : data) {
			Integer currKey = currTuple.key;
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

		PebbleKit.sendAckToPebble(context, transactionId);

		UpdateHueAction update = new UpdateHueAction();
		update.updateHue(state);

		MyLog.exiting(CLASS_NAME, "receiveData");
	}
}


