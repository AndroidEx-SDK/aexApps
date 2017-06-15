package com.androidex.face.utils;

import android.os.Handler;
import android.os.Message;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import android_serialport_api.SerialPort;

/**
 * Created by cts on 17/4/24.
 * 按键的操作类
 */

public class KeyUtil {
    public static final int MSG_ASSEMBLE_KEY = 0x1011;
    private InputStream inputStream=null;
    private SerialPort serialPort;
    private ReadThread readThread;
    private byte COMMAND_HEAD=(byte)0xA1;
    private byte COMMAND_KEYWORD=(byte)0x20;
    private Handler handler;

    public KeyUtil(Handler handler){
        this.handler = handler;
        try {
            serialPort = new SerialPort(new File("/dev/ttyS0"),115200,0);
            inputStream = serialPort.getInputStream();
            readThread = new ReadThread();
            readThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class ReadThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                try {
                    byte[] buffer = new byte[1024];
                    int size = 0;
                    if (inputStream == null)
                        return;
                    size = inputStream.read(buffer);
                    byte[] data=copyFrom(buffer,0,size);
                    onData(data);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    public byte[] copyFrom(byte[] buffer,int from,int length){
        byte[] result=new byte[length];
        for(int i=from;i<(from+length);i++){
            result[i-from]=buffer[i];
        }
        return result;
    }

    protected void onData(byte [] data){
        if (data[0] ==COMMAND_HEAD){
            byte command=data[1];
            byte[] lengthArray=new byte[2];
            lengthArray[0]=data[2];
            lengthArray[1]=data[3];
            int length=getShort(lengthArray);
            length--;
            byte[] info=null;
            if(length>0){
                info=copyFrom(data,4,length);
            }
            if (command == COMMAND_KEYWORD){
                onKeyDown(info[0]);
            }
        }
    }

    protected void onKeyDown(byte key){
        Message message = handler.obtainMessage();
        message.what = MSG_ASSEMBLE_KEY;
        message.obj = key;
        handler.sendMessage(message);
    }

    private short getShort(byte[] bytes)
    {
        return (short) ((0xff & bytes[1]) | (0xff00 & (bytes[0] << 8)));
    }

}
