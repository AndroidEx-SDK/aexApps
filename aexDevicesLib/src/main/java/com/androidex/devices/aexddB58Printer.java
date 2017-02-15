package com.androidex.devices;

import android.content.Context;

import com.androidex.aexlibs.hwService;
import com.androidex.apps.aexdeviceslib.R;
import com.androidex.logger.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Created by yangjun on 2016/10/24.
 * AndroidEx 定制的58mm热敏打印机，打印机指令主要是ESC指令集，直接通过串口发送和接收指令即可。
 * 打印机状态反馈可以通过
 */

public class aexddB58Printer extends aexddPrinter {
    private static final String TAG = "B58T";

    public aexddB58Printer(Context ctx) {
        super(ctx);
    }

    public aexddB58Printer(Context ctx, JSONObject args) {
        super(ctx, args);
    }

    @Override
    public String getDeviceName() {
        return mContext.getString(R.string.DEVICE_PRINTER_B58);
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

                if (action.equals("print")) {
                    int index = 0;
                    String r;
                    String arg = args.optString("params");
                } else if (action.equals("cutPaper")) {
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
            }
        };
        pthread = new Thread(run);
        pthread.start();
        return 0;
    }

    @Override
    public int reset() {
        //复位初始化打印机,B58没有复位初始化命令
        return 0;
    }

    /**
     * n = 1: 传送打印机状态
     * n = 2: 传送脱机状态
     * n = 3: 传送错误状态
     * n = 4: 传送卷纸传感器状态
     *
     * @return
     */
    @Override
    public int checkStatus() {
        int r = 0;
        byte[] rs;

        WriteDataHex("100401");
        rs = ReciveData(1, 1000 * delayUint);
        if ((rs != null) && (rs.length > 1)) {
            Log.d(TAG, String.format("checkStatus return 0x%02X\n", rs[0]));
            r = rs[0];
        }

        WriteDataHex("100402");
        rs = ReciveData(1, 1000 * delayUint);
        if ((rs != null) && rs.length > 1) {
            Log.d(TAG, String.format("checkStatus return 0x%02X\n", rs[0]));
            r |= rs[0] << 8;
        }

        WriteDataHex("100403");
        rs = ReciveData(1, 1000 * delayUint);
        if ((rs != null) && rs.length > 1) {
            Log.d(TAG, String.format("checkStatus return 0x%02X\n", rs[0]));
            r |= rs[0] << 16;
        }

        WriteDataHex("100404");
        rs = ReciveData(1, 1000 * delayUint);
        if ((rs != null) && rs.length > 1) {
            Log.d(TAG, String.format("checkStatus return 0x%02X\n", rs[0]));
            r = rs[0] << 24;
        }
        return r;
    }

    @Override
    public void ln() {
        WriteDataHex("0A");
    }

    @Override
    public void ln(int lines) {
        while (lines > 0) {
            WriteDataHex("0A");
            lines--;
        }
    }

    @Override
    public int print(String str) {
        //打印英文
        WriteData(str.getBytes(), str.getBytes().length);
        return 0;
    }

    @Override
    public int printChinese(String str) {
        //打印中文
        try {
            byte[] sgbk = str.getBytes("GBK");
            WriteData(sgbk, sgbk.length);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int cutPaper(int n) {
        //切纸
        WriteDataHex(String.format("1D564200"));
        return 0;
    }

    @Override
    public int printBarcode(char wide, char high, char code) {
        //打印条码
        return 0;
    }

    @Override
    public int printQrcode(String code) {
        //打印二维码
        return 0;
    }

    @Override
    public void setAlign(int n) {
        //设置对齐方式：0-左对齐,1-居中,2-右对齐
        WriteDataHex(String.format("1B61%02X", n & 0xFF));     //设置左对齐
        WriteDataHex("1D2108");     //设置字体大小为16
    }

    @Override
    public void setSize(int n) {
        WriteDataHex(String.format("1D21%02X", n & 0xFF));     //设置字体大小为n
    }

    /**
     * 打印机类对象的自检函数，此函数会打印一些文字内容并切纸。
     */

    @Override
    public boolean selfTest() {
        String testEnStr = "AndroidEx SDK 5.0";
        String testChStr = "Printer驱动测试，本机信息：\n\t固件版本号：\n\t设备序列号：\n\t设备安卓ID：\n";
        String companyStr = "\n" +
                "深圳市安卓工控设备有限公司\n" +
                "深圳市龙岗区布吉龙景工业园E栋东二楼\n" +
                "http://www.androidex.cn\n";

        hwService hwservice = new hwService(mContext);
        testChStr = String.format(testChStr, hwservice.getSdkVersion(), hwservice.get_uuid(), hwservice.get_serial());

        reset();       //初始化
        //监测打印机状态，n = 1: 传送打印机状态 ，n = 2: 传送脱机状态 ，n = 3: 传送错误状态 ，n = 4: 传送卷纸传感器状态
        Log.d(TAG, String.format("Printer status 0x%08X", checkStatus()));
        ln();         //换行
        try {
            //打印：
            //设置对齐方式：0-左对齐,1-居中,2-右对齐
            setAlign(0);    //设置左对齐
            WriteDataHex("1D2108");     //设置字体大小为16
            //打印英文
            WriteData(testEnStr.getBytes("GBK"), testEnStr.length());
            WriteDataHex("1C26");   //打印中文
            WriteData(testChStr.getBytes("GBK"), testChStr.length());
            WriteData(companyStr.getBytes("GBK"), companyStr.length());

            WriteDataHex("1B6101");     //设置居中
            WriteDataHex("1D2108");     //设置字体大小为22
            //打印英文
            WriteData(testEnStr.getBytes("GBK"), testEnStr.length());
            WriteDataHex("1C26");   //打印中文
            WriteData(testChStr.getBytes("GBK"), testChStr.length());
            WriteData(companyStr.getBytes("GBK"), companyStr.length());

            WriteDataHex("1B6102");     //设置右对齐
            WriteDataHex("1D2108");     //设置字体大小为24
            //打印英文
            WriteData(testEnStr.getBytes("GBK"), testEnStr.length());
            WriteDataHex("1C26");   //打印中文

            byte[] sgbk = testChStr.getBytes("GBK");
            WriteData(sgbk, sgbk.length);
            sgbk = companyStr.getBytes("GBK");
            WriteData(sgbk, sgbk.length);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //打印条码,char wide=03 ,char high=A2,char code=49
        WriteDataHex(String.format("1D77031D68A21D6B49%02X", hwservice.get_uuid().length()));
        WriteData(hwservice.get_uuid().getBytes(), hwservice.get_uuid().length());
        WriteDataHex("0A");         //换行

        //二维码
        WriteDataHex(String.format("1F1C0801%02X00", companyStr.length()));
        WriteData(companyStr.getBytes(), companyStr.length());
        WriteDataHex("0A");         //换行
        //切纸
        cutPaper(1);       //全切纸
        //WriteDataHex("1B6D");       //半切纸
        Log.i(TAG,"打印成功");
        return true;
    }

    /**
     * selfText方法的重载
     * @param value
     * @return
     */
    public boolean selfTest (String value,int totalLength,int length){
        reset();       //初始化
        try {
            //打印：
            //设置对齐方式：0-左对齐,1-居中,2-右对齐
            setAlign(0);    //设置左对齐
            WriteDataHex("1D2108");     //设置字体大小为16
            //打印英文
            WriteDataHex("1C26");   //打印中文
            byte[] sgbk = value.getBytes("GBK");
            WriteData(sgbk, sgbk.length);
            ln();
            if (length==totalLength){
                ln();
                ln();
                //cutPaper(1);


            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return true;
    }

}
