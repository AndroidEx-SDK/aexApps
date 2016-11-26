package com.androidex.devices;

import android.content.Context;

import com.androidex.apps.aexdeviceslib.R;
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

    /**
     * 发送键盘命令。
     * @param cmd   命令内容，不包含头、尾和BCC
     */
    public void pkSendCmd(String cmd)
    {
        kmySendCmd(mSerialFd,cmd,cmd.length());
    }

    /**
     * 发送Base16格式命令。
     * @param cmd   Base16命令内容，不包含头、尾和BCC
     */
    public void pkSendHexCmd(String cmd)
    {
        kmySendHexCmd(mSerialFd,cmd,cmd.length());
    }

    /**
     * 程序自检复位:02h+01h+31h+<BCC>+[03h]
     * <p>
     *     键盘进行自检完毕，不破坏密钥区，如果主密钥有效(将 16 个主密钥用 BCC 校验)，将蜂鸣器响一声;无效蜂鸣器响三声，
     *     自检状态在 ST 中。返回信息后，复位所有变量，并关闭键盘及加密状态。
     * </p>
     * <p>
     *     命令返回:02h+01h+<ST>+<BCC>+[03h]。ST 可能是 04h、15h、E0h、FXh。
     * </p>
     * @return  返回成功或失败
     */
    @Override
    public boolean pkReset()
    {
        boolean ret = false;
        String rhex = "";

        pkSendHexCmd("0131");
        rhex = ReciveDataHex(255,3000*delayUint);
        ret = !rhex.isEmpty();
        return ret;
    }

    /**
     * 取产品版本号等参数
     * <p>
     *     <p>命令:02h+01h+30h+<BCC>+ [03h]</p>
     *     <p>返回:02h+Ln+<ST>+<DATA>+<BCC>+[03h]。ST可能是 04h、15h、E0h、F0h。ST=F0h表示没装E2ROM芯片。</p>
     *     <p>描述:DATA=Ver+SN+Rechang 其中 Ver 表示 16 字节(ASCII 码)版本号，SN 前 4 字节(BCD)表示生产序 号，
     *     后 4 个字节是全为“00”(如果有密码算法芯片，则是其编号)，Rechang 表示 2 字节充电时间(需 硬件支持)。
     *     返回信息后关闭加密状态。</p>
     * </p>
     * @return
     */
    @Override
    public String pkGetVersion()
    {
        String ret = "";
        String rhex = "";

        pkSendHexCmd("0130");
        rhex = ReciveDataHex(255,3000*delayUint);
        ret = rhex;
        return ret;
    }

    /**
     *
     * @param mode
     */
    public void pkSetEncryptMode(int mode)
    {

    }

    public void pkSetEncryptMac(int mode)
    {

    }

    public void pkDownloadWorkKey(int mKeyNo,int wKeyNo,String wKeyAsc)
    {

    }

    public void pkActiveWorkKey(int mKeyNo,int wKeyNo)
    {

    }

    /**
     * <p>命令:02h+<Ln>+36h+<字符串>+<BCC>+[03h]</p>
     * <p>描述:将(Ln-1=)8 倍字节明文字符串用当前工作密钥(DES/3DES)以 ECB 方式进行加密运算 C=eK(P)，</p>
     * <p>返回密文数据。返回信息后关闭加密状态。要求 Ln-1 表示小于等于 248 字节。</p>
     * <p>返回: 02h+<Ln>+<ST>+<密文字串>+<BCC>+[03h]。 ST 可能是 04h、15h、A4h、B5h、C4h、D5h、E0h。</p>
     * @param data
     * @return
     */
    public String pkEncrypt(String data)
    {
        String ret = "";

        return ret;
    }

    /**
     * <p>命令:02h+<Ln>+37h+<密文字串>+<BCC>+[03h]</p>
     * <p>描述:将(Ln-1=)8 倍字节密文字符串用当前工作密钥(DES/3DES)以 ECB 方式进行解密运算 P=dK(C)，</p>
     * <p>返回明文数据。返回信息后关闭加密状态。要求 Ln-1 表示小于等于 248 字节。</p>
     * <p>返回: 02h+<Ln>+<ST>+<明文字串>+<BCC>+[03h]。ST 可能是 04h、15h、A4h、B5h、C4h、D5h、E0h。</p>
     * @param data
     * @return
     */
    public String pkDecrypt(String data)
    {
        String ret = "";

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
     *     <li>Pin block:使用提供的银行卡号进行PINBLOCK运算</li>
     *     <li>Start read key:读取并响应按键信息，按取消(0x1B)或者确认(0x0D)或者超时退出</li>
     *     <li>Read pin:读取密码密文</li>
     * </ul>
     * */
    public String pkStartAllStep()
    {
        String ret = "";
        //
        return ret;
    }

    public  native void kmySendCmd(int fd,String cmd,int size);
    public  native void kmySendHexCmd(int fd,String hexcmd,int size);

}
