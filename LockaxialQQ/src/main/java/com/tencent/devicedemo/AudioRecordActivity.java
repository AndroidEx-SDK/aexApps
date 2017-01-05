package com.tencent.devicedemo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.KeyEvent;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidex.DoorLock;
import com.audiorecoder.AudioRecoderDialog;
import com.audiorecoder.AudioRecoderUtils;
import com.tencent.device.TXDeviceService;

import java.io.File;
import java.io.IOException;

/**
 * Created by xinshuhao on 16/7/17.
 */
public class AudioRecordActivity extends Activity implements AudioRecoderUtils.OnAudioStatusUpdateListener {
    private AudioRecoderDialog recoderDialog;
    private AudioRecoderUtils recoderUtils;
    private CheckBox cb_record;
    private TextView tt_tv;
    private long downT;
    private String audioFile;
    private Long peerTinyId;
    private long[] aa=new long[1];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audiorecord);
        recoderDialog = new AudioRecoderDialog(this);
        recoderDialog.setShowAlpha(0.98f);
        recoderDialog.setFocusable(false);

        Intent intent = getIntent();
        peerTinyId = intent.getLongExtra("tinyid", 0);
        aa[0]=peerTinyId;

         audioFile = this.getCacheDir().getAbsolutePath() + "/recoder.amr";
        recoderUtils = new AudioRecoderUtils(new File(audioFile));
        recoderUtils.setOnAudioStatusUpdateListener(this);
        cb_record=(CheckBox)findViewById(R.id.cb_audiorecord);
        tt_tv=(TextView)findViewById(R.id.tv_recordname);

      TextView  tt_tv1=(TextView)findViewById(R.id.tv_recordname);
        TextView  tt_tv2=(TextView)findViewById(R.id.btn_send_audio);
        TextView  tt_tv3=(TextView)findViewById(R.id.tv_back);
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/GBK.TTF");
       tt_tv1.setTypeface(typeFace);
        tt_tv2.setTypeface(typeFace);
        tt_tv3.setTypeface(typeFace);


        cb_record.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    recoderUtils.startRecord();
                    downT = System.currentTimeMillis();
                   recoderDialog.showAtLocation(cb_record, Gravity.CENTER, 0, 0);
                    tt_tv.setText("录音中(2键停止)");
                   }else if(!isChecked){
                    recoderUtils.stopRecord();
                    tt_tv.setText("录音(1键)");
                    recoderDialog.dismiss();
                }
            }
        });


        String audiopath=intent.getStringExtra("filepath");
         mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            if(audiopath.endsWith("amr")){
            mediaPlayer.setDataSource(audiopath);}
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.start();

        IntentFilter filter = new IntentFilter();
        filter.addAction(TXDeviceService.voicereceive);
        mNotifyReceiver = new NotifyReceiver();
        registerReceiver(mNotifyReceiver, filter);

    }
    private   MediaPlayer mediaPlayer;
    @Override
    public void onUpdate(double db) {
        if(null != recoderDialog) {
            int level = (int) db;
            recoderDialog.setLevel((int) db);
            recoderDialog.setTime(System.currentTimeMillis() - downT);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mNotifyReceiver);
        mediaPlayer.stop();
         mediaPlayer.release();
    }
    private NotifyReceiver  mNotifyReceiver;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode){
            case KeyEvent.KEYCODE_1:
                cb_record.setChecked(true);
                return true;
            case KeyEvent.KEYCODE_2:
                recoderDialog.dismiss();
             cb_record.setChecked(false);
                return true;
            case KeyEvent.KEYCODE_3:
                {
                TXDeviceService.sendAudioMsg(audioFile, (int)(System.currentTimeMillis() - downT),1 , aa);
                }
                return true;
            case KeyEvent.KEYCODE_DEL:
                finish();
                break;
        }
        return super.onKeyUp(keyCode, event);
    }

    public class NotifyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
             if(intent.getAction().equals(TXDeviceService.voicereceive)){
                String filepath=intent.getStringExtra("filepath");
                if ("".equals(filepath)&&!filepath.endsWith("amr")) return;
                 mediaPlayer = new MediaPlayer();
                 mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                 try {
                     mediaPlayer.setDataSource(filepath);
                     mediaPlayer.prepare();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
                 mediaPlayer.start();
            }
        }
    }
}
