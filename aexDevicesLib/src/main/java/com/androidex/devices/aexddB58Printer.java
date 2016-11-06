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
 */

public class aexddB58Printer extends aexddPrinter {
    private static final String TAG = "B58T";
    static
    {
        try {
            System.loadLibrary("appDevicesLibs");
        } catch (UnsatisfiedLinkError e) {
            Log.d("B58TPrinter", "appDevicesLibs.so library not found!");
        }
    }

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
        String printerPort = mParams.optString(PORT_ADDRESS);
        if(mSerialFd > 0)
            Close();
        String ret = native_open(printerPort);
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
        native_close();
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

    @Override
    public int reset()
    {
        //复位初始化打印机,B58没有复位初始化命令
        return 0;
    }

    /**
     * n = 1: 传送打印机状态
     * n = 2: 传送脱机状态
     * n = 3: 传送错误状态
     * n = 4: 传送卷纸传感器状态
     * @return
     */
    @Override
    public int checkStatus()
    {
        int r = 0;
        byte[] rs;

        WriteDataHex("100401");
        rs = ReciveData(1,1000*delayUint);
        if((rs != null) && (rs.length >1)) {
            Log.d(TAG, String.format("checkStatus return 0x%02X\n", rs[0]));
            r = rs[0];
        }

        WriteDataHex("100402");
        rs = ReciveData(1,1000*delayUint);
        if((rs != null) && rs.length >1) {
            Log.d(TAG, String.format("checkStatus return 0x%02X\n", rs[0]));
            r |= rs[0] << 8;
        }

        WriteDataHex("100403");
        rs = ReciveData(1,1000*delayUint);
        if((rs != null) && rs.length >1) {
            Log.d(TAG, String.format("checkStatus return 0x%02X\n", rs[0]));
            r |= rs[0] << 16;
        }

        WriteDataHex("100404");
        rs = ReciveData(1,1000*delayUint);
        if((rs != null) && rs.length >1) {
            Log.d(TAG, String.format("checkStatus return 0x%02X\n", rs[0]));
            r = rs[0] << 24;
        }
        return r;
    }

    @Override
    public void ln()
    {
        WriteDataHex("0A");
    }

    @Override
    public int print(String str)
    {
        //打印英文
        WriteData(str.getBytes(),str.getBytes().length);
        return 0;
    }

    @Override
    public int printChinese(String str)
    {
        //打印中文
        try {
            byte[] sgbk = str.getBytes("GBK");
            WriteData(sgbk,sgbk.length);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int cutPaper(int n)
    {
        //切纸
        WriteDataHex(String.format("1D564200"));
        return 0;
    }

    @Override
    public int printBarcode(char wide,char high,char code)
    {
        //打印条码
        return 0;
    }

    @Override
    public int printQrcode(String code)
    {
        //打印二维码
        return 0;
    }

    @Override
    public void setAlign(int n)
    {
        //设置对齐方式：0-左对齐,1-居中,2-右对齐
        WriteDataHex(String.format("1B61%02X",n&0xFF));     //设置左对齐
        WriteDataHex("1D2110");     //设置字体大小为16
    }

    @Override
    public void setSize(int n)
    {
        WriteDataHex(String.format("1D21%02X",n&0xFF));     //设置字体大小为n
    }

    /**
     * 打印机类对象的自检函数，此函数会打印一些文字内容并切纸。
     */
    @Override
    public boolean selfTest()
    {
        String testEnStr = "AndroidEx SDK 5.0";
        String testChStr = "Printer驱动测试，本机信息：\n\t固件版本号：\n\t设备序列号：\n\t设备安卓ID：\n";
        String companyStr = "\n" +
                "深圳市安卓工控设备有限公司\n" +
                "深圳市龙岗区布吉龙景工业园E栋东二楼\n" +
                "http://www.androidex.cn\n";

        hwService hwservice = new hwService(mContext);
        testChStr = String.format(testChStr,hwservice.getSdkVersion(),hwservice.get_uuid(),hwservice.get_serial());

        reset();       //初始化
        //监测打印机状态，n = 1: 传送打印机状态 ，n = 2: 传送脱机状态 ，n = 3: 传送错误状态 ，n = 4: 传送卷纸传感器状态
        Log.d(TAG,String.format("Printer status 0x%08X",checkStatus()));
        ln();         //换行
        ln();         //换行
        //打印：
        //设置对齐方式：0-左对齐,1-居中,2-右对齐
        setAlign(0);    //设置左对齐
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
        try {
            byte[] sgbk = testChStr.getBytes("GBK");
            WriteData(sgbk,sgbk.length);
            sgbk = companyStr.getBytes("GBK");
            WriteData(sgbk,sgbk.length);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //打印条码,char wide=03 ,char high=A2,char code=49
        WriteDataHex(String.format("1D77031D68A21D6B49%02X",hwservice.get_uuid().length()));
        WriteData(hwservice.get_uuid().getBytes(),hwservice.get_uuid().length());
        WriteDataHex("0A");         //换行

        //二维码
        WriteDataHex(String.format("1F1C0801%02X00",companyStr.length()));
        WriteData(companyStr.getBytes(),companyStr.length());
        WriteDataHex("0A");         //换行
        //切纸
        cutPaper(1);       //全切纸
        //WriteDataHex("1B6D");       //半切纸
        return true;
    }

    public native String native_open(String arg);						           // 打开打印机
    public native int native_close();								           // 关闭打印机

    //2.打印机指令操作
    /****************************************标准ESC/POS指令集******************************
     @1.PM58,ET58 LF 打印并换行                                          0x0a
     @2.PM58,ET58 ESC ! n 设置字符打印方式                                1B 21 n
     @3.PM58,ET58 ESC 3 n 设置行间距为n点行（n/203英寸）                    1B 33 n ,设置行间距为n点行。n=0～255
     @4.PM58,ET58 ESC @ 初始化打印机                                     1B 40
     @5.PM58,ET58 ESC SO 设置字符倍宽打印                                 1B 0E ,在一行内该命令之后的所有字符均以正常宽度的2倍打
     @6.PM58,ET58 ESC DC4 取消字符倍宽打印                                1B 14 ,执行此命令后，字符恢复正常宽度打
     @7.PM58,ET58 ESC * m n1 n2 d1⋯dk 设定点图命令                       1B 2A m n1 n2 [d]k,
     @8.PM58,ET58 GS w n 设置条码宽度                                     1D 77 n,设置条形码水平尺寸，2 £  n £ 6
     @9.PM58,ET58 GS h n 设置条形码高度                                   1D 68 n,设置条形码高度，1 £  n £ 255。
     @10.PM58,ET58 GS k m d1 ... dk NUL ② GS k m n d1 ... dn 打印条形码   1D 6B m n d1 .. dn
     @11.PM58,ET58 打印英文  写入缓冲区直接打印  0x0a

      ****PM58,ET58****相同功能，指令不同************
     @51.PM58 US <7> H [cn] +[data] 设置和打印QR条码                  1F 07 48，cn : 01 设置版本, 02 纠错等级，03 模块宽, 04传送QR数据, 05 打印QR数据 , 08设置是否为连接模式
     ET58 十六进制码   1D      6B     m    d1 ... dk   00  ,1D      6B     m    n    d1 ... dn

     @52.PM58 GS V m 选择切纸方式并切纸                                1D 56 m，m ＝ 0，全切纸；
     ET58 ESC  I or ESC m十六进制：1b 69 或1b 6d

     @53.ET58 十六进制：1B    64    n打印行缓冲器里的数据并向前走纸n字符行。n=0～255
     PM58 ESC J n 打印并进纸n点行     1B 4A n  ,n=0～255。该命令只在本行打印有效

     @54.打印中文
     PM58 写入缓冲区直接打印  0x0a
     ET58 进入汉字模式 十六进制：1C   26
     */
    public native int newline(int n);							           // 换行 n为行数
    public native int set_fontsize(int isize);				           // 字符打印方式设置命令，用于选择打印字符的大小
    public native int set_linewide(int isize);				           // 设置行间距为n 点行 （n ∕203 英寸） 默认n=30
    public native int initialize();						               // 初始化打印机
    public native int set_charDSize();                                   // 在一行内该命令之后的所有字符均以正常宽度的2 倍打印
    public native int set_charNSize();                                   // 恢复正常宽度打印	public native int out_ch(byte[] pCh);					               //打印字符或中文(中文必须为gb2312编码，即一个中文站2个字节)
    public native int set_graph( char m, char n1, char n2, String strbmppath);    //设定点图命令

    //条码控制命令
    public native int set_barcodeHigh( char n);                                   //设置条码高度
    public native int set_barcodeWide( char n);	                                //设定条码宽度,水平方向点数 2<=n<=6,缺省值为3
    public native int out_barcode(char wide,char high,char code,String data, int len);// 打印条形码

    public native int cmdline(byte[] cmd);                               //透传指令

    /*****************功能相同，指令集不同****************************/
    public native int out_2Dimensional(String content,int ilen);	                 // content二维码内容
    public native int cut(byte[] code, int iflag);							           // iflag = 1全切纸  0部分切纸
    public native int stepline(byte[] code,int n);                                   // 打印行缓冲器里的内容，并向前n行   n=0--255

    /******************PM58************************/
    public native int set_charwide();                                    // 设置字符行间距为 1/6 英寸
    public native int getstatus(int n);						           // 获取打印机状态
    public native int getfactory(int n);						           // 获取打印机厂商

    /******************ET58**********************/
    //public native int steppoint(int ifalg ,int n);					   // 打印行缓冲器里的内容，iflag  1向前走纸 ，0 退纸
    public native int set_align(int iflag);				               // 设置排列方式:0-左对齐,1-居中,2-右对齐

    /******************WH-E25************************/
    public native int WHE25_getstatus(int n);						           // 获取打印机状态

    /******************SGT-801************************/
    public native int SGT801_out_2Dimensional(int size,byte[] content,int ilen);		//// content二维码内容

    /******************T_500AP************************/
    public native int T_500AP_out_barcode(char wide,char high,char code,String data, int len);// 打印条形码
    public native int T_500AP_out_2Dimensional(String content,int ilen);	                 // content二维码内容

    /******************北京瑞工RG_CB532打印机************************/
    public native int RG_CB532_out_barcode(char wide,char high,char code,String data, int len);// 打印条形码
    public native int RG_CB532_out_2Dimensional(String content,int ilen);	                 // content二维码内容

    // TA500 打印机
    public native int TA_500_out_2Dimensional(String data, int len);
    public native int TA_500_cut(int n);


    /*****************打印位图 ***********************/
    public native int out_bitmap(byte[] data, int size, int bmpwidth, int bmphigh, int width, int high);

}
