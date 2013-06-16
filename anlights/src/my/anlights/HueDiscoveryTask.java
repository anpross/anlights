package my.anlights;

import android.os.AsyncTask;
import my.anlights.data.HueBridge;
import my.anlights.data.SsdpDevice;
import my.anlights.util.MyLog;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * to debug using wireshark, use filter "udp.port eq 1900"
 * <p/>
 * discovery of a HueBridge folows these steps: (happy case)
 * - send SSDP broadcast and wait for responses
 * - read the first one
 * - check if SERVER header matches the one used by Hue
 * - use the LOCATION header to read the description.xml from Hue
 * - check if this matches the the supported ones and read UDN for multi-bridge support
 *
 * @author Andreas
 */
public class HueDiscoveryTask extends AsyncTask<Void, Void, HueBridge> {

	private static final int DISCOVERY_TIMEOUT = 2000;
	private static final int DISCOVERY_PORT = 1900;
	private static final String DISCOVERY_IP = "239.255.255.250";

	private DefaultHttpClient httpClient = null;
	private XMLReader xmlReader = null;
	private CallbackListener callback;

	private HueBridge bridge;

	private static final String CLASS_NAME = HueDiscoveryTask.class.getCanonicalName();

	public void setCallback(CallbackListener callback) {
		this.callback = callback;
	}

	private static final String DISCOVERY_BROADCAST = "M-SEARCH * HTTP/1.1\r\n" +
			"HOST: " + DISCOVERY_IP + ":" + DISCOVERY_PORT + "\r\n" +
			"MAN: ssdp:discover\r\n" +
			"MX: " + (DISCOVERY_TIMEOUT / 1000) + "\r\n" +
			"ST: ssdp:all\r\n\r\n";


	/**
	 * @return the first gateway found or null if timeout was reached.
	 */
	@Override
	protected HueBridge doInBackground(Void... callback) {
		MyLog.entering(CLASS_NAME, "doInBackground", callback);
		HueBridge hue = null;

		String location = AlConfig.getExistingInstance().getLastBridgeLocation();
		if (location != null) {
			MyLog.i("trying old bridge location:" + location);
			hue = readDescriptionXml(location);
		}

		if (hue == null) {
			MyLog.i("not found at old location, running ssdp discovery");
			String newLocation = null;
			try {
				newLocation = doSsdpDiscovery();
			} catch (Exception e) {
				MyLog.e("problem during SSDP discovery", e);
			}
			MyLog.i("found bridge at:" + newLocation);
			AlConfig.getExistingInstance().setLastBridgeLocation(newLocation);
			if (newLocation != null) {
				hue = readDescriptionXml(newLocation);
			} else {
				MyLog.i("problem reading bridge location");
			}
		} else {
			MyLog.i("old location was good");
		}
		if (hue != null) {
			MyLog.d("is Supported Device:" + hue.isSupported() + " udn:" + hue.getUdn());
		}
		return hue;
	}

	private String doSsdpDiscovery() throws Exception {
		MyLog.entering(CLASS_NAME, "doSsdpDiscovery");
		SsdpDevice bridge = null;
		String location = null;
		final DatagramSocket socket = new DatagramSocket(DISCOVERY_PORT);
		final DatagramPacket reply;
		final byte[] replyBuffer;
		socket.setSoTimeout(DISCOVERY_TIMEOUT);

		boolean foundHue = false;
		try {
			MyLog.d("sending request at " + getCurrentTS());
			socket.send(getHueSearchPackage());

			replyBuffer = new byte[1000];
			reply = new DatagramPacket(replyBuffer, replyBuffer.length);

			while (!foundHue) {
				socket.receive(reply);
				bridge = new SsdpDevice(reply);
				if (bridge.isHueBridge()) {
					foundHue = true;
				}
			}
			location = bridge.getLocation();

		} finally {
			MyLog.d("socket closed at " + getCurrentTS());
			socket.close();
		}

		MyLog.exiting(CLASS_NAME, "doSsdpDiscovery", location);
		return location;
	}

	@Override
	protected void onPostExecute(HueBridge result) {
		MyLog.entering(CLASS_NAME, "onPostExecute", result);
		this.bridge = result;
		super.onPostExecute(result);
		AlConfig.getExistingInstance().setBridgeUrlBase(result.getUrlBase());
		callback.callback(this);
		MyLog.exiting(CLASS_NAME, "onPostExecute");
	}

	/**
	 * generates a search message as per http://burgestrand.github.com/hue-api/api/discovery/
	 *
	 * @return
	 */
	private DatagramPacket getHueSearchPackage() throws UnknownHostException {
		MyLog.entering(CLASS_NAME, "getHueSearchPackage");
		MyLog.d("sending packet:\n" + DISCOVERY_BROADCAST);
		MyLog.d("  to: " + InetAddress.getByName(DISCOVERY_IP));
		DatagramPacket packet = new DatagramPacket(
				DISCOVERY_BROADCAST.getBytes(),
				DISCOVERY_BROADCAST.length(),
				InetAddress.getByName(DISCOVERY_IP),
				DISCOVERY_PORT
		);
		MyLog.exiting(CLASS_NAME, "getHueSearchPackage", packet);
		return packet;
	}

	public HueBridge getBridge() {
		return bridge;
	}

	private static String getCurrentTS() {
		Date date = new Date();
		DateFormat sdf = SimpleDateFormat.getTimeInstance(SimpleDateFormat.LONG);
		String formattedDate = sdf.format(date);
		return formattedDate;
	}

	private HueBridge readDescriptionXml(String url) {
		MyLog.entering(CLASS_NAME, "readDescriptionXml", url);
		int retCode = -1;

		boolean isSupported = false;
		String udn = null;
		String urlBase = null;

		HueBridge newBridge = null;

		initHttpClient();
		initXmlReader();

		HttpGet httpGet = new HttpGet(url);

		try {
			HttpResponse response = httpClient.execute(httpGet);
			retCode = response.getStatusLine().getStatusCode();
			if (retCode == HttpStatus.SC_OK) {
				InputStream isContent = response.getEntity().getContent();
				InputStreamReader isr = new InputStreamReader(isContent);

//                Log.d(TAG, convertStreamToString(isContent));
				DescriptionHandler descHandler = new DescriptionHandler();
				xmlReader.setContentHandler(descHandler);

				xmlReader.parse(new InputSource(isContent));

				isSupported = descHandler.isHue();
				udn = descHandler.getUdn();
				urlBase = descHandler.getUrlBase();

				newBridge = new HueBridge(isSupported, udn, urlBase);
			}
		} catch (SAXException e) {
			MyLog.e("problem reading SSDP description", e);
		} catch (ClientProtocolException e) {
			MyLog.e("problem reading SSDP description", e);
		} catch (IOException e) {
			MyLog.e("problem reading SSDP description", e);
		}

		MyLog.exiting(CLASS_NAME, "readDescriptionXml", newBridge);
		return newBridge;
	}

	private void initHttpClient() {
		MyLog.entering(CLASS_NAME, "initHttpClient");
		if (httpClient == null) {
			httpClient = new DefaultHttpClient();
		}
		MyLog.exiting(CLASS_NAME, "initHttpClient");
	}

	private void initXmlReader() {
		MyLog.entering(CLASS_NAME, "initXmlReader");
		if (xmlReader == null) {
			System.setProperty("org.xml.sax.driver", "org.xmlpull.v1.sax2.Driver");

			try {
				xmlReader = XMLReaderFactory.createXMLReader();
			} catch (SAXException e) {
				MyLog.e("problem creating xmlReader", e);
			}
		}
		MyLog.exiting(CLASS_NAME, "initXmlReader");
	}
}
