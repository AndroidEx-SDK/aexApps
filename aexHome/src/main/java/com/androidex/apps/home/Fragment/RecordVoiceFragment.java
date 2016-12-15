package com.androidex.apps.home.fragment;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import com.androidex.apps.home.FullscreenActivity;
import com.androidex.apps.home.R;

import java.io.IOException;

/**
 * Created by liyp on 16/12/10.
 */

public class RecordVoiceFragment extends DialogFragment implements View.OnClickListener {
    private static final String TAG = "setpassword";
    private View rootView;
    private static RecordVoiceFragment recordVoiceFragment;
    private FullscreenActivity activity;
    public boolean isCancelable = false;
    //语音操作对象
    private MediaPlayer mPlayer = null;
    private MediaRecorder mRecorder = null;

    //语音文件保存路径
    private String FileName = null;

    private boolean flag =true; //控制按钮是播放还是结束
    
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_recordvoice, container, false);
        }
        setCancelable(isCancelable);
        //设置sdcard的路径
        FileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        FileName += "/audiorecord.3gp";
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        activity = (FullscreenActivity) getActivity();
        initView();
        return rootView;
    }

    public void initView() {
        ImageView stop = (ImageView) rootView.findViewById(R.id.iv_stop);
        ImageView play = (ImageView) rootView.findViewById(R.id.iv_play);
        ImageView record = (ImageView) rootView.findViewById(R.id.iv_record);
        stop.setOnClickListener(this);
        play.setOnClickListener(this);
        record.setOnClickListener(this);
    }

    protected void showToast(String message) {
        if (!TextUtils.isEmpty(message)) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }

    }
    public static RecordVoiceFragment instance() {
        if (recordVoiceFragment == null) {
            recordVoiceFragment = new RecordVoiceFragment();
        }
        return recordVoiceFragment;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_stop:
                stopRecord();
                break;

            case R.id.iv_play:
                startPlayRecord();
                break;

            case R.id.iv_record:
                startRecord();
                break;
        }

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
