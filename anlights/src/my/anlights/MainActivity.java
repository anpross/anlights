package my.anlights;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.getpebble.android.kit.util.PebbleTuple;
import my.anlights.data.*;
import my.anlights.gui.LightView;
import my.anlights.util.MyLog;
import my.anlights.util.UserNameGenerator;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity implements CallbackListener, OnClickListener, CompoundButton.OnCheckedChangeListener, LightView.OnLightStateChangeListener {

	private HueGroup hGroup;
	Switch onToggle;
	Button onHelloPebble;
	LightView lightView;

	HueBridge bridge;

	private PebbleKit.PebbleDataReceiver pebbleDataHandler = null;

	private static final String CLASS_NAME = MainActivity.class.getCanonicalName();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		MyLog.entering(CLASS_NAME, "onCreate", savedInstanceState);
		super.onCreate(savedInstanceState);
		MyLog.exiting(CLASS_NAME, "onCreate");
	}

	@Override
	protected void onStart() {
		MyLog.entering(CLASS_NAME, "onStart");

		super.onStart();

		AlConfig.getInstance(this).setBridgeUser(Constants.BRIDGE_USER);
		initBridge();

		initUi();


		final Handler handler = new Handler();
		pebbleDataHandler = new PebbleKit.PebbleDataReceiver(Constants.PEBBLE_UUID) {

			private final String CLASS_NAME = PebbleKit.PebbleDataReceiver.class.getCanonicalName();

			@Override
			public void receiveData(final Context context, final int transactionId, final PebbleDictionary data) {
				MyLog.entering(CLASS_NAME, "receiveData", context, transactionId, data);
				//int newState = data.getUnsignedInteger(Constants.SPORTS_STATE_KEY).intValue();                sportsState = newState;
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


				setLightState(state);
				MyLog.exiting(CLASS_NAME, "receiveData");
			}
		};
		PebbleKit.registerReceivedDataHandler(this, pebbleDataHandler);
		MyLog.exiting(CLASS_NAME, "onStart");
	}


	private void initBridge() {
		MyLog.entering(CLASS_NAME, "initBridge");
		HueDiscoveryTask discovery = new HueDiscoveryTask();
		discovery.setCallback(this);
		discovery.execute();
		MyLog.exiting(CLASS_NAME, "initBridge");
	}

	@Override
	protected void onDestroy() {
		MyLog.entering(CLASS_NAME, "onDestroy");
		// TODO Auto-generated method stub
		super.onDestroy();
		MyLog.exiting(CLASS_NAME, "onDestroy");
	}

	@Override
	protected void onPause() {
		MyLog.entering(CLASS_NAME, "onPause");
		// TODO Auto-generated method stub
		super.onPause();

		// Always deregister any Activity-scoped BroadcastReceivers when the Activity is paused
		if (pebbleDataHandler != null) {
			unregisterReceiver(pebbleDataHandler);
			pebbleDataHandler = null;
		}
		MyLog.exiting(CLASS_NAME, "onPause");
	}

	private void initUi() {
		MyLog.entering(CLASS_NAME, "initUi");
		setContentView(R.layout.activity_main);

		lightView = (LightView) findViewById(R.id.lightView);
		lightView.setOnLightStateChangeListener(this);
		MyLog.exiting(CLASS_NAME, "initUi");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MyLog.entering(CLASS_NAME, "onCreateOptionsMenu", menu);
		getMenuInflater().inflate(R.menu.activity_main, menu);

		MenuItem item = menu.findItem(R.id.menu_switch); //
		View actionView = item.getActionView();
		if (actionView instanceof LinearLayout) {
			onToggle = (Switch) actionView.findViewById(R.id.onToggleSwitch);
			onToggle.setOnCheckedChangeListener(this);
		}

		boolean menuCreated = true;
		MyLog.exiting(CLASS_NAME, "onCreateOptionsMenu", menuCreated);
		return menuCreated;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		MyLog.entering(CLASS_NAME, "onOptionsItemSelected", item);
		// Handle item selection
		MyLog.d("option item selected: " + item);
		switch (item.getItemId()) {
			case R.id.menu_settings:
				MyLog.d("menu switch");
				Intent intent = new Intent(this, SettingsActivity.class);
				startActivity(intent);
				return true;
			case R.id.onToggleSwitch:
				MyLog.d("on toggle switch");
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	public void callback(Object source) {
		MyLog.entering(CLASS_NAME, "callback", source);
		if (source instanceof HueDiscoveryTask) {
			HueDiscoveryTask discovery = (HueDiscoveryTask) source;
			MyLog.d("discover done - base url:" + AlConfig.getExistingInstance().getBridgeUrlBase());

//			new HueLights().registerUser();
			bridge = discovery.getBridge();
			try{
				if (bridge.isConnected()) {
					List<HueLight> lights = bridge.getLightNames();

					hGroup = new HueGroup();
					for (HueLight currLight : lights) {
						hGroup.addLight(currLight);
					}
					hGroup.readLightStatus();

					updateControls();

					MyLog.d("group state:" + hGroup.getLightState());
				}
			} catch (HueException e) {
				if(e.isAuthProblem()){
					MyLog.i("not authorized! - starting registration");
					doUserRegistration();
				} else {
					MyLog.e("HueException",e);
				}
			}
		}
		MyLog.exiting(CLASS_NAME, "callback");
	}

	private void doUserRegistration() {
		try {
			bridge.registerUser();
		} catch (HueException e) {
			e.printStackTrace();
		}
	}

	private void updateControls() {
		MyLog.entering(CLASS_NAME, "updateControls");
		if (hGroup != null) {
			HueState lights = hGroup.getLightState();

			if (lights.isOn() != null && onToggle != null) {
				onToggle.setChecked(lights.isOn());
			}

			if (lights.isOn() != null && onToggle != null) {
				onToggle.setChecked(lights.isOn());
			}
		}
		MyLog.exiting(CLASS_NAME, "updateControls");
	}

	public void onClick(View v) {
		MyLog.entering(CLASS_NAME, "onClick", v);
		MyLog.d("got clickEvent:" + v);
		if (v.equals(onToggle)) {
			try {
				toggleOnState();
			} catch (HueException e) {
				MyLog.e("problem toggeling on-state",e);
			}
		} else if (v.equals(onHelloPebble)) {
			sendAlertToPebble();
		}
		MyLog.exiting(CLASS_NAME, "onClick");
	}

	@Override
	public void onCheckedChanged(CompoundButton compoundButton, boolean onState) {
		MyLog.entering(CLASS_NAME, "onCheckedChanged", compoundButton, onState);
		if (compoundButton.equals(onToggle)) {
			try {
				setOnState(onState);
			} catch (HueException e) {
				MyLog.e("problem setting on-state",e);
			}
		}
		MyLog.exiting(CLASS_NAME, "onCheckedChanged");
	}

	@Override
	public void onLightStateChanged(LightView lightView, int brightness, int temperature) {
		MyLog.entering(CLASS_NAME, "onLightStateChanged", lightView, brightness, temperature);
		MyLog.i("setting lightState to: " + brightness + "/" + temperature);
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
		MyLog.exiting(CLASS_NAME, "onLightStateChanged");
	}

	public void toggleOnState() throws HueException {
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

	public void setOnState(boolean newOnState) throws HueException{
		MyLog.entering(CLASS_NAME, "setOnState", newOnState);
		if (hGroup != null) {
			HueState newState = new HueState();
			newState.setOn(newOnState);
			onToggle.setChecked(newOnState);
			hGroup.setLightState(newState);
			hGroup.readLightStatus();
		} else {
			MyLog.e("no lightgroup to set state to");
			Toast.makeText(getApplicationContext(), "no lightgroup", Toast.LENGTH_SHORT).show();
		}
		MyLog.exiting(CLASS_NAME, "setOnState");
	}

	public void setLightState(HueState newState) {
		MyLog.entering(CLASS_NAME, "setLightState", newState);
		if (hGroup != null) {
			hGroup.setLightState(newState);
		} else {
			onToggle.setChecked(!onToggle.isChecked());
			MyLog.e("no lightgroup to set state to");
			Toast.makeText(getApplicationContext(), "no lightgroup", Toast.LENGTH_SHORT).show();
		}
		MyLog.exiting(CLASS_NAME, "setLightState");
	}

	//just pebble sample code
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

		MyLog.d("About to send a modal alert to Pebble: " + notificationData);
		sendBroadcast(i);
	}


}
