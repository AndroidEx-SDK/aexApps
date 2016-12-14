package com.androidex.apps.home.utils;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.IOException;

/**
 * Created by cts on 16/12/14.
 * 开始录音以及播放录音的工具类
 */

public class RecordVoice {
    //语音操作对象
    private MediaPlayer mPlayer = null;
    private MediaRecorder mRecorder = null;

    //语音文件保存路径
    private String FileName = null;

    private boolean flag =true; //控制按钮是播放还是结束

    public RecordVoice(){
        //设置sdcard的路径
        FileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        FileName += "/audiorecord.3gp";
    }

    /**
     * 开始录音
     */
    public  void startRecord(){
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(FileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e("RecordVoice","RecordVoice Fail");
        }
        mRecorder.start();
    }

    /**
     * 停止录音
     */
    public void stopRecord(){//录完音之后才能去执行
        if(mRecorder!=null){
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }

    }

    /**
     * 开始播放录音
     */
    public void startPlayRecord(){
        mPlayer = new MediaPlayer();
        if(FileName!=null){
            try{
                mPlayer.setDataSource(FileName);
                mPlayer.prepare();
                mPlayer.start();
            }catch(IOException e){
                Log.e("RecordVoice","RecordPlay Fail");
            }
        }
    }

    /**
     * 停止播放录音
     */
    public void stopPlayRecord(){
        if (mPlayer!=null){
            mPlayer.release();
            mPlayer = null;
        }

    }
}
