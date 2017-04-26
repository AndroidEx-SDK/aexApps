package com.phone.utils;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;

import java.util.List;

public class WifiAdmin {
    //定义一个WifiManager对象
    private WifiManager wifiManager;
    //定义一个WifiInfo对象
    private WifiInfo wifiInfo;
    //扫描出的网络连接列表
    private List<ScanResult> wifiList;
    //网络连接列表
    private List<WifiConfiguration> wifiConfigurations;
    WifiLock wifiLock;

    public WifiAdmin(Context context){
        //取得WifiManager对象
        wifiManager =(WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        //取得WifiInfo对象
        wifiInfo = wifiManager.getConnectionInfo();
    }
    //打开wifi
    public void openWifi(){
        if(!wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(true);
        }
    }
    //关闭wifi
    public void closeWifi(){
        if(!wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(false);
        }
    }
    // 检查当前wifi状态
    public int checkState() {
        return wifiManager.getWifiState();
    }
    //锁定wifiLock
    public void acquireWifiLock(){
        wifiLock.acquire();
    }
    //解锁wifiLock
    public void releaseWifiLock(){
        //判断是否锁定
        if(wifiLock.isHeld()){
            wifiLock.acquire();
        }
    }
    //创建一个wifiLock
    public void createWifiLock(){
        wifiLock = wifiManager.createWifiLock("test");
    }
    //得到配置好的网络
    public List<WifiConfiguration> getConfiguration(){
        return wifiConfigurations;
    }
    //指定配置好的网络进行连接
    public void connetionConfiguration(int index){
        if(index> wifiConfigurations.size()){
            return ;
        }
        //连接配置好指定ID的网络
        wifiManager.enableNetwork(wifiConfigurations.get(index).networkId, true);
    }
    public void startScan(){
        wifiManager.startScan();
        //得到扫描结果
        wifiList = wifiManager.getScanResults();
        //得到配置好的网络连接
        wifiConfigurations = wifiManager.getConfiguredNetworks();
    }
    //得到网络列表
    public List<ScanResult> getWifiList(){
        return wifiList;
    }
    //查看扫描结果
    public StringBuffer lookUpScan(){
        StringBuffer sb=new StringBuffer();
        for(int i = 0; i< wifiList.size(); i++){
            sb.append("Index_" + new Integer(i + 1).toString() + ":");
            // 将ScanResult信息转换成一个字符串包
            // 其中把包括：BSSID、SSID、capabilities、frequency、level
            sb.append((wifiList.get(i)).toString()).append("\n");
        }
        return sb;
    }
    public String getMacAddress(){
        return (wifiInfo ==null)?"NULL": wifiInfo.getMacAddress();
    }
    public String getBSSID(){
        return (wifiInfo ==null)?"NULL": wifiInfo.getBSSID();
    }
    public int getIpAddress(){
        return (wifiInfo ==null)?0: wifiInfo.getIpAddress();
    }
    //得到连接的ID
    public int getNetWordId(){
        return (wifiInfo ==null)?0: wifiInfo.getNetworkId();
    }
    //得到wifiInfo的所有信息
    public String getWifiInfo(){
        return (wifiInfo ==null)?"NULL": wifiInfo.toString();
    }
    //添加一个网络并连接
    public void addNetWork(WifiConfiguration configuration){
        int wcgId= wifiManager.addNetwork(configuration);
        wifiManager.enableNetwork(wcgId, true);
    }
    //断开指定ID的网络
    public void disConnectionWifi(int netId){
        wifiManager.disableNetwork(netId);
        wifiManager.disconnect();
    }

    public boolean connectWifi(String ssid, String password){
        WifiConfiguration wifiConfig;
        wifiConfig = setWifiParams(ssid,password);
        int wcgID = wifiManager.addNetwork(wifiConfig);
        boolean flag = wifiManager.enableNetwork(wcgID, true);
        if(flag){
            wifiManager.saveConfiguration();
        }
        return flag;
    }

    public WifiConfiguration setWifiParams(String ssid, String password) {
        WifiConfiguration apConfig = new WifiConfiguration();
        apConfig.SSID = "\"" + ssid + "\"";
        apConfig.preSharedKey = "\"" + password + "\"";
        apConfig.hiddenSSID = false;
        apConfig.status = WifiConfiguration.Status.ENABLED;
        apConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);

        apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);

        apConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        apConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        apConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
        apConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);

        apConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        apConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        apConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.NONE);

        apConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        // 必须添加，否则无线路由无法连接
        apConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        return apConfig;
    }
}
