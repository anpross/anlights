package my.anlights;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.getpebble.android.kit.util.PebbleTuple;
import my.anlights.data.HueBridge;
import my.anlights.data.HueGroup;
import my.anlights.data.HueLight;
import my.anlights.data.HueState;
import my.anlights.gui.LightView;
import my.anlights.servicetest.LocalWordService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity implements CallbackListener, OnClickListener, CompoundButton.OnCheckedChangeListener, LightView.OnLightStateChangeListener {

    private static final String TAG = Constants.LOGGING_TAG;

    private HueGroup hGroup;
    Switch onToggle;
    Button onHelloPebble;
    LightView lightView;

    private PebbleKit.PebbleDataReceiver pebbleDataHandler = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onStart() {

        super.onStart();

        AlConfig.getInstance(this).setBridgeUser(Constants.BRIDGE_USER);
        initBridge();

        initUi();


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
        setContentView(R.layout.activity_main);

        lightView = (LightView) findViewById(R.id.lightView);
        lightView.setOnLightStateChangeListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG,"onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.activity_main, menu);

        MenuItem item = menu.findItem(R.id.menu_switch); //
        View actionView = item.getActionView();
        if ( actionView instanceof LinearLayout) {
            onToggle = (Switch)actionView.findViewById(R.id.onToggleSwitch);
            onToggle.setOnCheckedChangeListener(this);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        Log.i(TAG, "option item selected: "+item);
        switch (item.getItemId()) {
            case R.id.menu_switch:
                Log.i(TAG,"menu switch");
                return true;
            case R.id.onToggleSwitch:
                Log.i(TAG,"on toggle switch");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
        if(hGroup != null){
            HueState lights = hGroup.getLightState();

            if(lights.isOn() != null && onToggle != null){
                onToggle.setChecked(lights.isOn());
            }

            if(lights.isOn() != null && onToggle != null){
                onToggle.setChecked(lights.isOn());
            }
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

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean onState) {
        if (compoundButton.equals(onToggle)) {
            setOnState(onState);
        }
    }

    @Override
    public void onLightStateChanged(LightView lightView, int brightness, int temperature) {
        Log.i(TAG, "setting lightState to: "+brightness+"/"+temperature);
        if (hGroup != null) {
            if (lightView.equals(this.lightView)) {
                HueState newState = new HueState();
                newState.setBri(brightness);
                newState.setCt(temperature + 154);
                hGroup.setLightState(newState);
            }
        } else {
            onToggle.setChecked(!onToggle.isChecked());
            Toast.makeText(getApplicationContext(), "no lightgroup", Toast.LENGTH_SHORT).show();
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

    public void setOnState(boolean newOnState) {
        if (hGroup != null) {
            HueState newState = new HueState();
            newState.setOn(newOnState);
            onToggle.setChecked(newOnState);
            hGroup.setLightState(newState);
            hGroup.readLightStatus();
        } else {
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
