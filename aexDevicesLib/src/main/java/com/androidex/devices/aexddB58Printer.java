package com.androidex.devices;

import android.content.Context;

import com.androidex.aexapplibs.R;
import com.androidex.aexlibs.hwService;
import com.androidex.logger.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yangjun on 2016/10/24.
 */

public class aexddB58Printer extends aexddPrinter {

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

                if (action.equals("print")) {
                    int index = 0;
                    String r;
                    String arg = args.optString("params");
                }else if (action.equals("cutPaper")) {
                    int timeout = 10000*delayUint;
                    obj.put("success", true);
                }else {
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

    /**
     * 打印机类对象的自检函数，此函数会打印一些文字内容并切纸。
     */
    @Override
    public void selfTest()
    {
        String testEnStr = "AndroidEx SDK 5.0";
        String testChStr = "Printer驱动测试，本机信息：\n\t固件版本号：\n\t设备序列号：\n\t设备安卓ID：\n";
        String companyStr = "\n" +
                "深圳市安卓工控设备有限公司\n" +
                "深圳市龙岗区布吉龙景工业园E栋东二楼\n" +
                "http://www.androidex.cn\n";

        hwService hwservice = new hwService(mContext);
        testChStr = String.format(testChStr,hwservice.getSdkVersion(),hwservice.get_uuid(),hwservice.get_serial());

        WriteDataHex("1B40");       //初始化
        //监测打印机状态，n = 1: 传送打印机状态 ，n = 2: 传送脱机状态 ，n = 3: 传送错误状态 ，n = 4: 传送卷纸传感器状态
        WriteDataHex("100404");     //查询卷纸状态
        String ret = ReciveDataHex(2,1000*delayUint);     //读取一个字节
        if((ret != null) && (ret.length() > 0)){
            int s = Integer.parseInt(ret,16);
            if((s & 0x60) == 0){
                //打印机有纸
            }else{
                Log.d(TAG, "打印机无纸。");
                return;
            }
        }
        WriteDataHex("0A");         //换行
        WriteDataHex("0A");         //换行
        //打印：
        //设置对齐方式：0-左对齐,1-居中,2-右对齐
        WriteDataHex("1B6100");     //设置左对齐
        WriteDataHex("1D2110");     //设置字体大小为16
        //打印英文
        WriteData(testEnStr.getBytes(),testEnStr.length());
        WriteDataHex("1C26");   //打印中文
        WriteData(testChStr.getBytes(),testChStr.length());
        WriteData(companyStr.getBytes(),companyStr.length());

        WriteDataHex("1B6101");     //设置居中
        WriteDataHex("1D2116");     //设置字体大小为22
        //打印英文
        WriteData(testEnStr.getBytes(),testEnStr.length());
        WriteDataHex("1C26");   //打印中文
        WriteData(testChStr.getBytes(),testChStr.length());
        WriteData(companyStr.getBytes(),companyStr.length());

        WriteDataHex("1B6102");     //设置右对齐
        WriteDataHex("1D2118");     //设置字体大小为24
        //打印英文
        WriteData(testEnStr.getBytes(),testEnStr.length());
        WriteDataHex("1C26");   //打印中文
        WriteData(testChStr.getBytes(),testChStr.length());
        WriteData(companyStr.getBytes(),companyStr.length());

        //打印条码,char wide=03 ,char high=A2,char code=49
        WriteDataHex(String.format("1D77031D68A21D6B49%02X",hwservice.get_uuid().length()));
        WriteData(hwservice.get_uuid().getBytes(),hwservice.get_uuid().length());
        WriteDataHex("0A");         //换行

        //二维码
        WriteDataHex(String.format("1F1C0801%02X00",companyStr.length()));
        WriteData(companyStr.getBytes(),companyStr.length());
        WriteDataHex("0A");         //换行
        //切纸
        WriteDataHex("1B69");       //全切纸
        //WriteDataHex("1B6D");       //半切纸
    }

}
