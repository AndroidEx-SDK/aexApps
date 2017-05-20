package com.androidex.devices;

import android.content.Context;

import com.androidex.apps.aexdeviceslib.R;
import com.androidex.logger.Log;

import org.json.JSONObject;

/**
 * X3指纹仪
 * Created by cts on 17/5/16.
 */

public class aexddX3Biovo extends aexddBiovo {
    public static final String TAG = "X3Biovo";

    static {
        try {
            System.loadLibrary("appDevicesLibs");
        } catch (UnsatisfiedLinkError e) {
            Log.d(TAG, "appDevicesLibs.so library not found!");
        }
    }

    public aexddX3Biovo(Context ctx) {
        super(ctx);
    }

    public aexddX3Biovo(Context ctx, JSONObject args) {
        super(ctx, args);
    }

    @Override
    public boolean selfTest() {
        if (getUserNum()!=null){
            Log.d(TAG, "指纹识别仪测试OK");
        }else {
            Log.d(TAG, "指纹识别仪测试失败");
        }

        return false;
    }


    @Override
    public String getDeviceName() {
        return mContext.getString(R.string.DEVICE_X3_BIOVO);
    }

    /**
     * @return
     */
    @Override
    public int ReciveDataLoop() {
        Runnable run = new Runnable() {
            public void run() {
                //在线程中执行jni函数
                //OnBackCall.ONBACKCALL_RECIVEDATA



            }
        };
        pthread = new Thread(run);
        pthread.start();
        return 0;
    }

    /**
     *
     * @return
     */
    public String getUserNum() {

        WriteDataHex("F5090000000009F5");
        String data = ReciveDataHex(20, 3 * 1000);

        Log.e(TAG,"===data:"+data);
        if (data != null) {
            return data;
        }
        return null;
    }
}
