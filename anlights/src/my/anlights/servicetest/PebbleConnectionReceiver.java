package my.anlights.servicetest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import my.anlights.Constants;

/**
 * Created by Andreas on 18.05.13.
 */


public class PebbleConnectionReceiver extends BroadcastReceiver {
    private static final String TAG = Constants.LOGGING_TAG;

    @Override
    public void onReceive(Context context, Intent intent) {
        final String pebbleAddress = intent.getStringExtra("address");

        Log.i(TAG, "Pebble "+pebbleAddress+" connected");
    }
}
