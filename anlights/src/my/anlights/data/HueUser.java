package my.anlights.data;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import my.anlights.util.MyLog;

/**
 * Created by Andreas on 09.06.13.
 */
public class HueUser {

    private static String HUE_TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    private static String CLASS_NAME = HueUser.class.getCanonicalName();

    private Date lastUseDate;
    private Date createdDate;
    private String name;
    private String id;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getLastUseDate() {
        return lastUseDate;
    }

    public void setLastUseDate(Date lastUseDate) {
        this.lastUseDate = lastUseDate;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void updateHueUser(String key, JSONObject obj) throws JSONException {
        MyLog.entering(CLASS_NAME, "updateHueUser", obj);
        this.id = key;
        SimpleDateFormat sdf = new SimpleDateFormat(HUE_TIMESTAMP_FORMAT);
        try {
            this.lastUseDate = sdf.parse(obj.getString("last use date"));
            this.createdDate = sdf.parse(obj.getString("create date"));
        } catch (ParseException e) {
            MyLog.e("problem parsing timestamps for HueUser", e);
        }
        this.name = obj.getString("name");
        MyLog.exiting(CLASS_NAME, "updateHueUser");
    }

}
