package com.androidex.apps.home.utils;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.androidex.apps.home.FullscreenActivity;

/**
 * Created by cts on 16/12/14.
 * 网络连接
 */
public class NetWork {
    /**
     * 测试wifi网络环境
     */
    public static void wifiManger(Context ctx){
        Intent i = new Intent();
        i.setAction(Intent.ACTION_VIEW);
        if(android.os.Build.VERSION.SDK_INT >= 11){
            i.setClassName("com.android.settings", "com.android.settings.Settings$WifiSettingsActivity");
        }else{
            i.setClassName("com.android.settings"
                    , "com.android.settings.wifi.WifiSettings");
        }
        i.putExtra("back",true);
        ctx.sendBroadcast(new Intent("com.android.action.display_navigationbar"));
        ((FullscreenActivity)ctx).startActivityForResult(i,1001);
    }

    public static void netWorkManger(Context ctx){
        Toast.makeText(ctx,"请关闭WIFI并打开以太网开关",Toast.LENGTH_LONG).show();
        Intent i = new Intent();
        i.setAction(Intent.ACTION_VIEW);
        if(android.os.Build.VERSION.SDK_INT >= 11){//$EtherNetSettingsActivity
            i.setClassName("com.android.settings", "com.android.settings.Settings");
        }else{
            i.setClassName("com.android.settings"
                    , "com.android.settings.wifi.WifiSettings");
        }
        i.putExtra("back",true);
        ctx.sendBroadcast(new Intent("com.android.action.display_navigationbar"));
        ((FullscreenActivity)ctx).startActivityForResult(i,1002);
    }

    /**
     * 判断是否有以太网的连接成功
     */
    public static boolean isConnect(Context context){
        if (context!=null) {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isAvailable()) {
                int infoType = networkInfo.getType();
                if (ConnectivityManager.TYPE_ETHERNET == infoType) {//以太网
                    return true;
                }
            }
        }
        return false;
    }
}
