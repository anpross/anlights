package my.anlights;

import java.util.UUID;

public class Constants {

	public static final String LOGGING_TAG = "AnLight";

	public static final String DESC_MODEL_NAME = "modelName";
	public static final String DESC_UDN = "UDN";
	public static final String DESC_URL_BASE = "URLBase";

	public static final String[] COMPATIBLE_MODELS = {"Philips hue bridge 2012"};

	public static final String APPLICATION_NAME = "AnLight Client";

	public static final UUID PEBBLE_UUID = UUID.fromString("EC555866-504F-41C0-91E0-D7EA491F0FEA");
	//public static final String BRIDGE_USER = "anlight223456";

	public static final int REGISTRATION_DURATION_S = 15;
	public static final int REGISTRATION_INTERVAL_MS = 1000;
}
