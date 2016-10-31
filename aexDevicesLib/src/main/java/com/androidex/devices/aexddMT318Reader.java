package com.androidex.devices;

import android.content.Context;

import com.androidex.apps.aexdeviceslib.R;
import com.androidex.hwuitls.Base16;
import com.androidex.logger.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yangjun on 2016/10/24.
 */

public class aexddMT318Reader extends aexddPoscReader {

    public static final String TAG = "mt318";

    public aexddMT318Reader(Context ctx) {
        super(ctx);
    }

    public aexddMT318Reader(Context ctx, JSONObject args) {
        super(ctx, args);
    }

    @Override
    public String getDeviceName() {
        return mContext.getString(R.string.DEVICE_READER_MT318);
    }

    /**
     * WebJavaBridge.OnJavaBridgePlugin接口的函数，当Web控件通过js调用插件时会调用此函数。
     * @param action        js调用java的动作
     * @param args          js调用java的参数
     * @param callbackId    js调用java完成后返回结果的回调函数
     * @return              返回结果，它会作为回调函数的参数使用
     */
    @Override
    public JSONObject onExecute(String action, JSONObject args, String callbackId) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("success", false);
            try {

                if (action.equals("queryCard")) {
                    int index = 0;
                    String r;
                    String arg = args.optString("params");
                }else if (action.equals("popCard")) {
                    int timeout = 10000*delayUint;
                    obj.put("success", true);
                }else{
                    obj = super.onExecute(action,args,callbackId);
                }

            } catch (Exception e) {
                obj.put("success", false);
                obj.put("message", e.getLocalizedMessage());
            }
        } catch (JSONException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        return obj;
    }

    /**
     *
     * @return
     */
    @Override
    public int ReciveDataLoop() {
        Runnable run=new Runnable() {
            public void run() {
                //在线程中执行jni函数
                //OnBackCall.ONBACKCALL_RECIVEDATA
            }
        };
        pthread = new Thread(run);
        pthread.start();
        return 0;
    }

    @Override
    public boolean selfTest() {
        return true;
    }

    @Override
    public boolean reset()
    {
        boolean ret = false;
        //
        WriteDataHex("3040");
        String r = ReciveDataHex(255,3000*delayUint);
        Log.d(TAG,String.format("readerReset:%s",r));
        ret = r.length() > 0;
        return ret;
    }

    @Override
    public boolean popCard(){
        boolean ret = false;
        String rhex = "";
        //
        WriteDataHex("3240");
        byte[] r = ReciveData(255,3000*delayUint);
        try {
            rhex = Base16.encode(r);
            Log.d(TAG,String.format("readerPopCard:%s",rhex));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(r.length > 5) {
            ret = r[5] == 0x59;     //rd[5] = 0x59
        }
        return ret;
    }

    @Override
    public int m1Serial(){
        int ret = 0;
        String rhex = "";
        //
        WriteDataHex("3431");
        byte[] r = ReciveData(255,3000*delayUint);
        try {
            rhex = Base16.encode(r);
            Log.d(TAG,String.format("readerM1Serial:%s", rhex));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(r.length > 9) {
            if(r[5] == 'Y'){
                ret = r[6]<<24 + r[7]<<16 + r[8]<<8 + r[9];
            }
        }
        return ret;
    }

    @Override
    public String getCardStatusString(int type)
    {
        switch(type){
            case 0x3F: // 卡机内无卡或卡机内有未知类型卡
                return "卡机内无卡或卡机内有未知类型卡";
            case 0x31: // 接触式cpu卡
                return "接触式CPU卡";
            case 0x32: // RF--TYPE B CPU卡
                return "RF--TYPE B CPU卡";
            case 0x33: // RF—TYPE A CPU卡
                return "RF--TYPE A CPU卡";
            case 0x34: // RF—M1卡
                return "RF-M1卡";
            case 0x37: // 磁卡
                return "磁卡";
            case 0x38: // 磁卡和M1卡
                return "磁卡和M1卡";
            case 0x39: // 磁卡和接触式cpu卡
                return "磁卡和接触式cpu卡";
            case 0x3A: // 接触式cpu卡和M1卡
                return "接触式cpu卡和M1卡";
            case 0x3B: // 磁卡、接触式cpu卡和M1卡
                return "磁卡、接触式cpu卡和M1卡";
            default :
                return "未知代码";
        }
    }

    @Override
    public int queryCard()
    {
        int ret = 0;
        String rhex = "";
        //
        WriteDataHex("3144");
        byte[] r = ReciveData(255,3000*delayUint);
        try {
            rhex = Base16.encode(r);
            Log.d(TAG,String.format("readerQueryCard:%s", rhex));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(r.length > 9) {
            if(r[3] == 0x31 && r[4] == 0x44 && (r[5] == 0x30 || r[5] == 0x31)){
                ret = r[6];
            }
        }
        return ret;
    }

    public native int native_Open(String arg);						           // 打开打印机
    public native int native_Close();								           // 关闭打印机

}
