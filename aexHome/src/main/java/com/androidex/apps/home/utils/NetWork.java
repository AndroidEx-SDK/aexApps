package com.androidex.apps.home.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by cts on 16/12/14.
 * 网络连接
 */

public class NetWork {

    /**
     * 判断是否有以太网的连接
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
