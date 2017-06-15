package com.wificonnect;

/**
 * Created by xinshuhao on 16/7/17.
 */
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.devicedemo.R;

public class WifiConnActivity extends Activity implements OnItemClickListener, OnClickListener {
    private ListView wifiList;
    private Button wifi_switch_btn;
    private Button wifi_scan_btn;
    private Button wifi_cancle_btn;
    private List<ScanResult> list;
    private ScanResult mScanResult;
    private WifiAdmin mWifiAdmin;
    private WifiConnListAdapter mConnList;
    private TextView showConn;
    private ArrayList<WifiElement> wifiElement = new ArrayList<WifiElement>();
    private boolean isOpen = false;

    private ImageView im_wifi_anim;
    private AnimationDrawable animationDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wificonn);
        mWifiAdmin = new WifiAdmin(WifiConnActivity.this);
        initView();
    }

    private void initView() {
        wifiList = (ListView) this.findViewById(R.id.wifi_conn_lv);
        wifi_switch_btn = (Button) this.findViewById(R.id.wifi_conn_switch_btn);
        wifi_scan_btn = (Button) this.findViewById(R.id.wifi_conn_scan_btn);
        wifi_cancle_btn = (Button) this.findViewById(R.id.wifi_conn_cancle_btn);
        showConn = (TextView) this.findViewById(R.id.wifi_show_conn);

        im_wifi_anim=(ImageView)findViewById(R.id.iv_wifianim);

        if (mWifiAdmin.getWifiState() == WifiManager.WIFI_STATE_DISABLED) {
            wifi_scan_btn.setText("打开wifi");
        } else {
            wifi_scan_btn.setText("关闭wifi");
            isOpen = true;
        }
        showConn.setText("已连接：   " + initShowConn());
        wifi_cancle_btn.setOnClickListener(this);
        wifi_switch_btn.setOnClickListener(this);
        wifiList.setOnItemClickListener(this);
        wifi_scan_btn.setOnClickListener(this);
        mConnList = new WifiConnListAdapter(getApplicationContext(), getAllNetWorkList());
        wifiList.setAdapter(mConnList);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {

            case R.id.btn_setwifi:

                if (isOpen) {
                    Toast.makeText(getApplicationContext(), "正在关闭wifi", Toast.LENGTH_SHORT).show();
                    if (mWifiAdmin.closeWifi()) {
                        Toast.makeText(getApplicationContext(), "wifi关闭成功", Toast.LENGTH_SHORT).show();
                        wifi_scan_btn.setText("打开wifi");
                        isOpen = false;
                        ((LinearLayout)findViewById(R.id.ll_net)).setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(getApplicationContext(), "wifi关闭失败", Toast.LENGTH_SHORT).show();
                        ((LinearLayout)findViewById(R.id.ll_net)).setVisibility(View.GONE);
                    }
                } else {
                    //Toast.makeText(getApplicationContext(), "正在打开wifi", Toast.LENGTH_SHORT).show();
                    ((TextView)findViewById(R.id.tv_netstate)).setText("正在打开wifi");
                    start_animation(R.anim.animation_wifi);
                    handler.postDelayed(runnable,5000);
                  /* if if (mWifiAdmin.OpenWifi()) {
                        Toast.makeText(getApplicationContext(), "wifi打开成功", Toast.LENGTH_SHORT).show();
                        wifi_scan_btn.setText("关闭wifi");
                        isOpen = true;
                        getAllNetWorkList();
                        mConnList.notifyDataSetChanged();
                        ((LinearLayout)findViewById(R.id.ll_net)).setVisibility(View.GONE);
                        animationDrawable.stop();
                    } else {
                        ((TextView)findViewById(R.id.tv_netstate)).setText("wifi打开失败");
                        Toast.makeText(getApplicationContext(), "wifi打开失败", Toast.LENGTH_SHORT).show();
                        ((LinearLayout)findViewById(R.id.ll_net)).setVisibility(View.VISIBLE);
                    }*/
                }


                /*if(getAllNetWorkList().size()<=0){
                    ((LinearLayout)findViewById(R.id.ll_net)).setVisibility(View.VISIBLE);
                }else if(getAllNetWorkList().size()>0){
                    ((LinearLayout)findViewById(R.id.ll_net)).setVisibility(View.GONE);
                }*/
                break;
            case R.id.wifi_conn_cancle_btn:
                finish();
                break;
            case R.id.wifi_conn_switch_btn:
               /* mConnList = new WifiConnListAdapter(getApplicationContext(), getAllNetWorkList());
                wifiList.setAdapter(mConnList);*/
                getAllNetWorkList();
                mConnList.notifyDataSetChanged();
                if(getAllNetWorkList().size()<=0){
                    ((LinearLayout)findViewById(R.id.ll_net)).setVisibility(View.VISIBLE);
                }else if(getAllNetWorkList().size()>0){
                    ((LinearLayout)findViewById(R.id.ll_net)).setVisibility(View.GONE);
                }
                break;
            case R.id.wifi_conn_scan_btn:
                if (isOpen) {
                    Toast.makeText(getApplicationContext(), "正在关闭wifi", Toast.LENGTH_SHORT).show();
                    if (mWifiAdmin.closeWifi()) {
                        Toast.makeText(getApplicationContext(), "wifi关闭成功", Toast.LENGTH_SHORT).show();
                        wifi_scan_btn.setText("打开wifi");
                        isOpen = false;
                    } else {
                        Toast.makeText(getApplicationContext(), "wifi关闭失败", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "正在打开wifi", Toast.LENGTH_SHORT).show();
                    if (mWifiAdmin.OpenWifi()) {
                        Toast.makeText(getApplicationContext(), "wifi打开成功", Toast.LENGTH_SHORT).show();
                        wifi_scan_btn.setText("关闭wifi");
                        isOpen = true;
                    } else {
                        Toast.makeText(getApplicationContext(), "wifi打开失败", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            default:
                break;
        }
    }

    Runnable runnable=new Runnable() {
        @Override
        public void run() {
            if (mWifiAdmin.OpenWifi()) {
                ((LinearLayout)findViewById(R.id.ll_net)).setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "wifi打开成功", Toast.LENGTH_SHORT).show();
                wifi_scan_btn.setText("关闭wifi");
                isOpen = true;
                getAllNetWorkList();
                mConnList.notifyDataSetChanged();
                animationDrawable.stop();
                ((TextView)findViewById(R.id.tv_netstate)).setText("设备未联网");
            } else {
                ((TextView)findViewById(R.id.tv_netstate)).setText("wifi打开失败");
                Toast.makeText(getApplicationContext(), "wifi打开失败", Toast.LENGTH_SHORT).show();
                ((LinearLayout)findViewById(R.id.ll_net)).setVisibility(View.VISIBLE);
            }
        }
    };

    private Handler handler=new Handler();


    private void start_animation(int id){
        im_wifi_anim.setImageResource(id);
        animationDrawable = (AnimationDrawable) im_wifi_anim
                .getDrawable();
        animationDrawable.start();
    }

    private String initShowConn() {
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String s = wifiInfo.getSSID() + "    IP地址:" + mWifiAdmin.ipIntToString(wifiInfo.getIpAddress()) + "    Mac地址：" + wifiInfo.getMacAddress();
        return s;
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
        // TODO Auto-generated method stub
        final String ssid = wifiElement.get(position).getSsid();
        Builder dialog = new AlertDialog.Builder(WifiConnActivity.this);
        final WifiConfiguration wifiConfiguration = mWifiAdmin.IsExsits(ssid);
        dialog.setTitle("是否连接");
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (null == wifiConfiguration) {
                    setMessage(ssid);
                } else {
                    mWifiAdmin.Connect(wifiConfiguration);
                }
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub

            }
        }).setNeutralButton("移除", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                if (null != wifiConfiguration) {
                    int id = wifiConfiguration.networkId;
                    System.out.println("id>>>>>>>>>>" + id);
                    mWifiAdmin.removeNetworkLink(id);
                }
            }
        }).create();
        dialog.show();
    }

    private void setMessage(final String ssid) {
        Builder dialog = new AlertDialog.Builder(WifiConnActivity.this);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout lay = (LinearLayout) inflater.inflate(R.layout.widget_wifi_pwd, null);
        dialog.setView(lay);
        final EditText pwd = (EditText) lay.findViewById(R.id.wifi_pwd_edit);
        dialog.setTitle(ssid);
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub

                String pwdStr = pwd.getText().toString();
                boolean flag = mWifiAdmin.Connect(ssid, pwdStr, WifiAdmin.WifiCipherType.WIFICIPHER_WPA);
                if (flag) {
                    Toast.makeText(getApplicationContext(), "正在连接，请稍后", Toast.LENGTH_SHORT).show();
                } else {
                    showLog("链接错误");
                }
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub

            }
        }).create();
        dialog.show();
    }

    private ArrayList<WifiElement> getAllNetWorkList() {
        // 每次点击扫描之前清空上一次的扫描结果
        wifiElement.clear();
        // 开始扫描网络
        mWifiAdmin.startScan();
        list = mWifiAdmin.getWifiList();
        WifiElement element;
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                // 得到扫描结果
                mScanResult = list.get(i);
                element = new WifiElement();
                element.setSsid(mScanResult.SSID);
                element.setBssid(mScanResult.BSSID);
                element.setCapabilities(mScanResult.capabilities);
                element.setFrequency(mScanResult.frequency);
                element.setLevel(mScanResult.level);
                wifiElement.add(element);
            }
        }
        return wifiElement;
    }

    /**
     * 提示信息对话框
     *
     * @param msg
     */
    private void showLog(final String msg) {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                // TODO Auto-generated method stub
                super.onPostExecute(result);
                Dialog dialog = new AlertDialog.Builder(WifiConnActivity.this).setTitle("提示").setMessage(msg).setNegativeButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub

                    }
                }).create();// 创建
                // 显示对话框
                dialog.show();
            }

        }.execute();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        IntentFilter ins = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(netConnReceiver, ins);
    }

    private BroadcastReceiver netConnReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {

                if (checknet()) {
                    Log.d("111111>>>>>>>>>>", "成功");
                    showConn.setText("已连接：   " + initShowConn());
                    getAllNetWorkList();
                    mConnList.notifyDataSetChanged();
                    if(getAllNetWorkList().size()<=0){
                        ((LinearLayout)findViewById(R.id.ll_net)).setVisibility(View.VISIBLE);
                    }else if(getAllNetWorkList().size()>0){
                        ((LinearLayout)findViewById(R.id.ll_net)).setVisibility(View.GONE);
                    }
                    finish();
                } else {
                    Log.d("22222222>>>>>>>>>>", "失败");
                    showConn.setText("正在尝试连接：     " + initShowConn());
                    getAllNetWorkList();
                    mConnList.notifyDataSetChanged();
                    if(getAllNetWorkList().size()<=0){
                        ((LinearLayout)findViewById(R.id.ll_net)).setVisibility(View.VISIBLE);
                    }else if(getAllNetWorkList().size()>0){
                        ((LinearLayout)findViewById(R.id.ll_net)).setVisibility(View.GONE);
                    }
                }
            }
        }

    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(netConnReceiver);
    }

    /**
     * 获取网络
     */
    private NetworkInfo networkInfo;

    /**
     * 监测网络链接
     *
     * @return true 链接正常 false 链接断开
     */
    private boolean checknet() {
        ConnectivityManager connManager = (ConnectivityManager) this.getSystemService(CONNECTIVITY_SERVICE);
        // 获取代表联网状态的NetWorkInfo对象
        networkInfo = connManager.getActiveNetworkInfo();
        if (null != networkInfo) {
            return networkInfo.isAvailable();
        }
        return false;
    }

}

