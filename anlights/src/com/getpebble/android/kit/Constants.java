package com.getpebble.android.kit;

/**
 * Constant values used by Pebble-enabled kit applications.
 *
 * @author zulak@getpebble.com
 */
public final class Constants {

	/**
	 * Intent broadcast by pebble.apk when a new connection to a Pebble is established.
	 */
	public static final String INTENT_PEBBLE_CONNECTED = "com.getpebble.action.PEBBLE_CONNECTED";
	/**
	 * Intent broadcast by pebble.apk when the connection to a Pebble is closed or lost.
	 */
	public static final String INTENT_PEBBLE_DISCONNECTED = "com.getpebble.action.PEBBLE_DISCONNECTED";

	/**
	 * Intent broadcast to pebble.apk to indicate that a message was received from the watch. To avoid protocol timeouts
	 * on the watch, applications <em>must</em> ACK or NACK all received messages.
	 */
	public static final String INTENT_APP_ACK = "com.getpebble.action.app.ACK";

	/**
	 * Intent broadcast to pebble.apk to indicate that a message was unsuccessfully received from the watch.
	 */
	public static final String INTENT_APP_NACK = "com.getpebble.action.app.NACK";

	/**
	 * Intent broadcast from pebble.apk containing one-or-more key-value pairs sent from the watch to the phone.
	 */
	public static final String INTENT_APP_RECEIVE = "com.getpebble.action.app.RECEIVE";

	/**
	 * Intent broadcast to pebble.apk containing one-or-more key-value pairs to be sent to the watch from the phone.
	 */
	public static final String INTENT_APP_SEND = "com.getpebble.action.app.SEND";

	/**
	 * Intent broadcast to pebble.apk responsible for launching a watch-app on the connected watch. This intent is
	 * idempotent.
	 */
	public static final String INTENT_APP_START = "com.getpebble.action.app.START";

	/**
	 * Intent broadcast to pebble.apk responsible for closing a running watch-app on the connected watch. This intent is
	 * idempotent.
	 */
	public static final String INTENT_APP_STOP = "com.getpebble.action.app.STOP";

	/**
	 * Intent broadcast to pebble.apk responsible for customzing the name and icon of the 'stock' Sports and Golf
	 * applications included in the watch's firmware.
	 */
	public static final String INTENT_APP_CUSTOMIZE = "com.getpebble.action.app.CONFIGURE";

	/**
	 * The bundle-key used to store a message's transaction id.
	 */
	public static final String TRANSACTION_ID = "transaction_id";
	/**
	 * The bundle-key used to store a message's UUID.
	 */
	public static final String APP_UUID = "uuid";
	/**
	 * The bundle-key used to store a message's JSON payload send-to or received-from the watch.
	 */
	public static final String MSG_DATA = "msg_data";
	/**
	 * The bundle-key used to store the type of application being customized in a CUSTOMIZE intent.
	 */
	public static final String CUST_APP_TYPE = "app_type";
	/**
	 * The bundle-key used to store the custom name provided in a CUSTOMIZE intent.
	 */
	public static final String CUST_NAME = "name";
	/**
	 * The bundle-key used to store the custom icon provided in a CUSTOMIZE intent.
	 */
	public static final String CUST_ICON = "icon";


	private Constants() {

	}

	public static enum PebbleAppType {
		SPORTS(0x00),
		GOLF(0x01),
		OTHER(0xff);

		public final int ord;

		private PebbleAppType(final int ord) {
			this.ord = ord;
		}
	}
}
