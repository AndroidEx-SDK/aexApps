package com.androidex.ble;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import static com.androidex.ble.RemindService.START_SEND;
import static com.androidex.ble.RemindService.STOP_SEND;

/**
 * Created by cts on 17/6/10.
 */

public class TextActivity extends AppCompatActivity implements View.OnClickListener {
    private AudioManager mAudioManager;
    private int currVolume;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_fragment);
        initView();
        //获取对象
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//        getVolume();//获取设备的最大音量
//        //获取当前正在播放音频的硬件信息
//        getVoiceDevice();
//        //控制手机音量大小，当传入的第一个参数为 AudioManager.ADJUST_LOWER 时，可将音量调小一个单位，传入 AudioManager.ADJUST_RAISE 时，则可以将音量调大一个单位
//        //mAudioManager.adjustVolume(AudioManager.ADJUST_RAISE, int flags);
//
//        //当前音频模式
//        mAudioManager.getMode();
//        //返回当前的铃声模式。
//        mAudioManager.getRingerMode();
//        //解释：取得当前手机的音量，最大值为7，最小值为0，当为0时，手机自动将模式调整为“震动模式”。
//        mAudioManager.getStreamVolume(mAudioManager.ROUTE_SPEAKER);
//
//        //改变铃声模式RINGER_MODE_NORMAL声音模式   RINGER_MODE_SILENT静音模式   RINGER_MODE_VIBRATE震动模式
//        mAudioManager.setRingerMode(mAudioManager.RINGER_MODE_NORMAL);


        Intent intent = new Intent(TextActivity.this, RemindService.class);
        startService(intent);

    }

    // 关闭扬声器
    private void closeSpeaker() {
        if (mAudioManager.isSpeakerphoneOn()) {
            mAudioManager.setSpeakerphoneOn(false);
            mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, currVolume,
                    AudioManager.STREAM_VOICE_CALL);
            Toast.makeText(this, "关闭扬声器", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "扬声器已关闭", Toast.LENGTH_LONG).show();
        }
    }

    //打开扬声器
    private void openSpeaker() {
        mAudioManager.setMode(AudioManager.ROUTE_SPEAKER);
        //获取扬声器的音量
        currVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        if (!mAudioManager.isSpeakerphoneOn()) {
            mAudioManager.setSpeakerphoneOn(true);
            mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                    mAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL),
                    AudioManager.STREAM_VOICE_CALL);
            Toast.makeText(this, "打开扬声器", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "扬声器已打开", Toast.LENGTH_LONG).show();
        }
    }


    /**
     * 获取当前正在播放音频的硬件信息
     */
    private void getVoiceDevice() {
        if (mAudioManager.isBluetoothA2dpOn()) {
            // Adjust output for Bluetooth. 蓝牙设备
        } else if (mAudioManager.isSpeakerphoneOn()) {
            // Adjust output for Speakerphone. 内置扬声器(免提)
        } else if (mAudioManager.isWiredHeadsetOn()) {
            // Adjust output for headsets 有线耳机
        } else {
            // If audio plays and noone can hear it, is it still playing? 未知设备
        }
    }

    /**
     * 获取设备的最大音量
     */
    private void getVolume() {
        //通话音量
        int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
        int current = mAudioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        Log.d("VIOCE_CALL", "max:" + max + "current:" + current);
        //系统音量
        max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
        current = mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
        Log.d("SYSTEM", "max:" + max + "current:" + current);
        //铃声音量
        max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
        current = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
        Log.d("RING", "max:" + max + "current:" + current);
        //音乐音量
        max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        current = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        Log.d("MUSIC", "max:" + max + "current:" + current);
        //提示声音音量
        max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        current = mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        Log.d("ALARM", "max:" + max + "current:" + current);
    }

    private void initView() {
        Button btn_start = (Button) findViewById(R.id.btn_start);
        Button btn_end = (Button) findViewById(R.id.btn_end);
        btn_start.setOnClickListener(this);
        btn_end.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_start:
                openSpeaker();
                Intent intent1 = new Intent();
                intent1.setAction(START_SEND);
                sendBroadcast(intent1);
                Toast.makeText(TextActivity.this, "启动报警", Toast.LENGTH_LONG).show();
                break;

            case R.id.btn_end:
                closeSpeaker();
                Intent intent = new Intent();
                intent.setAction(STOP_SEND);
                sendBroadcast(intent);
                Toast.makeText(TextActivity.this, "停止报警", Toast.LENGTH_LONG).show();
                break;
        }
    }
}
