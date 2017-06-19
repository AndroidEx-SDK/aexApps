package com.phone.utils;

/**
 * Created by simon on 2016/7/11.
 */

import android.os.Handler;
import android.os.Message;

import com.example.seriport.SerialPort;
import com.phone.config.DeviceConfig;
import com.phone.service.MainService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

public class AssembleUtil {
    public static byte[] COMMAND_SUCCESS={(byte)0xA1,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0xA2};
    public static byte[] COMMAND_FAILED={(byte)0xA1,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0xA3};
    public static byte[] COMMAND_OPENLOCK={(byte)0xA1,(byte)0x90,(byte)0x00,(byte)0x01,(byte)0x32};
    public static byte[] COMMAND_TEST={(byte)0xA1,(byte)0x81,(byte)0x00,(byte)0x0F,(byte)0x00,(byte)0x06,(byte)0x02,(byte)0x02,(byte)0x02,(byte)0x02,(byte)0x02,(byte)0x02,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0x3d};

    public static byte COMMAND_HEAD=(byte)0xA1;
    public static byte COMMAND_CORRECT=(byte)0x00;
    public static byte COMMAND_ERROR=(byte)0x01;
    public static byte COMMAND_OPEN_BY_FINGER=(byte)0x10;
    public static byte COMMAND_OPEN_BY_PASSWORD=(byte)0x11;
    public static byte COMMAND_OPEN_BY_CARD=(byte)0x12;
    public static byte COMMAND_KEYWORD=(byte)0x20;
    public static byte COMMAND_OPEN_ERROR=(byte)0x1f;
    public static byte COMMAND_WRITE_FINGER=(byte)0x80;
    public static byte COMMAND_WRITE_CARD=(byte)0x82;

    private SerialPort serialPort=null;
    private int baudrate=115200;
    private String port= DeviceConfig.ASSEMBLE_PORT;
    private OutputStream outputStream=null;
    private InputStream inputStream=null;
    private Handler handler=null;
    private ReadThread readThread = null;

    private JSONArray changedFingerList=null;
    private int changeFingerListIndex=-100;
    private JSONArray fingerListSuccess=new JSONArray();
    private JSONArray fingerListFailed=new JSONArray();

    private JSONArray changedCardList=null;
    private int changeCardListIndex=-100;
    private JSONArray cardListSuccess=new JSONArray();
    private JSONArray cardListFailed=new JSONArray();

    public AssembleUtil(Handler handler){
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

    public byte[] copyFrom(byte[] buffer,int from,int length){
        byte[] result=new byte[length];
        for(int i=from;i<(from+length);i++){
            result[i-from]=buffer[i];
        }
        return result;
    }

    public void copyTo(byte[] fromData,int from,byte[] toData){
        for(int i=0;i<fromData.length;i++){
            toData[from+i]=fromData[i];
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

    protected void sendMessage(byte[] data){
        try {
            outputStream.write(data);
            outputStream.flush();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    protected void sendSuccessMessage(){
        sendMessage(COMMAND_SUCCESS);
    }

    protected void sendFailedMessage(){
        sendMessage(COMMAND_FAILED);
    }

    public void openLock(){
        sendMessage(COMMAND_OPENLOCK);
    }

    protected void onData(byte[] data){
        if(data[0]==COMMAND_HEAD){
            byte command=data[1];
            byte[] lengthArray=new byte[2];
            lengthArray[0]=data[2];
            lengthArray[1]=data[3];
            int length=CommUtil.getShort(lengthArray);
            length--;
            byte[] info=null;
            if(length>0){
                info=copyFrom(data,4,length);
            }
            if(command==COMMAND_OPEN_BY_CARD){
                onCardNo(info);
                sendSuccessMessage();
            }else if(command==COMMAND_OPEN_BY_FINGER){
                onFinger(info);
                sendSuccessMessage();
            }else if(command==COMMAND_OPEN_BY_PASSWORD){
                onPassword(info);
                sendSuccessMessage();
            }else if(command==COMMAND_KEYWORD){
                onKeyDown(info[0]);
                sendSuccessMessage();
            }else if(command==COMMAND_OPEN_ERROR){
                onOpenError(info[0],copyFrom(info,1,2));
                sendSuccessMessage();
            }else if(command==COMMAND_CORRECT){
                if(changeFingerListIndex>=0){
                    addCurrentToList(fingerListSuccess,fingerListFailed);
                    nextFinger();
                }else if(changeCardListIndex>=0){
                    addCurrentCardToList(cardListSuccess);
                    nextCard();
                }
            }else if(command==COMMAND_ERROR){
                if(changeFingerListIndex>=0){
                    addCurrentToList(fingerListFailed,fingerListSuccess);
                    nextFinger();
                }else if(changeCardListIndex>=0){
                    addCurrentCardToList(cardListFailed);
                    nextCard();
                }
            }
        }
    }

    protected void removeFromList(int index,JSONArray list){
        for(int i=0;i<list.length();i++){
            try {
                int value = list.getInt(i);
                if(value==index){
                    list.remove(i);
                    break;
                }
            }catch(JSONException e){}
        }
    }

    protected void addCurrentToList(JSONArray list,JSONArray removelist){
        try {
            JSONObject item = changedFingerList.getJSONObject(changeFingerListIndex);
            int index=item.getInt("lockIndex");
            removeFromList(index,list);
            removeFromList(index,removelist);
            list.put(index);
        }catch(JSONException e){}
    }

    protected void addCurrentCardToList(JSONArray list){
        try {
            JSONObject item = changedCardList.getJSONObject(changeCardListIndex);
            int index=item.getInt("lockIndex");
            list.put(index);
        }catch(JSONException e){}
    }

    protected void onKeyDown(byte key){
        Message message = handler.obtainMessage();
        message.what = MainService.MSG_ASSEMBLE_KEY;
        message.obj = key;
        handler.sendMessage(message);
    }

    protected void onCardNo(byte[] card){
        int index=CommUtil.getShort(card);
        onCardOpenLock(index);
    }

    protected void onFinger(byte[] finder){
        int index=CommUtil.getShort(finder);
        onFingerOpenLock(index);
    }

    protected void onPassword(byte[] password){
    }

    protected void onOpenError(byte type,byte[] id){

    }

    protected void writeCard(byte[] cardNo,int index){
        if(cardNo.length==4){
            byte[] data=new byte[11];
            data[0]=COMMAND_HEAD;
            data[1]=COMMAND_WRITE_CARD;
            byte[] dataLength=CommUtil.getBytes((short)7);
            data[2]=dataLength[0];
            data[3]=dataLength[1];
            byte[] dataIndex=CommUtil.getBytes((short)index);
            data[4]=dataIndex[0];
            data[5]=dataIndex[1];
            copyTo(cardNo,6,data);
            convertData(data);
            sendMessage(data);
        }
    }

    protected void removeCard(int index){
        byte[] data=new byte[7];
        data[0]=COMMAND_HEAD;
        data[1]=COMMAND_WRITE_CARD;
        byte[] dataLength=CommUtil.getBytes((short)3);
        data[2]=dataLength[0];
        data[3]=dataLength[1];
        byte[] dataIndex=CommUtil.getBytes((short)index);
        data[4]=dataIndex[0];
        data[5]=dataIndex[1];
        convertData(data);
        sendMessage(data);
    }

    protected void writeFinger(byte[] finger,int index){
        if(finger.length==498){
            byte[] data=new byte[505];
            data[0]=COMMAND_HEAD;
            data[1]=COMMAND_WRITE_FINGER;
            byte[] dataLength=CommUtil.getBytes((short)501);
            data[2]=dataLength[0];
            data[3]=dataLength[1];
            byte[] dataIndex=CommUtil.getBytes((short)index);
            data[4]=dataIndex[0];
            data[5]=dataIndex[1];
            copyTo(finger,6,data);
            convertData(data);
            sendMessage(data);
        }
    }

    protected void removeFinger(int index){
        byte[] data=new byte[7];
        data[0]=COMMAND_HEAD;
        data[1]=COMMAND_WRITE_FINGER;
        byte[] dataLength=CommUtil.getBytes((short)3);
        data[2]=dataLength[0];
        data[3]=dataLength[1];
        byte[] dataIndex=CommUtil.getBytes((short)index);
        data[4]=dataIndex[0];
        data[5]=dataIndex[1];
        convertData(data);
        sendMessage(data);
    }

    public void changeFinger(JSONArray fingerList){
        this.changedFingerList=fingerList;
        this.changeFingerListIndex=-1;
        nextFinger();
    }

    protected void nextFinger(){
        changeFingerListIndex++;
        if(changeFingerListIndex<changedFingerList.length()) {
            try {
                JSONObject fingerItem = changedFingerList.getJSONObject(changeFingerListIndex);
                int index = fingerItem.getInt("lockIndex");
                String fingerStr = fingerItem.getString("finger");
                String state=fingerItem.getString("state");
                if(state.equals("N")||state.equals("F") || state.equals("G")){
                    byte[] fingerData = convertStringToByte(fingerStr);
                    writeFinger(fingerData, index);
                }else if(state.equals("D") || state.equals("R")){
                    removeFinger(index);
                }else if(state.equals("U")){
                    JSONObject newItem=new JSONObject();
                    newItem.put("lockIndex",index);
                    newItem.put("finger",fingerStr);
                    newItem.put("state","N");
                    changedFingerList.put(newItem);
                    removeFinger(index);
                }
            } catch (JSONException e) {
            }
        }else{
            changeFingerListIndex=-100;
            changedFingerList=null;
            onChangeFingerComplete();
            fingerListSuccess=new JSONArray();
            fingerListFailed=new JSONArray();
        }
    }

    public void changeCard(JSONArray cardList){
        this.changedCardList=cardList;
        this.changeCardListIndex=-1;
        nextCard();
    }

    protected void nextCard(){
        changeCardListIndex++;
        if(changeCardListIndex<changedCardList.length()) {
            try {
                JSONObject cardItem = changedCardList.getJSONObject(changeCardListIndex);
                int index = cardItem.getInt("lockIndex");
                String cardNo = cardItem.getString("cardNo");
                String state=cardItem.getString("state");
                if(state.equals("N")||state.equals("F")||state.equals("G")){
                    byte[] cardNoData = convertCardNoToByte(cardNo);
                    writeCard(cardNoData, index);
                }else if(state.equals("D") || state.equals("R")){
                    removeCard(index);
                }
            } catch (JSONException e) {
            }
        }else{
            changeCardListIndex=-100;
            changedCardList=null;
            onChangeCardComplete();
            cardListSuccess=new JSONArray();
            cardListFailed=new JSONArray();
        }
    }

    protected byte[] convertCardNoToByte(String cardNo){
        byte[] data=null;
        if(cardNo.length()==8){
            data=new byte[4];
            data[0]=(byte)(charToByte(cardNo.charAt(0)) << 4 | charToByte(cardNo.charAt(1)));
            data[1]=(byte)(charToByte(cardNo.charAt(2)) << 4 | charToByte(cardNo.charAt(3)));
            data[2]=(byte)(charToByte(cardNo.charAt(4)) << 4 | charToByte(cardNo.charAt(5)));
            data[3]=(byte)(charToByte(cardNo.charAt(6)) << 4 | charToByte(cardNo.charAt(7)));
        }
        return data;
    }

    private byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    protected byte[] convertStringToByte(String fingerStr){
        byte[] data=null;
        try {
            JSONArray finger = new JSONArray(fingerStr);
            data=new byte[finger.length()];
            for(int i=0;i<finger.length();i++){
                int value=finger.getInt(i);
                data[i]=(byte)value;
            }
        }catch(JSONException e){
        }
        return data;
    }

    protected void convertData(byte[] data){
        int total=0;
        for(int i=0;i<(data.length-1);i++){
            int byteValye=data[i]&0xff;
            total+=byteValye;
        }
        total=total%256;
        data[data.length-1]=(byte)total;
    }

    protected void onChangeFingerComplete(){
        Message message = handler.obtainMessage();
        message.what = MainService.MSG_CHANGE_FINGER;
        JSONArray[] lists=new JSONArray[2];
        lists[0]=fingerListSuccess;
        lists[1]=fingerListFailed;
        message.obj = lists;
        handler.sendMessage(message);
    }

    protected void onChangeCardComplete(){
        Message message = handler.obtainMessage();
        message.what = MainService.MSG_CHANGE_CARD;
        JSONArray[] lists=new JSONArray[2];
        lists[0]=cardListSuccess;
        lists[1]=cardListFailed;
        message.obj = lists;
        handler.sendMessage(message);
    }

    protected void onCardOpenLock(int index){
        Message message = handler.obtainMessage();
        message.what = MainService.MSG_CARD_OPENLOCK;
        message.obj = index;
        handler.sendMessage(message);
    }

    protected void onFingerOpenLock(int index){
        Message message = handler.obtainMessage();
        message.what = MainService.MSG_FINGER_OPENLOCK;
        message.obj = index;
        handler.sendMessage(message);
    }
}
