package my.anlights;

import android.os.AsyncTask;

import java.util.List;

import my.anlights.data.HueBridge;
import my.anlights.data.HueUser;
import my.anlights.data.messages.HueDeleteUserMessage;
import my.anlights.util.MyLog;

/**
 * Created by Andreas on 16.06.13.
 * <p/>
 * TODO:
 * - two modes:
 * - remove ALL whitelist items with our APPLICATION_NAME
 * - remove only the ones not used for x days (0 days = mode 1)
 */
public class HueUserCleanupTask extends AsyncTask<Void, Void, Void> {

    private HueBridge bridge;
    private static String CLASS_NAME = HueUserCleanupTask.class.getCanonicalName();

	HueThread thread = new HueThread();

	public void setBridge(HueBridge bridge) {
        this.bridge = bridge;
    }

    @Override
    protected Void doInBackground(Void... voids) {
	    String activeUserId = AlConfig.getExistingInstance().getBridgeUser();
	    MyLog.entering(CLASS_NAME, "doInBackground");
        List<HueUser> hueUsers = bridge.getBridgeConfig().getUsers();
        for (HueUser currUser : hueUsers) {
            MyLog.d("user:" + currUser.getId() + "; " + currUser.getName());
            if (currUser.getName().equals(Constants.APPLICATION_NAME)) {
                MyLog.d("try to delete user:" + currUser.getId());

	            // delete the active user last
	            if (!currUser.getId().equals(activeUserId)) {
		            // do DELETE on /api/<username>/config/whitelist/{username
		            HueDeleteUserMessage message = new HueDeleteUserMessage(currUser.getId());
		            thread.pushMessage(message);
	            }
            }
        }

	    // delete the active user last
	    HueDeleteUserMessage message = new HueDeleteUserMessage(activeUserId);
	    thread.pushMessage(message);

	    MyLog.exiting(CLASS_NAME, "doInBackground");
        return null;
    }
}
