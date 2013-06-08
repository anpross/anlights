package my.anlights;

import my.anlights.data.HueBridge;
import my.anlights.util.MyLog;
import my.anlights.util.ParserHelper;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class HueController {

	private AlConfig config;
	private HueHttpPostThread postThread;

	private static String TAG = Constants.LOGGING_TAG;

	private String user;
	private String urlBase;

	private static final String CLASS_NAME = HueController.class.getCanonicalName();

	public HueController(HueBridge bridge) {
		config = AlConfig.getExistingInstance();

		bridge.setController(this);

		HueHttpGetThread getThread = new HueHttpGetThread();
		postThread = new HueHttpPostThread();
		HueHttpPutThread putThread = new HueHttpPutThread();
	}


	private void readConfig() {
		user = config.getBridgeUser();
		urlBase = config.getBridgeUrlBase();
	}

//	public List<HueLight> getLightNames() {
//
//		readConfig();
//
//		String url = urlBase+"api/"+user+"/lights";
//
//		JSONObject result = executeGet(url);
//
//		return parseLights(result);
//	}

//	public JSONObject readLightState(HueLight light){
//		String url = urlBase+"api/"+user+"/lights/"+light.getId();
//		JSONObject lightJson = executeGet(url);
//		return lightJson;
//	}
//
//	public boolean writeLightState(HueLight light, HueState state){
//		JSONObject input =state.toJsonObject();
//		String url;
//		url = urlBase+"api/"+user+"/lights/"+light.getId()+"/state";
//
//		//TODO evaluate response
//		executePut(url, input);
//
//		return true;
//	}

	public void registerUser() {
		MyLog.entering(CLASS_NAME, "registerUser");

		JSONObject register = new JSONObject();

		readConfig();

		String url = urlBase + "api";
		try {
			register.put("devicetype", Constants.APPLICATION_NAME);
			register.put("username", user);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		MyLog.d("register json:" + register);

		JSONObject result = executePost(url, register);

		MyLog.exiting(CLASS_NAME, "registerUser", result);
	}

//	private JSONObject executeGet(String url){
//		Log.d(TAG,"execute get on:"+url);
//		JSONObject result = null;
//		try {
//			getThread.setUrl(url);
//		    Thread t = new Thread(getThread);
//			t.start();
//			t.join();
//			result = getThread.getResult();
//		} catch (InterruptedException e) {
//			Log.e(TAG, "problem executing thread", e);
//		}
//		return result;
//	}

	private JSONObject executePost(String url, JSONObject input) {
		MyLog.entering(CLASS_NAME, "executePost", url, input);
		JSONObject result = null;
		try {
			postThread.setUrl(url);
			postThread.setInput(input);
			Thread t = new Thread(postThread);
			t.start();
			t.join();
			result = postThread.getResult();
		} catch (InterruptedException e) {
			MyLog.e("problem executing thread", e);
		}
		return result;
	}

//	private JSONObject executePut(String url, JSONObject input){
//		Log.d(TAG,"execute put on:"+url);
//		Log.d(TAG,"json:"+input);
//		JSONObject result = null;
//		try {
//			putThread.setUrl(url);
//			putThread.setInput(input);
//		    Thread t = new Thread(putThread);
//			t.start();
//			t.join();
//			result = putThread.getResult();
//		} catch (InterruptedException e) {
//			Log.e(TAG, "problem executing thread", e);
//		}
//		return result;
//	}

//	private List<HueLight> parseLights(JSONObject result) {
//		List<HueLight> lights = new LinkedList<HueLight>();
//		@SuppressWarnings("unchecked")
//		Iterator<String> iKeys = result.keys();
//		while(iKeys.hasNext()){
//			String currKey = iKeys.next();
//			JSONObject currLamp;
//			try {
//				currLamp = result.getJSONObject(currKey);
//				HueLight currLight = new HueLight(currKey, currLamp.getString("name"), bridge);
//				lights.add(currLight);
//			} catch (JSONException e) {
//				e.printStackTrace();
//			}
//		}
//		return lights;
//	}


}

class HueHttpGetThread implements Runnable {

	private HttpClient httpClient;
	private JSONObject result;
	private String url;

	private static final String CLASS_NAME = HueHttpGetThread.class.getCanonicalName();

	public void setUrl(String url) {
		this.url = url;
	}

	public void run() {
		MyLog.entering(CLASS_NAME, "run");

		if (httpClient == null) {
			httpClient = new DefaultHttpClient();
		}

		HttpGet get = new HttpGet(url);
		try {
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
		MyLog.exiting(CLASS_NAME, "run");
	}

	public JSONObject getResult() {
		return result;
	}
}

class HueHttpPostThread implements Runnable {

	private HttpClient httpClient;
	private JSONObject result;
	private JSONObject input;
	private String url;

	private static final String CLASS_NAME = HueHttpPostThread.class.getCanonicalName();

	public void setUrl(String url) {
		this.url = url;
	}

	public void setInput(JSONObject input) {
		this.input = input;
	}

	public void run() {
		MyLog.entering(CLASS_NAME, "run");

		if (httpClient == null) {
			httpClient = new DefaultHttpClient();
		}

		HttpPost post = new HttpPost(url);
		try {
			post.setEntity(new StringEntity(input.toString()));

			HttpResponse response = httpClient.execute(post);
			int retCode = response.getStatusLine().getStatusCode();
			if (retCode == HttpStatus.SC_OK) {
				String sResult = ParserHelper.readInputStream(response.getEntity().getContent());
				sResult = ParserHelper.removeBrackets(sResult.trim());
				MyLog.d("recieved response:\n" + sResult);
				result = new JSONObject(sResult);
			}
		} catch (ClientProtocolException e) {
			MyLog.e("ClientProtocolException during GET", e);
		} catch (IOException e) {
			MyLog.e("IOException during GET", e);
		} catch (JSONException e) {
			MyLog.e("problem parsing JSON from GET request", e);
		}
		MyLog.exiting(CLASS_NAME, "run");
	}

	public JSONObject getResult() {
		return result;
	}
}

class HueHttpPutThread implements Runnable {

	private static String TAG = Constants.LOGGING_TAG;

	private HttpClient httpClient;
	private JSONObject result;
	private JSONObject input;
	private String url;

	private static final String CLASS_NAME = HueHttpPutThread.class.getCanonicalName();

	public void setUrl(String url) {
		this.url = url;
	}

	public void setInput(JSONObject input) {
		this.input = input;
	}

	public void run() {
		MyLog.entering(CLASS_NAME, "run");

		if (httpClient == null) {
			httpClient = new DefaultHttpClient();
		}

		HttpPut put = new HttpPut(url);
		try {
			put.setEntity(new StringEntity(input.toString()));

			MyLog.d("PutThreadUrl:" + put.getURI());
			MyLog.d("content:" + input.toString());
			HttpResponse response = httpClient.execute(put);

			int retCode = response.getStatusLine().getStatusCode();
			if (retCode == HttpStatus.SC_OK) {
				String sResult = ParserHelper.readInputStream(response.getEntity().getContent());
				sResult = ParserHelper.removeBrackets(sResult.trim());
				MyLog.d("recived respnse:" + sResult);
				result = new JSONObject(sResult);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		MyLog.exiting(CLASS_NAME, "run");
	}

	public JSONObject getResult() {
		return result;
	}
}