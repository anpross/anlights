package my.anlights.data;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import my.anlights.util.MyLog;

/**
 * Created by Andreas on 09.06.13.
 */
public class HueConfig {

	private List<HueUser> users;
    private String CLASS_NAME = HueConfig.class.getCanonicalName();

    public HueConfig() {
        users = new LinkedList<HueUser>();

    }

    public List<HueUser> getUsers() {
		return users;
	}

	public void updateConfigStatus(JSONObject obj){
        MyLog.entering(CLASS_NAME, "updateConfigStatus", obj);

        try {
            JSONObject whitelist = obj.getJSONObject("whitelist");
            Iterator iWhitelist = whitelist.keys();
            MyLog.d("object:" + whitelist);

            while (iWhitelist.hasNext()) {
                String currKey = (String) iWhitelist.next();
                JSONObject currUser = (JSONObject) whitelist.get(currKey);

                HueUser newUser = new HueUser();
                newUser.updateHueUser(currKey, currUser);
                users.add(newUser);
            }
        } catch (JSONException e1) {
            MyLog.e("problem parsing HueConfig JSON", e1);
        }
    }

}
