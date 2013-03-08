package anlights.data;

import org.json.JSONException;
import org.json.JSONObject;


import android.util.Log;
import anlights.Constants;


public class HueLight {

	private static final String TAG = Constants.LOGGING_TAG;
	// using all objects to have a null state
	private String id; 
	private String name;
	private HueState state;
	private String type;
	private String modelId;
	private String swVersion;
	private PointSymbol pointSymbol; //no clue what that is for
	
	private HueState oldState;
	
	private HueBridge bridge;
	
	
	public HueLight(){
		state = new HueState();
	}
	
	public HueLight(String id, String name, HueBridge bridge){
		this.id = id;
		this.name = name;
		this.bridge = bridge;
		state = new HueState();
	}
	
	@Override
	public String toString() {
		return "HueLight[id:"+id+", name:"+name+", state:"+state+"]";
	}
	
	public String getId() {
		return id;
	}
	
	
	public HueState getState() {
		return state;
	}

	public void setState(HueState state) {
		this.state = state;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	public void update(HueState state) {
		bridge.writeLightState(this, state);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getModelId() {
		return modelId;
	}

	public void setModelId(String modelId) {
		this.modelId = modelId;
	}

	public String getSwVersion() {
		return swVersion;
	}

	public void setSwVersion(String swVersion) {
		this.swVersion = swVersion;
	}

	public PointSymbol getPointSymbol() {
		return pointSymbol;
	}

	public void setPointSymbol(PointSymbol pointSymbol) {
		this.pointSymbol = pointSymbol;
	}

	public void pushLightStatus(HueState newState) {
		Log.d(TAG,"refreshLightStatus("+newState+")");
		if(state == null) {
			state = newState;
			update(newState);
		} else {
			oldState = new HueState(newState);
			HueState deltaState = HueState.getDeltaState(oldState, newState);
			update(deltaState);
		}
		
	}

	/**
	 * updates the status in the light bean
	 * @param jLight
	 */
	private void updateLightStatus(JSONObject jLight) {
		try {
			JSONObject jState = jLight.getJSONObject("state");

			state.setOn(jState.getBoolean("on"));
			state.setBri(jState.getInt("bri"));

			String colorMode = jState.getString("colormode");
			
			if(colorMode.equalsIgnoreCase(HueState.MODE_LABLE_CT)) {
				state.setCt(jState.getInt("ct"));
			} else if (colorMode.equalsIgnoreCase(HueState.MODE_LABLE_HS)) {
				state.setHue(jState.getInt("hue"));
				state.setSat(jState.getInt("sat"));
			} else if (colorMode.equalsIgnoreCase(HueState.MODE_LABLE_XY)) {
				state.setXy(new XY(jState.getJSONArray("xy")));
			}
			type = jLight.getString("type");
			name = jLight.getString("name");
			modelId = jLight.getString("modelid");
			swVersion = jLight.getString("swversion");
			pointSymbol = new PointSymbol(jLight.getJSONObject("pointsymbol"));
			
		} catch (JSONException e) {
			Log.e(TAG,"problem updating light status",e);
		}
	}
	
	public void readLightStatus(){

		JSONObject lightJson = bridge.readLightState(this);
		updateLightStatus(lightJson);
	}

}

class PointSymbol {
	
	String value1;	
	String value2;
	String value3;
	String value4;
	String value5;
	String value6;
	String value7;
	String value8;
	
	public PointSymbol(JSONObject pointSymbol) {
		try {
			value1 = pointSymbol.getString("1");
			value2 = pointSymbol.getString("2");
			value3 = pointSymbol.getString("3");
			value4 = pointSymbol.getString("4");
			value5 = pointSymbol.getString("5");
			value6 = pointSymbol.getString("6");
			value7 = pointSymbol.getString("7");
			value8 = pointSymbol.getString("8");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public String getValue1() {
		return value1;
	}
	public void setValue1(String value1) {
		this.value1 = value1;
	}
	public String getValue2() {
		return value2;
	}
	public void setValue2(String value2) {
		this.value2 = value2;
	}
	public String getValue3() {
		return value3;
	}
	public void setValue3(String value3) {
		this.value3 = value3;
	}
	public String getValue4() {
		return value4;
	}
	public void setValue4(String value4) {
		this.value4 = value4;
	}
	public String getValue5() {
		return value5;
	}
	public void setValue5(String value5) {
		this.value5 = value5;
	}
	public String getValue6() {
		return value6;
	}
	public void setValue6(String value6) {
		this.value6 = value6;
	}
	public String getValue7() {
		return value7;
	}
	public void setValue7(String value7) {
		this.value7 = value7;
	}
	public String getValue8() {
		return value8;
	}
	public void setValue8(String value8) {
		this.value8 = value8;
	}

}