package com.androidex.devices;

import android.content.Context;

import com.androidex.aexlibs.WebJavaBridge;
import com.androidex.logger.Log;
import com.androidex.plugins.OnBackCall;
import com.androidex.plugins.kkserial;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by cts on 17/5/16.
 */

public abstract class aexddBiovo extends appDeviceDriver implements WebJavaBridge.OnJavaBridgePlugin{
    public static final String TAG = "biovo";
    protected int mSerialFd;
    protected kkserial serial;
    protected Thread pthread = null;

    public aexddBiovo(Context ctx) {
        super(ctx);
        this.mDeviceType = 4354;

        try {
            WebJavaBridge.pluginManager.pluginInfo e = this.getPlugin("kkserial");
            this.serial = (kkserial)e.plugin;
        } catch (ClassNotFoundException var3) {
            var3.printStackTrace();
        }

    }

    public aexddBiovo(Context ctx, JSONObject args) {
        super(ctx, args);
        this.mDeviceType = 4609;

        try {
            WebJavaBridge.pluginManager.pluginInfo e = this.getPlugin("kkserial");
            this.serial = (kkserial)e.plugin;
        } catch (ClassNotFoundException var4) {
            var4.printStackTrace();
        }

    }

    public void onBackCallEvent(int _code, String _msg) {
        Log.d("printer", String.format("code=%d,msg=%s\n", new Object[]{Integer.valueOf(_code), _msg}));
        if(this.mContext != null && this.mContext instanceof OnBackCall) {
            OnBackCall listener = (OnBackCall)this.mContext;
            listener.onBackCallEvent(_code, _msg);

        }
    }

    @Override
    public boolean Open() {
        String printerPort = this.mParams.optString("portAddress");
        this.mSerialFd = this.serial.serial_open(printerPort);
        return this.mSerialFd > 0;
    }

    @Override
    public boolean Close() {
        this.serial.native_serial_close(this.mSerialFd);
        this.mSerialFd = 0;
        return true;
    }

    @Override
    public int WriteData(byte[] bytes, int size) {
        int ret = this.serial.serial_write(this.mSerialFd, bytes, size);
        return ret;
    }

    @Override
    public int WriteDataHex(String Data) {
        int ret = this.serial.serial_writeHex(this.mSerialFd, Data);
        return ret;
    }

    @Override
    public byte[] ReciveData(int length, int timeout) {
        byte[] data = this.serial.serial_read(this.mSerialFd, length, timeout);
        return data;
    }

    @Override
    public String ReciveDataHex(int length, int timeout) {
        String data = this.serial.serial_readHex(this.mSerialFd, length, timeout);
        return data;
    }

    /**
     * WebJavaBridge.OnJavaBridgePlugin接口的函数，当Web控件通过js调用插件时会调用此函数。
     *
     * @param action     js调用java的动作
     * @param jsonObject       js调用java的参数
     * @param callbackId js调用java完成后返回结果的回调函数
     * @return 返回结果，它会作为回调函数的参数使用
     */
    @Override
    public JSONObject onExecute(String action, JSONObject jsonObject, String callbackId) {
        JSONObject obj = new JSONObject();

        try {
            obj.put("success", false);

            try {
                if(action.equals("biovo")) {

                }
            } catch (Exception var6) {
                obj.put("success", false);
                obj.put("message", var6.getLocalizedMessage());
            }
        } catch (JSONException var7) {
            var7.printStackTrace();
        }

        return obj;
    }

    public abstract boolean selfTest();


}
