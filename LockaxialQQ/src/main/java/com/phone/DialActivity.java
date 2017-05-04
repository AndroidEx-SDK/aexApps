package com.phone;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.phone.config.DeviceConfig;
import com.phone.service.MainService;
import com.phone.utils.AdvertiseHandler;
import com.phone.utils.HttpUtils;
import com.phone.utils.UploadUtil;
import com.tencent.devicedemo.R;

import org.json.JSONArray;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import jni.util.Utils;

public class DialActivity extends Activity implements TakePictureCallback{
    public static final int MSG_RTC_NEWCALL=10000;
    public static final int MSG_RTC_ONVIDEO=10001;
    public static final int MSG_RTC_DISCONNECT=10002;
    public static final int MSG_PASSWORD_CHECK=10003;
    public static final int MSG_LOCK_OPENED=10004;
    public static final int MSG_CALLMEMBER_ERROR=10005;
    public static final int MSG_CALLMEMBER_TIMEOUT=11005;
    public static final int MSG_CALLMEMBER_NO_ONLINE=12005;
    public static final int MSG_CALLMEMBER_SERVER_ERROR=12105;
    public static final int MSG_CALLMEMBER_TIMEOUT_AND_TRY_DIRECT=13005;
    public static final int MSG_CALLMEMBER_DIRECT_TIMEOUT=14005;
    public static final int MSG_CALLMEMBER_DIRECT_DIALING=15005;
    public static final int MSG_CALLMEMBER_DIRECT_SUCCESS=16005;
    public static final int MSG_CALLMEMBER_DIRECT_FAILED=17005;
    public static final int MSG_CALLMEMBER_DIRECT_COMPLETE=18005;

    public static final int MSG_CONNECT_ERROR=10007;
    public static final int MSG_CONNECT_SUCCESS=10008;
    public static final int ON_YUNTONGXUN_INIT_ERROR=10009;
    public static final int ON_YUNTONGXUN_LOGIN_SUCCESS=10010;
    public static final int ON_YUNTONGXUN_LOGIN_FAIL=10011;
    public static final int MSG_CANCEL_CALL_COMPLETE=10012;

    public static final int MSG_ADVERTISE_REFRESH=10013;
    public static final int MSG_ADVERTISE_IMAGE=10014;
    public static final int MSG_INVALID_CARD=10015;
    public static final int MSG_CHECK_BLOCKNO=10016;
    public static final int MSG_FINGER_CHECK=10017;
    public static final int MSG_REFRESH_DATA=10018;
    public static final int MSG_REFRESH_COMMUNITYNAME=10019;
    public static final int MSG_REFRESH_LOCKNAME=10020;

    public static final int CALL_MODE=1;
    public static final int PASSWORD_MODE=2;
    public static final int CALLING_MODE=3;
    public static final int ONVIDEO_MODE=4;
    public static final int DIRECT_MODE=5;
    public static final int ERROR_MODE=6;
    public static final int DIRECT_CALLING_MODE=7;
    public static final int DIRECT_CALLING_TRY_MODE=8;
    public static final int PASSWORD_CHECKING_MODE=9;
    public static final int CALL_CANCEL_MODE=10;
    protected Messenger serviceMessenger;
    protected Messenger dialMessenger;
    protected Handler handler=null;

    public static int currentStatus=CALL_MODE;
    private String blockNo="";
    private int blockId=0;
    private String callNumber="";
    private String guestPassword="";
    private HashMap<String,String> uuidMaps=new HashMap<String,String>();
    private String lastImageUuid="";
    private int checkingStatus=0;

    //VideoView video;
    TextView headPaneTextView=null;
    SurfaceView videoView = null;
    ImageView imageView=null;

    SurfaceView localView = null;
    SurfaceView remoteView = null;
//    LinearLayout videoPane=null;
//    LinearLayout imagePane=null;
    //LinearLayout callLayout;
    //LinearLayout guestLayout;
    LinearLayout videoLayout;

    SurfaceView autoCameraSurfaceView=null;
    SurfaceHolder autoCameraHolder=null;

    AdvertiseHandler advertiseHandler=null;
    Thread passwordTimeoutThread=null;
    Thread clockRefreshThread=null;

    //private static SZOEMHost_Lib fingerHost;
    SoundPool soundPool=null;
    int keyVoiceIndex=0;

    //private NfcReader nfcReader;
    private static final String TAG = "DialActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        if(DeviceConfig.HIDE_SCREEN_STATUS==1) {
            Window window = getWindow();
            WindowManager.LayoutParams params = window.getAttributes();
            params.systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE;
            window.setAttributes(params);
        }
        setContentView(R.layout.activity_dial);
        initScreen();
        //initSuperID();
        Intent intent = new Intent(DialActivity.this,MainService.class);
        bindService(intent,connection,0);
        initHandler();
        initVoiceHandler();
        initVoiceVolume();
        initAdvertiseHandler();
        initAutoCamera();
        if(DeviceConfig.DEVICE_TYPE.equals("C")){
            setDialStatus("请输入楼栋编号");
        }
        startClockRefresh();
        /*initFingerHost();
        initAexNfcReader();
        openFingerDevice();*/
        boolean initStatus=this.getIntent().getBooleanExtra("INIT_STATUS",true);
        if(!initStatus){
            onConnectionError();
        }
    }

   /* private void initAexNfcReader(){
        if(DeviceConfig.IS_NFC_AVAILABLE){
            nfcReader = new NfcReader(this);
            enableReaderMode();
            Receive receive = new Receive();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(NfcReader.ACTION_NFC_CARDINFO);
            registerReceiver(receive, intentFilter);
        }
    }*/

    /**
     * 启用NFC读卡
     * 传入的第二个参数为当前activity的时候在activity的回调方法会被调用
     */
    /*public void enableReaderMode() {
        if(DeviceConfig.IS_NFC_AVAILABLE) {
            NfcAdapter nfc = NfcAdapter.getDefaultAdapter(this);
            if (nfc != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    if (this instanceof NfcAdapter.ReaderCallback) {
                        nfc.enableReaderMode(this, this, NfcReader.READER_FLAGS, null);
                    }
                }
            }
        }
    }*/

    /**
     * 禁用NFC读卡
     */
    /*public void disableReaderMode() {
        if(DeviceConfig.IS_NFC_AVAILABLE) {
            NfcAdapter nfc = NfcAdapter.getDefaultAdapter(this);
            if (nfc != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    nfc.disableReaderMode(this);
                }
            }
        }
    }*/

    /**
     * 此函数实现NfcAdapter.ReaderCallback接口，这里调用NFC Reader类的接口来实现该函数的功能。
     * 这里只是为了把这个调用映射到此Activity而已。
     *
     * @param
     */
   /* @Override
    public void onTagDiscovered(Tag tag) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if ((nfcReader != null) && (nfcReader instanceof NfcAdapter.ReaderCallback)) {
                NfcAdapter.ReaderCallback nfcReader = (NfcAdapter.ReaderCallback) this.nfcReader;
                nfcReader.onTagDiscovered(tag);
            }
        }
    }*/

    /*@Override
    public void onAccountReceived(String account){
        Message message = Message.obtain();
        message.what = MainService.MSG_CARD_INCOME;
        message.obj = account;
        try {
            serviceMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }*/


    protected void initAutoCamera(){
        autoCameraSurfaceView = (SurfaceView) findViewById(R.id.autoCameraSurfaceview);
        autoCameraHolder = autoCameraSurfaceView.getHolder();
        autoCameraHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    private String getUUID(){
        UUID uuid  =  UUID.randomUUID();
        String result = UUID.randomUUID().toString();
        return result;
    }

    private boolean checkTakePictureAvailable(String uuid){
        String thisValue=uuidMaps.get(uuid);
        boolean result=false;
        if(thisValue!=null&&thisValue.equals("Y")){
            result=true;
        }
        Log.v("MainService", "检查UUID"+uuid+result);
        return result;
    }
    private void setImageUuidAvaibale(String uuid){
        Log.v("MainService", "加入UUID"+uuid);
        uuidMaps.put(uuid,"Y");
    }

    private void clearImageUuidAvaible(String uuid){
        Log.v("MainService", "清除UUID"+uuid);
        uuidMaps.remove(uuid);
    }

    protected void takePicture(final String thisValue, final boolean isCall, final TakePictureCallback callback){
        if(currentStatus==CALLING_MODE||currentStatus==PASSWORD_CHECKING_MODE){
            final String uuid=getUUID();
            lastImageUuid=uuid;
            setImageUuidAvaibale(uuid);
            callback.beforeTakePickture(thisValue,isCall,uuid);
            Log.v("MainService", "开始启动拍照");
            new Thread(){
                public void run(){
                    final String thisUuid=uuid;
                    if(checkTakePictureAvailable(thisUuid)){
                        doTakePicture(thisValue,isCall,uuid,callback);
                    }else{
                        Log.v("MainService", "取消拍照");
                    }
                }
            }.start();
        }
    }

    private synchronized void doTakePicture(final String thisValue, final boolean isCall, final String uuid, final TakePictureCallback callback){
        Camera camera=null;
        try{
            camera= Camera.open();

        }catch(Exception e){
        }
        Log.v("MainService", "打开相机");
        if(camera==null){
            try {
                camera = Camera.open(0);
            }catch(Exception e){}
        }
        if(camera!=null){
            try {
                Camera.Parameters parameters = camera.getParameters();
                parameters.setPreviewSize(320,240);
                try {
                    camera.setParameters(parameters);
                }catch(Exception err){
                    err.printStackTrace();
                }
                camera.setPreviewDisplay(autoCameraHolder);
                camera.startPreview();
                camera.autoFocus(null);
                Log.v("MainService", "开始拍照");
                camera.takePicture(null, null, new Camera.PictureCallback() {
                    public void onPictureTaken(byte[] data, Camera camera) {
                        try {
                            Log.v("MainService", "拍照成功");
                            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                            final File file = new File(Environment.getExternalStorageDirectory(), System.currentTimeMillis() + ".jpg");
                            FileOutputStream outputStream = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                            outputStream.close();
                            camera.stopPreview();
                            camera.release();
                            Log.v("MainService", "释放照相机资源");
                            final String url = DeviceConfig.SERVER_URL + "/app/upload/image";
                            if(checkTakePictureAvailable(uuid)) {
                                new Thread() {
                                    public void run() {
                                        String fileUrl = null;
                                        try {
                                            Log.v("MainService", "开始上传照片");
                                            fileUrl = UploadUtil.uploadFile(file, url);
                                            Log.v("MainService", "上传照片成功");
                                        } catch (Exception e) {
                                        }
                                        if(checkTakePictureAvailable(uuid)) {
                                            callback.afterTakePickture(thisValue, fileUrl, isCall, uuid);
                                        }else{
                                            Log.v("MainService", "上传照片成功,但已取消");
                                        }
                                        clearImageUuidAvaible(uuid);
                                        Log.v("MainService", "正常清除"+uuid);
                                        try {
                                            if (file != null) {
                                                file.deleteOnExit();
                                            }
                                        } catch (Exception e) {
                                        }
                                    }
                                }.start();
                            }else{
                                Log.v("MainService", "拍照成功，但已取消");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }catch(Exception e){
                try{
                    camera.stopPreview();
                }catch(Exception err){
                }
                try{
                    camera.release();
                }catch(Exception err){
                }
                callback.afterTakePickture(thisValue,null,isCall,uuid);
                Log.v("MainService", "照相出异常清除UUID");
                clearImageUuidAvaible(uuid);
            }
        }
    }
    public void beforeTakePickture(final String thisValue, final boolean isCall, String uuid){
        startDialorPasswordDirectly(thisValue,null,isCall,uuid);
    }

    public void afterTakePickture(final String thisValue, String fileUrl, final boolean isCall, String uuid){
        startSendPictureDirectly(thisValue,fileUrl,isCall,uuid);
    }

    protected void startDialorPasswordDirectly(final String thisValue, final String fileUrl, final boolean isCall, String uuid){
        if(currentStatus==CALLING_MODE||currentStatus==PASSWORD_CHECKING_MODE){
            Message message = Message.obtain();
            if(isCall) {
                setDialValue("呼叫"+thisValue+"，取消请按#号键");
                message.what = MainService.MSG_START_DIAL;
            }else{
                setTempkeyValue("准备验证密码"+thisValue+"...");
                message.what = MainService.MSG_CHECK_PASSWORD;
            }
            String[] parameters=new String[3];
            parameters[0]=thisValue;
            parameters[1]=fileUrl;
            parameters[2]=uuid;
            message.obj = parameters;
            try {
                serviceMessenger.send(message);
            } catch (RemoteException er) {
                er.printStackTrace();
            }
        }
    }

    protected void startSendPictureDirectly(final String thisValue, final String fileUrl, final boolean isCall, String uuid){
        if(fileUrl==null||fileUrl.length()==0){
            return;
        }
        Message message = Message.obtain();
        if(isCall) {
            message.what = MainService.MSG_START_DIAL_PICTURE;
        }else{
            message.what = MainService.MSG_CHECK_PASSWORD_PICTURE;
        }
        String[] parameters=new String[3];
        parameters[0]=thisValue;
        parameters[1]=fileUrl;
        parameters[2]=uuid;
        message.obj = parameters;
        try {
            serviceMessenger.send(message);
        } catch (RemoteException er) {
            er.printStackTrace();
        }
    }

   /* protected void initFingerHost(){
        if(DeviceConfig.IS_FINGER_AVAILABLE){
            if(fingerHost == null){
                fingerHost = new SZOEMHost_Lib(this,this);
            }else{
                fingerHost.SZOEMHost_Lib_Init(this,this);
            }
        }
    }*/

    /*public void openFingerDevice()
    {
        if(DeviceConfig.IS_FINGER_AVAILABLE) {
            if (fingerHost.OpenDevice() == 0) {
            }
        }
    }*/

    protected void initVoiceVolume(){
        AudioManager audioManager=(AudioManager)getSystemService(this.AUDIO_SERVICE);
        initVoiceVolume(audioManager, AudioManager.STREAM_MUSIC,DeviceConfig.VOLUME_STREAM_MUSIC);
        initVoiceVolume(audioManager, AudioManager.STREAM_RING,DeviceConfig.VOLUME_STREAM_RING);
        initVoiceVolume(audioManager, AudioManager.STREAM_SYSTEM,DeviceConfig.VOLUME_STREAM_SYSTEM);
        initVoiceVolume(audioManager, AudioManager.STREAM_VOICE_CALL,DeviceConfig.VOLUME_STREAM_VOICE_CALL);
    }

    protected void initVoiceVolume(AudioManager audioManager, int type, int value){
        int thisValue=audioManager.getStreamMaxVolume(type);
        thisValue=thisValue*value/10;
        audioManager.setStreamVolume(type,thisValue, AudioManager.FLAG_PLAY_SOUND);
    }

    /*protected void initSuperID(){
        SuperID.initFaceSDK(this);
        SuperID.setDebugMode(true);
        HashMap<String, String> map = new HashMap<String,String>();
        map.put(SDKConfig.KEY_CAMERATYPE,"0");
        SuperID.getInstance().setHashMap(map);
    }*/

    protected void initScreen(){
        //callLayout=(LinearLayout) findViewById(R.id.call_pane);
        //guestLayout=(LinearLayout) findViewById(R.id.guest_pane);
        headPaneTextView=(TextView)findViewById(R.id.header_pane);
        videoLayout=(LinearLayout) findViewById(R.id.ll_video);

//        videoPane = (LinearLayout) findViewById(R.id.video_pane);
//        imagePane = (LinearLayout) findViewById(R.id.image_pane);
        //remoteLayout = (LinearLayout) findViewById(R.id.ll_remote);

        setTextView(R.id.tv_community,MainService.communityName);
        setTextView(R.id.tv_lock,MainService.lockName);
    }

    protected void initAdvertiseHandler(){
        advertiseHandler=new AdvertiseHandler();
        videoView=(SurfaceView)findViewById(R.id.surface_view);

        imageView=(ImageView)findViewById(R.id.image_view);
        //advertiseHandler.init(videoView,imageView,videoPane,imagePane);
        advertiseHandler.init(videoView,imageView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
        advertiseHandler.onDestroy();
        /*fingerHost.CloseDevice();
        disableReaderMode();*/
    }

    private void initHandler(){
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == MSG_RTC_NEWCALL){
                    onRtcConnected();
                }else if(msg.what == MSG_RTC_ONVIDEO){
                    onRtcVideoOn();
                }else if(msg.what == MSG_RTC_DISCONNECT){
                    onRtcDisconnect();
                }else if(msg.what==MSG_PASSWORD_CHECK){
                    onPasswordCheck((Integer) msg.obj);
                }else if(msg.what==MSG_LOCK_OPENED){
                    onLockOpened();
                }else if(msg.what==MSG_CALLMEMBER_ERROR){
                    onCallMemberError(msg.what);
                }else if(msg.what==MSG_CALLMEMBER_SERVER_ERROR){
                    onCallMemberError(msg.what);
                }else if(msg.what==MSG_CALLMEMBER_NO_ONLINE){
                    onCallMemberError(msg.what);
                }else if(msg.what==MSG_CALLMEMBER_TIMEOUT){
                    onCallMemberError(msg.what);
                }else if(msg.what==MSG_CALLMEMBER_TIMEOUT_AND_TRY_DIRECT){
                    Utils.DisplayToast(DialActivity.this, "可视对讲无法拨通，尝试直拨电话");
                    setCurrentStatus(DIRECT_CALLING_TRY_MODE);
                }else if(msg.what==MSG_CALLMEMBER_DIRECT_TIMEOUT){
                    onCallMemberError(msg.what);
                }else if(msg.what==MSG_CALLMEMBER_DIRECT_DIALING){
                    Utils.DisplayToast(DialActivity.this, "开始直拨电话");
                    setCurrentStatus(DIRECT_CALLING_MODE);
                }else if(msg.what==MSG_CALLMEMBER_DIRECT_SUCCESS){
                    Utils.DisplayToast(DialActivity.this, "电话已接通，请让对方按#号键开门");
                    onCallDirectlyBegin();
                }else if(msg.what==MSG_CALLMEMBER_DIRECT_FAILED){
                    Utils.DisplayToast(DialActivity.this, "电话未能接通，重试中..");
                }else if(msg.what==MSG_CALLMEMBER_DIRECT_COMPLETE){
                    onCallDirectlyComplete();
                }else if(msg.what==MSG_CONNECT_ERROR){
                    onConnectionError();
                }else if(msg.what==MSG_CONNECT_SUCCESS){
                    onConnectionSuccess();
                }else if(msg.what==ON_YUNTONGXUN_INIT_ERROR){
                    Utils.DisplayToast(DialActivity.this, "直拨电话初始化异常");
                }else if(msg.what==ON_YUNTONGXUN_LOGIN_SUCCESS){
                    Utils.DisplayToast(DialActivity.this, "直拨电话服务器连接成功");
                }else if(msg.what==ON_YUNTONGXUN_LOGIN_FAIL){
                    Utils.DisplayToast(DialActivity.this, "直拨电话服务器连接失败");
                }else if(msg.what==MSG_CANCEL_CALL_COMPLETE){
                    setCurrentStatus(CALL_MODE);
                }else if(msg.what==MSG_ADVERTISE_REFRESH){
                    onAdvertiseRefresh(msg.obj);
                }else if(msg.what==MSG_ADVERTISE_IMAGE){
                    onAdvertiseImageChange(msg.obj);
                }else if(msg.what==MSG_INVALID_CARD){
                    Utils.DisplayToast(DialActivity.this, "无效房卡");
                }else if(msg.what==MainService.MSG_ASSEMBLE_KEY){
                    int keyCode=(Integer)msg.obj;
                    onKeyDown(keyCode);
                }else if(msg.what==MSG_CHECK_BLOCKNO){
                    blockId=(Integer)msg.obj;
                    onCheckBlockNo();
                }else if(msg.what==MSG_FINGER_CHECK){
                    boolean result=(Boolean)msg.obj;
                    onFingerCheck(result);
                }else if(msg.what==MSG_REFRESH_DATA){
                    onFreshData((String)msg.obj);
                }else if(msg.what==MSG_REFRESH_COMMUNITYNAME){
                    onFreshCommunityName((String)msg.obj);
                }else if(msg.what==MSG_REFRESH_LOCKNAME){
                    onFreshLockName((String)msg.obj);
                }
            }
        };
        dialMessenger=new Messenger(handler);
    }
//
//    protected void startAdvertisementInit(){
//        Message message = Message.obtain();
//        message.what = MainService.MSG_ADVERTISE_INIT;
//        try {
//            serviceMessenger.send(message);
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
//    }

    private void onFreshData(String type){
        if("card".equals(type)){
            Utils.DisplayToast(DialActivity.this, "更新卡数据");
        }else if("finger".equals(type)){
            Utils.DisplayToast(DialActivity.this, "更新指纹数据");
        }
    }

    private void onFreshCommunityName(String communityName){
        if(communityName!=null){
            setCommunityName(communityName);
        }
    }

    private void onFreshLockName(String lockName){
        if(lockName!=null){
            setLockName(lockName);
        }
    }

    private void onFingerCheck(boolean result){
        if(result){
            Utils.DisplayToast(DialActivity.this, "指纹开门成功");
        }else{
            Utils.DisplayToast(DialActivity.this, "指纹开门失败，您没有获得该门禁的权限");
        }
    }

    private void onCheckBlockNo(){
        checkingStatus=0;
        if(blockId==0){
            blockNo="";
            callNumber="";
            setDialValue(blockNo);
            Utils.DisplayToast(DialActivity.this, "楼栋编号不存在");
        }if(blockId<0){
            blockNo="";
            callNumber="";
            blockId=0;
            setDialValue(blockNo);
            Utils.DisplayToast(DialActivity.this, "获取楼栋数据失败，请联系管理处");
        }else{
            callNumber="";
            setDialValue(callNumber);
            setDialStatus("输入房间号呼叫");
        }
    }

    private void onConnectionError(){
        setCurrentStatus(ERROR_MODE);
        setTextView(R.id.header_pane,"可视对讲设备异常，网络连接已断开");
        headPaneTextView.setVisibility(View.VISIBLE);
    }

    private void onConnectionSuccess(){
        if(currentStatus==ERROR_MODE){
            initDialStatus();
            setTextView(R.id.header_pane,"");
            headPaneTextView.setVisibility(View.INVISIBLE);
        }
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //获取Service端的Messenger
            serviceMessenger = new Messenger(service);
            Message message = Message.obtain();
            message.what = MainService.REGISTER_ACTIVITY_DIAL;
            message.replyTo = dialMessenger;
            try {
                //通过ServiceMessenger将注册消息发送到Service中的Handler
                serviceMessenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private void keyInput(int key){

    }

    private void startCancelDirectCall(){
        Message message = Message.obtain();
        message.what = MainService.MSG_CANCEL_DIRECT;
        try {
            serviceMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void startDisconnectDirectCall(){
        Message message = Message.obtain();
        message.what = MainService.MSG_DISCONNECT_DIRECT;
        try {
            serviceMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void startDisconnectVideo(){
        Message message = Message.obtain();
        message.what = MainService.MSG_DISCONNECT_VIEDO;
        try {
            serviceMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void startDialing(){
        setCurrentStatus(CALLING_MODE);
        String thisNumber=callNumber;
        callNumber="";
        if(DeviceConfig.DEVICE_TYPE=="C"){
            blockId=0;
            blockNo ="";
            setDialStatus("请输入楼栋编号");
        }
        takePicture(thisNumber,true,this);
    }

    private void callInput(int key){
        if(DeviceConfig.DEVICE_TYPE.equals("C")){
            if(blockId==0){
                if(blockNo.length()<DeviceConfig.BLOCK_NO_LENGTH){
                    blockNo=blockNo+key;
                    setDialValue(blockNo);
                }
                if(blockNo.length()== DeviceConfig.BLOCK_NO_LENGTH){
                    checkingStatus=1;
                    setDialValue("检查楼栋编号："+blockNo);
                    Message message = Message.obtain();
                    message.what = MainService.MSG_CHECK_BLOCKNO;
                    message.obj = blockNo;
                    try {
                        serviceMessenger.send(message);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }else{
                if(checkingStatus==0) {
                    unitNoInput(key);
                }
            }
        }else{
            unitNoInput(key);
        }
    }
    private void unitNoInput(int key){
        callNumber=callNumber+key;
        setDialValue(callNumber);
        if(callNumber.length()== DeviceConfig.UNIT_NO_LENGTH){
            startDialing();
        }
    }

    private String backKey(String code){
        if(code!=null&&code!=""){
            int length=code.length();
            if(length==1){
                code="";
            }else{
                code=code.substring(0,(length-1));
            }
        }
        return code;
    }
    private void callInput(){
        if(DeviceConfig.DEVICE_TYPE.equals("C")) {
            if(blockId>0){
                if(callNumber.equals("")){
                    blockId=0;
                    blockNo = backKey(blockNo);
                    setDialStatus("请输入楼栋编号");
                    setDialValue(blockNo);
                }else{
                    callNumber = backKey(callNumber);
                    setDialValue(callNumber);
                }
            }else{
                blockNo = backKey(blockNo);
                setDialValue(blockNo);
            }
        }else{
            callNumber = backKey(callNumber);
            setDialValue(callNumber);
        }
    }

    private void checkPassword(){
        setCurrentStatus(PASSWORD_CHECKING_MODE);
        String thisPassword=guestPassword;
        guestPassword="";
        takePicture(thisPassword,false,this);
    }

    private void onLockOpened(){
        setDialValue("");
        setTempkeyValue("");
        if(currentStatus!=PASSWORD_MODE&&currentStatus!=PASSWORD_CHECKING_MODE){
            setCurrentStatus(CALL_MODE);
        }
        Utils.DisplayToast(DialActivity.this, "门锁已经打开");
    }

    private void onPasswordCheck(int code){
        setCurrentStatus(PASSWORD_MODE);
        setTempkeyValue("");
        if(code==0){
            Utils.DisplayToast(DialActivity.this, "您输入的密码验证成功");
        }else{
            if(code==1){
                Utils.DisplayToast(DialActivity.this, "您输入的密码不存在");
            }else if(code==2){
                Utils.DisplayToast(DialActivity.this, "您输入的密码已经过期");
            }else if(code<0){
                Utils.DisplayToast(DialActivity.this, "密码验证不成功，请联系管理员");
            }
        }
    }

    private void passwordInput(int key){
        guestPassword=guestPassword+key;
        setTempkeyValue(guestPassword);
        if(guestPassword.length()==6){
            checkPassword();
        }
    }

    private void passwordInput(){
        guestPassword=backKey(guestPassword);
        setTempkeyValue(guestPassword);
    }
/*
    private void onKeyDown(int keyCode){
        if ((keyCode == KeyEvent.KEYCODE_0)) {
            keyInput(0);
        }else if ((keyCode == KeyEvent.KEYCODE_1)) {
            keyInput(1);
        }else if ((keyCode == KeyEvent.KEYCODE_2)) {
            keyInput(2);
        }else if ((keyCode == KeyEvent.KEYCODE_3)) {
            keyInput(3);
        }else if ((keyCode == KeyEvent.KEYCODE_4)) {
            keyInput(4);
        }else if ((keyCode == KeyEvent.KEYCODE_5)) {
            keyInput(5);
        }else if ((keyCode == KeyEvent.KEYCODE_6)) {
            keyInput(6);
        }else if ((keyCode == KeyEvent.KEYCODE_7)) {
            keyInput(7);
        }else if ((keyCode == KeyEvent.KEYCODE_8)) {
            keyInput(8);
        }else if ((keyCode == KeyEvent.KEYCODE_9)) {
            keyInput(9);
        }else if ((keyCode == KeyEvent.KEYCODE_POUND)) {
            if(currentStatus==CALLING_MODE){
                Utils.DisplayToast(DialActivity.this, "您已经取消拨号");
                resetDial();
                startCancelCall();
            }else if(currentStatus==ONVIDEO_MODE){
                startDisconnectVideo();
            }else if(currentStatus==DIRECT_CALLING_MODE){
                resetDial();
                startCancelDirectCall();
            }else if(currentStatus==DIRECT_MODE){
                resetDial();
                startDisconnectDirectCall();
            }else if(currentStatus!=ERROR_MODE){
                initDialStatus();
            }
        }else if ((keyCode == KeyEvent.KEYCODE_STAR)) {
            if(currentStatus!=CALLING_MODE&&currentStatus!=ONVIDEO_MODE&&currentStatus!=ERROR_MODE){
                initPasswordStatus();
            }
        }
    }
*/
    private int convertKeyCode(int keyCode){
        int value=-1;
        if ((keyCode == KeyEvent.KEYCODE_0)) {
            value=0;
        }else if ((keyCode == KeyEvent.KEYCODE_1)) {
            value=1;
        }else if ((keyCode == KeyEvent.KEYCODE_2)) {
            value=2;
        }else if ((keyCode == KeyEvent.KEYCODE_3)) {
            value=3;
        }else if ((keyCode == KeyEvent.KEYCODE_4)) {
            value=4;
        }else if ((keyCode == KeyEvent.KEYCODE_5)) {
            value=5;
        }else if ((keyCode == KeyEvent.KEYCODE_6)) {
            value=6;
        }else if ((keyCode == KeyEvent.KEYCODE_7)) {
            value=7;
        }else if ((keyCode == KeyEvent.KEYCODE_8)) {
            value=8;
        }else if ((keyCode == KeyEvent.KEYCODE_9)) {
            value=9;
        }
        return value;
    }

    private void onKeyDown(int keyCode){
        if(currentStatus==CALL_MODE || currentStatus==PASSWORD_MODE){
            int key=convertKeyCode(keyCode);
            if(key>=0){
                if(currentStatus==CALL_MODE){
                    callInput(key);
                }else{
                    passwordInput(key);
                }
            }else if(keyCode== KeyEvent.KEYCODE_POUND||keyCode==DeviceConfig.DEVICE_KEYCODE_POUND){
                if(currentStatus==CALL_MODE){
                    initPasswordStatus();
                }else{
                    initDialStatus();
                }
            }else if(keyCode== KeyEvent.KEYCODE_STAR||keyCode==DeviceConfig.DEVICE_KEYCODE_STAR){
                if(currentStatus==CALL_MODE){
                    callInput();
                }else{
                    passwordInput();
                }
            }
        }else if(currentStatus==ERROR_MODE){
            Utils.DisplayToast(DialActivity.this, "当前网络异常");
        }else if(currentStatus==CALLING_MODE){
            if(keyCode== KeyEvent.KEYCODE_POUND||keyCode==DeviceConfig.DEVICE_KEYCODE_POUND){
                startCancelCall();
            }
        }else if(currentStatus==ONVIDEO_MODE){
            if(keyCode== KeyEvent.KEYCODE_POUND||keyCode==DeviceConfig.DEVICE_KEYCODE_POUND) {
                startDisconnectVideo();
            }
        }else if(currentStatus==DIRECT_CALLING_MODE){
            if(keyCode== KeyEvent.KEYCODE_POUND||keyCode==DeviceConfig.DEVICE_KEYCODE_POUND) {
                resetDial();
                startCancelDirectCall();
            }
        }else if(currentStatus==DIRECT_CALLING_TRY_MODE){
            if(keyCode== KeyEvent.KEYCODE_POUND||keyCode==DeviceConfig.DEVICE_KEYCODE_POUND) {
                resetDial();
                startCancelDirectCall();
            }
        }else if(currentStatus==DIRECT_MODE){
            if(keyCode== KeyEvent.KEYCODE_POUND||keyCode==DeviceConfig.DEVICE_KEYCODE_POUND) {
                resetDial();
                startDisconnectDirectCall();
            }
        }
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getAction() == KeyEvent.ACTION_DOWN){
            int keyCode=event.getKeyCode();
            onKeyDown(keyCode);
            keyVoice();
        }
        return false;
    }

    private void initVoiceHandler(){
        soundPool= new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);//第一个参数为同时播放数据流的最大个数，第二数据流类型，第三为声音质量
        keyVoiceIndex=soundPool.load(this, R.raw.key, 1); //把你的声音素材放到res/raw里，第2个参数即为资源文件，第3个为音乐的优先级
    }

    private void keyVoice(){
        if(DeviceConfig.IS_KEY_VOICE_AVAILABLE){
            soundPool.play(keyVoiceIndex, 1, 1, 0, 0, 1);
        }
    }

   /* protected void startFaceLogin(){
        //SuperID.faceLogin(this);
        SuperID.faceVerify(this,1);
    }*/

   /* public void onFaceLogin(View viw) {
        startFaceLogin();
    }*/

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case SDKConfig.VERIFY_SUCCESS:
                String openid = Cache.getCached(this,SDKConfig.KEY_OPENID);
                if(openid!=null){
                    Utils.DisplayToast(DialActivity.this, openid);
                }
                break;
            case SDKConfig.VERIFY_FAIL:
                Utils.DisplayToast(DialActivity.this, "您不是注册用户");
                break;
            default:
                break;
        }
    }*/

    protected void resetDial(){
        callNumber="";
        setDialValue(callNumber);
        setCurrentStatus(CALL_MODE);
    }

    protected void startCancelCall(){
        new Thread(){
            public void run(){
                stopCallCamera();
                try{
                    sleep(1000);
                }catch(Exception e){}
                doCancelCall();
                try{
                    sleep(1000);
                }catch(Exception e){}
                toast("您已经取消拨号");
                resetDial();
            }
        }.start();
    }

    protected void stopCallCamera(){
        setDialValue("正在取消拨号");
        setCurrentStatus(CALL_CANCEL_MODE);
        clearImageUuidAvaible(lastImageUuid);
        Log.v("MainService", "取消拍照"+lastImageUuid);
    }
    protected void doCancelCall(){
        Message message = Message.obtain();
        message.what = MainService.MSG_CANCEL_CALL;
        try {
            serviceMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void initDialStatus(){
        //callLayout.setVisibility(View.VISIBLE);
        //guestLayout.setVisibility(View.INVISIBLE);
        videoLayout.setVisibility(View.INVISIBLE);
        setCurrentStatus(CALL_MODE);
        callNumber="";
        blockNo="";
        blockId=0;
        if(DeviceConfig.DEVICE_TYPE=="C"){
            setDialStatus("请输入楼栋编号");
        }else{
            setDialStatus("请输入房屋编号");
        }
        setDialValue(callNumber);
    }

    private void initPasswordStatus(){
        stopPasswordTimeoutChecking();
        setDialStatus("请输入访客密码");
        //callLayout.setVisibility(View.INVISIBLE);
        //guestLayout.setVisibility(View.VISIBLE);
        videoLayout.setVisibility(View.INVISIBLE);
        setCurrentStatus(PASSWORD_MODE);
        guestPassword="";
        setTempkeyValue(guestPassword);
        startTimeoutChecking();
    }

    private void startClockRefresh(){
        clockRefreshThread=new Thread(){
            public void run(){
                try {
                    setNewTime();
                    while(true) {
                        sleep(1000 * 60); //等待指定的一个等待时间
                        if (!isInterrupted()) { //检查线程没有被停止
                            setNewTime();
                        }
                    }
                }catch(InterruptedException e){
                }
                clockRefreshThread=null;
            }
        };
        clockRefreshThread.start();
    }
    private void setNewTime(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                Date now=new Date();
                SimpleDateFormat dateFormat=new SimpleDateFormat("E");
                String dayStr=dateFormat.format(now);
                dateFormat=new SimpleDateFormat("yyyy-MM-dd");
                String dateStr=dateFormat.format(now);
                dateFormat=new SimpleDateFormat("HH:mm");
                String timeStr=dateFormat.format(now);;

                setTextView(R.id.tv_day,dayStr);
                setTextView(R.id.tv_date,dateStr);
                setTextView(R.id.tv_time,timeStr);
            }
        });
    }

    private void startTimeoutChecking(){
        passwordTimeoutThread=new Thread(){
            public void run(){
                try {
                    sleep(DeviceConfig.PASSWORD_WAIT_TIME); //等待指定的一个等待时间
                    if(!isInterrupted()){ //检查线程没有被停止
                        if(currentStatus==PASSWORD_MODE){ //如果现在是密码输入状态
                            if(guestPassword.equals("")){ //如果密码一直是空白的
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        initDialStatus();
                                    }
                                });
                                stopPasswordTimeoutChecking();
                            }
                        }
                    }
                }catch(InterruptedException e){
                }
                passwordTimeoutThread=null;
            }
        };
        passwordTimeoutThread.start();
    }

    protected void stopPasswordTimeoutChecking(){
        if(passwordTimeoutThread!=null){
            passwordTimeoutThread.interrupt();
            passwordTimeoutThread=null;
        }
    }
    synchronized void setCurrentStatus(int status) {
        currentStatus=status;
    }
    void toast(final String message){
        handler.post(new Runnable() {
            @Override
            public void run() {
                Utils.DisplayToast(DialActivity.this,message );
            }
        });
    }
    void setDialStatus(String value) {
        final String thisValue=value;
        handler.post(new Runnable() {
            @Override
            public void run() {
                setTextView(R.id.tv_input_label,thisValue);
            }
        });
    }
    void setDialValue(String value) {
        final String thisValue=value;
        handler.post(new Runnable() {
            @Override
            public void run() {
                setTextView(R.id.tv_input_text,thisValue);
            }
        });
    }
    void setTempkeyValue(String value) {
        final String thisValue=value;
        handler.post(new Runnable() {
            @Override
            public void run() {
                setTextView(R.id.tv_input_text,thisValue);
            }
        });
    }

    void setCommunityName(String value) {
        final String thisValue=value;
        handler.post(new Runnable() {
            @Override
            public void run() {
                setTextView(R.id.tv_community,thisValue);
            }
        });
    }

    void setLockName(String value) {
        final String thisValue=value;
        handler.post(new Runnable() {
            @Override
            public void run() {
                setTextView(R.id.tv_lock,thisValue);
            }
        });
    }

    void setTextView(int id,String txt) { ((TextView)findViewById(id)).setText(txt); }

    void initVideoViews() {
        if (localView !=null) return;
        if(MainService.callConnection != null)
            localView = (SurfaceView) MainService.callConnection.createVideoView(true, this, true);
        localView.setVisibility(View.INVISIBLE);
        videoLayout.addView(localView);
        localView.setKeepScreenOn(true);
        localView.setZOrderMediaOverlay(true);
        localView.setZOrderOnTop(true);

        if(MainService.callConnection != null)
            remoteView = (SurfaceView) MainService.callConnection.createVideoView(false, this, true);
        remoteView.setVisibility(View.INVISIBLE);
        remoteView.setKeepScreenOn(true);
        remoteView.setZOrderMediaOverlay(true);
        remoteView.setZOrderOnTop(true);
        //remoteLayout.addView(remoteView);
    }

    /**
     * Sets the video surface visibility.
     *
     * @param visible the new video surface visibility
     */
    void setVideoSurfaceVisibility(int visible) {
        if(localView !=null)
            localView.setVisibility(visible);
        if(remoteView !=null)
            remoteView.setVisibility(visible);
    }
    protected void onCallMemberError(int reason){
        setDialValue("");
        setCurrentStatus(CALL_MODE);
        if(reason==MSG_CALLMEMBER_ERROR){
            Utils.DisplayToast(DialActivity.this, "您呼叫的房间号错误或者无注册用户");
            Log.v("MainService", "无用户取消呼叫");
            clearImageUuidAvaible(lastImageUuid);
        }else if(reason==MSG_CALLMEMBER_NO_ONLINE){
            Utils.DisplayToast(DialActivity.this, "您呼叫的房间号无人在线");
        }else if(reason==MSG_CALLMEMBER_TIMEOUT){
            Utils.DisplayToast(DialActivity.this, "您呼叫的房间号无人应答");
        }else if(reason==MSG_CALLMEMBER_DIRECT_TIMEOUT){
            Utils.DisplayToast(DialActivity.this, "您呼叫的房间直拨电话无人应答");
        }else if(reason==MSG_CALLMEMBER_SERVER_ERROR){
            Utils.DisplayToast(DialActivity.this, "无法从服务器获取住户信息，请联系管理处");
        }
    }

    public void onCallDirectlyBegin(){
        setCurrentStatus(DIRECT_MODE);
        advertiseHandler.pause();
    }

    public void onCallDirectlyComplete(){
        setCurrentStatus(CALL_MODE);
        callNumber="";
        setDialValue(callNumber);
        advertiseHandler.start();
    }

    public void onRtcConnected(){
        setCurrentStatus(ONVIDEO_MODE);
        setDialValue("");
        advertiseHandler.pause();
    }

    public void onRtcVideoOn(){
        initVideoViews();
        MainService.callConnection.buildVideo(remoteView);
        //callLayout.setVisibility(View.INVISIBLE);
        //guestLayout.setVisibility(View.INVISIBLE);
        videoLayout.setVisibility(View.VISIBLE);
        setVideoSurfaceVisibility(View.VISIBLE);
    }

    public void onRtcDisconnect(){
        setCurrentStatus(CALL_MODE);
        advertiseHandler.start();
        //callLayout.setVisibility(View.VISIBLE);
        //guestLayout.setVisibility(View.INVISIBLE);
        videoLayout.setVisibility(View.INVISIBLE);
        setVideoSurfaceVisibility(View.INVISIBLE);
    }

    protected void onAdvertiseRefresh(Object obj){
        JSONArray rows=(JSONArray)obj;
        advertiseHandler.initData(rows,dialMessenger,(currentStatus==ONVIDEO_MODE));
    }

    protected void onAdvertiseImageChange(Object obj){
        String source=(String)obj;
        source= HttpUtils.getLocalFileFromUrl(source);
        Bitmap bm = BitmapFactory.decodeFile(source);
        imageView.setImageBitmap(bm);
    }

   /* @Override
    public void onFinger(byte[] fingerData) {
        Message message = Message.obtain();
        message.what = MainService.MSG_FINGER_DETECT;
        message.obj = fingerData;
        try {
            serviceMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }*/

   /* class Receive extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String actionName=intent.getAction();
            if(NfcReader.ACTION_NFC_CARDINFO.equals(actionName)){
                String cardInfo=intent.getStringExtra("cardinfo");
                System.out.print(cardInfo);
            }
        }
    }*/
}

interface TakePictureCallback{
    public void beforeTakePickture(final String thisValue, final boolean isCall, String uuid);
    public void afterTakePickture(final String thisValue, String fileUrl, final boolean isCall, String uuid);
}
//class CameraHandler{
//    public int status=0; //0：正常状态 1:工作状态
//
//    public boolean isWorking(){
//        return status==1;
//    }
//
//    public void startWork(){
//        status=1;
//    }
//
//    public void endWork(){
//        status=0;
//    }
//}