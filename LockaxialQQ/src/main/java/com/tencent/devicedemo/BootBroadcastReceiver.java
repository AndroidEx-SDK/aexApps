package com.tencent.devicedemo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootBroadcastReceiver extends BroadcastReceiver {
	private static final String TAG = "BootBroadcastReceiver";
	static final String ACTION = "android.intent.action.BOOT_COMPLETED";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "onReceive: 执行了吗?");
		System.out.format("\nqqDoorLock BootBroadcastReceiver:%s\n",intent.getAction());
		if (intent.getAction().equals(ACTION)){
			Intent sayHelloIntent=new Intent(context, InitActivity.class);
			sayHelloIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(sayHelloIntent);
            System.out.format("\t\tBootBroadcastReceiver\n");
        }
	}
}

