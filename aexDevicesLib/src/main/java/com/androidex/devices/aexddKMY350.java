package com.androidex.devices;

import android.content.Context;

import com.androidex.apps.aexdeviceslib.R;
import com.androidex.common.Base16;
import com.androidex.logger.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yangjun on 2016/10/24.
 */

public class aexddKMY350 extends aexddPasswordKeypad {
    static
    {
        try {
            System.loadLibrary("appDevicesLibs");
        } catch (UnsatisfiedLinkError e) {
            Log.d("KMY350", "appDevicesLibs.so library not found!");
        }
    }

    public static final String TAG = "kmy350";

    public aexddKMY350(Context ctx) {
        super(ctx);
    }

    public aexddKMY350(Context ctx, JSONObject args) {
        super(ctx, args);
    }

    @Override
    public String getDeviceName() {
        return mContext.getString(R.string.DEVICE_PK_KMY350);
    }

    @Override
    public boolean Open() {
        String printerPort = mParams.optString(PORT_ADDRESS);
        if(mSerialFd > 0)
            Close();
        String ret = kmyOpen(printerPort);
        try {
            JSONObject r = new JSONObject(ret);
            mSerialFd = r.optInt("fd",0);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return mSerialFd > 0;
    }

    @Override
    public boolean Close()
    {
        kmyClose();
        mSerialFd = 0;
        return true;
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

                if (action.equals("pkReset")) {
                    pkReset();
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

    @Override
    public boolean selfTest() {
        return false;
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
                //OnBackCall.ONBACKCALL_RECIVEDATA；
            }
        };
        pthread = new Thread(run);
        pthread.start();
        return 0;
    }

    @Override
    public String getStatusStr(int st)
    {
        switch(st){
            case 0x15:
                return "命令参数错";
            case 0x80:
                return "超时错误";
            case 0xA4:
                return "命令可成功执行,但主密钥无效";
            case 0xB5:
                return "命令无效,且主密钥无效";
            case 0xC4:
                return "命令可成功执行,但电池可能损坏";
            case 0xD5:
                return "命令无效,且电池可能损坏";
            case 0xE0:
                return "无效命令";
            default:
                if((st&0xF0) == 0xF0){
                    switch(st&0x0F){
                        case 0:
                            return "自检错误，CPU错";
                        case 1:
                            return "自检错误，SRAM错";
                        case 2:
                            return "自检错误，键盘有短路错";
                        case 3:
                            return "自检错误，串口电平错";
                        case 4:
                            return "自检错误，CPU卡出错";
                        case 5:
                            return "自检错误，电池可能损坏";
                        case 6:
                            return "自检错误，主密钥失效";
                        case 7:
                            return "自检错误，杂项错";
                        default :
                            return "自检错误，未知类型";
                    }
                }else{
                    return "未知错误代码";
                }
        }
    }

    @Override
    public boolean pkReset()
    {
        boolean ret = false;
        String rhex = "";

        WriteDataHex("0131");
        byte[] r = ReciveData(255,3000*delayUint);
        rhex = new String(r);
        Log.d(TAG,String.format("pkReset:%s",rhex));
        ret = rhex.contains("303500");

        //kmyReset(3000*delayUint);
        return ret;
    }

    @Override
    public String pkGetVersion()
    {
        String ret = "";
        String rhex = "";

        WriteDataHex("0130");
        byte[] r = ReciveData(255,3000*delayUint);
        rhex = new String(r);
        try {
            r = Base16.decode(rhex.substring(5,5+32));
            ret = new String(r);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ret = kmyGetVersion(3000*delayUint);

        return ret;
    }

    /**
     * <ul>
     *     <strong>加密全套步骤</strong>
     *     <li>Reset:复位键盘，避免键盘处于其他状态影响最终结果。注意此复位不能清除主秘钥等</li>
     *     <li>Set encryt mode:设置加密模式，0:DES,1:3DES</li>
     *     <li>Set encrypt mac:设置Mac算法模式，01 MAC采用ASNI X9.9算法 *   02 MAC采用SAM卡算法  03 MAC采用银联的算法</li>
     *     <li>Download work key:下载工作秘钥</li>
     *     <li>Active work key:激活工作秘钥</li>
     *     <li>Start read key:读取并响应按键信息，按取消(0x1B)或者确认(0x0D)退出</li>
     * </ul>
     int ire=kmy_reset(kmy,env,obj,timeout);
     ire=kmy_set_encrypt_mode(kmy,env,obj,0,timeout);
     // __android_log_print(ANDROID_LOG_INFO, "kmy","Set encrypt mode %s",ire?"TRUE":"FALSE");
     ire=kmy_set_encrypt_mac(kmy,env,obj,1,timeout);
     // __android_log_print(ANDROID_LOG_INFO, "kmy","Set mac encrypt mode %s",ire?"TRUE":"FALSE");

     int r = kmy_dl_work_key(kmy,env,obj,mKeyNo,wKeyNo, chwKey,timeout);
     if(r){
     if(kmy_active_work_key(kmy,env,obj,mKeyNo,wKeyNo,timeout)){
     if(kmy_pin_block(kmy,env,obj,chcardNo,timeout)){
     kmy_event(kmy,env,obj,KE_START_PIN,"{success:true,status:\"%d\",msg:\"%s\"}",1,"请提示用户输入密码!");
     if(kmy_start_read_key(kmy,env,obj,chcb,timeout)){
     usleep(100000);
     return kmy_read_pin(kmy,env,obj,NULL,passHex,timeout);
     }else{
     __android_log_print(ANDROID_LOG_INFO, "kmy","kmy_start_read_key fail");
     }
     return FALSE;
     }else{
     return FALSE;
     }
     }else{
     return FALSE;
     }
     }else{
     return FALSE;
     }
     */
    // jni相关函数
    public  native String  kmyOpen(String arg);//传入printerPort
    public  native void kmyClose();
    public  native int kmyReset(int timeout);
    public  native int kmyResetWithPpin(int timeout);
    public  native String kmyGetSn(int timeout);
    public  native int kmySetSn(String sn,int timeout);
    public  native String kmyGetVersion(int timeout);
    public  native int kmySetEncryptMode(int ewm,int timeout);
    public  native int kmyDlMasterKey(int MKeyNo, String MKeyAsc,int timeout);
    public  native int kmyDlWorkKey(int MKeyNo, int WKeyNo, String WKeyAsc,int timeout);
    public  native int kmyActiveWorkKey(int MKeyNo, int WKeyNo,int timeout);
    public  native int kmyOpenKeypad(int CTL,int timeout);
    public  native int kmyDlCardNo(String pchCardNo,int timeout);
    public  native int kmyStartPin(short PinLen, short DispMode, short AddMode, short PromMode,short nTimeOut,int timeout);
    public  native int kmyPinBlock(String pchCardNo,int timeout);
    public  native String kmyReadPin(int timeout);
    public  native String kmyEncrypt(String DataInput,int timeout);
    public  native String kmyDecrypt(String DataInput,int timeout);
    public  native String kmyCalcMacData(String DataInput,int timeout);
    public  native void kmyStartReadKey(String callback,int timeout);
    public  native String kmyStartAllStep(int mKeyNo,int wKeyNo,String wkey,String CardNo,String callback,String port,int timeout);
    public  native void kmySendCmd(int fd,String cmd,int size);
    public  native void kmySendHexCmd(int fd,String hexcmd,int size);

}
