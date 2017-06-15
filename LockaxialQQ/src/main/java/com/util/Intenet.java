package com.util;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by cts on 17/5/23.
 */

public class Intenet {
    /**
     * 获取ip地址
     * @return
     */
    public static String getHostIP() {

        String hostIp = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    String ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip)) {
                        hostIp = ia.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            Log.i("yao", "SocketException");
            e.printStackTrace();
        }
        return hostIp;
    }

    /**
     * 系统设置
     */
    public static void system_set(Context context) {
        Intent intent =  new Intent(Settings.ACTION_SETTINGS);
        intent.putExtra("back", true);
        context.startActivity(intent);
    }

    public static void wifiManger(Context ctx){
        Intent wifiSettingsIntent = new Intent("android.settings.WIFI_SETTINGS");
        wifiSettingsIntent.putExtra("back",true);
        ctx.startActivity(wifiSettingsIntent);

    }
}
