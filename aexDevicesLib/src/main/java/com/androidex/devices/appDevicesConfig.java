package com.androidex.devices;


import android.content.Context;

import com.androidex.aexapplibs.appLibsConfig;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yangjun on 2016/10/26.
 */

public class appDevicesConfig extends appLibsConfig {
    public static final String PRINTER = "printer";
    public static final String BANK_READER = "bankReader";
    public static final String CAS_READER = "casReader";
    public static final String PASSWORD_KEYPAD = "passwordKeypad";
    public static final String X3BIOVO = "biovo";
    public static final String CRT310_READER = "CRT310Reader";
    public String PORT_ADDR_PRINTER = "/dev/ttyS0,115200,N,1,8";//打印机
    public String PORT_ADDR_PASSWORD_KEYPAD = "/dev/ttyS2,9600,N,1,8";//密码键盘
    public String PORT_ADDR_BANKREADER = "/dev/ttyS4,9600,N,1,8";//银行卡读卡器
    public String PORT_ADDR_CASREADER = "/dev/ttyS6,9600,N,1,8";//燃气卡读卡器
    public String PORT_ADDR_CASREADER_LCC = "/dev/ttyS4,9600,N,1,8";//莱卡的s4串口
    public String PORT_ADDR_X3BIOVO = "/dev/ttyS4,19200,N,1,8";//指纹仪
    public String PORT_ADDR_CRT310READER = "/dev/ttyS4,9600,N,1,8";//电动读卡器
    public JSONObject mConfigPrinter;
    public JSONObject mConfigBankReader;
    public JSONObject mConfigCasReader;
    public JSONObject mConfigPasswordKeypad;
    public JSONObject mConfigBiovo;
    public JSONObject mConfigCRT310Reader;

    public appDevicesConfig(Context context) {
        super(context);
        if (loadFromUserInfo()) {
            //成功读取配置
            mConfigPrinter = propertys.optJSONObject(PRINTER);
            if (mConfigPrinter == null) {
                mConfigPrinter = new JSONObject();
                //Toast.makeText(context, "打印机未成功链接", Toast.LENGTH_SHORT).show();
            }
            if (mConfigPrinter.optString(appDeviceDriver.PORT_ADDRESS, "") == "") {
                try {
                    mConfigPrinter.put(appDeviceDriver.PORT_ADDRESS, PORT_ADDR_PRINTER);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            mConfigBankReader = propertys.optJSONObject(BANK_READER);
            if (mConfigBankReader == null) {
                mConfigBankReader = new JSONObject();
            }
            if (mConfigBankReader.optString(appDeviceDriver.PORT_ADDRESS, "") == "") {
                try {
                    mConfigBankReader.put(appDeviceDriver.PORT_ADDRESS, PORT_ADDR_BANKREADER);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            mConfigCasReader = propertys.optJSONObject(CAS_READER);
            if (mConfigCasReader == null) {
                mConfigCasReader = new JSONObject();
            }
            if (mConfigCasReader.optString(appDeviceDriver.PORT_ADDRESS, "") == "") {
                try {
                    mConfigCasReader.put(appDeviceDriver.PORT_ADDRESS, PORT_ADDR_CASREADER);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            mConfigPasswordKeypad = propertys.optJSONObject(PASSWORD_KEYPAD);
            if (mConfigPasswordKeypad == null) {
                mConfigPasswordKeypad = new JSONObject();
            }
            if (mConfigPasswordKeypad.optString(appDeviceDriver.PORT_ADDRESS, "") == "") {
                try {
                    mConfigPasswordKeypad.put(appDeviceDriver.PORT_ADDRESS, PORT_ADDR_PASSWORD_KEYPAD);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            mConfigBiovo = propertys.optJSONObject(X3BIOVO);
            if (mConfigBiovo == null) {
                mConfigBiovo = new JSONObject();
            }
            if (mConfigBiovo.optString(appDeviceDriver.PORT_ADDRESS, "") == "") {
                try {
                    mConfigBiovo.put(appDeviceDriver.PORT_ADDRESS, PORT_ADDR_X3BIOVO);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            mConfigCRT310Reader = propertys.optJSONObject(CRT310_READER);
            if (mConfigCRT310Reader == null) {
                mConfigCRT310Reader = new JSONObject();
            }
            if (mConfigCRT310Reader.optString(appDeviceDriver.PORT_ADDRESS, "") == "") {
                try {
                    mConfigCRT310Reader.put(appDeviceDriver.PORT_ADDRESS, PORT_ADDR_CRT310READER);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


        } else {
            //读取配置失败
            mConfigPrinter = new JSONObject();
            try {
                mConfigPrinter.put(appDeviceDriver.PORT_ADDRESS, PORT_ADDR_PRINTER);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mConfigBankReader = new JSONObject();
            try {
                mConfigBankReader.put(appDeviceDriver.PORT_ADDRESS, PORT_ADDR_BANKREADER);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mConfigCasReader = new JSONObject();
            try {
                mConfigCasReader.put(appDeviceDriver.PORT_ADDRESS, PORT_ADDR_CASREADER);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mConfigPasswordKeypad = new JSONObject();
            try {
                mConfigPasswordKeypad.put(appDeviceDriver.PORT_ADDRESS, PORT_ADDR_PASSWORD_KEYPAD);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mConfigBiovo = new JSONObject();
            try {
                mConfigBiovo.put(appDeviceDriver.PORT_ADDRESS, PORT_ADDR_X3BIOVO);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mConfigCRT310Reader = new JSONObject();
            try {
                mConfigCRT310Reader.put(appDeviceDriver.PORT_ADDRESS, PORT_ADDR_CRT310READER);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
