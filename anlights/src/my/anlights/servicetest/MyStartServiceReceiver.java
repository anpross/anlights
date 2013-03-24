package my.anlights.servicetest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyStartServiceReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
	  System.out.println("startServiceReceiver() + intent:"+intent);
    Intent service = new Intent(context, LocalWordService.class);
    context.startService(service);
  }
} 