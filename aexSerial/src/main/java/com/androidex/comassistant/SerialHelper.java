package com.androidex.comassistant;

import android.content.Context;
import android.util.Log;

import com.androidex.bean.ComBean;
import com.androidex.plugins.kkserial;

/**
 * @author liyp
 * 串口辅助工具类
 */
public abstract class SerialHelper {
    private static String sPort = "/dev/ttyS2,9600,N,1,8";     //打开串口的参数
    private static int iBaudRate = 9600;
    private byte[] _bLoopData = new byte[]{0x30};
    private int iDelay = 500;
    private Context context;
    private kkserial serial;
    private int mSerialFd;

    public SerialHelper(Context context) {
        this(sPort, iBaudRate, context);
    }

    /**
     * 传入操作串口的必须参数
     *
     * @param sPort     串口地址
     * @param iBaudRate 波特率
     * @param context
     */
    public SerialHelper(String sPort, int iBaudRate, Context context) {
        this.sPort = sPort;
        this.iBaudRate = iBaudRate;
        this.context = context;
        serial = new kkserial(context);
    }

    public SerialHelper(String sPort, Context context) {
        this(sPort, 9600, context);
    }

    public SerialHelper(String sPort, String sBaudRate, Context context) {
        this(sPort, Integer.parseInt(sBaudRate), context);
    }

    /**
     * 打开串口
     */
    public int open() {
        mSerialFd = serial.serial_open(sPort);
        return mSerialFd;
    }

    /**
     * 关闭串口
     */
    public void close() {
        if(mSerialFd > 0) {
            onClearMessage();
            serial.serial_close(mSerialFd);
            log(String.format("关闭串口%s:%d",sPort,mSerialFd));
            mSerialFd = 0;
        }
    }

    /**
     * 发送字节数组格式的数据
     *
     * @param bOutArray
     */
    public void send(byte[] bOutArray) {
        if(mSerialFd > 0) {
            serial.serial_write(mSerialFd, bOutArray, bOutArray.length);
            log("发送指令：" + MyFunc.ByteArrToHex(bOutArray));
        }
    }

    /**
     * 发送16进制字符串数据
     *
     * @param sHex
     */
    public void sendHex(String sHex) {
        if(mSerialFd > 0) {
            serial.serial_writeHex(mSerialFd, sHex);
            log("发送指令HEX:" + sHex);
        }
    }

    /**
     * 发送文本数据
     */
    public void sendTxt(String sTxt) {
        byte[] bOutArray = sTxt.getBytes();
        send(bOutArray);
    }

    /**
     * 启动读取串口数据
     */
    public void startReadSerial(){
        Runnable run=new Runnable() {
            public void run() {
                log("开始读取串口数据");
                while(mSerialFd > 0) {
                    byte[] r = serial.serial_read(mSerialFd, 100, 3000);
                    if (r != null) {
                        ComBean ComRecData = new ComBean(sPort, r, r.length);
                        onDataReceived(ComRecData);
                        Log.i("TS", String.format("(%d)%s", r.length, MyFunc.ByteArrToHex(r)));
                    }
                }
                log("读取结束");
            }
        };
        Thread pthread = new Thread(run);
        pthread.start();

    }

    public void startSendSerial(){
        send(getbLoopData());
    }

    private void log(String msg) {
        onLog(msg);
    }

    //----------------------------------------------------
    public int getBaudRate() {
        return iBaudRate;
    }

    public boolean setBaudRate(int iBaud) {
        if (mSerialFd > 0) {
            return false;
        } else {
            iBaudRate = iBaud;
            return true;
        }
    }

    public boolean setBaudRate(String sBaud) {
        int iBaud = Integer.parseInt(sBaud);
        return setBaudRate(iBaud);
    }

    /**
     * 获取串口地址
     *
     * @return
     */
    public String getPort() {
        return sPort;
    }

    /**
     * 设置串口地址
     *
     * @param sPort
     * @return
     */
    public boolean setPort(String sPort) {
        if (mSerialFd > 0) {
            return false;
        } else {
            this.sPort = sPort;
            return true;
        }
    }

    /**
     * 判断串口是否打开
     *
     * @return
     */
    public boolean isOpen() {
        return mSerialFd > 0;
    }

    /**
     * 是否正在循环
     *
     * @return
     */
    public byte[] getbLoopData() {
        return _bLoopData;
    }

    /**
     * 设置循环发送数据
     *
     * @param bLoopData
     */
    public void setbLoopData(byte[] bLoopData) {
        this._bLoopData = bLoopData;
    }

    /**
     * 设置循环发送数据
     *
     * @param sTxt
     */
    public void setTxtLoopData(String sTxt) {
        this._bLoopData = sTxt.getBytes();
    }

    /**
     * 设置循环发送数据
     * @param sHex
     */
    public void setHexLoopData(String sHex) {
        this._bLoopData = MyFunc.HexToByteArr(sHex);
    }

    /**
     * 读取循环发送间隔时间
     * @param
     */
    public int getiDelay() {
        return iDelay;
    }

    /**
     * 设置循环发送间隔时间
     * @param
     */
    public void setiDelay(int iDelay) {
        this.iDelay = iDelay;
    }

    protected abstract void onDataReceived(ComBean ComRecData);
    protected abstract void onLog(final String msg);
    protected abstract void onClearMessage();
}
