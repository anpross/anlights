package my.anlights;

import my.anlights.data.HueBridge;
import my.anlights.data.HueGroup;
import my.anlights.data.HueLight;
import my.anlights.data.HueState;
import my.anlights.util.MyLog;

import java.util.List;

/**
 * Created by Andreas on 20.05.13.
 */
public class UpdateHueAction implements CallbackListener {

	HueState newState;

	private static final String CLASS_NAME = UpdateHueAction.class.getCanonicalName();

	public void updateHue(HueState newState) {
		this.newState = newState;
		HueDiscoveryTask discovery = new HueDiscoveryTask();
		discovery.setCallback(this);
		discovery.execute();
	}

	@Override
	public void callback(Object source) {
		MyLog.entering(CLASS_NAME, "callback", source);
		if (source instanceof HueDiscoveryTask) {
			HueDiscoveryTask discovery = (HueDiscoveryTask) source;
			MyLog.d("discover done - base url:" + AlConfig.getExistingInstance().getBridgeUrlBase());

			HueBridge bridge = discovery.getBridge();

			if (bridge.isConnected()) {
				List<HueLight> lights = bridge.getLightNames();

				HueGroup hGroup = new HueGroup();
				for (HueLight currLight : lights) {
					hGroup.addLight(currLight);
				}
				hGroup.setLightState(newState);
			}
		}
		MyLog.exiting(CLASS_NAME, "callback");
	}
}
