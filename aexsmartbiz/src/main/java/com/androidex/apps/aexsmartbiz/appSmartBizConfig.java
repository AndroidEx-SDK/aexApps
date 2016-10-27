package com.androidex.apps.aexSmartBiz;


import android.content.Context;

import com.androidex.aexapplibs.appLibsConfig;
import com.androidex.devices.appDeviceDriver;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yangjun on 2016/10/26.
 */

public class appSmartBizConfig extends appLibsConfig {
    public static final String PRINTER = "printer";
    public static final String BANK_READER = "bankReader";
    public static final String CAS_READER = "casReader";
    public static final String PASSWORD_KEYPAD = "passwordKeypad";
    public String PORT_ADDR_PRINTER = "/dev/ttyS0,115200,1,N,8";
    public String PORT_ADDR_BANKREADER = "/dev/ttyS2,115200,1,N,8";
    public String PORT_ADDR_CASREADER = "/dev/ttyS4,115200,1,N,8";
    public String PORT_ADDR_PASSWORD_KEYPAD = "/dev/ttyS6,115200,1,N,8";
    public JSONObject mConfigPrinter;
    public JSONObject mConfigBankReader;
    public JSONObject mConfigCasReader;
    public JSONObject mConfigPasswordKeypad;

    public appSmartBizConfig(Context context) {
        super(context);
        if(loadFromUserInfo()) {
            //成功读取配置
            mConfigPrinter = propertys.optJSONObject(PRINTER);
            mConfigBankReader = propertys.optJSONObject(BANK_READER);
            mConfigCasReader = propertys.optJSONObject(CAS_READER);
            mConfigPasswordKeypad = propertys.optJSONObject(PASSWORD_KEYPAD);
        }else{
            //读取配置失败
            mConfigPrinter = new JSONObject();
            try {
                mConfigPrinter.put(appDeviceDriver.PORT_ADDRESS,PORT_ADDR_PRINTER);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mConfigBankReader = new JSONObject();
            try {
                mConfigBankReader.put(appDeviceDriver.PORT_ADDRESS,PORT_ADDR_BANKREADER);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mConfigCasReader = new JSONObject();
            try {
                mConfigCasReader.put(appDeviceDriver.PORT_ADDRESS,PORT_ADDR_CASREADER);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mConfigPasswordKeypad = new JSONObject();
            try {
                mConfigPasswordKeypad.put(appDeviceDriver.PORT_ADDRESS,PORT_ADDR_PASSWORD_KEYPAD);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
