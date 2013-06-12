package my.anlights.data;

import my.anlights.util.MyLog;

import java.util.LinkedList;
import java.util.List;


public class HueGroup {

	private static final String CLASS_NAME = HueGroup.class.getCanonicalName();
	private List<HueLight> lights;

	public HueGroup() {
		lights = new LinkedList<HueLight>();
	}

	public void addLight(HueLight light) {
		lights.add(light);
	}

	public void setLightState(HueState state) {
		for (HueLight currLight : lights) {
			currLight.pushLightStatus(state);
		}
	}

	/**
	 * returns a new HueState with all fields that are common to the group members
	 *
	 * @return
	 */
	public HueState getLightState() {
		MyLog.entering(CLASS_NAME, "getLightState");

		HueState commonState = null;

		for (HueLight currLight : lights) {
			HueState currState = currLight.getState();
			assert currState != null;
			if (commonState == null) { // first light
				// copy the state
				commonState = new HueState(currState);
			} else { // reset if different value
				if (commonState.isOn() != null && currState.isOn() != commonState.isOn()) commonState.setOn(null);
				if (commonState.getBri() != null && (!currState.getBri().equals(commonState.getBri())))
					commonState.setBri(null);
				if (commonState.getHue() != null && (!currState.getHue().equals(commonState.getHue())))
					commonState.setHue(null);
				if (commonState.getSat() != null && (!currState.getSat().equals(commonState.getSat())))
					commonState.setSat(null);
			}
		}

		MyLog.exiting(CLASS_NAME, "getLightState", commonState);
		return commonState;
	}

	/**
	 * reading the current light status
	 */
	public void readLightStatus() throws HueException {
		MyLog.entering(CLASS_NAME, "readLightStatus");
		for (HueLight currLight : lights) {
			currLight.readLightStatus();
		}
		MyLog.exiting(CLASS_NAME, "readLightStatus");
	}


}
