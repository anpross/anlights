package my.anlights;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.SeekBar.OnSeekBarChangeListener;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.getpebble.android.kit.util.PebbleTuple;
import my.anlights.data.HueBridge;
import my.anlights.data.HueGroup;
import my.anlights.data.HueLight;
import my.anlights.data.HueState;
import my.anlights.servicetest.LocalWordService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity implements CallbackListener, OnClickListener, OnSeekBarChangeListener {

    private static final String TAG = Constants.LOGGING_TAG;

    private HueGroup hGroup;
    ToggleButton onToggle;
    Button onHelloPebble;
    SeekBar briSeek;
    SeekBar tempSeek;
    private PebbleKit.PebbleDataReceiver pebbleDataHandler = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        doBindService();
        // username 10-40 chars
        AlConfig.getInstance(this).setBridgeUser(Constants.BRIDGE_USER);

        setContentView(R.layout.activity_main);
        initUi();

        initBridge();

        System.out.println("checking service: " + s);
    }

    @Override
    protected void onResume() {
        super.onResume();
        final Handler handler = new Handler();
        pebbleDataHandler = new PebbleKit.PebbleDataReceiver(Constants.PEBBLE_UUID) {
            @Override
            public void receiveData(final Context context, final int transactionId, final PebbleDictionary data) {
                //int newState = data.getUnsignedInteger(Constants.SPORTS_STATE_KEY).intValue();                sportsState = newState;
                Log.i(TAG, "got data:" + data);

                HueState state = new HueState();
                Iterator<PebbleTuple> iData = data.iterator();
                while (iData.hasNext()){
                    PebbleTuple currTuple = iData.next();
                    Integer currKey = (Integer)currTuple.key;
                    Long currValue = (Long)currTuple.value;

                    if(currKey == PebbleConstants.ONOFF_KEY){
                        if(currValue == PebbleConstants.ONOFF_VALUE_ON){
                            state.setOn(true);
                        } else if (currValue == PebbleConstants.ONOFF_VALUE_OFF) {
                            state.setOn(false);
                        }
                    } else if(currKey ==  PebbleConstants.BRIGHTNESS_KEY){
                        state.setBri((int)(currValue * 255 / 100));
                    } else if (currKey == PebbleConstants.TEMP_KEY) {
                        state.setCt((int)(154 + (currValue * 346 / 100)));
                    }
                }

                PebbleKit.sendAckToPebble(context, transactionId);


                 setLightState(state);
            }
        };
        PebbleKit.registerReceivedDataHandler(this, pebbleDataHandler);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onRestoreInstanceState(savedInstanceState);

        initBridge();
    }

    private void initBridge() {
        HueDiscoveryTask discovery = new HueDiscoveryTask();
        discovery.setCallback(this);
        discovery.execute();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();

        // Always deregister any Activity-scoped BroadcastReceivers when the Activity is paused
        if (pebbleDataHandler != null) {
            unregisterReceiver(pebbleDataHandler);
            pebbleDataHandler = null;
        }
    }

    private void initUi() {
        onToggle = (ToggleButton) findViewById(R.id.onToggleButton);
        onToggle.setOnClickListener(this);
        onHelloPebble = (Button) findViewById(R.id.onHelloPebble);
        onHelloPebble.setOnClickListener(this);
        briSeek = (SeekBar) findViewById(R.id.briSeekBar);
        briSeek.setOnSeekBarChangeListener(this);
        tempSeek = (SeekBar) findViewById(R.id.tempSeekBar);
        tempSeek.setOnSeekBarChangeListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    public void callback(Object source) {
        if (source instanceof HueDiscoveryTask) {
            HueDiscoveryTask discovery = (HueDiscoveryTask) source;
            Log.d("TAG", "discover done - base url:" + AlConfig.getExistingInstance().getBridgeUrlBase());

//			new HueLights().registerUser();
            HueBridge bridge = discovery.getBridge();

            if (bridge.isConnected()) {
                List<HueLight> lights = bridge.getLightNames();

                hGroup = new HueGroup();
                for (HueLight currLight : lights) {
                    hGroup.addLight(currLight);
                }
                hGroup.readLightStatus();

                updateControls();

                Log.d(TAG, "group state:" + hGroup.getLightState());
            }

        }


    }

    private void updateControls() {
        HueState lights = hGroup.getLightState();
        onToggle.setChecked(lights.isOn());
        briSeek.setProgress(lights.getBri());
        if (lights.getCt() != null) {
            tempSeek.setProgress(lights.getCt() - 154);
        }
    }

    public void onClick(View v) {
        // TODO Auto-generated method stub
        Log.d(TAG, "got clickEvent:" + v);
        if (v.equals(onToggle)) {
            toggleOnState();
        } else if (v.equals(onHelloPebble)) {
            sendAlertToPebble();
        }

    }

    public void toggleOnState() {
        if (hGroup != null) {
            HueState lights = hGroup.getLightState();
            boolean newOnState = !lights.isOn();
            HueState newState = new HueState();
            newState.setOn(newOnState);
            onToggle.setChecked(newOnState);
            hGroup.setLightState(newState);
            hGroup.readLightStatus();
        } else {
            onToggle.setChecked(!onToggle.isChecked());
            Toast.makeText(getApplicationContext(), "no lightgroup", Toast.LENGTH_SHORT).show();
        }
    }

    public  void setLightState(HueState newState){
        if (hGroup != null) {
            hGroup.setLightState(newState);
        } else {
            onToggle.setChecked(!onToggle.isChecked());
            Toast.makeText(getApplicationContext(), "no lightgroup", Toast.LENGTH_SHORT).show();
        }
    }


    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        // TODO Auto-generated method stub


    }

    public void onStartTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        if (hGroup != null) {
            if (seekBar.equals(briSeek)) {
                HueState newState = new HueState();
                newState.setBri(briSeek.getProgress());
                hGroup.setLightState(newState);

            } else if (seekBar.equals(tempSeek)) {
                HueState newState = new HueState();
                newState.setCt(tempSeek.getProgress() + 154);
                hGroup.setLightState(newState);

            }
        } else {
            onToggle.setChecked(!onToggle.isChecked());
            Toast.makeText(getApplicationContext(), "no lightgroup", Toast.LENGTH_SHORT).show();
        }

    }

    // http://www.vogella.com/articles/AndroidServices/article.html
    private LocalWordService s;

    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder binder) {
            s = ((LocalWordService.MyBinder) binder).getService();
            Toast.makeText(MainActivity.this, "Connected",
                    Toast.LENGTH_SHORT).show();

        }

        public void onServiceDisconnected(ComponentName className) {
            s = null;
        }
    };
    private ArrayAdapter<String> adapter;

    void doBindService() {
        bindService(new Intent(this, LocalWordService.class), mConnection,
                Context.BIND_AUTO_CREATE);
    }

    public void showServiceData(View view) {
        if (s != null) {

            Toast.makeText(this, "Number of elements",
                    Toast.LENGTH_SHORT).show();
            adapter.notifyDataSetChanged();
        }
    }

    public void sendAlertToPebble() {
        final Intent i = new Intent("com.getpebble.action.SEND_NOTIFICATION");

        final Map data = new HashMap();
        data.put("title", "Test Message");
        data.put("body", "body");
        final JSONObject jsonData = new JSONObject(data);
        final String notificationData = new JSONArray().put(jsonData).toString();

        i.putExtra("messageType", "PEBBLE_ALERT");
        i.putExtra("sender", "AnLight");
        i.putExtra("notificationData", notificationData);

        Log.d(TAG, "About to send a modal alert to Pebble: " + notificationData);
        sendBroadcast(i);
    }
}
