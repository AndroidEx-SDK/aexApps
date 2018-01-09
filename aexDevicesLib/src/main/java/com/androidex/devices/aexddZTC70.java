package com.androidex.devices;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.androidex.apps.aexdeviceslib.R;
import com.androidex.common.SoundPoolUtil;
import com.androidex.logger.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by cts on 16/11/21.
 */
public class aexddZTC70 extends aexddPasswordKeypad {
    static {
        try {
            System.loadLibrary("appDevicesLibs");
        } catch (UnsatisfiedLinkError e) {
            Log.d("ZTC70", "appDevicesLibs.so library not found!");
        }
    }

    public static final String TAG = "ztc70";

    public aexddZTC70(Context ctx) {
        super(ctx);
    }

    public CallBack callBack;
    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                int key = (int) msg.obj;
                callBack.mCallBack(key);
            }
        }
    };

    public aexddZTC70(Context ctx, JSONObject args) {
        super(ctx, args);
    }

    //public CallBack callBack;
    @Override
    public String getDeviceName() {

        return mContext.getString(R.string.DEVICE_PK_ZTC70);
    }

    /**
     * 接收返按键返回信息的事件或者JNI的其他事件信息。
     * <ul>
     * <li>_code == 0x10100   表示按键信息，_msg表示按键的ASCII码</li>
     * </ul>
     *
     * @param _code 事件代码
     * @param _msg  事件消息
     */
    @Override
    public void onBackCallEvent(int _code, String _msg) {
        //KE_PRESSED = 0x10100
        switch (_code) {

            case 0x10100: {
                //按键信息
                try {
                    JSONObject msgArgs = new JSONObject(_msg);
                    int key = Integer.parseInt(msgArgs.optString("key"), 16);
                    android.util.Log.e("按键：", key + "");
                    if (key >= 0 && key <= 9) {
                        SoundPoolUtil.getSoundPoolUtil().loadVoice(mContext, key);
                        /*Message message = mHandler.obtainMessage();
                        message.obj = key;
                        message.what = 0;
                        mHandler.sendMessage(message);*/
                        //
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            }
            default:
                break;
        }
    }


    //设置一个回调，用于更新edxit

    public interface CallBack {
        public void mCallBack(int key);
    }

    public void setCallBack(CallBack cb) {
        callBack = cb;
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
                if (action.equals("pkReset")) {
                    pkReset();
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

    @Override
    public boolean selfTest() {
        if (pkGetVersion() != null) {
            Log.d(TAG, "密码键盘OK");
        } else {
            Log.d(TAG, "密码键盘失败");
        }
        // Toast.makeText(mContext,String.format("Version:%s", pkGetVersion()), Toast.LENGTH_LONG).show();
        //setQuery();
        // android.util.Log.e(TAG,String.format("Status:%s", getStatusStr(setQuery())));
        return true;
    }

    public void setQuery() {
        pkSetEncryptMode(2);
    }

    @Override
    public String getStatusStr(int st) {
        switch (st) {
            case 0x04:
                return "命令执行成功";
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
                if ((st & 0xF0) == 0xF0) {
                    switch (st & 0x0F) {
                        case 0:
                            return "自检错误，CPU错";
                        case 1:
                            return "自检错误，SRAM错";
                        case 2:
                            return "自检错误，键盘有短路错";
                        case 3:
                            return "自检错误，串口电平错";
                        case 4:
                            return "自检错" +
                                    "" +
                                    "误，CPU卡出错";
                        case 5:
                            return "自检错误，电池可能损坏";
                        case 6:
                            return "自检错误，主密钥失效";
                        case 7:
                            return "自检错误，杂项错";
                        default:
                            return "自检错误，未知类型";
                    }
                } else {
                    return "未知错误代码";
                }
        }
    }

    /**
     * 读取键盘按键信息，按键通过onBackCallEvent返回。
     *
     * @return
     */
    @Override
    public int ReciveDataLoop() {
        Runnable run = new Runnable() {
            public void run() {
                //在线程中执行jni函数
                r = ztReadKeyLoop(mSerialFd, 10000 * delayUint);
            }
        };
        pthread = new Thread(run);
        pthread.start();
        return r;
    }

    private int r;

    @Override
    public int pkReadLoop(int i) {
        return 0;
    }

    @Override
    public byte[] pkReadPacket(int timeout) {
        return ztReadPacket(mSerialFd, timeout);
    }

    /**
     * 发送键盘命令。
     *
     * @param cmd 命令内容，不包含头、尾和BCC
     */
    public void pkSendCmd(byte[] cmd, int size) {
        ztSendCmd(mSerialFd, cmd, size);
    }

    /**
     * 发送Base16格式命令。
     *
     * @param cmd Base16命令内容，不包含头、尾和BCC
     */
    public void pkSendHexCmd(String cmd) {
        ztSendHexCmd(mSerialFd, cmd, cmd.length());
    }

    /**
     * 程序自检复位:02h+01h+31h+<BCC>+[03h]
     * <p>
     * 键盘进行自检完毕，不破坏密钥区，如果主密钥有效(将 16 个主密钥用 BCC 校验)，将蜂鸣器响一声;无效蜂鸣器响三声，
     * 自检状态在 ST 中。返回信息后，复位所有变量，并关闭键盘及加密状态。
     * </p>
     * <p>
     * 命令返回:02h+01h+<ST>+<BCC>+[03h]。ST 可能是 04h、15h、E0h、FXh。
     * </p>
     *
     * @return 返回成功或失败
     */
    @Override
    public boolean pkReset() {
        boolean ret = false;
        String rhex = "";

        pkSendHexCmd("0131");
        rhex = ReciveDataHex(255, 3000 * delayUint);
        ret = !rhex.isEmpty();
        return ret;
    }

    /**
     * 取产品版本号等参数
     * <p>
     * <p>命令:02h+01h+30h+<BCC>+ [03h]</p>
     * <p>返回:02h+Ln+<ST>+<DATA>+<BCC>+[03h]。ST可能是 04h、15h、E0h、F0h。ST=F0h表示没装E2ROM芯片。</p>
     * <p>描述:DATA=Ver+SN+Rechang 其中 Ver 表示 16 字节(ASCII 码)版本号，SN 前 4 字节(BCD)表示生产序 号，
     * 后 4 个字节是全为“00”(如果有密码算法芯片，则是其编号)，Rechang 表示 2 字节充电时间(需 硬件支持)。
     * 返回信息后关闭加密状态。</p>
     * </p>
     *
     * @return
     */
    @Override
    public String pkGetVersion() {
        String ret = null;
        //pkSendHexCmd("0233013330333103");
        WriteDataHex("0230313330333103");
        byte[] r = pkReadPacket(10000 * delayUint);
        String s = hexString(r);
        if (s != null) {
            ret = s.substring(10, 30);//35 41 35 34 33 35 33 39 33 38
            //对接收的字符串进行处理
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(s.charAt(0));
            stringBuilder.append(s.charAt(1));
            for (int i = 1; i <= (s.length() - 2) / 2; i++) {
                stringBuilder.append(s.charAt(2 * i + 1));
            }

            String str = stringBuilder.substring(4, 6);
            //Toast.makeText(mContext, getStatusStr(Integer.parseInt(str, 16)), Toast.LENGTH_LONG).show();
            Log.e(TAG, "获取密码键盘版本号的结果=" + str);
        }
        return ret;
    }

    /**
     * 设置加密模式
     *
     * @param mode
     */
    public void pkSetEncryptMode(int mode) {
        String r = "";
        switch (mode) {
            case 0: {
                pkSendHexCmd("03810020");
                r = ReciveDataHex(255, 3000 * delayUint);
                pkSendHexCmd("03810030");
                r = ReciveDataHex(255, 3000 * delayUint);
                pkSendHexCmd("03810040");
                r = ReciveDataHex(255, 3000 * delayUint);
            }
            break;
            case 1: {
                pkSendHexCmd("03810110");
                r = ReciveDataHex(255, 3000 * delayUint);
                pkSendHexCmd("03810120");
                r = ReciveDataHex(255, 3000 * delayUint);
            }
            break;
            case 2: {

               /* pkSendHexCmd("03810210");
                r = ReciveDataHex(255, 3000 * delayUint);*/
               /*
                r = ReciveDataHex(500, 5000 * delayUint);
               */
                pkSendHexCmd("03810220");
                byte[] b = pkReadPacket(delayUint * delayUint * 30);
            }
            break;
        }
    }

    /**
     * 转换成16进制
     *
     * @param b
     * @return
     */
    public String hexString(byte[] b) {
        if (b != null) {
            StringBuffer string = new StringBuffer();
            for (int i = 0; i < b.length; i++) {
                String hex = Integer.toHexString(b[i] & 0xFF);
                if (hex.length() == 1) {
                    hex = '0' + hex;
                }
                string.append(hex.toUpperCase());
            }
            return string.toString();
        } else {
            return null;
        }
    }

    /**
     * 数据 MAC 运算 (ANSI X9.9)
     * <p>命令:02h+<Ln>+41h+<字符串>+<BCC>+[03h]</p>
     * <p>描述:将 Ln(5~247)个字节明文字符串，用当前的工作密钥(DES/3DES)以 CBC 方式进行加密运算 C1=eK(P1)及 Ci=eK(Pi⊕C i-1)i=2,3, ...,n。返回 8 字节 MAC 字串数据。返回 MAC 信息后关闭加密状态。</p>
     * <p>返回:02h+09h+<ST>+<MAC 字串>+<BCC>+[03h]。 ST 可能是 04h、15h、A4h、B5h、C4h、D5h、E0h。 </p>
     * <p>注意:MAC 是按 8 字节进行分组，每组需要 25/75mS 等待 DES/3DES 运算，根据此确立等待返回时间。</p>
     *
     * @param mode 设置Mac加密模式
     */
    public void pkSetEncryptMac(int mode) {

    }

    /**
     * 下载工作密钥
     * <p>命令: 02h+0Bh+33h+<M>+<N>+<WP>+<BCC>+[03h] 或 02h+13h+33h+<M>+<N>+<WP>+<BCC>+[03h] 或 02h+1Bh+33h+<M>+<N>+<WP>+<BCC>+[03h]</p>
     * <p>描述:工作密钥密文 WP 均为 8/16/24 字节(对应 DES/3DES)。用主密钥号为 M 的主密钥(DES/3DES) ，以 ECB 方式解密得到工作密钥 WK，保存到指定的工作密钥号 N(00~03h)中。如果命令中工作密钥号 N =40h~7Fh，保存到对应的工作密钥号 N(00~3Fh)中，此时以验证方式返回信息。返回信息后关闭加 密状态。</p>
     * <p>返回:02h+01h+<ST>+<BCC>+[03h]。ST 可能是 04h、15h、A4h、B5h、C4h、D5h、E0h。 注:验证方式返回 02h+05h+ST+<DATA>+<BCC>+[03h]。其中<DATA>4 个字节返回码作验证用。</p>
     *
     * @param mKeyNo  主秘钥序号
     * @param wKeyNo  工作秘钥序号
     * @param wKeyAsc 工作秘钥
     */
    public void pkDownloadWorkKey(int mKeyNo, int wKeyNo, String wKeyAsc) {

    }

    /**
     * 激活工作密钥
     * <p>命令:02h+03h+43h+<M>+<N>+<BCC>+[03h]</p>
     * <p>描述:如果在 B.16 命令中，指定主密钥作为当前工作密钥的方案，激活的是 M(00~0Fh)号的主密钥，与工作密钥无关，但会验证主密钥有效性。如果在 B.16 命令中，指定工作密钥作为当前工作密钥的方 案，将主密钥号为 M 所属工作密钥号为 N 激活为当前工作密钥，也会验证主密钥有效性。但是这种 情况下，如果工作密钥号 N=40~7Fh，与主密钥 M 无关，不验证主密钥有效性。</p>
     * <p>      总之一旦激活了当前工作密钥，以后所有密码运算用都是指定该当前工作密钥。返回信息后不关闭加密状态。</p>
     * <p>返回: 02h+01h+<ST>+<BCC>+[03h]。ST 可能是 04h、15h、A4h、B5h、C4h、D5h、E0h。</p>
     *
     * @param mKeyNo 主秘钥序号
     * @param wKeyNo 工作秘钥序号
     */
    public void pkActiveWorkKey(int mKeyNo, int wKeyNo) {

    }

    /**
     * <p>命令:02h+<Ln>+36h+<字符串>+<BCC>+[03h]</p>
     * <p>描述:将(Ln-1=)8 倍字节明文字符串用当前工作密钥(DES/3DES)以 ECB 方式进行加密运算 C=eK(P)，</p>
     * <p>返回密文数据。返回信息后关闭加密状态。要求 Ln-1 表示小于等于 248 字节。</p>
     * <p>返回: 02h+<Ln>+<ST>+<密文字串>+<BCC>+[03h]。 ST 可能是 04h、15h、A4h、B5h、C4h、D5h、E0h。</p>
     *
     * @param data
     * @return
     */
    public String pkEncrypt(String data) {
        String ret = "";

        return ret;
    }

    /**
     * <p>命令:02h+<Ln>+37h+<密文字串>+<BCC>+[03h]</p>
     * <p>描述:将(Ln-1=)8 倍字节密文字符串用当前工作密钥(DES/3DES)以 ECB 方式进行解密运算 P=dK(C)，</p>
     * <p>返回明文数据。返回信息后关闭加密状态。要求 Ln-1 表示小于等于 248 字节。</p>
     * <p>返回: 02h+<Ln>+<ST>+<明文字串>+<BCC>+[03h]。ST 可能是 04h、15h、A4h、B5h、C4h、D5h、E0h。</p>
     *
     * @param data
     * @return
     */
    public String pkDecrypt(String data) {
        String ret = "";

        return ret;
    }

    /**
     * <ul>
     * <strong>加密全套步骤</strong>
     * <li>Reset:复位键盘，避免键盘处于其他状态影响最终结果。注意此复位不能清除主秘钥等</li>
     * <li>Set encryt mode:设置加密模式，0:DES,1:3DES</li>
     * <li>Set encrypt mac:设置Mac算法模式，01 MAC采用ASNI X9.9算法 *   02 MAC采用SAM卡算法  03 MAC采用银联的算法</li>
     * <li>Download work key:下载工作秘钥</li>
     * <li>Active work key:激活工作秘钥</li>
     * <li>Pin block:使用提供的银行卡号进行PINBLOCK运算</li>
     * <li>Start read key:读取并响应按键信息，按取消(0x1B)或者确认(0x0D)或者超时退出</li>
     * <li>Read pin:读取密码密文</li>
     * </ul>
     */
    public String pkStartAllStep() {
        String ret = "";
        //
        return ret;
    }

    public native byte[] ztReadPacket(int fd, int timeout);

    public native int ztReadKeyLoop(int fd, int timeout);

    public native void ztSendCmd(int fd, byte[] cmd, int size);

    public native void ztSendHexCmd(int fd, String hexcmd, int size);

}
