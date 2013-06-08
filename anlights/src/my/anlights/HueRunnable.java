package my.anlights;

import my.anlights.data.*;
import my.anlights.util.MyLog;
import my.anlights.util.ParserHelper;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import static junit.framework.Assert.assertNotNull;

public class HueRunnable implements Runnable {

	private static final String TAG = Constants.LOGGING_TAG;

	private DefaultHttpClient httpClient;
	private String user;
	private String urlBase;

	private JSONObject result;

	private HueMessage message;

	private final static String CLASS_NAME = HueRunnable.class.getCanonicalName();

	private void readConfig() {
		MyLog.entering(CLASS_NAME, "readConfig");
		AlConfig config = AlConfig.getExistingInstance();

		user = config.getBridgeUser();
		urlBase = config.getBridgeUrlBase();
		MyLog.exiting(CLASS_NAME, "readConfig");
	}

	public HueRunnable() {

		readConfig();

	}

	public void setMessage(HueMessage message) {
		this.message = message;
	}

	public void run() {
		MyLog.entering(CLASS_NAME, "run");
		if (user == null || urlBase == null) {
			readConfig();
		}
		processMessage(message);
		MyLog.exiting(CLASS_NAME, "run");
	}

	private void processMessage(HueMessage message) {
		MyLog.entering(CLASS_NAME, "processMessage", message);
		if (message instanceof HueLightNamesMessage) {
			processGetLightNames((HueLightNamesMessage) message);
		} else if (message instanceof HueReadStateMessage) {
			processReadLightState((HueReadStateMessage) message);
		} else if (message instanceof HueWriteStateMessage) {
			processWriteLightState((HueWriteStateMessage) message);
		}
		MyLog.exiting(CLASS_NAME, "processMessage");
	}

	private void processGetLightNames(HueLightNamesMessage message) {
		MyLog.entering(CLASS_NAME, "processGetLightNames", message);

		assertNotNull("base url is null", urlBase);
		String url = urlBase + "api/" + user + "/lights";
		executeGet(url);

		MyLog.exiting(CLASS_NAME, "processGetLightNames");
	}

	private void processWriteLightState(HueWriteStateMessage message) {
		HueLight light = message.getLight();
		HueState state = message.getState();

		String url = urlBase + "api/" + user + "/lights/" + light.getId() + "/state";

		executePut(url, state.toJsonObject());
	}

	private void executeGet(String url) {
		MyLog.entering(CLASS_NAME, "executeGet", url);

		readConfig();

		if (httpClient == null) {
			httpClient = new DefaultHttpClient();
		}
		HttpGet get = new HttpGet(url);
		try {
			// java.lang.IllegalStateException: Target host must not be null, or set in parameters. scheme=null, host=null, path=nullapi/anlight123/lights
			// only on first run when there is no host in config and discovery task did not yet get a response
			HttpResponse response = httpClient.execute(get);
			int retCode = response.getStatusLine().getStatusCode();
			if (retCode == HttpStatus.SC_OK) {
				String sResult = ParserHelper.readInputStream(response.getEntity().getContent());
				sResult = ParserHelper.removeBrackets(sResult.trim());
				MyLog.d("recived response:\n" + sResult);
				result = new JSONObject(sResult);
			}
		} catch (ClientProtocolException e) {
			MyLog.e("ClientProtocolException during GET", e);
		} catch (IOException e) {
			MyLog.e("IOException during GET", e);
		} catch (JSONException e) {
			MyLog.e("problem parsing JSON from GET request", e);
		}
		MyLog.exiting(CLASS_NAME, "executeGet");
	}


	private void executePut(String url, JSONObject input) {
		MyLog.entering(CLASS_NAME, "executePut", url, input);
		if (httpClient == null) {
			httpClient = new DefaultHttpClient();
		}

		HttpPut put = new HttpPut(url);
		try {
			put.setEntity(new StringEntity(input.toString()));

			MyLog.d("PutThreadUrl:" + put.getURI());
			MyLog.d("  content:" + input.toString());
			HttpResponse response = httpClient.execute(put);

			int retCode = response.getStatusLine().getStatusCode();
			if (retCode == HttpStatus.SC_OK) {
				String sResult = ParserHelper.readInputStream(response.getEntity().getContent());
				sResult = ParserHelper.removeBrackets(sResult.trim());
				MyLog.d("recieved resonse:" + sResult);
				result = new JSONObject(sResult);
			}
		} catch (ClientProtocolException e) {
			MyLog.e("ClientProtocolException during GET", e);
		} catch (IOException e) {
			MyLog.e("IOException during GET", e);
		} catch (JSONException e) {
			MyLog.e("problem parsing JSON from GET request", e);
		}
		MyLog.exiting(CLASS_NAME, "executePut");
	}

	private void processReadLightState(HueReadStateMessage message) {
		MyLog.entering(CLASS_NAME, "processReadLightState", message);

		HueLight light = message.getLight();
		String url = urlBase + "api/" + user + "/lights/" + light.getId();
		executeGet(url);

		MyLog.exiting(CLASS_NAME, "processReadLightState");
	}

	public JSONObject getResult() {
		return result;
	}
}
