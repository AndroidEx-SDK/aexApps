package com.androidex;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Created by cts on 17/4/27.
 */

public class NetWork {

    /**
     * 检查当前网络是否可用
     *
     * @param
     * @return
     */

    public static boolean isNetworkAvailable(Context context) {
        //Context context = activity.getApplicationContext();
        // 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) {
            return false;
        } else {
            // 获取NetworkInfo对象
            NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();

            if (networkInfo != null && networkInfo.length > 0) {
                for (int i = 0; i < networkInfo.length; i++) {
                    networkInfo[i].isAvailable();
                    // 判断当前网络状态是否为连接状态
                    if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    /**
     * 获取当前网络状态的类型 *
     *
     * @param mContext
     * @return 返回网络类型
     */
    public static final int NETWORK_TYPE_NONE = -0x1; // 断网情况
    public static final int NETWORK_TYPE_WIFI = 0x1; // WIFI模式
    public static final int NETWOKR_TYPE_MOBILE = 0x2; // GPRS模式
    public static final int NETWOKR_TYPE_ETHERNET = 0x3; // 以太网模式

    public static int getCurrentNetType(Context mContext) {
        ConnectivityManager connManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI); // WIFI
        NetworkInfo gprs = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE); // GPRS
        NetworkInfo ethernet = connManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET); // GPRS

        if (wifi != null && wifi.getState() == NetworkInfo.State.CONNECTED) {
            Log.d("==NetWork", "Current net type:  WIFI.");
            return NETWORK_TYPE_WIFI;
        } else if (gprs != null && gprs.getState() == NetworkInfo.State.CONNECTED) {
            Log.d("==NetWork", "Current net type:  GPRS.");
            return NETWOKR_TYPE_MOBILE;
        }else if (ethernet != null && ethernet.getState() == NetworkInfo.State.CONNECTED) {
            Log.d("==NetWork", "Current net type:  ethernet.");
            return NETWOKR_TYPE_ETHERNET;
        }
        Log.e("==NetWork", "Current net type:  NONE.");
        return NETWORK_TYPE_NONE;
    }


}
