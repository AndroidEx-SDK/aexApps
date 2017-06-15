package com.androidex.face.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.androidex.face.FaceCardActivity;

/**
 * Created by cts on 17/4/24.
 * 开机广播
 */

public class BootRecevier extends BroadcastReceiver{
    private static final String ACTION = "android.intent.action.BOOT_COMPLETED";
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(ACTION)) {
            Intent activityIntent = new Intent(context, FaceCardActivity.class);
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(activityIntent);
        }
    }
}
