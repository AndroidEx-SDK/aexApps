package com.brocast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.tencent.devicedemo.InitActivity;

public class UpdateAPKReceiver extends BroadcastReceiver {
	private static final String TAG = "UpdateAPKReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.e(TAG,"BroadcastReceive：UpdateService");
		if (intent.getAction().equals("android.intent.action.PACKAGE_REPLACED")){
			Toast.makeText(context,"升级了一个安装包",Toast.LENGTH_SHORT).show();
			Log.e(TAG,"BroadcastReceive：UpdateService");
			Intent intent2 = new Intent(context, InitActivity.class);
			intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent2);
		}
	}
}

