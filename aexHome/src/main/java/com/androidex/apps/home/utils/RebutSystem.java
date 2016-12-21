package com.androidex.apps.home.utils;

import com.androidex.apps.home.FullscreenActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by cts on 16/12/19.
 * 定时重启系统
 */

public class RebutSystem {

    public static long delyTime = 1000*60*5;//设置5分钟重启
    private static long times =0;

    public static long startTime;//开始老化测试的起始时间
    public static long endTime;//测试结束的时间
                                //测试时间应该为endtime-starttime

    public static void reBut(final FullscreenActivity context){
        //先得到userinfo里面的值
        try {
            JSONObject jsonObject = new JSONObject(context.hwservice.getUserInfo());
            String string = jsonObject.optString("times");
            if ((string!=null)){
                times  = Long.parseLong(string);
            }else{
                times=0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        times++;
        //用于记录重启次数
        context.hwservice.setUserInfo(stringToJson(times+""));
        //开启定时器
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                context.hwservice.runReboot();
            }
        };

        Timer timer = new Timer();
        timer.schedule(task,delyTime);
    }

    /**
    将字符串转换成json格式
     */
    private static String stringToJson(String value){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("times",value);
            jsonObject.put("starttime",getDelyTime());//测试开始时间怎么存?
            jsonObject.put("endTime",getDelyTime());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }
    /**
     * 获得当前时间的毫秒数
     */
    private static long getDelyTime(){
        Date dt= new Date();
        Long time= dt.getTime();
        return time;
    }
    /**
     * 判断起始时间的存入条件
     */
    private static boolean isFlag(){
        return false;
    }
}
