package my.anlights.data;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import my.anlights.AlConfig;
import my.anlights.CallbackListener;
import my.anlights.HueController;
import my.anlights.HueThread;

import org.json.JSONException;
import org.json.JSONObject;



public class HueBridge implements CallbackListener{

	private boolean isSupported = false;
	private String udn;
	private String urlBase;
	private String user;
	
	private AlConfig config;
	private HueThread t;
	
	private HueController controller;

	public HueBridge(boolean isSupported, String udn, String urlBase) {
		
		config = AlConfig.getExistingInstance();
		
		t = new HueThread();
		
		this.isSupported = isSupported;
		this.udn = udn;
		this.urlBase = urlBase;
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

//		readConfig();
		HueLightNamesMessage message = new HueLightNamesMessage();
		
		t.pushMessage(message); // replace this with EmptyCallbackListenerObj

		
		return parseLights(t.getResult());
	}
	
	public boolean isConnected() {
		boolean userOk = (user != null) && (!user.isEmpty());
		boolean urlBaseOk = (urlBase != null) && (!urlBase.isEmpty());
				
		return userOk && urlBaseOk;
	}
	
	public JSONObject readLightState(HueLight light){
		
		HueReadStateMessage message = new HueReadStateMessage(light);

		JSONObject result = t.pushMessage(message);

		
		return result;
	}
	
	public boolean writeLightState(HueLight light, HueState state){
		HueWriteStateMessage message = new HueWriteStateMessage(light, state);
		
		t.pushMessage(message);

		return true;
	}
	
	private List<HueLight> parseLights(JSONObject result) {
		List<HueLight> lights = new LinkedList<HueLight>();
		@SuppressWarnings("unchecked")
		Iterator<String> iKeys = result.keys();
		while(iKeys.hasNext()){
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
		return lights;
	}
	
	private void readConfig() {
		user = config.getBridgeUser();
		urlBase = config.getBridgeUrlBase();
	}

	public void callback(Object source) {
		// TODO Auto-generated method stub
		
	}
}
