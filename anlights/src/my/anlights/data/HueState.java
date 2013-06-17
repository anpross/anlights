package my.anlights.data;

import android.os.Parcel;
import android.os.Parcelable;
import my.anlights.Constants;
import my.anlights.util.MyLog;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HueState implements Parcelable {

	private Boolean on = null;
	private Integer bri = null;
	private Integer hue = null;
	private Integer sat = null;
	private XY xy = null;
	private Integer ct = null;
	private String alert = null; //need possible values, convert to int then
	private String effect = null; //need possible values, convert to int then
	private int colorMode = 0; //need possible values, convert to int then
	private Boolean reachable = null;

	private static final String TAG = Constants.LOGGING_TAG;

	public static final int MODE_CT = 0;
	public static final int MODE_HS = 1;
	public static final int MODE_XY = 2;
	public static final String MODE_LABLE_CT = "ct";
	public static final String MODE_LABLE_HS = "hs";
	public static final String MODE_LABLE_XY = "xy";

	private static final String CLASS_NAME = HueState.class.getCanonicalName();

	public HueState() {
	}

	/**
	 * copy constructor
	 *
	 * @param otherState the template state
	 */
	public HueState(HueState otherState) {
		this.on = otherState.on;

		this.bri = otherState.bri;

		this.hue = otherState.hue;
		this.sat = otherState.sat;

		this.xy = otherState.xy;

		this.ct = otherState.ct;

		this.alert = otherState.alert;
		this.effect = otherState.effect;
		this.colorMode = otherState.colorMode;

		this.reachable = otherState.reachable;
	}

	@SuppressWarnings("UnusedDeclaration")
	public static final Parcelable.Creator<HueState> CREATOR
			= new Parcelable.Creator<HueState>() {
		public HueState createFromParcel(Parcel in) {
			return new HueState(in);
		}

		public HueState[] newArray(int size) {
			return new HueState[size];
		}
	};

	private HueState(Parcel in) {
		on = (Boolean) in.readValue(null);
		bri = (Integer) in.readValue(null);
		hue = (Integer) in.readValue(null);
		sat = (Integer) in.readValue(null);
		xy = (XY) in.readValue(null);
		ct = (Integer) in.readValue(null);
		alert = in.readString();
		effect = in.readString();
		colorMode = in.readInt();
		reachable = (Boolean) in.readValue(null);
	}

	public Boolean isOn() {
		return on;
	}

	public void setOn(Boolean on) {
		this.on = on;
	}

	public Integer getBri() {
		return bri;
	}

	public void setBri(Integer bri) {
		this.bri = bri;
		this.ct = null;
		this.xy = null;
	}

	public Integer getHue() {
		return hue;
	}

	public void setHue(Integer hue) {
		this.hue = hue;
		this.ct = null;
		this.xy = null;
		this.colorMode = MODE_HS;
	}

	public Integer getSat() {
		return sat;
	}

	public void setSat(Integer sat) {
		this.sat = sat;
		this.colorMode = MODE_HS;
	}

	public XY getXy() {
		return xy;
	}

	public void setXy(XY xy) {
		this.xy = xy;
		this.ct = null;
		this.hue = null;
		this.sat = null;
		this.colorMode = MODE_XY;
	}

	public Integer getCt() {
		return ct;
	}

	public void setCt(Integer ct) {
		this.ct = ct;
		this.sat = null;
		this.hue = null;
		this.xy = null;
		this.colorMode = MODE_CT;
	}

	public String getAlert() {
		return alert;
	}

	public void setAlert(String alert) {
		this.alert = alert;
	}

	public String getEffect() {
		return effect;
	}

	public void setEffect(String effect) {
		this.effect = effect;
	}

	public int getColorMode() {
		return colorMode;
	}

	public void setColorMode(String sColorMode) {
		if (sColorMode.equalsIgnoreCase(MODE_LABLE_CT)) {
			this.colorMode = MODE_CT;
		} else if (sColorMode.equalsIgnoreCase(MODE_LABLE_HS)) {
			this.colorMode = MODE_HS;
		} else if (sColorMode.equalsIgnoreCase(MODE_LABLE_XY)) {
			this.colorMode = MODE_XY;
		}
	}

	public Boolean getReachable() {
		return reachable;
	}

	public JSONObject toJsonObject() {
		MyLog.entering(CLASS_NAME, "toJsonObject");
		JSONObject jLight = new JSONObject();
		try {
			if (this.isOn() != null) jLight.put("on", this.isOn());
			if (this.getBri() != null) jLight.put("bri", this.getBri());
			if (this.getHue() != null) jLight.put("hue", this.getHue());
			if (this.getSat() != null) jLight.put("sat", this.getSat());
			if (this.getCt() != null) jLight.put("ct", this.getCt());
			if (this.getXy() != null) jLight.put("xy", this.getXy().toJSONArray());
		} catch (JSONException e) {
			MyLog.e("problem converting light to JSON", e);
		}
		MyLog.exiting(CLASS_NAME, "toJsonObject", jLight);
		return jLight;
	}

	public String toString() {
		return "HueState[on:" + isOn() + ", bri:" + getBri() + ", hue:" + getHue() + ", sat:" + getSat() + "]";
	}

	public static HueState getDeltaState(HueState oldState, HueState newState) {
		MyLog.entering(CLASS_NAME, "getDeltaState", oldState, newState);
		HueState resultState = new HueState();
		MyLog.d("getDeltaState needs to be implemented");
		boolean stateChange = false;

		resultState = newState;

		// warum wollte ich nochmal n delta update?

//		if(hLight.state.isOn() != null){
//			setStateOn(hLight.isStateOn());
//		}
//		setIfNotNull(hLight.isStateOn(), this.isStateOn());
//		setIfNotNull(hLight.getStateBri(), this.getStateBri());
//		setIfNotNull(hLight.getStateHue(), this.getStateHue());
//		setIfNotNull(hLight.getStateSat(), this.getStateSat());
//
//		setIfNotNull(hLight.getStateXy(), this.getStateXy());
//
//		setIfNotNull(hLight.getType(), this.getType());
//		setIfNotNull(hLight.getName(), this.getName());
//		setIfNotNull(hLight.getModelId(), this.getModelId());
//		setIfNotNull(hLight.getSwVersion(), this.getSwVersion());
//		setIfNotNull(hLight.getPointSymbol(), this.getPointSymbol());

		MyLog.exiting(CLASS_NAME, "getDeltaState", resultState);
		return resultState;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int i) {

		parcel.writeValue(on);
		parcel.writeValue(bri);
		parcel.writeValue(hue);
		parcel.writeValue(sat);
		parcel.writeValue(xy);
		parcel.writeValue(ct);
		parcel.writeValue(alert);
		parcel.writeValue(effect);
		parcel.writeInt(colorMode);
		parcel.writeValue(reachable);
	}
}

class XY {
	Integer x;
	Integer y;

	public XY(Integer x, Integer y) {
		this.x = x;
		this.y = y;
	}

	public XY(JSONArray xy) {
		try {
			this.x = xy.getInt(0);
			this.y = xy.getInt(1);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public Integer getX() {
		return x;
	}

	public void setX(Integer x) {
		this.x = x;
	}

	public JSONArray toJSONArray() {
		JSONArray array = new JSONArray();
		array.put(x);
		array.put(y);
		return array;
	}

	public Integer getY() {
		return y;
	}

	public void setY(Integer y) {
		this.y = y;
	}
}