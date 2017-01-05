package com.kedaxunfei;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by cts on 16/12/29.
 * 科大讯飞的语音服务，后台运行
 */

public class SpeechService extends Service{

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Speech.initSpeech(getApplicationContext());
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
