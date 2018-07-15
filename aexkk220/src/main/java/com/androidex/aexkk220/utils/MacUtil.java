package com.androidex.aexkk220.utils;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by cts on 16/12/14.
 */

public class MacUtil {
    public static String getWIFIMacAddress(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return info.getMacAddress();
    }

    public static String getBTMacAddress() {
        BluetoothAdapter btAda = BluetoothAdapter.getDefaultAdapter();
        //开启蓝牙
        if (btAda.isEnabled() == false) {
            if (btAda.enable()) {
                while (btAda.getState() == BluetoothAdapter.STATE_TURNING_ON
                        || btAda.getState() != BluetoothAdapter.STATE_ON) {
                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return btAda.getAddress();
    }


    public static String getEth1Mac() {
        String eth1Mac = "";
        String ethernetMac = getEthernetMac();
        String substring = ethernetMac.substring(ethernetMac.length() - 2, ethernetMac.length());
        String mac = ethernetMac.substring(0, ethernetMac.length() - 2);
        if (substring.length() == 2) {
            int decimal = Integer.parseInt(substring, 16);
            if (decimal == 255) {
                decimal = -1;
            }
            eth1Mac = String.format("%s%2X", mac, decimal + 1);
        }
        return eth1Mac;
    }

    public static String getEthernetMac() {
        BufferedReader reader = null;
        String ethernetMac = "";
        try {
            reader = new BufferedReader(new FileReader("sys/class/net/eth0/address"));
            ethernetMac = reader.readLine();
            if (ethernetMac == null || ethernetMac.trim().length() == 0) {
                ethernetMac = null;
            }
        } catch (Exception e) {
            Log.e("aexkk220", "open sys/class/net/eth0/address failed : " + e);
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                Log.e("aexkk220", "close sys/class/net/eth0/address failed : " + e);
            }
        }
        return ethernetMac;
    }

    public static String getAndroidExParameter(String path) {
        BufferedReader reader = null;
        String ethernetMac = "";
        try {
            reader = new BufferedReader(new FileReader(path));
            ethernetMac = reader.readLine();
            if (ethernetMac == null || ethernetMac.trim().length() == 0) {
                ethernetMac = null;
            }
        } catch (Exception e) {
            Log.e("aexkk220", String.format("open %s failed :%s", path, e.toString()));
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                Log.e("aexkk220", String.format("close %s failed :%s", path, e.toString()));
            }
        }
        return ethernetMac;
    }


    public static String getNETMacAddress() /* throws UnknownHostException */ {
        String strMacAddr = null;
        try {
            InetAddress ip = getLocalInetAddress();

            byte[] b = NetworkInterface.getByInetAddress(ip).getHardwareAddress();
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < b.length; i++) {
                if (i != 0) {
                    buffer.append('-');
                }
                String str = Integer.toHexString(b[i] & 0xFF);
                buffer.append(str.length() == 1 ? 0 + str : str);
            }
            strMacAddr = buffer.toString().toUpperCase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strMacAddr;
    }

    public static InetAddress getLocalInetAddress() {
        InetAddress ip = null;
        try {
            Enumeration<NetworkInterface> en_netInterface = NetworkInterface.getNetworkInterfaces();
            while (en_netInterface.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) en_netInterface.nextElement();
                Enumeration<InetAddress> en_ip = ni.getInetAddresses();
                while (en_ip.hasMoreElements()) {
                    ip = en_ip.nextElement();
                    if (!ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1)
                        break;
                    else
                        ip = null;
                }
                if (ip != null) {
                    break;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return ip;
    }

    /**
     * 获取ip地址
     *
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
            e.printStackTrace();
        }
        return hostIp;

    }
}