package com.tencent.devicedemo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.phone.InitActivity;

public class BootBroadcastReceiver extends BroadcastReceiver {

	static final String ACTION = "android.intent.action.BOOT_COMPLETED";
	
	@Override
	public void onReceive(Context context, Intent intent) {

        System.out.format("\nqqDoorLock BootBroadcastReceiver:%s\n",intent.getAction());
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
			Intent sayHelloIntent=new Intent(context, InitActivity.class);
			sayHelloIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			context.startActivity(sayHelloIntent);
            System.out.format("\t\tBootBroadcastReceiver\n");
        }
	}
}

