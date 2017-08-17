package com.androidex.comassistant;

/**
 * Created by cts on 17/3/31.
 */

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;

import com.androidex.bean.*;
import com.androidex.plugins.kkserial;

/**
 * @author benjaminwan
 *         串口辅助工具类
 */
public abstract class SerialHelper {
    private String sPort = "/dev/ttymxc0";
    private int iBaudRate = 115200;
    private byte[] _bLoopData = new byte[]{0x30};
    private int iDelay = 500;
    private Context context;
    private kkserial serial;
    private int mSerialFd;

    //----------------------------------------------------
    public SerialHelper(String sPort, int iBaudRate, Context context) {
        this.sPort = sPort;
        this.iBaudRate = iBaudRate;
        this.context = context;
        serial = new kkserial(context);
    }


    public SerialHelper(Context context) {
        this("/dev/ttyS0", 9600, context);
    }

    public SerialHelper(String sPort, Context context) {
        this(sPort, 9600, context);
    }

    public SerialHelper(String sPort, String sBaudRate, Context context) {
        this(sPort, Integer.parseInt(sBaudRate), context);
    }

    //----------------------------------------------------
    public int open() {
        mSerialFd = serial.serial_open(sPort + "," + iBaudRate + ",N,1,8");
        log(String.format("打开%s成功：%d",sPort,mSerialFd));
        return mSerialFd;
    }

    //----------------------------------------------------
    public void close() {
        if(mSerialFd > 0) {
            onClearMessage();
            serial.serial_close(mSerialFd);
            log(String.format("关闭串口%s:%d",sPort,mSerialFd));
            mSerialFd = 0;
        }else {
            //log("关闭串口时串口句柄无效，也许没有打开串口。");
        }
    }

    //----------------------------------------------------
    public void send(byte[] bOutArray) {
        if(mSerialFd > 0) {
            serial.serial_write(mSerialFd, bOutArray, bOutArray.length);
            log("发送指令：" + MyFunc.ByteArrToHex(bOutArray));
        }
    }

    //----------------------------------------------------
    public void sendHex(String sHex) {
        if(mSerialFd > 0) {
            serial.serial_writeHex(mSerialFd, sHex);
            log("发送指令HEX:" + sHex);
        }
    }

    //----------------------------------------------------
    public void sendTxt(String sTxt) {
        byte[] bOutArray = sTxt.getBytes();
        send(bOutArray);
    }

    public void startReadSerial(){
        Runnable run=new Runnable() {
            public void run() {
                log("开始读取串口数据");
                while(mSerialFd > 0) {
                    byte[] r = serial.serial_read(mSerialFd, 100, 3000);
                    if (r != null) {
                        ComBean ComRecData = new ComBean(sPort, r, r.length);
                        onDataReceived(ComRecData);
                    }
                    //log(String.format("read:%d",mSerialFd));
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

    //----------------------------------------------------
    public String getPort() {
        return sPort;
    }

    public boolean setPort(String sPort) {
        if (mSerialFd > 0) {
            return false;
        } else {
            this.sPort = sPort;
            return true;
        }
    }

    //----------------------------------------------------
    public boolean isOpen() {
        return mSerialFd > 0;
    }

    //----------------------------------------------------
    public byte[] getbLoopData() {
        return _bLoopData;
    }

    //----------------------------------------------------
    public void setbLoopData(byte[] bLoopData) {
        this._bLoopData = bLoopData;
    }

    //----------------------------------------------------
    public void setTxtLoopData(String sTxt) {
        this._bLoopData = sTxt.getBytes();
    }

    //----------------------------------------------------
    public void setHexLoopData(String sHex) {
        this._bLoopData = MyFunc.HexToByteArr(sHex);
    }

    //----------------------------------------------------
    public int getiDelay() {
        return iDelay;
    }

    //----------------------------------------------------
    public void setiDelay(int iDelay) {
        this.iDelay = iDelay;
    }

    //----------------------------------------------------
    protected abstract void onDataReceived(ComBean ComRecData);
    protected abstract void onLog(final String msg);
    protected abstract void onClearMessage();
}
