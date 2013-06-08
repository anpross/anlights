package my.anlights.servicetest;

import android.app.Service;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;

import java.util.ArrayList;

public class LocalWordService extends Service {
	private final IBinder mBinder = new MyBinder();
	private ArrayList<String> list = new ArrayList<String>();

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		System.out.println("onStartServiceCommand() + intent:" + intent + ", flags:" + flags + ", startId:" + startId);

		boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
		String reason = intent.getStringExtra(ConnectivityManager.EXTRA_REASON);
		boolean isFailover = intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false);

		NetworkInfo currentNetworkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
		NetworkInfo otherNetworkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO);

		return Service.START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	public class MyBinder extends Binder {
		public LocalWordService getService() {
			return LocalWordService.this;
		}
	}
}