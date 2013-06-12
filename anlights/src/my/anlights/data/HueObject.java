package my.anlights.data;

import my.anlights.util.MyLog;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Andreas on 09.06.13.
 */
public class HueObject {

	protected HueError checkForError(JSONObject obj) {
		HueError error = null;
		try {
			JSONObject errorObj = obj.getJSONObject("error");
			if (errorObj != null) {
				int type = errorObj.getInt("type");
				String address = errorObj.getString("address");
				String desc = errorObj.getString("description");
				error = new HueError(type, address, desc);
			}
		} catch (JSONException e) {
			MyLog.e("problem reading error json", e);
		}
		return error;
	}
}
