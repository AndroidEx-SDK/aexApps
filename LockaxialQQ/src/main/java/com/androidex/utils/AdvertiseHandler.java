package com.androidex.utils;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import static com.util.Constant.MSG_ADVERTISE_IMAGE;

/**
 * Created by simon on 2016/7/30.
 */
public class AdvertiseHandler implements SurfaceHolder.Callback{
    SurfaceView videoView = null;
    SurfaceHolder surfaceHolder=null;
    ImageView imageView=null;
//    LinearLayout videoPane=null;
//    LinearLayout imagePane=null;

    private MediaPlayer mediaPlayer;
    private MediaPlayer voicePlayer;
    private String mediaPlayerSource;
    private JSONArray list=null;
    private int listIndex=0;
    ImageDisplayThread imageDialpayThread=null;
    private JSONArray imageList=null;
    private int imageListIndex=0;
    private int imagePeroid=5000;

    protected Messenger dialMessenger;

    public AdvertiseHandler(){

    }

   /* public void init(SurfaceView videoView,ImageView imageView,LinearLayout videoPane,LinearLayout imagePane){
//    public void init(SurfaceView videoView,ImageView imageView,LinearLayout videoPane,LinearLayout imagePane){
//        this.videoView=videoView;
//        this.imageView=imageView;
//        this.videoPane=videoPane;
//        this.imagePane=imagePane;
//        prepareMediaView();
//    }
    */
    public void init(SurfaceView videoView, ImageView imageView){
        this.videoView=videoView;
        this.imageView=imageView;
        prepareMediaView();
    }

    public void prepareMediaView(){
        surfaceHolder=videoView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder arg0) {
        //必须在surface创建后才能初始化MediaPlayer,否则不会显示图像
        //startMediaPlay(mediaPlayerSource);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
        // TODO Auto-generated method stub
    }

    public void initData(JSONArray rows, Messenger dialMessenger, boolean isOnVideo){
        this.dialMessenger=dialMessenger;
        try {
            JSONObject row = rows.getJSONObject(0);
            list=row.getJSONArray("items");
            listIndex = 0;
            //initScreen();
            play();
            if(isOnVideo){
                pause();
            }
        }catch(JSONException e){}
    }

    public void next(){
        if(listIndex==list.length()-1){
            listIndex=0;
        }else{
            listIndex++;
        }
        play();
    }
    protected String getCurrentAdType(){
        String adType="N";
        try {
            if(list!=null&&list.length()>0){
                JSONObject item = list.getJSONObject(listIndex);
                adType=item.getString("adType");
            }
        }catch(JSONException e){
        }
        return adType;
    }

    public void play(){
        try {
            JSONObject item = list.getJSONObject(listIndex);
            String adType=item.getString("adType");
            if(adType.equals("V")){
                playVideo(item);
            }else if(adType.equals("I")){
                playImage(item);
            }
        }catch(JSONException e){
        }
    }

    public void playVideo(JSONObject item){
        try {
            String fileUrls = item.getString("fileUrls");
            JSONObject urls = new JSONObject(fileUrls);
            String source=urls.getString("video");
            source=HttpUtils.getLocalFileFromUrl(source);
            if(source!=null){
                videoView.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.INVISIBLE);
                mediaPlayerSource=source;
                initMediaPlayer();
                startMediaPlay(mediaPlayerSource);
            }else{
                next();
            }
        }catch(Exception e){}
    }

    public void playImage(JSONObject item){
        try {
            String fileUrls = item.getString("fileUrls");
            JSONObject urls = new JSONObject(fileUrls);
            String source=urls.getString("voice");
            try {
                imagePeroid = urls.getInt("period");
            }catch(Exception e){
                imagePeroid = urls.getInt("peroid");
            }
            imageList=urls.getJSONArray("images");
            source=HttpUtils.getLocalFileFromUrl(source);
            if(source!=null){
                videoView.setVisibility(View.INVISIBLE);
                imageView.setVisibility(View.VISIBLE);
                startImageDisplay();
                initVoicePlayer();
                startVoicePlay(source);
            }else{
                next();
            }
        }catch(JSONException e){}
    }

    private void startImageDisplay(){
        stopImageDisplay();
        Log.v("AdvertiseHandler","------>start image display thread<-------"+new Date());
        imageDialpayThread=new ImageDisplayThread(){
            public void run(){
                showImage();
                while(!isInterrupted()&&isWorking){ //检查线程没有被停止
                    try {
                        sleep(imagePeroid); //等待指定的一个并行时间
                    }catch(InterruptedException e){
                    }
                    if(isWorking) {
                        nextImage();
                    }
                }
                Log.v("AdvertiseHandler","------>end image display thread<-------"+new Date());
                isWorking=false;
                imageDialpayThread=null;
            }
        };
        imageDialpayThread.start();
    }

    public void nextImage(){
        if(imageListIndex==imageList.length()-1){
            imageListIndex=0;
        }else{
            imageListIndex++;
        }
        showImage();
        Log.v("AdvertiseHandler","------>showing image<-------"+new Date());
    }

    public void showImage(){
        try {
            JSONObject image = imageList.getJSONObject(imageListIndex);
            String imageFile=image.getString("image");
            if(dialMessenger!=null){
                sendDialMessenger(MSG_ADVERTISE_IMAGE,imageFile);
            }
        }catch(JSONException e){
        }
    }

    protected void sendDialMessenger(int code,Object object){
        Message message = Message.obtain();
        message.what = code;
        message.obj=object;
        try {
            dialMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void stopImageDisplay(){
        if(imageDialpayThread!=null){
            imageDialpayThread.isWorking=false;
            imageDialpayThread.interrupt();
            imageDialpayThread=null;
        }
    }

    public void initMediaPlayer() {
        //必须在surface创建后才能初始化MediaPlayer,否则不会显示图像
        mediaPlayer=new MediaPlayer();
        //mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setDisplay(surfaceHolder);
        //设置显示视频显示在SurfaceView上
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                onMediaPlayerComplete();
            }
        });
    }

    public void initVoicePlayer() {
        voicePlayer=new MediaPlayer();
        voicePlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                onVoicePlayerComplete();
            }
        });
    }

    protected void onMediaPlayerComplete(){
        mediaPlayer.release();
        next();
    }

    protected void onVoicePlayerComplete(){
        voicePlayer.release();
        stopImageDisplay();
        next();
    }

    public void startMediaPlay(String source){
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(source);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startVoicePlay(String source){
        try {
            voicePlayer.reset();
            voicePlayer.setDataSource(source);
            voicePlayer.prepare();
            voicePlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onDestroy(){
        if(mediaPlayer!=null){
            if(mediaPlayer.isPlaying()){
                mediaPlayer.stop();
            }
            mediaPlayer.release();
        }
        if(voicePlayer!=null){
            if(voicePlayer.isPlaying()){
                voicePlayer.stop();
            }
            voicePlayer.release();
        }
    }

    public void start(){
        if(getCurrentAdType().equals("V")){
            mediaPlayer.start();
        }else if(getCurrentAdType().equals("I")){
            voicePlayer.start();
        }
    }

    public void pause(){
        if(getCurrentAdType().equals("V")){
            mediaPlayer.pause();
        }else if(getCurrentAdType().equals("I")){
            voicePlayer.pause();
        }
    }
}

class ImageDisplayThread extends Thread {
    public boolean isWorking=true;
}