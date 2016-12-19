package com.androidex.apps.home.utils;

import com.androidex.apps.home.FullscreenActivity;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by cts on 16/12/19.
 * 定时重启系统
 */

public class RebutSystem {

    public static long delyTime = 1000*60*5;

    public static void reBut(final FullscreenActivity context){
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
}
