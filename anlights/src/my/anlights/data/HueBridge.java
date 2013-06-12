package my.anlights.data;

import my.anlights.*;
import my.anlights.data.messages.*;
import my.anlights.util.MyLog;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


public class HueBridge extends HueObject implements CallbackListener {

	private boolean isSupported = false;
	private String udn;
	private String urlBase;
	private String user;

	private AlConfig config;
	private HueThread hueThread;

	private static final String CLASS_NAME = HueBridge.class.getCanonicalName();

	public HueBridge(boolean isSupported, String udn, String urlBase) {
		MyLog.entering(CLASS_NAME, "HueBridge", isSupported, udn, urlBase);


		config = AlConfig.getExistingInstance();

		hueThread = new HueThread();

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


	public List<HueLight> getLightNames() throws HueException {
		MyLog.entering(CLASS_NAME, "getLightNames");

		readConfig();
		HueLightNamesMessage message = new HueLightNamesMessage();

		hueThread.pushMessage(message); // replace this with EmptyCallbackListenerObj


		List<HueLight> lights = parseLights(hueThread.getResult());
		MyLog.exiting(CLASS_NAME, "getLightNames", lights);
		return lights;
	}

	public boolean isConnected() {
		MyLog.entering(CLASS_NAME, "isConnected");
		boolean userOk = (user != null) && (!user.isEmpty());
		boolean urlBaseOk = (urlBase != null) && (!urlBase.isEmpty());

		// not checking if our user is whitelisted - will notice that if the read action fails

		boolean isConnected = userOk && urlBaseOk;
		MyLog.exiting(CLASS_NAME, "isConnected", isConnected);
		return isConnected;
	}

	private boolean isUserWhitelisted(String user, HueConfig config) {
		MyLog.entering(CLASS_NAME, "isUserWhitelisted", user, config);

		boolean isUserWhitelisted = false;
		List<HueUser> users = config.getUsers();

		for (HueUser currUser : users) {
			if (currUser.getId().equals(user)) {
				isUserWhitelisted = true;
				break;
			}
		}
		MyLog.exiting(CLASS_NAME, "isUserWhitelisted", isUserWhitelisted);
		return isUserWhitelisted;
	}

	private HueConfig readBridgeConfig() {
		MyLog.entering(CLASS_NAME, "readBridgeConfig");

		HueReadConfigMessage message = new HueReadConfigMessage();

		JSONObject result = hueThread.pushMessage(message);
		HueConfig config = new HueConfig();
		config.updateConfigStatus(result);

		MyLog.exiting(CLASS_NAME, "readBridgeConfig", config);
		return config;
	}

	public JSONObject readLightState(HueLight light) {
		MyLog.entering(CLASS_NAME, "readLightState", light);

		HueReadStateMessage message = new HueReadStateMessage(light);

		JSONObject result = hueThread.pushMessage(message);

		MyLog.exiting(CLASS_NAME, "readLightState", result);

		return result;
	}

	public void writeLightState(HueLight light, HueState state) {
		MyLog.entering(CLASS_NAME, "writeLightState", light, state);

		HueWriteStateMessage message = new HueWriteStateMessage(light, state);

		hueThread.pushMessage(message);

		MyLog.exiting(CLASS_NAME, "writeLightState");
	}

	/**
	 * got put in the bridge because it handles multiple lights
	 * @param result
	 * @return
	 */
	private List<HueLight> parseLights(JSONObject result) throws HueException {
		HueError error = checkForError(result);
		if(error != null) {
			throw new HueException(error);
		}
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

	public void registerUser() throws HueException {
		MyLog.entering(CLASS_NAME, "registerUser");

		String devicetype = Constants.APPLICATION_NAME;

		//TODO build username generator for added security
		String username = user;

		HueRegistrationMessage message = new HueRegistrationMessage(devicetype, username);

		JSONObject result = hueThread.pushMessage(message);

		HueError error = checkForError(result);

		if(error != null) {
			throw new HueException(error);
		}

		MyLog.exiting(CLASS_NAME, "registerUser", result);
	}
}
