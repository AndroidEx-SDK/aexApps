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

    public static long delyTime = 1000 * 60 * 60;//设置60分钟重启
    private static long times = 0;

    public static String startTime = "startTime";//开始老化测试的起始时间
    public static String endTime = "endTime";//测试结束的时间
    private static TimerTask task;

    //测试时间应该为endtime-starttime

    public static void reBut(final FullscreenActivity context) {
        //先得到userinfo里面的值、

        try {
            JSONObject jsonObject = new JSONObject(context.hwservice.getUserInfo());
            String string = jsonObject.optString("times");
            if ((string != null)) {
                times = Long.parseLong(string);
            } else {
                times = 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        times++;
        //用于记录重启次数
        context.hwservice.setUserInfo(stringToJson(times + "", context));
        //开启定时器
        task = new TimerTask() {


            @Override
            public void run() {
                context.hwservice.runReboot();
            }
        };

        Timer timer = new Timer();
        timer.schedule(task, delyTime);
    }

    public static void stopReBut() {
        if (task != null) {
            task.cancel();
        }
    }
    public static void startRebut(){
        if (task == null) {
           task.run();
        }
    }

    /**
     * 将字符串转换成json格式
     */
    private static String stringToJson(String value, final FullscreenActivity context) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("times", value);
            if (isFlag(context)) {//存入当前时间
                jsonObject.put(startTime, getDelyTime());//测试开始时间怎么存?
            } else {//得到上次存入的时间
                jsonObject.put(startTime, getStartTime(context));
            }
            jsonObject.put(endTime, getDelyTime());

        } catch (JSONException e) {
            e.printStackTrace();

        }
        return jsonObject.toString();
    }

    /**
     * 获得当前时间的毫秒数
     */

    public static long getDelyTime() {
        Date dt = new Date();
        Long time = dt.getTime();
        return time;
    }

    /**
     * 判断起始时间的存入条件
     */
    private static boolean isFlag(final FullscreenActivity context) {
        try {
            JSONObject jsonObject = new JSONObject(context.hwservice.getUserInfo());
            String string = jsonObject.optString(startTime);
            if (!"".equals(string)) {//里面有值，返回false，不存
                return false;
            } else {//里面没值，存入
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String getStartTime(final FullscreenActivity context) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(context.hwservice.getUserInfo());
            String string = jsonObject.optString(startTime);
            return string;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param startTime
     * @param endTime
     * @return 计算测试的时长（天/时/分/秒）
     */
    public static String getTextHours(String startTime, String endTime) {
        long start = Long.parseLong(startTime);
        long end = Long.parseLong(endTime);
        long time = end - start;
        String result = formatTime(time);
        return result;
    }

    /*
    * 毫秒转化时分秒毫秒
    */
    private static String formatTime(Long ms) {
        Integer ss = 1000;
        Integer mi = ss * 60;
        Integer hh = mi * 60;
        Integer dd = hh * 24;

        Long day = ms / dd;
        Long hour = (ms - day * dd) / hh;
        Long minute = (ms - day * dd - hour * hh) / mi;
        Long second = (ms - day * dd - hour * hh - minute * mi) / ss;
        Long milliSecond = ms - day * dd - hour * hh - minute * mi - second * ss;

        StringBuffer sb = new StringBuffer();
        if (day > 0) {
            sb.append(day + "天");
        }
        if (hour > 0) {
            sb.append(hour + "小时");
        }
        if (minute > 0) {
            sb.append(minute + "分");
        }
        if (second > 0) {
            sb.append(second + "秒");
        }
        if (milliSecond > 0) {
            sb.append(milliSecond + "毫秒");
        }
        return sb.toString();
    }
}
