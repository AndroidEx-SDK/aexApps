package com.androidex.ble.utils;

import android.media.AudioManager;

import java.util.Random;

/**
 * Created by cts on 17/6/10.
 */

public class Util {
    public static AudioManager mAudioManager;
    public static int currVolume;

    // 关闭扬声器
    public static void closeSpeaker() {
        if (mAudioManager.isSpeakerphoneOn()) {
            mAudioManager.setSpeakerphoneOn(false);
            mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, currVolume,
                    AudioManager.STREAM_VOICE_CALL);

        }
    }

    //打开扬声器
    public static void openSpeaker() {
        mAudioManager.setMode(AudioManager.ROUTE_SPEAKER);
        //获取扬声器的音量
        currVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        if (!mAudioManager.isSpeakerphoneOn()) {
            mAudioManager.setSpeakerphoneOn(true);
            mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                    mAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL),
                    AudioManager.STREAM_VOICE_CALL);
        }
    }

    public static int getRendom(int min,int max){

        Random random = new Random();

        return random.nextInt(max)%(max-min+1) + min;
    }
}
