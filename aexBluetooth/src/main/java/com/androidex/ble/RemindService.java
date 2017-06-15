package com.androidex.ble;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

import java.io.IOException;

/**
 * Created by cts on 17/6/10.
 */

public class RemindService extends Service {
    public static final String START_SEND = "com.androidex.start_send";
    public static final String STOP_SEND = "com.androidex.stop_send";
    private MediaPlayer mMediaPlayer = null;
    private Vibrator vibrator;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

        MyReceiver myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(START_SEND);
        intentFilter.addAction(STOP_SEND);

        registerReceiver(myReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            // 要释放资源，不然会打开很多个MediaPlayer
            mMediaPlayer.release();
        }
        if (vibrator != null) {
            vibrator.cancel();
        }
        super.onDestroy();
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        return super.onStartCommand(intent, flags, startId);
    }

    public void stopMessage() {
        // TODO Auto-generated method stub
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            // 要释放资源，不然会打开很多个MediaPlayer
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        if (vibrator != null) {
            vibrator.cancel();
            vibrator = null;
        }
    }

    private void startMessage() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        PlaySound(this);//响铃

        //震动
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        // 等待3秒，震动3秒，从第0个索引开始，一直循环
        vibrator.vibrate(new long[]{3000, 3000}, 0);

        // 无论是否震动、响铃，都有状态栏提示
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentInfo("紧急报警");
        builder.setContentText("箱子丢失，请尽快连接上蓝牙");
        builder.setContentTitle("紧急报警");
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setTicker("箱子丢失");
        builder.setAutoCancel(true);
        builder.setWhen(System.currentTimeMillis());//设置时间，设置为系统当前的时间
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, DeviceScanActivity.class), PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();

        /**
         * 手机处于锁屏状态时， LED灯就会不停地闪烁， 提醒用户去查看手机,下面是绿色的灯光一 闪一闪的效果
         */
        notification.ledARGB = Color.GREEN;// 控制 LED 灯的颜色，一般有红绿蓝三种颜色可选
        notification.ledOnMS = 1000;// 指定 LED 灯亮起的时长，以毫秒为单位
        notification.ledOffMS = 1000;// 指定 LED 灯暗去的时长，也是以毫秒为单位
        notification.flags = Notification.FLAG_SHOW_LIGHTS;// 指定通知的一些行为，其中就包括显示

        manager.notify(1, notification);
    }


    public void PlaySound(final Context context) {
        Log.e("ee", "正在响铃");
        // 使用来电铃声的铃声路径
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        // 如果为空，才构造，不为空，说明之前有构造过
        try {
            if (mMediaPlayer == null) {
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setDataSource(context, uri);
                mMediaPlayer.setLooping(true); //循环播放
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            }else{
                //Toast.makeText(RemindService.this, "重复响铃", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case START_SEND:
                    startMessage();
                    break;
                case STOP_SEND:
                    stopMessage();
                    break;
            }
        }
    }

}
