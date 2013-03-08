package anlights;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


import android.os.AsyncTask;
import android.util.Log;
import anlights.data.HueBridge;
import anlights.data.SsdpDevice;

/**
 * to debug using wireshark, use filter "udp.port eq 1900"
 * 
 * discovery of a HueBridge folows these steps: (happy case)
 * - send SSDP broadcast and wait for responses
 * - read the first one
 * - check if SERVER header matches the one used by Hue
 * - use the LOCATION header to read the description.xml from Hue
 * - check if this matches the the supported ones and read UDN for multi-bridge support
 * 
 * @author Andreas
 *
 */
public class HueDiscoveryTask extends AsyncTask<Void, Void, HueBridge> {

	private static final int DISCOVERY_TIMEOUT = 2000;
	private static final int DISCOVERY_PORT = 1900;
	private static final String DISCOVERY_IP = "239.255.255.250";
	
	private DefaultHttpClient httpClient = null;
	private XMLReader xmlReader = null;
	private CallbackListener callback;
	
	private HueBridge bridge;
	
	private static final String TAG = Constants.LOGGING_TAG;
	
	public void setCallback(CallbackListener callback) {
		this.callback = callback;
	}
	
	private static final String DISCOVERY_BROADCAST = "M-SEARCH * HTTP/1.1\r\n" +
			"HOST: "+DISCOVERY_IP+":"+DISCOVERY_PORT+"\r\n" +
			"MAN: ssdp:discover\r\n" +
			"MX: "+(DISCOVERY_TIMEOUT/1000)+"\r\n" +
			"ST: ssdp:all\r\n\r\n";
	
	
	/**
	 * @return the first gateway found or null if timeout was reached.
	 */
	@Override
	protected HueBridge doInBackground(Void... callback) {
		Log.d(TAG,"entering doInBackground(Void... callback)");
		HueBridge hue = null;

		String location = AlConfig.getExistingInstance().getLastBridgeLocation();
		if(location != null){
			Log.d(TAG,"  trying old bridge location:"+location);
			hue = readDescriptionXml(location);
		}
				
		if(hue == null){
			Log.d(TAG,"  not found at old location, running ssdp discovery");
			String newLocation = doSsdpDiscovery();
			Log.d(TAG,"  found bridge at:"+newLocation);
			AlConfig.getExistingInstance().setLastBridgeLocation(newLocation);
			if(newLocation != null){
				hue = readDescriptionXml(newLocation);
			} else {
				Log.e(TAG,"  problem reading bridge location");
			}
		} else {
			Log.d(TAG,"  old location was good");
		}
		Log.d(TAG,"  is Supported Device:"+hue.isSupported()+" udn:"+hue.getUdn());
				
		return hue;
	}
	
	private String doSsdpDiscovery(){
		SsdpDevice bridge = null;
		String location = null;		
		try {
			final DatagramSocket socket = new DatagramSocket(DISCOVERY_PORT);
			final DatagramPacket reply;
			final byte[] replyBuffer;
			socket.setSoTimeout(DISCOVERY_TIMEOUT);
			
			boolean foundHue = false;
			try{ 
				Log.d(TAG,"sending request at "+ getCurrentTS());
				socket.send(getHueSearchPackage());

				replyBuffer = new byte[1000];
	            reply = new DatagramPacket(replyBuffer, replyBuffer.length);
				
				while(!foundHue){
					socket.receive(reply);
					bridge = new SsdpDevice(reply);
					if(bridge.isHueBridge()){
						foundHue = true;
					}
				}
				location = bridge.getLocation();
				
			} finally {
				Log.d(TAG,"  socket closed at "+ getCurrentTS());
				socket.close();
			}
			
		} catch (SocketException e) {
			Log.e(TAG,"problem discovering hue",e);
		} catch (UnknownHostException e) {
			Log.e(TAG,"problem discovering hue",e);
		} catch (IOException e) {
			Log.e(TAG,"problem discovering hue",e);
		}
		return location;
	}
	
	@Override
	protected void onPostExecute(HueBridge result) {
		this.bridge = result;
		super.onPostExecute(result);
		AlConfig.getExistingInstance().setBridgeUrlBase(result.getUrlBase());
		callback.callback(this);
	}
	
	/**
	 * generates a search message as per http://burgestrand.github.com/hue-api/api/discovery/
	 * @return
	 */
	private DatagramPacket getHueSearchPackage() throws UnknownHostException {
		Log.d(TAG,"sending packet:\n"+DISCOVERY_BROADCAST);
		Log.d(TAG,"  to: "+InetAddress.getByName(DISCOVERY_IP));
		DatagramPacket packet = new DatagramPacket(
				DISCOVERY_BROADCAST.getBytes(),
				DISCOVERY_BROADCAST.length(),
				InetAddress.getByName(DISCOVERY_IP),
				DISCOVERY_PORT
				);
		return packet;
	}
	
	public HueBridge getBridge() {
		return bridge;
	}
	
	private static String getCurrentTS(){
		Date date = new Date();
		DateFormat sdf = SimpleDateFormat.getTimeInstance(SimpleDateFormat.LONG);
		String formattedDate = sdf.format(date);
		return formattedDate;
	}
	
	private HueBridge readDescriptionXml(String url){
		int retCode = -1;
		
		boolean isSupported = false;
		String udn = null;
		String urlBase = null;

		initHttpClient();
		initXmlReader();
		
		HttpGet httpGet = new HttpGet(url);
		
		try {
			HttpResponse response = httpClient.execute(httpGet);
			retCode = response.getStatusLine().getStatusCode();
			if(retCode == HttpStatus.SC_OK) {
				InputStream isContent = response.getEntity().getContent();
				DescriptionHandler descHandler = new DescriptionHandler();
				xmlReader.setContentHandler(descHandler);

				xmlReader.parse(new InputSource(isContent));
				
				isSupported = descHandler.isHue();
				udn = descHandler.getUdn();
				urlBase = descHandler.getUrlBase();
			}
		} catch (SAXException e) {
			Log.e(TAG,"problem reading SSDP description",e);
		} catch (ClientProtocolException e) {
			Log.e(TAG,"problem reading SSDP description",e);
		} catch (IOException e) {
			Log.e(TAG,"problem reading SSDP description",e);
		}
		return new HueBridge(isSupported, udn, urlBase);
	}
	
	private void initHttpClient() {
		if(httpClient == null) {
			httpClient = new DefaultHttpClient();			
		}
	}
	
	private void initXmlReader() {
		if(xmlReader == null) {
			System.setProperty("org.xml.sax.driver","org.xmlpull.v1.sax2.Driver");

			try {
				xmlReader = XMLReaderFactory.createXMLReader();
			} catch (SAXException e) {
				Log.e(TAG,"problem creating xmlReader",e);
			}
		}
	}
}
