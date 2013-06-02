package my.anlights;

import java.io.IOException;

import my.anlights.data.HueLight;
import my.anlights.data.HueLightNamesMessage;
import my.anlights.data.HueMessage;
import my.anlights.data.HueReadStateMessage;
import my.anlights.data.HueState;
import my.anlights.data.HueWriteStateMessage;
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

import android.util.Log;
import static junit.framework.Assert.*;

public class HueRunnable implements Runnable {

    private static final String TAG = Constants.LOGGING_TAG;


	

	private DefaultHttpClient httpClient;
	private String user;
	private String urlBase;
	
	private AlConfig config;
	
	private JSONObject result;
	
	private HueMessage message;
	
	
	private void readConfig() {
		config = AlConfig.getExistingInstance();

		user = config.getBridgeUser();
		urlBase = config.getBridgeUrlBase();
		

	}
	
	public HueRunnable() {
		
		readConfig();
		
	}
	
	public void setMessage(HueMessage message){
		this.message = message;
	}
	public void run() {
        if(user == null || urlBase == null){
            readConfig();
        }
		Log.d(TAG,"HueRunnable - run()");
		processMessage(message);
	}
	private void processMessage(HueMessage message){
		if(message instanceof HueLightNamesMessage){
			processGetLightNames((HueLightNamesMessage)message);
		} else if (message instanceof HueReadStateMessage){
			processReadLightState((HueReadStateMessage)message);
		} else if (message instanceof HueWriteStateMessage) {
			processWriteLightState((HueWriteStateMessage)message);
		}
	}
	
	private void processGetLightNames(HueLightNamesMessage message) {
		assertNotNull("base url is null",urlBase);
		Log.d(TAG,"processGetLightNames - message:"+message);


		String url = urlBase+"api/"+user+"/lights";

		executeGet(url);
	}
	
	private void processWriteLightState(HueWriteStateMessage message){
		HueLight light = message.getLight();
		HueState state = message.getState();
		
		String url = urlBase+"api/"+user+"/lights/"+light.getId()+"/state";

		executePut(url, state.toJsonObject());
	}

	private void executeGet(String url) {
		Log.d(TAG,"executeGet - url:"+url);
		
		readConfig();
		
		if(httpClient == null) {
			httpClient = new DefaultHttpClient();			
		}
		HttpGet get = new HttpGet(url);
		try {
            // java.lang.IllegalStateException: Target host must not be null, or set in parameters. scheme=null, host=null, path=nullapi/anlight123/lights
            // only on first run when there is no host in config and discovery task did not yet get a response
			HttpResponse response = httpClient.execute(get);
			int retCode = response.getStatusLine().getStatusCode();
			if(retCode == HttpStatus.SC_OK){
				String sResult = ParserHelper.readInputStream(response.getEntity().getContent());
				sResult = ParserHelper.removeBrackets(sResult.trim());
				Log.d(TAG, "need to make sense from:\n"+sResult);
				result = new JSONObject(sResult);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	
	private void executePut(String url, JSONObject input){
		if(httpClient == null) {
			httpClient = new DefaultHttpClient();			
		}
		
		HttpPut put = new HttpPut(url);
		try {
			put.setEntity(new StringEntity(input.toString()));
			
			Log.d(TAG,"PutThreadUrl:"+put.getURI());
			Log.d(TAG,"content:\n"+input.toString());
			HttpResponse response = httpClient.execute(put);

			int retCode = response.getStatusLine().getStatusCode();
			if(retCode == HttpStatus.SC_OK){
				String sResult = ParserHelper.readInputStream(response.getEntity().getContent());
				sResult = ParserHelper.removeBrackets(sResult.trim());
				Log.d(TAG,"need to make sense from:\n"+sResult);
				result = new JSONObject(sResult);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}
	
	private void processReadLightState(HueReadStateMessage message) {
		HueLight light = message.getLight();
		String url = urlBase+"api/"+user+"/lights/"+light.getId();
		
		executeGet(url);
	}
	
	public JSONObject getResult(){
		return result;
	}

}
