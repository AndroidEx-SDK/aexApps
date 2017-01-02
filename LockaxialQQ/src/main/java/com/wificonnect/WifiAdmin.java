package com.wificonnect;

/**
 * Created by xinshuhao on 16/7/17.
 */
import java.net.Inet4Address;
import java.util.List;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;

public class WifiAdmin {

    private WifiManager wifiManager;

    /**
     * 声明管理对象
     */
    private WifiInfo wifiInfo;

    /**
     * Wifi信息
     */
    private List<ScanResult> scanResultList;

    /**
     * 扫描出来的网络连接列表
     */
    private List<WifiConfiguration> wifiConfigList;

    /**
     * 网络配置列表
     */
    private WifiLock wifiLock;

    /**
     * 加密类型
     *
     * @author Administrator
     *
     */
    public enum WifiCipherType {
        WIFICIPHER_WEP, WIFICIPHER_WPA, WIFICIPHER_NOPASS, WIFICIPHER_INVALID
    }

    /**
     * 无配置记录链接方式
     *
     * @param SSID
     * @param Password
     * @param Type
     * @return true or false
     */
    public boolean Connect(String SSID, String Password, WifiCipherType Type) {
        if (!this.OpenWifi()) {
            return false;
        }
        // 状态变成WIFI_STATE_ENABLED的时候才能执行下面的语句
        while (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
            try {
                // 为了避免程序一直while循环，让它睡个100毫秒在检测……
                Thread.currentThread();
                Thread.sleep(100);
            } catch (InterruptedException ie) {
            }
        }

        WifiConfiguration wifiConfig = this.CreateWifiInfo(SSID, Password, Type);
        //
        if (wifiConfig == null) {
            return false;
        }
        WifiConfiguration tempConfig = this.IsExsits(SSID);
        if (tempConfig != null) {
            wifiManager.removeNetwork(tempConfig.networkId);
        }
        int netID = wifiManager.addNetwork(wifiConfig);
        System.out.println(netID);
        wifiManager.startScan();

        for (WifiConfiguration c0 : wifiManager.getConfiguredNetworks()) {
            if (c0.networkId == netID) {
                boolean bRet = wifiManager.enableNetwork(c0.networkId, true);
            } else {
                wifiManager.enableNetwork(c0.networkId, false);
            }
        }
        boolean bRet = wifiManager.enableNetwork(netID, true);
        wifiManager.saveConfiguration();
        return bRet;
    }

    /**
     * 已有配置链接
     *
     * @param wf
     * @return
     */
    public boolean Connect(WifiConfiguration wf) {
        if (!this.OpenWifi()) {
            return false;
        }
        // 状态变成WIFI_STATE_ENABLED的时候才能执行下面的语句
        while (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
            try {
                // 为了避免程序一直while循环，让它睡个100毫秒在检测……
                Thread.currentThread();
                Thread.sleep(100);
            } catch (InterruptedException ie) {
            }
        }

        WifiConfiguration wifiConfig = wf;
        //
        if (wifiConfig == null) {
            return false;
        }
        WifiConfiguration tempConfig = this.IsExsits(wifiConfig.SSID);
        if (tempConfig != null) {
            wifiManager.removeNetwork(tempConfig.networkId);
        }
        int netID = wifiManager.addNetwork(wifiConfig);
        System.out.println(netID);
        wifiManager.startScan();

        for (WifiConfiguration c0 : wifiManager.getConfiguredNetworks()) {
            if (c0.networkId == netID) {
                boolean bRet = wifiManager.enableNetwork(c0.networkId, true);
            } else {
                wifiManager.enableNetwork(c0.networkId, false);
            }
        }
        boolean bRet = wifiManager.enableNetwork(netID, true);
        wifiManager.saveConfiguration();
        return bRet;
    }

    /**
     * 打开wifi功能
     *
     * @return true or false
     */
    public boolean OpenWifi() {
        boolean bRet = true;
        if (!wifiManager.isWifiEnabled()) {
            bRet = wifiManager.setWifiEnabled(true);
        }
        return bRet;
    }

    /**
     * 查看以前是否也配置过这个网络
     *
     * @param SSID
     * @return WifiConfiguration
     */
    public WifiConfiguration IsExsits(String SSID) {
        List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                return existingConfig;
            }
        }
        return null;
    }

    /**
     * 链接
     *
     * @param SSID
     * @param Password
     * @param Type
     * @return WifiConfiguration
     */
    private WifiConfiguration CreateWifiInfo(String SSID, String Password, WifiCipherType Type) {
        WifiConfiguration wc = new WifiConfiguration();
        wc.allowedAuthAlgorithms.clear();
        wc.allowedGroupCiphers.clear();
        wc.allowedKeyManagement.clear();
        wc.allowedPairwiseCiphers.clear();
        wc.allowedProtocols.clear();
        wc.SSID = "\"" + SSID + "\"";
        if (Type == WifiCipherType.WIFICIPHER_NOPASS) {
            wc.wepKeys[0] = "";
            wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wc.wepTxKeyIndex = 0;
        } else if (Type == WifiCipherType.WIFICIPHER_WEP) {
            wc.wepKeys[0] = "\"" + Password + "\"";
            wc.hiddenSSID = true;
            System.out.println("111111111111111111111111");
            wc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wc.wepTxKeyIndex = 0;
            // System.out.println(wc.preSharedKey);
            System.out.println(wc);
        } else if (Type == WifiCipherType.WIFICIPHER_WPA) {
            wc.preSharedKey = "\"" + Password + "\"";
            wc.hiddenSSID = true;
            // 用来判断加密方法。
            // 可选参数：LEAP只用于leap,
            // OPEN 被wpa/wpa2需要,
            // SHARED需要一个静态的wep key
            wc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            // 用来判断加密方法。可选参数：CCMP,TKIP,WEP104,WEP40
            wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            // WifiConfiguration.KeyMgmt 键管理机制（keymanagerment），使用KeyMgmt 进行。
            // 可选参数IEEE8021X,NONE,WPA_EAP,WPA_PSK
            wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            // WifiConfiguration.PairwiseCipher 设置加密方式。
            // 可选参数 CCMP,NONE,TKIP
            wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            // WifiConfiguration.Protocol 设置一种协议进行加密。
            // 可选参数 RSN,WPA,
            wc.allowedProtocols.set(WifiConfiguration.Protocol.WPA); // for WPA
            wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN); // for WPA2
            // wifiConfiguration.Status 获取当前网络的状态。
        } else {
            return null;
        }
        return wc;
    }

    /**
     * Wifi锁
     *
     * @param context
     */
    public WifiAdmin(Context context) {
        this.wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        // 获取Wifi服务
        // 得到Wifi信息
        this.wifiInfo = wifiManager.getConnectionInfo();
        // 得到连接信息
    }

    /**
     *
     * @return
     */
    public boolean getWifiStatus() {
        return wifiManager.isWifiEnabled();
    }

    /**
     * 获取当前网卡状态
     *
     * @return
     */
    public int getWifiState() {
        return wifiManager.getWifiState();
    }

    /**
     * 关闭wifi
     *
     * @return
     */
    public boolean closeWifi() {
        if (!wifiManager.isWifiEnabled()) {
            return true;
        } else {
            return wifiManager.setWifiEnabled(false);
        }
    }

    // 锁定/解锁wifi
    // 其实锁定WiFI就是判断wifi是否建立成功，在这里使用的是held，握手的意思acquire 得到！

    public void lockWifi() {
        wifiLock.acquire();
    }

    public void unLockWifi() {
        if (!wifiLock.isHeld()) {
            wifiLock.release();
            // 释放资源
        }
    }

    // 我本来是写在构造函数中了，但是考虑到不是每次都会使用Wifi锁，所以干脆自己建立一个方法！需要时调用，建立就OK

    public void createWifiLock() {
        wifiLock = wifiManager.createWifiLock("flyfly");
        // 创建一个锁的标志
    }

    /**
     * 扫描网络
     */
    public void startScan() {
        wifiManager.startScan();
        scanResultList = wifiManager.getScanResults();
        // 扫描返回结果列表
        wifiConfigList = wifiManager.getConfiguredNetworks();
        // 扫描配置列表
    }

    /**
     * 扫描到的AP集合
     *
     * @return
     */
    public List<ScanResult> getWifiList() {
        return scanResultList;
    }

    /**
     * 已配置的AP集合
     *
     * @return
     */
    public List<WifiConfiguration> getWifiConfigList() {
        return wifiConfigList;
    }

    /**
     * 获取扫描列表
     *
     * @return
     */
    public StringBuilder lookUpscan() {
        StringBuilder scanBuilder = new StringBuilder();
        for (int i = 0; i < scanResultList.size(); i++) {
            scanBuilder.append("编号：" + (i + 1));
            scanBuilder.append(scanResultList.get(i).toString());
            // 所有信息
            scanBuilder.append("\n");
        }
        return scanBuilder;
    }

    /**
     * 获取指定信号的强度
     *
     * @param NetId
     * @return
     */
    public int getLevel(int NetId) {
        return scanResultList.get(NetId).level;
    }

    /**
     * 获取本机Mac地址
     *
     * @return
     */
    public String getMac() {
        return (wifiInfo == null) ? "" : wifiInfo.getMacAddress();
    }

    /**
     * 获取本机链接BSSID地址
     *
     * @return
     */
    public String getBSSID() {
        return (wifiInfo == null) ? null : wifiInfo.getBSSID();
    }

    /**
     * 获取本机SSID
     *
     * @return
     */
    public String getSSID() {
        return (wifiInfo == null) ? null : wifiInfo.getSSID();
    }

    /**
     * 返回当前连接的网络的ID
     *
     * @return
     */
    public int getCurrentNetId() {
        return (wifiInfo == null) ? null : wifiInfo.getNetworkId();
    }

    /**
     * 返回所有信息
     *
     * @return
     */
    public String getwifiInfo() {
        return (wifiInfo == null) ? null : wifiInfo.toString();
    }

    /**
     * 获取IP地址
     *
     * @return
     */
    public int getIP() {
        return (wifiInfo == null) ? null : wifiInfo.getIpAddress();
    }

    /**
     * 添加一个连接
     *
     * @param config
     * @return
     */
    public boolean addNetWordLink(WifiConfiguration config) {
        int NetId = wifiManager.addNetwork(config);
        return wifiManager.enableNetwork(NetId, true);
    }

    /**
     * 禁用一个链接
     *
     * @param NetId
     * @return
     */
    public boolean disableNetWordLick(int NetId) {
        wifiManager.disableNetwork(NetId);
        return wifiManager.disconnect();
    }

    /**
     * 移除一个链接
     *
     * @param NetId
     * @return
     */
    public boolean removeNetworkLink(int NetId) {
        return wifiManager.removeNetwork(NetId);
    }

    /**
     * 不显示SSID
     *
     * @param NetId
     */
    public void hiddenSSID(int NetId) {
        wifiConfigList.get(NetId).hiddenSSID = true;
    }

    /**
     * 显示SSID
     *
     * @param NetId
     */
    public void displaySSID(int NetId) {
        wifiConfigList.get(NetId).hiddenSSID = false;
    }

    /**
     * 转换IP
     *
     * @param ip
     * @return
     */
    public String ipIntToString(int ip) {
        try {
            byte[] bytes = new byte[4];
            bytes[0] = (byte) (0xff & ip);
            bytes[1] = (byte) ((0xff00 & ip) >> 8);
            bytes[2] = (byte) ((0xff0000 & ip) >> 16);
            bytes[3] = (byte) ((0xff000000 & ip) >> 24);
            return Inet4Address.getByAddress(bytes).getHostAddress();
        } catch (Exception e) {
            return "";
        }
    }
}
