package my.anlights;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import my.anlights.data.HueBridge;
import my.anlights.data.HueGroup;
import my.anlights.data.HueLight;
import my.anlights.data.HueState;

import java.util.List;

/**
 * Created by Andreas on 20.05.13.
 */
public class UpdateHueAction implements CallbackListener{

    HueState newState;

    public void updateHue(HueState newState) {
        this.newState = newState;
        HueDiscoveryTask discovery = new HueDiscoveryTask();
        discovery.setCallback(this);
        discovery.execute();
    }

    @Override
    public void callback(Object source) {
        if (source instanceof HueDiscoveryTask) {
            HueDiscoveryTask discovery = (HueDiscoveryTask) source;
            Log.d("TAG", "discover done - base url:" + AlConfig.getExistingInstance().getBridgeUrlBase());

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
        //finish();
    }
}
