package com.androidex.comassistant;

/**
 * Created by cts on 17/3/31.
 */

import android.content.Context;
import android.util.Log;

import com.androidex.bean.*;
import com.androidex.plugins.kkserial;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

import android_serialport_api.*;

/**
 * @author benjaminwan
 *         串口辅助工具类
 */
public abstract class SerialHelper {
    private ReadThread mReadThread;
    private SendThread mSendThread;
    private String sPort = "/dev/ttyS0";
    private int iBaudRate = 9600;
    private boolean _isOpen = false;
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
    }

    public SerialHelper() {
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
        serial = new kkserial(context);
        mReadThread = new ReadThread();
        mReadThread.start();
        mSendThread = new SendThread();
        mSendThread.setSuspendFlag();
        mSendThread.start();
        _isOpen = true;
        mSerialFd = serial.serial_open(sPort + "," + iBaudRate + ",N,1,8");
        return mSerialFd;
    }

    //----------------------------------------------------
    public void close() {
        if (mReadThread != null)
            mReadThread.interrupt();
        if (serial != null) {
            serial.serial_close(mSerialFd);
            serial = null;
        }
        _isOpen = false;
    }

    //----------------------------------------------------
    public void send(byte[] bOutArray) {
        //mOutputStream.write(bOutArray);
        serial.serial_write(mSerialFd, bOutArray, bOutArray.length);
    }

    //----------------------------------------------------
    public void sendHex(String sHex) {
        byte[] bOutArray = MyFunc.HexToByteArr(sHex);
        //send(bOutArray);
        serial.serial_writeHex(mSerialFd, sHex);
    }

    //----------------------------------------------------
    public void sendTxt(String sTxt) {
        byte[] bOutArray = sTxt.getBytes();
        send(bOutArray);
        //serial.serial_write(mSerialFd, bOutArray, bOutArray.length);
    }

    //----------------------------------------------------
    private class ReadThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                try {
                    if (serial == null) return;
                    byte[] buffer = new byte[512];
                    //int size = mInputStream.read(buffer);
                    buffer = serial.serial_read(mSerialFd, buffer.length, 3 * 1000);
                    if (buffer.length > 0) {
                        ComBean ComRecData = new ComBean(sPort, buffer, buffer.length);
                        onDataReceived(ComRecData);
                        Log.i("xxx接收到的数据长度：", buffer.length + "");
                        Log.i("xxx接收到的数据长度：", MyFunc.ByteArrToHex(buffer) + "");

                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    //----------------------------------------------------
    private class SendThread extends Thread {
        public boolean suspendFlag = true;// 控制线程的执行

        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                synchronized (this) {
                    while (suspendFlag) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                send(getbLoopData());
                try {
                    Thread.sleep(iDelay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        //线程暂停
        public void setSuspendFlag() {
            this.suspendFlag = true;
        }

        //唤醒线程
        public synchronized void setResume() {
            this.suspendFlag = false;
            notify();
        }
    }

    //----------------------------------------------------
    public int getBaudRate() {
        return iBaudRate;
    }

    public boolean setBaudRate(int iBaud) {
        if (_isOpen) {
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
        if (_isOpen) {
            return false;
        } else {
            this.sPort = sPort;
            return true;
        }
    }

    //----------------------------------------------------
    public boolean isOpen() {
        return _isOpen;
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
    public void startSend() {
        if (mSendThread != null) {
            mSendThread.setResume();
        }
    }

    //----------------------------------------------------
    public void stopSend() {
        if (mSendThread != null) {
            mSendThread.setSuspendFlag();
        }
    }

    //----------------------------------------------------
    protected abstract void onDataReceived(ComBean ComRecData);
}
