package com.androidex.devices;

import android.content.Context;

import com.androidex.apps.aexdeviceslib.R;
import com.androidex.logger.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * Created by yangjun on 2016/10/24.
 */

public class aexddMT319Reader extends aexddPbocReader {
    static {
        try {
            System.loadLibrary("appDevicesLibs");
        } catch (UnsatisfiedLinkError e) {
            Log.d("MT319Reader", "appDevicesLibs.so library not found!");
        }
    }

    public static final String TAG = "mt319";
    private JSONObject mArgs;

    public aexddMT319Reader(Context ctx) {
        super(ctx);
    }

    public aexddMT319Reader(Context ctx, JSONObject args) {
        super(ctx, args);
        mArgs = args;
    }

    @Override
    public String getDeviceName() {
        return mContext.getString(R.string.DEVICE_READER_MT319);
    }

    /**
     * 接受读卡器返回的事件
     *
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
    public boolean Close() {
        return super.Close();
    }

    /**
     * WebJavaBridge.OnJavaBridgePlugin接口的函数，当Web控件通过js调用插件时会调用此函数。
     *
     * @param action     js调用java的动作
     * @param args       js调用java的参数
     * @param callbackId js调用java完成后返回结果的回调函数
     * @return 返回结果，它会作为回调函数的参数使用
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
                } else if (action.equals("popCard")) {
                    int timeout = 10000 * delayUint;
                    obj.put("success", true);
                } else {
                    obj = super.onExecute(action, args, callbackId);
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
     * @return
     */
    @Override
    public int ReciveDataLoop() {
        Runnable run = new Runnable() {
            public void run() {
                //在线程中执行jni函数
                //OnBackCall.ONBACKCALL_RECIVEDATA
                mt319ReadCardLoop(mSerialFd, 0);
            }
        };
        pthread = new Thread(run);
        pthread.start();
        return 0;
    }

    @Override
    public byte[] pbocReadPacket(int timeout) {
        return mt319ReadPacket(mSerialFd, timeout);
    }

    @Override
    public int pbocReadCardLoop(int timeout) {
        return mt319ReadCardLoop(mSerialFd, timeout);
    }

    @Override
    public void pbocSendCmd(byte[] cmd, int size) {
        mt319SendCmd(mSerialFd, cmd, size);
    }

    @Override
    public void pbocSendHexCmd(String hexcmd) {
        mt319SendHexCmd(mSerialFd, hexcmd);
    }

    /**
     * 设备自检函数，MT319首先获取版本号，接着查询插卡状态，如果显示正常则说明自检成功。
     *
     * @return 成功返回true，否则返回false
     */
    @Override
    public boolean selfTest()
    {

        if (getVersion() != null) {
            Log.d(TAG, "读卡器OK");
        } else {
            Log.d(TAG, "读卡器失败");
        }

//		Log.i(TAG,String.format("Version:%s",getVersion()));
//		Log.i(TAG,String.format("Status:%s",getCardStatusString(queryStatus())));
        int i = cpuPOR();
        if (i == 0x59) {
            JSONObject trackInfo = getTrackInfo();
            try {
                String tarck2 = trackInfo.getString("tarck2");
                String tarck3 = trackInfo.getString("tarck3");
                Log.i(TAG, String.format("TrackInfo:%s", trackInfo.toString()));
                Log.i(TAG, String.format("tarck2%s---tarck3:%s", tarck2, tarck3));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (trackInfo != null) {

            }
        }
        return true;
    }

    public boolean selfTest(int flag){
        if (flag==1){
            if (getVersion() != null) {
                Log.d(TAG, "银行卡读卡器OK");
            } else {
                Log.d(TAG, "银行卡读卡器失败");
            }
        }else if (flag==2){
            if (getVersion() != null) {
                Log.d(TAG, "燃气卡读卡器OK");
            } else {
                Log.d(TAG, "燃气卡读卡器失败");
            }
        }


//		Log.i(TAG,String.format("Version:%s",getVersion()));
//		Log.i(TAG,String.format("Status:%s",getCardStatusString(queryStatus())));
        int i = cpuPOR();
        if (i == 0x59) {
            JSONObject trackInfo = getTrackInfo();
            try {
                String tarck2 = trackInfo.getString("tarck2");
                String tarck3 = trackInfo.getString("tarck3");
                Log.i(TAG, String.format("TrackInfo:%s", trackInfo.toString()));
                Log.i(TAG, String.format("tarck2%s---tarck3:%s", tarck2, tarck3));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (trackInfo != null) {

            }
        }
        return true;
    }

    /**
     * 获得读卡器版本信息
     *
     * @return 成功返回版本字符串，否则返回空
     */
    @Override
    public String getVersion() {
        String result = null;
        pbocSendHexCmd("3140");
        byte[] r = pbocReadPacket(3000 * delayUint);
        if (r != null && r.length > 5) {
            int mlen = (r[1] << 8) | r[2];
            if (r[3] == 0x31 && r[4] == 0x40)
                result = new String(r, 5, mlen - 2);
        }
        return result;
    }

    /**
     * 查询设备的状态，将状态S1和S2合并，status=S1<<8 | S2。
     *
     * @return 合并后的状态码
     */
    @Override
    public int queryStatus() {
        int result = 0;
        pbocSendHexCmd("3144");
        byte[] r = pbocReadPacket(3000 * delayUint);
        if (r != null && r.length > 6) {
            int s1 = r[5];
            int s2 = r[6];
            result = (s1 << 8) | s2;
        }
        return result;
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
     *
     * @param type 状态
     * @return 返回状态字符串信息
     */
    @Override
    public String getCardStatusString(int type) {
        String s1 = "", s2 = "";
        switch (type & 0xFF00) {
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
            case 0x3500:
                s1 = "AT88SC102卡";
                break;
            case 0x3600:
                s1 = "SLE4442卡";
                break;
            case 0x3700:
                s1 = "AT88SC153卡";
                break;
            case 0x3800:
                s1 = "AT88SC1608卡";
                break;
            case 0x3F00:
                s1 = "卡机内有无法识别卡";
                break;
            default:
                s1 = "未知";
        }
        switch (type & 0x00FF) {
            case 0x30:
                s2 = "无可读磁卡信息";
                break;
            case 0x31:
                s2 = "有可读磁卡信息";
                break;
            default:
                s2 = "未知";
        }

        return String.format("%s-%s", s1, s2);
    }

    /**
     * 读取磁道信息，如果相应的磁道有读到数据则设置，否则为空或者未设置。
     * <ul>
     * <li>磁道1：track1</li>
     * <li>磁道2：track2</li>
     * <li>磁道3：track3</li>
     * </ul>
     *
     * @return 存储磁道信息的JSON对象
     */
    @Override
    public JSONObject getTrackInfo() {
        JSONObject track = new JSONObject();
        pbocSendHexCmd("3B35");
        byte[] r = pbocReadPacket(3000 * delayUint);
        if (r != null && r.length > 5) {
            int mlen = (r[1] << 8) | r[2];
            if (r[3] == 0x3B && r[4] == 0x35) {
                if (r[5] == 0x4E) {
                    //读磁道错误
                    if (r[6] != 0x03) {
                        //三个磁道有部分正确
                        try {
                            int len1 = 1;
                            if (r[6] != 0xE1) {
                                while (r[6 + len1] != 0x00) len1++;
                                track.put("tarck1", new String(r, 6, len1++));
                            } else len1++;
                            int len2 = 1;
                            if (r[6 + len1] != 0xE2) {
                                while (r[6 + len1 + len2] != 0x00) len2++;
                                track.put("tarck2", new String(r, 6 + len1, len2++));
                            } else len2++;
                            int len3 = 1;
                            if (r[6 + len1 + len2] != 0xE3) {
                                while (r[6 + len1 + len2 + len3] != 0x03) len3++;
                                track.put("tarck3", new String(r, 6 + len1 + len2, len3));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    return track;
                } else if (r[5] == 0x59) {
                    //三个磁道均读正确
                    int len1 = 0;
                    while (r[6 + len1] != 0x00) len1++;
                    try {
                        track.put("tarck1", new String(r, 6, len1++));
                        int len2 = 0;
                        while (r[6 + len1 + len2] != 0x00) len2++;
                        track.put("tarck2", new String(r, 6 + len1, len2++));
                        int len3 = 0;
                        while (r[6 + len1 + len2 + len3] != 0x03) len3++;
                        track.put("tarck3", new String(r, 6 + len1 + len2, len3));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return track;
    }

    /**
     * 清读磁卡标志
     * READER 返回:
     * 操作状态字 P=‘Y’(0x59) 清标志成功 P=‘N’(0x4E) 清标志失败
     *
     * @return 成功返回true，否则返回false
     */
    @Override
    public boolean clearTrackInfo() {
        boolean result = false;
        pbocSendHexCmd("3B36");
        byte[] r = pbocReadPacket(3000 * delayUint);
        if (r != null && r.length == 8) {
            if (r[3] == 0x3B && r[4] == 0x36) {
                result = r[5] == 0x59;
            }
        }
        return result;
    }

    /**
     * 上电复位
     *
     * @return <ul>
     * <li>返回 0 表示指令执行失败</li>
     * <li>返回 0x59(Y)  上电复位成功</li>
     * <li>返回 0x4E(N)  复位失败</li>
     * <li>返回 0x45(E)  机内无卡</li>
     * </ul>
     */
    @Override
    public int cpuPOR() {
        int result = 0;
        pbocSendHexCmd("3740");
        byte[] r = pbocReadPacket(3000 * delayUint);
        if (r != null && r.length >= 8) {
            if (r[3] == 0x37 && r[4] == 0x40) {
                result = r[5];
            }
        }
        return result;
    }

    /**
     * 热复位
     *
     * @return <ul>
     * <li>返回 0 表示指令执行失败</li>
     * <li>返回 0x59(Y)  上电复位成功</li>
     * <li>返回 0x4E(N)  复位失败</li>
     * <li>返回 0x45(E)  机内无卡</li>
     * </ul>
     */
    @Override
    public int cpuReset() {
        int result = 0;
        pbocSendHexCmd("3741");
        byte[] r = pbocReadPacket(3000 * delayUint);
        if (r != null && r.length >= 8) {
            if (r[3] == 0x37 && r[4] == 0x41) {
                result = r[5];
            }
        }
        return result;
    }

    /**
     * 下电休眠
     *
     * @return <ul>
     * <li>返回 0 表示指令执行失败</li>
     * <li>返回 0x59(Y)  上电复位成功</li>
     * <li>返回 0x4E(N)  复位失败</li>
     * <li>返回 0x45(E)  机内无卡</li>
     * </ul>
     */
    @Override
    public int cpuHibernate() {
        int result = 0;
        pbocSendHexCmd("3742");
        byte[] r = pbocReadPacket(3000 * delayUint);
        if (r != null && r.length >= 8) {
            if (r[3] == 0x37 && r[4] == 0x42) {
                result = r[5];
            }
        }
        return result;
    }

    /**
     * CPU卡执行APDU指令，返回APDU指令执行结果。
     *
     * @param apdu apdu指令
     * @return 返回apdu指令执行结果
     */
    @Override
    public byte[] cpuApdu(byte[] apdu) {
        int index = 0;
        byte[] data = new byte[apdu.length + 4];

        data[index++] = 0x37;
        data[index++] = 0x43;
        data[index++] = (byte) ((apdu.length >> 8) & 0xFF);
        data[index++] = (byte) (apdu.length & 0xFF);
        for (byte b : apdu) {
            data[index++] = b;
        }
        pbocSendCmd(data, data.length);
        byte[] r = pbocReadPacket(3000 * delayUint);
        if (r != null && r.length >= 8) {
            if (r[3] == 0x37 && r[4] == 0x43) {
                if (r[5] == 0x59) {
                    int len = ((r[6] << 8) | r[7]) & 0xFF00;
                    return Arrays.copyOfRange(r, 8, len + 8);
                }
            }
        }
        return null;
    }

    public native byte[] mt319ReadPacket(int fd, int timeout);

    public native int mt319ReadCardLoop(int fd, int timeout);

    public native void mt319SendCmd(int fd, byte[] cmd, int size);

    public native void mt319SendHexCmd(int fd, String hexcmd);

}
