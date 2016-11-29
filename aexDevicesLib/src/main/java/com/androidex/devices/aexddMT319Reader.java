package com.androidex.devices;

import android.content.Context;

import com.androidex.apps.aexdeviceslib.R;
import com.androidex.logger.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yangjun on 2016/10/24.
 */

public class aexddMT319Reader extends aexddPbocReader {
    static
    {
        try {
            System.loadLibrary("appDevicesLibs");
        } catch (UnsatisfiedLinkError e) {
            Log.d("B58TPrinter", "appDevicesLibs.so library not found!");
        }
    }

    public static final String TAG = "mt318";

    public aexddMT319Reader(Context ctx) {
        super(ctx);
    }

    public aexddMT319Reader(Context ctx, JSONObject args) {
        super(ctx, args);
    }

    @Override
    public String getDeviceName() {
        return mContext.getString(R.string.DEVICE_READER_MT319);
    }

    /**
     * 接受读卡器返回的事件
     * @param _code
     * @param _msg
     */
    @Override
    public void onBackCallEvent(int _code, String _msg) {
        super.onBackCallEvent(_code, _msg);
    }

    @Override
    public boolean Open() {
        return super.Open();
    }

    @Override
    public boolean Close()
    {
        return super.Close();
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
                mt319ReadCardLoop(mSerialFd,0);
            }
        };
        pthread = new Thread(run);
        pthread.start();
        return 0;
    }

    public byte[]   pbocReadPacket(int timeout)
    {
        return mt319ReadPacket(mSerialFd,timeout);
    }

    public int      pbocReadCardLoop(int timeout)
    {
        return mt319ReadCardLoop(mSerialFd,timeout);
    }

    public void     pbocSendCmd(String cmd,int size)
    {
        mt319SendCmd(mSerialFd,cmd,size);
    }

    public void     pbocSendHexCmd(String hexcmd)
    {
        mt319SendHexCmd(mSerialFd,hexcmd);
    }

    public boolean selfTest() {
        Log.i(TAG,String.format("Version:%s",getVersion()));
        Log.i(TAG,String.format("Status:%s",getCardStatusString(queryStatus())));
        return true;
    }

    /**
     * 获得读卡器版本信息
     * @return      成功返回版本字符串，否则返回空
     */
    public String getVersion()
    {
        String result = null;
        pbocSendHexCmd("3140");
        byte[] r = pbocReadPacket(3000*delayUint);
        if(r != null && r.length > 5){
            int mlen = (r[1]<<8) | r[2];
            if(r[3] == 0x31 && r[4] == 0x40)
                result = new String(r,5,mlen - 2);
        }
        return result;
    }

    public int queryStatus()
    {
        int result = 0;
        pbocSendHexCmd("3144");
        byte[] r = pbocReadPacket(3000*delayUint);
        if(r != null && r.length > 6){
            int s1 = r[5];
            int s2 = r[6];
            result = (s1<<8) | s2;
        }
        return result;
    }

    /**
     * 读取磁道信息，如果相应的磁道有读到数据则设置，否则为空或者未设置。
     * <ul>
     * <li>磁道1：track1</li>
     * <li>磁道2：track2</li>
     * <li>磁道3：track3</li>
     * </ul>
     * @return  存储磁道信息的JSON对象
     */
    public JSONObject getTrackInfo()
    {
        JSONObject track = new JSONObject();
        pbocSendHexCmd("3B35");
        byte[] r = pbocReadPacket(3000*delayUint);
        if(r != null && r.length > 5){
            int mlen = (r[1]<<8) | r[2];
            if(r[3] == 0x3B && r[4] == 0x35) {
                if(r[5] == 0x4E){
                    //读磁道错误
                    if(r[6] != 0x03) {
                        //三个磁道有部分正确
                        try {
                            int len1 = 1;
                            if(r[6] != 0xE1) {
                                while (r[6 + len1] != 0x00) len1++;
                                track.put("tarck1", new String(r, 6, len1++));
                            }else len1++;
                            int len2 = 1;
                            if(r[6+len1] != 0xE2) {
                                while (r[6 + len1 + len2] != 0x00) len2++;
                                track.put("tarck2", new String(r, 6 + len1, len2++));
                            }else len2++;
                            int len3 = 1;
                            if(r[6+len1+len2] != 0xE3) {
                                while (r[6 + len1 + len2 + len3] != 0x00) len3++;
                                track.put("tarck3", new String(r, 6 + len1 + len2, len3));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    return track;
                }else if(r[5] == 0x59){
                    //三个磁道均读正确
                    int len1 = 0;
                    while(r[6+len1] != 0x00)len1++;
                    try {
                        track.put("tarck1",new String(r,6,len1++));
                        int len2 = 0;
                        while(r[6+len1+len2] != 0x00)len2++;
                        track.put("tarck2",new String(r,6+len1,len2++));
                        int len3 = 0;
                        while(r[6+len1+len2+len3] != 0x00)len3++;
                        track.put("tarck3",new String(r,6+len1+len2,len3));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return track;
    }

    /**
     * S1:卡座状态
     * <ul>
     * <li>S1=0x30 卡机内无卡</li>
     * <li>S1=0x31 卡机内有接触式IC卡</li>
     * <li>S1=0x32 卡机内有非接触式A卡</li>
     * <li>S1=0x33 卡机内有非接触式B卡</li>
     * <li>S1=0x34 卡机内有非接触式M1卡</li>
     * <li>S1=0x3f 卡机内有无法识别卡</li>
     * </ul>
     * S2: 是否有可读磁卡信息
     * <ul>
     * <li>S2=0x30 无可读磁卡信息
     * <li>S2=0x31 有可读磁卡信息
     * </ul>
     * @param type      状态
     * @return  返回状态字符串信息
     */
    @Override
    public String getCardStatusString(int type)
    {
        String s1 = "" ,s2 = "";
        switch(type&0xFF00){
            case 0x3000:
                s1 = "卡机内无卡";
                break;
            case 0x3100:
                s1 = "卡机内有接触式IC卡";
                break;
            case 0x3200:
                s1 = "卡机内有非接触式A卡";
                break;
            case 0x3300:
                s1 = "卡机内有非接触式B卡";
                break;
            case 0x3400:
                s1 = "卡机内有非接触式M1卡";
                break;
            case 0x3F00:
                s1 = "卡机内有无法识别卡";
                break;
            default :
                s1 = "未知";
        }
        switch(type&0x00FF){
            case 0x30:
                s2 = "无可读磁卡信息";
                break;
            case 0x31:
                s2 = "有可读磁卡信息";
                break;
            default :
                s2 = "未知";
        }

        return String.format("%s-%s",s1,s2);
    }

    public  native byte[]   mt319ReadPacket(int fd,int timeout);
    public  native int      mt319ReadCardLoop(int fd,int timeout);
    public  native void     mt319SendCmd(int fd,String cmd,int size);
    public  native void     mt319SendHexCmd(int fd,String hexcmd);

}
