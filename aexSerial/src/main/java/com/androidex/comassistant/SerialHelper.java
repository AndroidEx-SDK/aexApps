package com.androidex.comassistant;

/**
 * Created by cts on 17/3/31.
 */

import android.content.Context;
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
    private TimeThread mTimeThread;
    boolean isRead = false;

    //----------------------------------------------------
    public SerialHelper(String sPort, int iBaudRate, Context context) {
        this.sPort = sPort;
        this.iBaudRate = iBaudRate;
        this.context = context;
        serial = new kkserial(context);
        mReadThread = new ReadThread();
        mReadThread.start();
        mSendThread = new SendThread();
        mSendThread.setSuspendFlag();
        mSendThread.start();
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
        if (serial == null) {
            serial = new kkserial(context);
        }
        if (mReadThread == null) {
            mReadThread = new ReadThread();
            mReadThread.start();

        }
        if (mSendThread == null) {
            mSendThread = new SendThread();
            mSendThread.setSuspendFlag();
            mSendThread.start();
        } else {
            mSendThread.setSuspendFlag();
        }
        mSerialFd = serial.serial_open(sPort + "," + iBaudRate + ",N,1,8");
        isRead = true;
        _isOpen = true;
        return mSerialFd;
    }

    //----------------------------------------------------
    public void close() {
        if (mReadThread != null) {
            isRead = false;
        }
        if (serial != null) {
            serial.serial_close(mSerialFd);
            serial = null;
        }
        if (mTimeThread != null) {
            mTimeThread.interrupt();
        }
        _isOpen = false;
    }

    //----------------------------------------------------
    public void send(byte[] bOutArray) {
        serial.serial_write(mSerialFd, bOutArray, bOutArray.length);
        Log.e("SerialHelper", "发送指令：" + MyFunc.ByteArrToHex(bOutArray));
    }

    //----------------------------------------------------
    public void sendHex(String sHex) {
        serial.serial_writeHex(mSerialFd, sHex);
        Log.e("SerialHelper", "发送指令HEX:" + sHex);
    }

    //----------------------------------------------------
    public void sendTxt(String sTxt) {
        byte[] bOutArray = sTxt.getBytes();
        send(bOutArray);
    }

    //----------------------------------------------------
    private class ReadThread extends Thread {

        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                if (isRead) {
                    try {
                        if (serial == null) return;
                        byte[] bytes = serial.serial_read(mSerialFd, 20, 3 * 1000);
                        if (bytes == null) continue;
                        if (bytes.length > 0) {
                            ComBean ComRecData = new ComBean(sPort, bytes, bytes.length);
                            onDataReceived(ComRecData);
                            Log.i("SerialHelper", "xxx接收到的数据：" + MyFunc.ByteArrToHex(bytes));
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                        return;
                    }
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
    private class TimeThread extends Thread {
        public boolean suspendFlag = true;// 控制线程的执行

        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                synchronized (this) {
                    while (suspendFlag) {
                        try {
                            if (serial == null) return;
                            if (isRead) {
                                isRead = false;
                                serial.serial_close(mSerialFd);
                                Thread.sleep(5 * 1000);
                            } else {
                                isRead = true;
                                mSerialFd = serial.serial_open(sPort + "," + iBaudRate + ",N,1,8");
                                Thread.sleep(20 * 1000);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            return;
                        }
                    }
                    mSerialFd = serial.serial_open(sPort + "," + iBaudRate + ",N,1,8");
                    isRead = true;
                }
            }
        }

        //线程暂停
        public void setSuspendFlag() {
            this.suspendFlag = false;
        }

        //唤醒线程
        public synchronized void setResume() {
            this.suspendFlag = true;
            notify();
        }
    }

    public void startTime() {
        if (mTimeThread == null) {
            mTimeThread = new TimeThread();
            mTimeThread.setSuspendFlag();
            mTimeThread.start();
        }
        mTimeThread.setResume();
    }

    public void stopTime() {
        if (mTimeThread != null) {
            mTimeThread.setSuspendFlag();
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
