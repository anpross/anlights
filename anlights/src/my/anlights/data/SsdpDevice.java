package my.anlights.data;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.DatagramPacket;

import my.anlights.util.MyLog;

public class SsdpDevice {

	private DatagramPacket packet;

	private String location;
	private String server;
	private String host;
	private static final String LABLE_LOCATION = "LOCATION";
	private static final String LABLE_SERVER = "SERVER";
	private static final String LABLE_HOST = "HOST";

	private static final String DELIMITER = ":";

	private static final String HUE_TEST_STRING = "FreeRTOS";

	private static final String CLASS_NAME = SsdpDevice.class.getCanonicalName();

	public SsdpDevice(DatagramPacket packet) {
		this.packet = packet;

		try {
			parsePacket();
		} catch (IOException e) {
			MyLog.e("problem parsing DatagramPacket to SSDP Device", e);
		}
	}

	private void parsePacket() throws IOException {
		MyLog.entering(CLASS_NAME, "parsePacket");
		ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData());
		LineNumberReader lnr = new LineNumberReader(new InputStreamReader(bais));
		String currLine = lnr.readLine();
		while (currLine != null) {
			int delPos = currLine.indexOf(DELIMITER);
			if (delPos > 0) {
				String lable = currLine.substring(0, delPos);
				String value = currLine.substring(delPos + 1).trim();
				if (lable.equals(LABLE_HOST)) {
					host = value;
				} else if (lable.equals(LABLE_LOCATION)) {
					location = value;
				} else if (lable.equals(LABLE_SERVER)) {
					server = value;
				}
				MyLog.d(lable + "->" + value);
			}
			currLine = lnr.readLine();
		}
		MyLog.exiting(CLASS_NAME, "parsePacket");
	}

	public DatagramPacket getPacket() {
		return packet;
	}

	public String getLocation() {
		return location;
	}

	public String getServer() {
		return server;
	}

	public String getHost() {
		return host;
	}

	public boolean isHueBridge() {
		boolean isHue = false;
		if (server.startsWith(HUE_TEST_STRING)) {
			isHue = true;
		}
		return isHue;
	}
}
