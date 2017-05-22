package com.phone.utils;

/**
 * Created by simon on 2017/3/14
 * 安卓工控设备控制器
 */

import android.os.Handler;

import com.androidex.plugins.kkfile;
import com.phone.config.DeviceConfig;

public class AexUtil {
    private String port= DeviceConfig.AEX_PORT;
    private Handler handler=null;

    public AexUtil(Handler handler){
        this.handler=handler;
    }

    /**
     * 打开设备
     */
    public void open(){
    }

    /**
     * 关闭设备
     */
    public void close(){
    }

    /**
     * 打开门禁
     */
    public int openLock(){
        int openResult=0;
        openResult=openLock(0x40,0xF0);//打开主要门
        openResult=openResult+openLock(0x40,0xF0); //打开附门
        return openResult;
    }

    /**
     * 打开门禁，延迟一段时间关门
     * @param time
     */
    public int openLock(int time,int index){
        kkfile rkey = new kkfile();
        int ident=0;
        int delay=0;
        if(index < 0 || index > 0xFE) index = 0;
        if(ident < 0 || ident > 0xFE) ident = 0;
        if(time < 0 || time > 0xFE) time = 0;
        String cmd = String.format("FB%02X2503%02X01%02X00FE",ident,index,time);
        int r = rkey.writeHex(port,cmd);
        return r > 0?1:0;
    }
}
