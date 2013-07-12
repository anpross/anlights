package my.anlights.data;

import android.os.BadParcelableException;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import my.anlights.util.MyLog;

public class HueLight extends HueObject implements Parcelable {

	// using all objects to have a null state
	private String id;
	private String name;
	private HueState state;
	private String type;
	private String modelId;
	private String swVersion;
	private PointSymbol pointSymbol; //no clue what that is for

	private HueBridge bridge;

	private static final String CLASS_NAME = HueLight.class.getCanonicalName();

	@SuppressWarnings("UnusedDeclaration")
	public static final Parcelable.Creator<HueLight> CREATOR = new Parcelable.Creator<HueLight>() {
		public HueLight createFromParcel(Parcel in) {
			return new HueLight(in);
		}

		public HueLight[] newArray(int size) {
			return new HueLight[size];
		}
	};

	public HueLight() {
		MyLog.entering(CLASS_NAME, "HueLight");
		state = new HueState();
		MyLog.exiting(CLASS_NAME, "HueLight");
	}

	public HueLight(Parcel in) {
		try {
			MyLog.d("reading id string");
			id = in.readString();
			MyLog.d("reading name string");
			name = in.readString();
			MyLog.d("reading state parcelable");
			state = in.readParcelable(HueState.class.getClassLoader());
			MyLog.d("reading type string");
			type = in.readString();
			MyLog.d("read modelID string");
			modelId = in.readString();
			MyLog.d("reading swVersion string");
			swVersion = in.readString();
			MyLog.d("reading point parcelable");
			pointSymbol = in.readParcelable(PointSymbol.class.getClassLoader());

		} catch (BadParcelableException e) {
			MyLog.e("old State was no good", e);
		}
	}

	/**
	 * only use for deserialization
	 *
	 * @param bridge
	 */
	@Deprecated
	public void setBridge(HueBridge bridge) {
		this.bridge = bridge;
	}

	public HueLight(String id, String name, HueBridge bridge) {
		MyLog.entering(CLASS_NAME, "HueLight", id, name, bridge);
		this.id = id;
		this.name = name;
		this.bridge = bridge;
		state = new HueState();
		MyLog.exiting(CLASS_NAME, "HueLight");
	}

	@Override
	public String toString() {
		return "HueLight[id:" + id + ", name:" + name + ", state:" + state + "]";
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
		MyLog.d("refreshLightStatus(" + newState + ")");
		if (state == null) {
			state = newState;
			update(newState);
		} else {
			HueState oldState = new HueState(newState);
			HueState deltaState = HueState.getDeltaState(oldState, newState);
			update(deltaState);
		}

	}

	/**
	 * updates the status in the light bean
	 *
	 * @param jLight
	 */
	private void updateLightStatus(JSONObject jLight) throws HueException {
		HueError error = checkForError(jLight);
		if (error != null) {
			throw new HueException(error);
		}

		try {
			JSONObject jState = jLight.getJSONObject("state");

			state.setOn(jState.getBoolean("on"));
			state.setBri(jState.getInt("bri"));

			String colorMode = jState.getString("colormode");

			if (colorMode.equalsIgnoreCase(HueState.MODE_LABLE_CT)) {
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
			MyLog.e("problem updating light status", e);
		}
	}

	public void readLightStatus() throws HueException {

		JSONObject lightJson = bridge.readLightState(this);
		updateLightStatus(lightJson);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int i) {
		MyLog.d("write id string");
		parcel.writeString(id);
		MyLog.d("write name string");
		parcel.writeString(name);
		MyLog.d("write state parcelable");
		parcel.writeParcelable(state, i);
		MyLog.d("write type string");
		parcel.writeString(type);
		MyLog.d("write modelId string");
		parcel.writeString(modelId);
		MyLog.d("write swVersion string");
		parcel.writeString(swVersion);
		MyLog.d("write pointSymbol parcelable");
		parcel.writeParcelable(pointSymbol, i);
	}
}

class PointSymbol implements Parcelable {

	String value1;
	String value2;
	String value3;
	String value4;
	String value5;
	String value6;
	String value7;
	String value8;

	private static final String CLASS_NAME = PointSymbol.class.getCanonicalName();

	@SuppressWarnings("UnusedDeclaration")
	public static final Parcelable.Creator<PointSymbol> CREATOR = new Parcelable.Creator<PointSymbol>() {
		public PointSymbol createFromParcel(Parcel in) {
			return new PointSymbol(in);
		}

		public PointSymbol[] newArray(int size) {
			return new PointSymbol[size];
		}
	};

	public PointSymbol(Parcel in) {
		MyLog.entering(CLASS_NAME, "PointSymbol", in);
		value1 = in.readString();
		value2 = in.readString();
		value3 = in.readString();
		value4 = in.readString();
		value5 = in.readString();
		value6 = in.readString();
		value7 = in.readString();
		value8 = in.readString();
		MyLog.exiting(CLASS_NAME, "PointSymbol");
	}

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

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int i) {
		parcel.writeString(value1);
		parcel.writeString(value2);
		parcel.writeString(value3);
		parcel.writeString(value4);
		parcel.writeString(value5);
		parcel.writeString(value6);
		parcel.writeString(value7);
		parcel.writeString(value8);
	}
}