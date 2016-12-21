package com.androidex.apps.home.utils;

import com.androidex.apps.home.FullscreenActivity;
import com.androidex.logger.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by cts on 16/12/19.
 * 定时重启系统
 */

public class RebutSystem {

    public static long delyTime = 1000*60*5;//设置5分钟重启
    private static long times =0;

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
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }
}
