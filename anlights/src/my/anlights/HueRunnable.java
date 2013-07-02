package my.anlights;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import my.anlights.data.HueLight;
import my.anlights.data.HueState;
import my.anlights.data.messages.HueDeleteUserMessage;
import my.anlights.data.messages.HueLightNamesMessage;
import my.anlights.data.messages.HueMessage;
import my.anlights.data.messages.HueReadConfigMessage;
import my.anlights.data.messages.HueReadStateMessage;
import my.anlights.data.messages.HueRegistrationMessage;
import my.anlights.data.messages.HueWriteStateMessage;
import my.anlights.util.MyLog;
import my.anlights.util.ParserHelper;

import static junit.framework.Assert.assertNotNull;

public class HueRunnable implements Runnable {

	private static final String TAG = Constants.LOGGING_TAG;

	private DefaultHttpClient httpClient;
	private String user;
	private String urlBase;

	private JSONObject result;

	private HueMessage message;

	private final static String[] CONTENT_METHODS = new String[]{"POST", "PUT"};
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
		} else if (message instanceof HueReadConfigMessage) {
			processReadConfig((HueReadConfigMessage) message);
		} else if (message instanceof HueRegistrationMessage) {
			processRegistration((HueRegistrationMessage) message);
		} else if (message instanceof HueDeleteUserMessage) {
			processDeleteUser((HueDeleteUserMessage) message);
		}
		MyLog.exiting(CLASS_NAME, "processMessage");
	}

	private void processRegistration(HueRegistrationMessage message) {
		MyLog.entering(CLASS_NAME, "processRegistration", message);

		JSONObject register = new JSONObject();

		String url = urlBase + "api";
		try {
			register.put("devicetype", message.getDeviceType());
			register.put("username", message.getUserName());
		} catch (JSONException e) {
			MyLog.e("error creating json for registration request", e);
		}
		MyLog.d("register json:" + register);

		executePost(url, register);


		MyLog.exiting(CLASS_NAME, "processRegistration");
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

	private void processDeleteUser(HueDeleteUserMessage message) {
		String url = urlBase + "api/" + user + "/config/whitelist/" + message.getUser();
		executeDelete(url);
	}

	// TODO refactor me
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
				MyLog.d("received response:\n" + sResult);
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

	// TODO refactor me
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
				MyLog.d("received response:" + sResult);
				result = new JSONObject(sResult);
			}
		} catch (ClientProtocolException e) {
			MyLog.e("ClientProtocolException during PUT", e);
		} catch (IOException e) {
			MyLog.e("IOException during PUT", e);
		} catch (JSONException e) {
			MyLog.e("problem parsing JSON from PUT request", e);
		}
		MyLog.exiting(CLASS_NAME, "executePut");
	}

	// TODO refactor me
	private void executePost(String url, JSONObject input) {
		MyLog.entering(CLASS_NAME, "executePost", url, input);
		if (httpClient == null) {
			httpClient = new DefaultHttpClient();
		}

		HttpPost post = new HttpPost(url);
		try {
			post.setEntity(new StringEntity(input.toString()));

			MyLog.d("PostThreadUrl:" + post.getURI());
			MyLog.d("  content:" + input.toString());
			HttpResponse response = httpClient.execute(post);

			int retCode = response.getStatusLine().getStatusCode();
			if (retCode == HttpStatus.SC_OK) {
				String sResult = ParserHelper.readInputStream(response.getEntity().getContent());
				sResult = ParserHelper.removeBrackets(sResult.trim());
				MyLog.d("received response:" + sResult);
				result = new JSONObject(sResult);
			}
		} catch (ClientProtocolException e) {
			MyLog.e("ClientProtocolException during POST", e);
		} catch (IOException e) {
			MyLog.e("IOException during POST", e);
		} catch (JSONException e) {
			MyLog.e("problem parsing JSON from POST request", e);
		}
		MyLog.exiting(CLASS_NAME, "executePost");
	}

	// TODO refactor me
	private void executeDelete(String url) {
		MyLog.entering(CLASS_NAME, "executeDelete", url);

		readConfig();

		if (httpClient == null) {
			httpClient = new DefaultHttpClient();
		}
		HttpDelete delete = new HttpDelete(url);
		try {
			HttpResponse response = httpClient.execute(delete);
			int retCode = response.getStatusLine().getStatusCode();
			if (retCode == HttpStatus.SC_OK) {
				String sResult = ParserHelper.readInputStream(response.getEntity().getContent());
				sResult = ParserHelper.removeBrackets(sResult.trim());
				MyLog.d("received response:\n" + sResult);
				result = new JSONObject(sResult);
			}
		} catch (ClientProtocolException e) {
			MyLog.e("ClientProtocolException during DELETE", e);
		} catch (IOException e) {
			MyLog.e("IOException during DELETE", e);
		} catch (JSONException e) {
			MyLog.e("problem parsing JSON from DELETE request", e);
		}
		MyLog.exiting(CLASS_NAME, "executeDelete");
	}


	private void processReadLightState(HueReadStateMessage message) {
		MyLog.entering(CLASS_NAME, "processReadLightState", message);

		HueLight light = message.getLight();
		String url = urlBase + "api/" + user + "/lights/" + light.getId();
		executeGet(url);

		MyLog.exiting(CLASS_NAME, "processReadLightState");
	}

	private void processReadConfig(HueReadConfigMessage message) {
		MyLog.entering(CLASS_NAME, "processReadConfig", message);
		String url = urlBase + "api/" + user + "/config/";
		executeGet(url);

		MyLog.exiting(CLASS_NAME, "processReadConfig");
	}

	public JSONObject getResult() {
		return result;
	}

}
