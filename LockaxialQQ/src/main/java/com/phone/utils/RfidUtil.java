package com.phone.utils;

/**
 * Created by simon on 2016/7/11.
 */

import android.os.Handler;
import android.os.Message;

import com.example.seriport.SerialPort;
import com.phone.config.DeviceConfig;
import com.androidex.service.MainService;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

public class RfidUtil {
    private SerialPort serialPort=null;
    private int baudrate=9600;
    private String port= DeviceConfig.RFID_PORT;
    private OutputStream outputStream=null;
    private InputStream inputStream=null;
    private Handler handler=null;
    private ReadThread readThread = null;

    public RfidUtil(Handler handler){
        this.handler=handler;
    }

    public void open() throws SecurityException, IOException, InvalidParameterException {
        FileDescriptor fileDescriptor = SerialPort.open(port, baudrate, 0);
        if (fileDescriptor == null) {
            throw new IOException();
        }
        inputStream = new FileInputStream(fileDescriptor);
        outputStream = new FileOutputStream(fileDescriptor);
        readThread = new ReadThread();
        readThread.start();
    }

    public void close(){
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        }catch(IOException e){}
        try {
            if(outputStream!=null){
                outputStream.close();
            }
        }catch(IOException e){}
        try {
            serialPort.close();
        }catch(Exception e){}
        if (readThread != null){
            readThread.interrupt();
        }
    }

    public static String convertToCardNo(byte[] b, int length) {
        String value="";
        for (int i = 0; i < length; ++i) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            value=value+hex;
        }
        return value.toUpperCase();
    }

    public class ReadThread extends Thread {
        @Override
        public void run() {
            super.run();
            List dataList=new ArrayList();
            while (!isInterrupted()) {
                try {
                    byte[] buffer = new byte[1024];
                    int size = 0;
                    if (inputStream == null)
                        return;
                    size = inputStream.read(buffer);
                    for(int i=0;i<size;i++){
                        dataList.add(buffer[i]);
                    }
                    byte headerByte=(Byte)dataList.get(0);
                    if(headerByte==87){
                        if(dataList.size()>=14){
                            byte[] newBuffer=new byte[4];
                            for(int i=0;i<4;i++){
                                //newBuffer[i]=(Byte)dataList.get(i+7);
                                newBuffer[i]=(Byte)dataList.get(i+8);
                            }
                            String card=convertToCardNo(newBuffer,4);
                            dataList.clear();
                            onCardIncome(card);
                        }
                    }else if(headerByte==65){
                        if(dataList.size()>=12){
                            byte[] newBuffer=new byte[4];
                            for(int i=0;i<4;i++){
                                newBuffer[i]=(Byte)dataList.get(i+6);
                            }
                            String card=convertToCardNo(newBuffer,4);
                            dataList.clear();
                            onCardIncome(card);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }
    protected void onCardIncome(String card){
        Message message = handler.obtainMessage();
        message.what = MainService.MSG_CARD_INCOME;
        message.obj = card;
        handler.sendMessage(message);
    }
}
