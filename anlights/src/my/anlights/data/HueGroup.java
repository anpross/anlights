package my.anlights.data;

import java.util.LinkedList;
import java.util.List;

import my.anlights.util.MyLog;


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

                commonState.setOn(nullIfNotEqual(currState.isOn(), commonState.isOn()));
                commonState.setBri(nullIfNotEqual(currState.getBri(), commonState.getBri()));
                commonState.setHue(nullIfNotEqual(currState.getHue(), commonState.getHue()));
                commonState.setSat(nullIfNotEqual(currState.getSat(), commonState.getSat()));
            }
        }

        MyLog.exiting(CLASS_NAME, "getLightState", commonState);
        return commonState;
    }

    private <T> T nullIfNotEqual(T value1, T value2) {
        if (value1 != null && value2 != null && value1.equals(value2)) {
            return value1;
        } else {
            return null;
        }

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

    /**
     * really only needed for serialization
     *
     * @return
     */
    @Deprecated
    public List<HueLight> getLights() {
        return lights;
    }


}
