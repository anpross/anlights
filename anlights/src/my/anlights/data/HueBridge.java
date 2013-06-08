package my.anlights.data;

import my.anlights.AlConfig;
import my.anlights.CallbackListener;
import my.anlights.HueController;
import my.anlights.HueThread;
import my.anlights.util.MyLog;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


public class HueBridge implements CallbackListener {

	private boolean isSupported = false;
	private String udn;
	private String urlBase;
	private String user;

	private AlConfig config;
	private HueThread t;

	private HueController controller;

	private static final String CLASS_NAME = HueBridge.class.getCanonicalName();

	public HueBridge(boolean isSupported, String udn, String urlBase) {
		MyLog.entering(CLASS_NAME, "HueBridge", isSupported, udn, urlBase);


		config = AlConfig.getExistingInstance();

		t = new HueThread();

		this.isSupported = isSupported;
		this.udn = udn;
		this.urlBase = urlBase;
		this.user = config.getBridgeUser();
		MyLog.exiting(CLASS_NAME, "HueBridge");
	}

	public boolean isSupported() {
		return isSupported;
	}

	public String getUdn() {
		return udn;
	}

	public String getUrlBase() {
		return urlBase;
	}

	public HueController getController() {
		return controller;
	}

	public void setController(HueController controller) {
		this.controller = controller;
	}

	public List<HueLight> getLightNames() {
		MyLog.entering(CLASS_NAME, "getLightNames");

		readConfig();
		HueLightNamesMessage message = new HueLightNamesMessage();

		t.pushMessage(message); // replace this with EmptyCallbackListenerObj


		List<HueLight> lights = parseLights(t.getResult());
		MyLog.exiting(CLASS_NAME, "getLightNames", lights);
		return lights;
	}

	public boolean isConnected() {
		MyLog.entering(CLASS_NAME, "isConnected");
		boolean userOk = (user != null) && (!user.isEmpty());
		boolean urlBaseOk = (urlBase != null) && (!urlBase.isEmpty());

		boolean isConnected = userOk && urlBaseOk;
		MyLog.exiting(CLASS_NAME, "isConnected", isConnected);
		return isConnected;
	}

	public JSONObject readLightState(HueLight light) {
		MyLog.entering(CLASS_NAME, "readLightState", light);

		HueReadStateMessage message = new HueReadStateMessage(light);

		JSONObject result = t.pushMessage(message);

		MyLog.exiting(CLASS_NAME, "readLightState", result);

		return result;
	}

	public void writeLightState(HueLight light, HueState state) {
		MyLog.entering(CLASS_NAME, "writeLightState", light, state);

		HueWriteStateMessage message = new HueWriteStateMessage(light, state);

		t.pushMessage(message);

		MyLog.exiting(CLASS_NAME, "writeLightState");
	}

	private List<HueLight> parseLights(JSONObject result) {
		MyLog.entering(CLASS_NAME, "parseLights", result);
		List<HueLight> lights = new LinkedList<HueLight>();
		@SuppressWarnings("unchecked")
		Iterator<String> iKeys = result.keys();
		while (iKeys.hasNext()) {
			JSONObject currLamp;
			String currKey = iKeys.next();
			try {
				currLamp = result.getJSONObject(currKey);
				HueLight currLight = new HueLight(currKey, currLamp.getString("name"), this);
				lights.add(currLight);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		MyLog.exiting(CLASS_NAME, "parseLights", lights);
		return lights;
	}

	private void readConfig() {
		MyLog.entering(CLASS_NAME, "readConfig");
		user = config.getBridgeUser();
		urlBase = config.getBridgeUrlBase();
		MyLog.exiting(CLASS_NAME, "readConfig");
	}

	public void callback(Object source) {
		// TODO Auto-generated method stub

	}
}
