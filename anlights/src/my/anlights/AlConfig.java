package my.anlights;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import my.anlights.util.UserNameGenerator;


public class AlConfig {

	private static AlConfig config;
	private SharedPreferences sp;

	private String bridgeUser;
	private String bridgeUrlBase;
	private String lastBridgeLocation;

	private AlConfig(Context context) {
		sp = PreferenceManager.getDefaultSharedPreferences(context);

		bridgeUser = sp.getString("bridgeUser_preference", UserNameGenerator.generateUserName());

		// store in case there was none before.
		setBridgeUser(bridgeUser);

		bridgeUrlBase = sp.getString("bridgeUrlBase_preference", null);
		lastBridgeLocation = sp.getString("bridgeLastLocation_preference", null);
	}

	public static AlConfig getInstance(Context context) {
		if (config == null) {
			config = new AlConfig(context);
		}
		return config;
	}

	public static synchronized AlConfig getExistingInstance() {
		if (config == null) throw new IllegalStateException("AlConfig instance is null - it needs to be created first");
		return config;
	}

	public void setBridgeUser(String bridgeUser) {
		sp.edit().putString("bridgeUser_preference", bridgeUser).apply();
		this.bridgeUser = bridgeUser;
	}

	public void setBridgeUrlBase(String bridgeUrlBase) {
		sp.edit().putString("bridgeUrlBase_preference", bridgeUrlBase).apply();
		this.bridgeUrlBase = bridgeUrlBase;
	}

	public void setLastBridgeLocation(String lastBridgeLocation) {
		sp.edit().putString("bridgeLastLocation_preference", lastBridgeLocation).apply();
		this.lastBridgeLocation = lastBridgeLocation;
	}

	public String getBridgeUser() {
		return bridgeUser;
	}

	public String getBridgeUrlBase() {
		return bridgeUrlBase;
	}

	public String getLastBridgeLocation() {
		return lastBridgeLocation;
	}


}
