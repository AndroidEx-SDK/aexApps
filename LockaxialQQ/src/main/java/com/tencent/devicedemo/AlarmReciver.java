package com.tencent.devicedemo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.androidex.DoorLock;

/**
 * Created by yangjun on 15/5/21.
 */
public class AlarmReciver extends BroadcastReceiver {
    //private static boolean showbar = true;
    private static int times = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        //Toast.makeText(context, String.format("alarm %s timers=%d",showbar?"true":"false",times), Toast.LENGTH_SHORT).show();
        /*if(showbar){
            context.sendBroadcast(new Intent("com.android.action.display_navigationbar"));
            showbar = false;
        }else{
            context.sendBroadcast(new Intent("com.android.action.hide_navigationbar"));
            showbar = true;
        }*/

        if(times == 10){
            DoorLock.getInstance().runReboot();
        }else {
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent1;
            PendingIntent pendingIntent;

            intent1 = new Intent(context, AlarmReciver.class);
            pendingIntent = PendingIntent.getBroadcast(context, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
            am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 10000/*从现在起30s*/, pendingIntent);

            {
                Intent ds_intent = new Intent();
                ds_intent.setAction(DoorLock.DoorLockOpenDoor);
                ds_intent.putExtra("index", 0);
                ds_intent.putExtra("status", 1);
                context.sendBroadcast(ds_intent);
            }
            /*{
                Intent ds_intent = new Intent();
                ds_intent.setAction(DoorLock.DoorLockOpenDoor);
                ds_intent.putExtra("index", 1);
                ds_intent.putExtra("status", 1);
                context.sendBroadcast(ds_intent);
            }*/
        }
        times++;
    }
}
