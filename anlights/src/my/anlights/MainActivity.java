package my.anlights;

import java.util.List;

import my.anlights.R;
import my.anlights.data.HueBridge;
import my.anlights.data.HueGroup;
import my.anlights.data.HueLight;
import my.anlights.data.HueState;
import my.anlights.servicetest.LocalWordService;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity implements CallbackListener, OnClickListener, OnSeekBarChangeListener {

	private static final String TAG = Constants.LOGGING_TAG;
	 
	private HueGroup hGroup;
	ToggleButton onToggle;
	SeekBar briSeek;
	SeekBar tempSeek;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        doBindService();
        // username 10-40 chars
        AlConfig.getInstance(this).setBridgeUser("anlight123");
        
        setContentView(R.layout.activity_main);
        initUi();

        initBridge();

        System.out.println("checking service: "+s);
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
    	// TODO Auto-generated method stub
    	super.onRestoreInstanceState(savedInstanceState);
    	
        initBridge();
    }
    
    private void initBridge() {
        HueDiscoveryTask discovery = new HueDiscoveryTask();
        discovery.setCallback(this);
        discovery.execute();
    }
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    }
    
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
    }

	private void initUi() {
		onToggle = (ToggleButton) findViewById(R.id.onToggleButton);
        onToggle.setOnClickListener(this);
        briSeek = (SeekBar) findViewById(R.id.briSeekBar);
        briSeek.setOnSeekBarChangeListener(this);
        tempSeek = (SeekBar) findViewById(R.id.tempSeekBar);
        tempSeek.setOnSeekBarChangeListener(this);
	} 

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
  
	public void callback(Object source) {
		if(source instanceof HueDiscoveryTask){
			HueDiscoveryTask discovery = (HueDiscoveryTask)source;
			Log.d("TAG","discover done - base url:"+AlConfig.getExistingInstance().getBridgeUrlBase());

//			new HueLights().registerUser();
			HueBridge bridge = discovery.getBridge();
			
			if(bridge.isConnected()){
				List<HueLight> lights = bridge.getLightNames();
			
				hGroup = new HueGroup();
				for (HueLight currLight : lights) {
					hGroup.addLight(currLight);
				}
				hGroup.readLightStatus();
				
				updateControls();
				
				Log.d(TAG,"group state:"+hGroup.getLightState());
			} 
			
//			HueState state = new HueState();
//			state.setOn(true);
//			state.setBri(200);
//			state.setSat(100);
//			
//			hGroup.setLightState(state);
			
		}
		
		 
	}
	 
	private void updateControls(){
		HueState lights = hGroup.getLightState();
		onToggle.setChecked(lights.isOn());
		briSeek.setProgress(lights.getBri());
		if(lights.getCt() != null){
			tempSeek.setProgress(lights.getCt()-154);
		}
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		Log.d(TAG,"got clickEvent:"+v);
		if(v.equals(onToggle)){
			HueState newState = new HueState();
			newState.setOn(onToggle.isChecked());
			hGroup.setLightState(newState);
		}
		
	} 


	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		
		
	}

	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	public void onStopTrackingTouch(SeekBar seekBar) {
		if(seekBar.equals(briSeek)){
			HueState newState = new HueState();
			newState.setBri(briSeek.getProgress());
			hGroup.setLightState(newState);
			
		} else if(seekBar.equals(tempSeek)){
			HueState newState = new HueState();
			newState.setCt(tempSeek.getProgress()+154);
			hGroup.setLightState(newState);
			
		}

	}
	
	// http://www.vogella.com/articles/AndroidServices/article.html
	  private LocalWordService s;
	  
	  private ServiceConnection mConnection = new ServiceConnection() {

		    public void onServiceConnected(ComponentName className, IBinder binder) {
		      s = ((LocalWordService.MyBinder) binder).getService();
		      Toast.makeText(MainActivity.this, "Connected",
		          Toast.LENGTH_SHORT).show();
		      
		    }

		    public void onServiceDisconnected(ComponentName className) {
		      s = null;
		    }
		  };
		  private ArrayAdapter<String> adapter;

		  void doBindService() {
		    bindService(new Intent(this, LocalWordService.class), mConnection,
		        Context.BIND_AUTO_CREATE);
		  }

		  public void showServiceData(View view) {
		    if (s != null) {

		      Toast.makeText(this, "Number of elements",
		          Toast.LENGTH_SHORT).show();
		      adapter.notifyDataSetChanged();
		    }
		  }
}
