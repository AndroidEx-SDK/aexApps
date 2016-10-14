package com.androidex.aexsettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootBroadcastReceiver extends BroadcastReceiver {    
	private static final String TAG = "AEXSDK";

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO 开机启动		
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){		
			//Intent sayHelloIntent = new Intent(context,DevicesListActivity.class);
			//sayHelloIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			//context.startActivity(sayHelloIntent);	
			Log.v(TAG, String.format("Recive %s,will create aexservice.\n",intent.getAction()));
            Intent dlIntent = new Intent(context,FullscreenActivity.class);
            dlIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(dlIntent);
			return;
		}
	}
}

