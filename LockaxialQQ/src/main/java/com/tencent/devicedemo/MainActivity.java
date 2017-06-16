package com.tencent.devicedemo;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidex.DoorLock;
import com.androidex.GetUserInfo;
import com.androidex.NetWork;
import com.androidex.SoundPoolUtil;
import com.androidex.Zxing;
import com.androidex.aexlibs.hwService;
import com.brocast.NotifyReceiver;
import com.entity.Banner;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.phone.config.DeviceConfig;
import com.phone.service.MainService;
import com.phone.utils.AdvertiseHandler;
import com.phone.utils.Ajax;
import com.phone.utils.HttpUtils;
import com.phone.utils.NfcReader;
import com.phone.utils.UploadUtil;
import com.tencent.device.TXBinderInfo;
import com.tencent.device.TXDeviceService;
import com.tencent.devicedemo.interfac.NetworkCallBack;
import com.tencent.devicedemo.interfac.TakePictureCallback;
import com.util.Intenet;
import com.viewpager.AutoScrollViewPager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import jni.util.Utils;

import static com.androidex.NetWork.NETWOKR_TYPE_ETHERNET;
import static com.androidex.NetWork.NETWORK_TYPE_WIFI;
import static com.androidex.NetWork.isNetworkAvailable;
import static com.phone.service.MainService.communityId;
import static com.phone.service.MainService.httpServerToken;
import static com.util.Constant.CALLING_MODE;
import static com.util.Constant.CALL_CANCEL_MODE;
import static com.util.Constant.CALL_MODE;
import static com.util.Constant.DIRECT_CALLING_MODE;
import static com.util.Constant.DIRECT_CALLING_TRY_MODE;
import static com.util.Constant.DIRECT_MODE;
import static com.util.Constant.ERROR_MODE;
import static com.util.Constant.MSG_ADVERTISE_IMAGE;
import static com.util.Constant.MSG_ADVERTISE_REFRESH;
import static com.util.Constant.MSG_CALLMEMBER_DIRECT_COMPLETE;
import static com.util.Constant.MSG_CALLMEMBER_DIRECT_DIALING;
import static com.util.Constant.MSG_CALLMEMBER_DIRECT_FAILED;
import static com.util.Constant.MSG_CALLMEMBER_DIRECT_SUCCESS;
import static com.util.Constant.MSG_CALLMEMBER_DIRECT_TIMEOUT;
import static com.util.Constant.MSG_CALLMEMBER_ERROR;
import static com.util.Constant.MSG_CALLMEMBER_NO_ONLINE;
import static com.util.Constant.MSG_CALLMEMBER_SERVER_ERROR;
import static com.util.Constant.MSG_CALLMEMBER_TIMEOUT;
import static com.util.Constant.MSG_CALLMEMBER_TIMEOUT_AND_TRY_DIRECT;
import static com.util.Constant.MSG_CANCEL_CALL_COMPLETE;
import static com.util.Constant.MSG_CHECK_BLOCKNO;
import static com.util.Constant.MSG_CONNECT_ERROR;
import static com.util.Constant.MSG_CONNECT_SUCCESS;
import static com.util.Constant.MSG_FINGER_CHECK;
import static com.util.Constant.MSG_INVALID_CARD;
import static com.util.Constant.MSG_LOCK_OPENED;
import static com.util.Constant.MSG_PASSWORD_CHECK;
import static com.util.Constant.MSG_REFRESH_COMMUNITYNAME;
import static com.util.Constant.MSG_REFRESH_DATA;
import static com.util.Constant.MSG_REFRESH_LOCKNAME;
import static com.util.Constant.MSG_RTC_DISCONNECT;
import static com.util.Constant.MSG_RTC_NEWCALL;
import static com.util.Constant.MSG_RTC_ONVIDEO;
import static com.util.Constant.ONVIDEO_MODE;
import static com.util.Constant.ON_YUNTONGXUN_INIT_ERROR;
import static com.util.Constant.ON_YUNTONGXUN_LOGIN_FAIL;
import static com.util.Constant.ON_YUNTONGXUN_LOGIN_SUCCESS;
import static com.util.Constant.PASSWORD_CHECKING_MODE;
import static com.util.Constant.PASSWORD_MODE;

public class MainActivity extends Activity implements NfcReader.AccountCallback, NfcAdapter.ReaderCallback, TakePictureCallback, NotifyReceiver.CallBack, View.OnClickListener {
    private static final String TAG = "MainActivity";

    private GridView mGridView;
    private BinderListAdapter mAdapter;
    private NotifyReceiver mNotifyReceiver;
    public boolean nfcFlag = false;
    public String isFrom = "";
    public RelativeLayout rl_nfc;
    public TextView tv_message;
    public String nfcMessage = "请将卡片放到感应区域，按确认键\n" +
            "确定录入卡片，按删除键取消录入卡片";
    // Recommend NfcAdapter flags for reading from other Android devices. Indicates that this
    // activity is interested in NFC-A devices (including other Android devices), and that the
    // system should not check for the presence of NDEF-formatted data (e.g. Android Beam).
    public static int READER_FLAGS = NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK;
    private NfcReader nfcReader;
    private EditText tv_input;
    private AutoScrollViewPager viewPager;
    private Banner banner;
    private Bg_Adapter bgAdapter;
    private ImageView imageView;
    private DisplayImageOptions options;
    private WifiInfo wifiInfo = null;        //获得的Wifi信息
    private WifiManager wifiManager = null;    //Wifi管理器
    private Handler handler;
    private ImageView wifi_image;            //信号图片显示
    private int level;                        //信号强度值
    private ImageView iv_setting;
    private RelativeLayout rl;
    private ImageView iv_bind;
    TextView headPaneTextView = null;
    LinearLayout videoLayout;
    protected Messenger serviceMessenger;
    protected Messenger dialMessenger;
    AdvertiseHandler advertiseHandler = null;
    SurfaceView localView = null;
    SurfaceView remoteView = null;
    private HashMap<String, String> uuidMaps = new HashMap<String, String>();
    private String lastImageUuid = "";
    private String callNumber = "";
    private String blockNo = "";
    private int blockId = 0;
    private int checkingStatus = 0;
    SurfaceHolder autoCameraHolder = null;
    private String guestPassword = "";
    Thread passwordTimeoutThread = null;
    SoundPool soundPool = null;
    int keyVoiceIndex = 0;
    SurfaceView videoView = null;
    SurfaceView autoCameraSurfaceView = null;
    Thread clockRefreshThread = null;
    public static int currentStatus = CALL_MODE;
    public EditText et_blackno;
    public EditText et_unitno;
    public boolean isFlag = true;
    private hwService hwservice;
    Parcelable[] listTemp1;

    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated constructor stub
        super.onCreate(savedInstanceState);
        //全屏设置，隐藏窗口所有装饰
        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);//清除FLAG
        getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN, android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //标题是属于View的，所以窗口所有的修饰部分被隐藏后标题依然有效
        //requestWindowFeature(getWindow().FEATURE_NO_TITLE);
        sendBroadcast(new Intent("com.android.action.hide_navigationbar"));
        {
            ActionBar ab = getActionBar();
            if (ab != null)
                ab.setDisplayHomeAsUpEnabled(true);
        }
        setContentView(R.layout.activity_main);
        initView();//初始化View
        initScreen();
        initHandler();
        initServer();//初始化服务类
        initVoiceHandler();
        initVoiceVolume();
        initAdvertiseHandler();
        initAutoCamera();
        if (DeviceConfig.DEVICE_TYPE.equals("C")) {
            setDialStatus("请输入楼栋编号");
        }
        startClockRefresh();
        boolean initStatus = this.getIntent().getBooleanExtra("INIT_STATUS", true);
        if (!initStatus) {
            onConnectionError();
        }
        initReceiver();//初始化广播
        rl = (RelativeLayout) findViewById(R.id.net_view_rl);
        rl.setOnClickListener(this);
        wifi_image = (ImageView) findViewById(R.id.wifi_image); //wifi图标控件初始化
        wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);//获得WifiManager
        getRssi();//使用定时器,每隔5秒获得一次信号强度值
        setNetWork();
        setAutioVolume();//获取系统相关音量

        if (false) {
            AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            Intent intent;
            PendingIntent pendingIntent;

            intent = new Intent(getApplicationContext(), AlarmReciver.class);
            pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 10000, pendingIntent);
        }
    }

    /**
     * 初始化view
     */
    public void initView() {
        rl_nfc = (RelativeLayout) findViewById(R.id.rl_nfc);
        et_blackno = (EditText) findViewById(R.id.et_blockno);
        et_unitno = (EditText) findViewById(R.id.et_unitno);
        iv_bind = (ImageView) findViewById(R.id.user_bind);
        tv_message = (TextView) findViewById(R.id.tv_message);
        viewPager = (AutoScrollViewPager) findViewById(R.id.vp_main);
        imageView = (ImageView) findViewById(R.id.iv_erweima);
        //getBgBanners();//网络获得轮播背景图片数据

        mGridView = (GridView) findViewById(R.id.gridView_binderlist);
        mAdapter = new BinderListAdapter(this);
        mGridView.setAdapter(mAdapter);

        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/GBK.TTF");
        //TextView com_log=(TextView)findViewById(R.id.tv_log);
        tv_input = (EditText) findViewById(R.id.tv_input);
        tv_input.setTypeface(typeFace);// com_log.setTypeface(typeFace);
        isFrom = getIntent().getStringExtra(StartActivity.FLAG);
        if ("from".equals(isFrom)) {//从登录界面跳转过来的
            rl_nfc.setVisibility(View.VISIBLE);
            tv_message.setText(nfcMessage);
            nfcFlag = true;
        }
        et_blackno.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    isFlag = true;
                } else {
                    isFlag = false;
                }
            }
        });
        getQR();//生成二维码
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {//跳转网络或网络设置
            case R.id.net_view_rl:
                Intenet.system_set(MainActivity.this);
                break;
        }
    }

    private void setNetWork() {
        boolean bNetworkSetted = this.getSharedPreferences("TXDeviceSDK", 0).getBoolean("NetworkSetted", false);
        SharedPreferences sharedPreferences = getSharedPreferences("test", Activity.MODE_PRIVATE);
        String networkSettingMode = sharedPreferences.getString("NetworkSettingMode", "");
        if ("".equals(networkSettingMode) && bNetworkSetted == false) {
            SharedPreferences mySharedPreferences = getSharedPreferences("test", Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = mySharedPreferences.edit();
            editor.putString("NetworkSettingMode", "true");
            editor.commit();
        } else if ("true".equals(networkSettingMode)) {

        }
    }


    /**
     * 注册QQ物联回调
     */
    private void initReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(TXDeviceService.OnEraseAllBinders);
        filter.addAction(TXDeviceService.wifisetting);
        filter.addAction(DoorLock.DoorLockStatusChange);
        filter.addAction(DoorLock.DoorLockOpenDoor);
        filter.addAction(TXDeviceService.voicereceive);
        filter.addAction(TXDeviceService.isconnected);
        filter.addAction(TXDeviceService.BinderListChange);
        filter.addAction(TXDeviceService.OnEraseAllBinders);
        filter.addAction(DoorLock.DoorLockStatusChange);
        mNotifyReceiver = new NotifyReceiver(this, mAdapter, iv_bind, dialog);
        registerReceiver(mNotifyReceiver, filter);
        mNotifyReceiver.setmCallBack(this);
    }

    /**
     * 使用定时器,每隔5秒获得一次信号强度值
     */
    private void getRssi() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                //获取网络连接状态
                if (!isNetworkAvailable(MainActivity.this)) {//无可用网络
                    Message message = new Message();
                    message.what = 6;
                    mHandler.sendMessage(message);
                } else {
                    Message message = new Message();
                    message.what = 7;
                    mHandler.sendMessage(message);
                }

                switch (NetWork.getCurrentNetType(MainActivity.this)){
                    case NETWORK_TYPE_WIFI:
                        wifiInfo = wifiManager.getConnectionInfo();
                        //获得信号强度值
                        level = wifiInfo.getRssi();
                        //根据获得的信号强度发送信息
                        if (level <= 0 && level >= -50) {
                            Message msg = new Message();
                            msg.what = 1;
                            mHandler.sendMessage(msg);
                        } else if (level < -50 && level >= -70) {
                            Message msg = new Message();
                            msg.what = 2;
                            mHandler.sendMessage(msg);
                        } else if (level < -70 && level >= -80) {
                            Message msg = new Message();
                            msg.what = 3;
                            mHandler.sendMessage(msg);
                        } else if (level < -80 && level >= -100) {
                            Message msg = new Message();
                            msg.what = 4;
                            mHandler.sendMessage(msg);
                        } else {
                            Message msg = new Message();
                            msg.what = 5;
                            mHandler.sendMessage(msg);
                        }
                        break;
                    case NETWOKR_TYPE_ETHERNET:
                        Message msg = new Message();
                        msg.what = 11;
                        mHandler.sendMessage(msg);
                        break;
                }

            }

        }, 1000, 5000);
    }

    /**
     * 网络获得轮播背景图片数据
     */
    private void getBgBanners() {
        XUtilsNetwork.getInstance().getBgBanners(new NetworkCallBack() {//网络请求
            // *网络获得轮播背景图片数据
            @Override
            public void onSuccess(Object o) {
                Gson gson = new Gson();
                banner = gson.fromJson(o.toString(), Banner.class);
                bgAdapter = new Bg_Adapter(MainActivity.this, banner.getData());
                viewPager.setAdapter(bgAdapter);
                viewPager.setCycle(true);
                //viewPager.setSwipeScrollDurationFactor(2000);//设置ViewPager滑动动画间隔时间的倍率，达到减慢动画或改变动画速度的效果
                viewPager.setInterval(10000);//设置自动滚动的间隔时间，单位为毫秒
                viewPager.startAutoScroll();
            }

            @Override
            public void onFailure(String error) {

            }
        });
    }

    /**
     * 获取系统相关音量
     */
    private void setAutioVolume() {
        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //通话音量
        int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
        int current = mAudioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        Log.d("VIOCE_CALL", "max : " + max + " current : " + current);
        //系统音量
        max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
        current = mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
        Log.d("SYSTEM", "max : " + max + " current : " + current);
        //铃声音量
        max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
        current = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
        Log.d("RING", "max : " + max + " current : " + current);
        //音乐音量
        max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        current = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        Log.d("MUSIC", "max : " + max + " current : " + current);
        //提示声音音量
        max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        current = mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        Log.d("ALARM", "max : " + max + " current : " + current);
    }

    /**
     * 生成一个可以绑定设备的二维码，qq物联绑定的二维码
     */
    private void getQR() {
        //生成一个可以绑定设备的二维码
        Log.d("mainactivity", GetUserInfo.getSn());
        Bitmap bitmap = Zxing.createQRImage("http://iot.qq.com/add?pid=1700003316&sn=" + GetUserInfo.getSn(), 200, 200, null);
        if (bitmap == null) {

            options = new DisplayImageOptions.Builder()
                    .showImageOnFail(R.mipmap.fail)
                    .showImageOnLoading(R.mipmap.loading)
                    .cacheOnDisk(true)
                    .bitmapConfig(Bitmap.Config.ARGB_8888)
                    .build();

            BaseApplication.getApplication().getImageLoader().displayImage("http://www.tyjdtzjc.cn/resource/kindeditor/attached/image/20150831/20150831021658_90595.png", imageView, options);
        } else {
            imageView.setImageBitmap(bitmap);
        }
    }

    /**
     * 初始化系统服务类
     */
    protected void initServer() {
        hwservice = new hwService(MainActivity.this);

        Intent i = new Intent(MainActivity.this, MainService.class);
        bindService(i, connection, 0);

        Intent startIntent = new Intent(this, TXDeviceService.class);
        startService(startIntent);

         /*Intent i = new Intent(this, SpeechService.class);
        startService(i);*/

        Intent dlIntent = new Intent(this, DoorLock.class);
        startService(dlIntent);
    }

    protected void initScreen() {
        //callLayout=(LinearLayout) findViewById(R.id.call_pane);
        //guestLayout=(LinearLayout) findViewById(R.id.guest_pane);
        headPaneTextView = (TextView) findViewById(R.id.header_pane);
        videoLayout = (LinearLayout) findViewById(R.id.ll_video);

//        videoPane = (LinearLayout) findViewById(R.id.video_pane);
//        imagePane = (LinearLayout) findViewById(R.id.image_pane);
//        remoteLayout = (LinearLayout) findViewById(R.id.ll_remote);

        setTextView(R.id.tv_community, MainService.communityName);
        setTextView(R.id.tv_lock, MainService.lockName);
    }

    private void startClockRefresh() {
        clockRefreshThread = new Thread() {
            public void run() {
                try {
                    setNewTime();
                    while (true) {
                        sleep(1000 * 60); //等待指定的一个等待时间
                        if (!isInterrupted()) { //检查线程没有被停止
                            setNewTime();
                        }
                    }
                } catch (InterruptedException e) {
                }
                clockRefreshThread = null;
            }
        };
        clockRefreshThread.start();
    }

    private void setNewTime() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Date now = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("E");
                String dayStr = dateFormat.format(now);
                dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String dateStr = dateFormat.format(now);
                dateFormat = new SimpleDateFormat("HH:mm");
                String timeStr = dateFormat.format(now);

                setTextView(R.id.tv_day, dayStr);
                setTextView(R.id.tv_date, dateStr);
                setTextView(R.id.tv_time, timeStr);
            }
        });
    }

    protected void initAutoCamera() {
        autoCameraSurfaceView = (SurfaceView) findViewById(R.id.autoCameraSurfaceview);
        autoCameraHolder = autoCameraSurfaceView.getHolder();
        autoCameraHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    protected void initAdvertiseHandler() {
        advertiseHandler = new AdvertiseHandler();
        videoView = (SurfaceView) findViewById(R.id.surface_view);

        imageView = (ImageView) findViewById(R.id.image_view);
        //advertiseHandler.init(videoView,imageView,videoPane,imagePane);
        advertiseHandler.init(videoView, imageView);
    }

    private void initVoiceHandler() {
        soundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);//第一个参数为同时播放数据流的最大个数，第二数据流类型，第三为声音质量
        keyVoiceIndex = soundPool.load(this, R.raw.key, 1); //把你的声音素材放到res/raw里，第2个参数即为资源文件，第3个为音乐的优先级
    }

    protected void initVoiceVolume() {
        AudioManager audioManager = (AudioManager) getSystemService(this.AUDIO_SERVICE);
        initVoiceVolume(audioManager, AudioManager.STREAM_MUSIC, DeviceConfig.VOLUME_STREAM_MUSIC);
        initVoiceVolume(audioManager, AudioManager.STREAM_RING, DeviceConfig.VOLUME_STREAM_RING);
        initVoiceVolume(audioManager, AudioManager.STREAM_SYSTEM, DeviceConfig.VOLUME_STREAM_SYSTEM);
        initVoiceVolume(audioManager, AudioManager.STREAM_VOICE_CALL, DeviceConfig.VOLUME_STREAM_VOICE_CALL);
    }

    protected void initVoiceVolume(AudioManager audioManager, int type, int value) {
        int thisValue = audioManager.getStreamMaxVolume(type);
        thisValue = thisValue * value / 10;
        audioManager.setStreamVolume(type, thisValue, AudioManager.FLAG_PLAY_SOUND);
    }

    private void initHandler() {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MSG_RTC_NEWCALL) {
                    onRtcConnected();
                } else if (msg.what == MSG_RTC_ONVIDEO) {
                    onRtcVideoOn();
                } else if (msg.what == MSG_RTC_DISCONNECT) {
                    onRtcDisconnect();
                } else if (msg.what == MSG_PASSWORD_CHECK) {
                    onPasswordCheck((Integer) msg.obj);
                } else if (msg.what == MSG_LOCK_OPENED) {
                    onLockOpened();
                } else if (msg.what == MSG_CALLMEMBER_ERROR) {
                    onCallMemberError(msg.what);
                } else if (msg.what == MSG_CALLMEMBER_SERVER_ERROR) {
                    onCallMemberError(msg.what);
                } else if (msg.what == MSG_CALLMEMBER_NO_ONLINE) {
                    onCallMemberError(msg.what);
                } else if (msg.what == MSG_CALLMEMBER_TIMEOUT) {
                    onCallMemberError(msg.what);
                } else if (msg.what == MSG_CALLMEMBER_TIMEOUT_AND_TRY_DIRECT) {
                    Utils.DisplayToast(MainActivity.this, "可视对讲无法拨通，尝试直拨电话");
                    setCurrentStatus(DIRECT_CALLING_TRY_MODE);
                } else if (msg.what == MSG_CALLMEMBER_DIRECT_TIMEOUT) {
                    onCallMemberError(msg.what);
                } else if (msg.what == MSG_CALLMEMBER_DIRECT_DIALING) {
                    Utils.DisplayToast(MainActivity.this, "开始直拨电话");
                    setCurrentStatus(DIRECT_CALLING_MODE);
                } else if (msg.what == MSG_CALLMEMBER_DIRECT_SUCCESS) {
                    Utils.DisplayToast(MainActivity.this, "电话已接通，请让对方按#号键开门");
                    onCallDirectlyBegin();
                } else if (msg.what == MSG_CALLMEMBER_DIRECT_FAILED) {
                    Utils.DisplayToast(MainActivity.this, "电话未能接通，重试中..");
                } else if (msg.what == MSG_CALLMEMBER_DIRECT_COMPLETE) {
                    onCallDirectlyComplete();
                } else if (msg.what == MSG_CONNECT_ERROR) {
                    onConnectionError();
                } else if (msg.what == MSG_CONNECT_SUCCESS) {
                    onConnectionSuccess();
                } else if (msg.what == ON_YUNTONGXUN_INIT_ERROR) {
                    Utils.DisplayToast(MainActivity.this, "直拨电话初始化异常");
                } else if (msg.what == ON_YUNTONGXUN_LOGIN_SUCCESS) {
                    Utils.DisplayToast(MainActivity.this, "直拨电话服务器连接成功");
                } else if (msg.what == ON_YUNTONGXUN_LOGIN_FAIL) {
                    Utils.DisplayToast(MainActivity.this, "直拨电话服务器连接失败");
                } else if (msg.what == MSG_CANCEL_CALL_COMPLETE) {
                    setCurrentStatus(CALL_MODE);
                } else if (msg.what == MSG_ADVERTISE_REFRESH) {
                    onAdvertiseRefresh(msg.obj);
                } else if (msg.what == MSG_ADVERTISE_IMAGE) {
                    onAdvertiseImageChange(msg.obj);
                } else if (msg.what == MSG_INVALID_CARD) {
                    Utils.DisplayToast(MainActivity.this, "无效房卡");
                } else if (msg.what == MainService.MSG_ASSEMBLE_KEY) {
                    int keyCode = (Integer) msg.obj;
                    onKeyDown(keyCode);
                } else if (msg.what == MSG_CHECK_BLOCKNO) {
                    blockId = (Integer) msg.obj;
                    onCheckBlockNo();
                } else if (msg.what == MSG_FINGER_CHECK) {
                    boolean result = (Boolean) msg.obj;
                    //onFingerCheck(result);
                } else if (msg.what == MSG_REFRESH_DATA) {
                    onFreshData((String) msg.obj);
                } else if (msg.what == MSG_REFRESH_COMMUNITYNAME) {
                    onFreshCommunityName((String) msg.obj);
                } else if (msg.what == MSG_REFRESH_LOCKNAME) {
                    onFreshLockName((String) msg.obj);
                } else if (msg.what == 0x11) {//录入成功
                    rl_nfc.setVisibility(View.GONE);
                    nfcFlag = false;
                    Toast.makeText(MainActivity.this, "录入成功", Toast.LENGTH_LONG).show();
                } else if (msg.what == 0x12) {//录入失败
                    Toast.makeText(MainActivity.this, "录入失败", Toast.LENGTH_LONG).show();
                }
            }
        };
        dialMessenger = new Messenger(handler);
    }

    private void onFreshLockName(String lockName) {
        if (lockName != null) {
            setLockName(lockName);
        }
    }

    private void onFreshCommunityName(String communityName) {
        if (communityName != null) {
            setCommunityName(communityName);
        }
    }

    private void onFreshData(String type) {
        if ("card".equals(type)) {
            Utils.DisplayToast(MainActivity.this, "更新卡数据");
        } else if ("finger".equals(type)) {
            Utils.DisplayToast(MainActivity.this, "更新指纹数据");
        }
    }

    private void onCheckBlockNo() {
        checkingStatus = 0;
        if (blockId == 0) {
            blockNo = "";
            callNumber = "";
            setDialValue(blockNo);
            Utils.DisplayToast(MainActivity.this, "楼栋编号不存在");
        }
        if (blockId < 0) {
            blockNo = "";
            callNumber = "";
            blockId = 0;
            setDialValue(blockNo);
            Utils.DisplayToast(MainActivity.this, "获取楼栋数据失败，请联系管理处");
        } else {
            callNumber = "";
            setDialValue(callNumber);
            setDialStatus("输入房间号呼叫");
        }
    }

    public void onRtcConnected() {
        setCurrentStatus(ONVIDEO_MODE);
        setDialValue("");
        advertiseHandler.pause();
    }

    public void onRtcVideoOn() {
        initVideoViews();
        MainService.callConnection.buildVideo(remoteView);
        //callLayout.setVisibility(View.INVISIBLE);
        //guestLayout.setVisibility(View.INVISIBLE);
        videoLayout.setVisibility(View.VISIBLE);
        setVideoSurfaceVisibility(View.VISIBLE);
    }

    public void onRtcDisconnect() {
        setCurrentStatus(CALL_MODE);
        advertiseHandler.start();
        //callLayout.setVisibility(View.VISIBLE);
        //guestLayout.setVisibility(View.INVISIBLE);
        videoLayout.setVisibility(View.INVISIBLE);
        setVideoSurfaceVisibility(View.INVISIBLE);
    }

    private void onPasswordCheck(int code) {
        setCurrentStatus(PASSWORD_MODE);
        setTempkeyValue("");
        if (code == 0) {
            Utils.DisplayToast(MainActivity.this, "您输入的密码验证成功");
        } else {
            if (code == 1) {
                Utils.DisplayToast(MainActivity.this, "您输入的密码不存在");
            } else if (code == 2) {
                Utils.DisplayToast(MainActivity.this, "您输入的密码已经过期");
            } else if (code < 0) {
                Utils.DisplayToast(MainActivity.this, "密码验证不成功，请联系管理员");
            }
        }
    }

    private void onLockOpened() {
        setDialValue("");
        setTempkeyValue("");
        if (currentStatus != PASSWORD_MODE && currentStatus != PASSWORD_CHECKING_MODE) {
            setCurrentStatus(CALL_MODE);
        }
        Utils.DisplayToast(MainActivity.this, "门锁已经打开");
    }

    protected void onCallMemberError(int reason) {
        setDialValue("");
        setCurrentStatus(CALL_MODE);
        if (reason == MSG_CALLMEMBER_ERROR) {
            Utils.DisplayToast(MainActivity.this, "您呼叫的房间号错误或者无注册用户");
            Log.v("MainService", "无用户取消呼叫");
            clearImageUuidAvaible(lastImageUuid);
        } else if (reason == MSG_CALLMEMBER_NO_ONLINE) {
            Utils.DisplayToast(MainActivity.this, "您呼叫的房间号无人在线");
        } else if (reason == MSG_CALLMEMBER_TIMEOUT) {
            Utils.DisplayToast(MainActivity.this, "您呼叫的房间号无人应答");
        } else if (reason == MSG_CALLMEMBER_DIRECT_TIMEOUT) {
            Utils.DisplayToast(MainActivity.this, "您呼叫的房间直拨电话无人应答");
        } else if (reason == MSG_CALLMEMBER_SERVER_ERROR) {
            Utils.DisplayToast(MainActivity.this, "无法从服务器获取住户信息，请联系管理处");
        }
    }

    public void onCallDirectlyBegin() {
        setCurrentStatus(DIRECT_MODE);
        advertiseHandler.pause();
    }

    public void onCallDirectlyComplete() {
        setCurrentStatus(CALL_MODE);
        callNumber = "";
        setDialValue(callNumber);
        advertiseHandler.start();
    }

    private void onConnectionError() {
        setCurrentStatus(ERROR_MODE);
        setTextView(R.id.header_pane, "可视对讲设备异常，网络连接已断开");
        headPaneTextView.setVisibility(View.VISIBLE);
    }

    private void onConnectionSuccess() {
        if (currentStatus == ERROR_MODE) {
            initDialStatus();
            setTextView(R.id.header_pane, "");
            headPaneTextView.setVisibility(View.INVISIBLE);
        }
    }

    protected void onAdvertiseRefresh(Object obj) {
        JSONArray rows = (JSONArray) obj;
        advertiseHandler.initData(rows, dialMessenger, (currentStatus == ONVIDEO_MODE));
    }

    protected void onAdvertiseImageChange(Object obj) {
        String source = (String) obj;
        source = HttpUtils.getLocalFileFromUrl(source);
        Bitmap bm = BitmapFactory.decodeFile(source);
        imageView.setImageBitmap(bm);
    }

    private void callInput(int key) {
        if (DeviceConfig.DEVICE_TYPE.equals("C")) {
            if (blockId == 0) {
                if (blockNo.length() < DeviceConfig.BLOCK_NO_LENGTH) {
                    blockNo = blockNo + key;
                    setDialValue(blockNo);
                }
                if (blockNo.length() == DeviceConfig.BLOCK_NO_LENGTH) {
                    checkingStatus = 1;
                    setDialValue( ""+blockNo);
                    Message message = Message.obtain();
                    message.what = MainService.MSG_CHECK_BLOCKNO;
                    message.obj = blockNo;
                    try {
                        serviceMessenger.send(message);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                if (checkingStatus == 0) {
                    unitNoInput(key);
                }
            }
        } else {
            unitNoInput(key);
        }
    }

    private void unitNoInput(int key) {
        callNumber = callNumber + key;
        setDialValue(callNumber);
        if (callNumber.length() == DeviceConfig.UNIT_NO_LENGTH) {
            startDialing();
        }
    }

    private void startDialing() {
        setCurrentStatus(CALLING_MODE);
        String thisNumber = callNumber;
        callNumber = "";
        if (DeviceConfig.DEVICE_TYPE == "C") {
            blockId = 0;
            blockNo = "";
            setDialStatus("请输入楼栋编号");
        }
        takePicture(thisNumber, true, MainActivity.this);
    }

    private int convertKeyCode(int keyCode) {
        int value = -1;
        if ((keyCode == KeyEvent.KEYCODE_0)) {
            SoundPoolUtil.getSoundPoolUtil().loadVoice(this, 0);
            value = 0;
        } else if ((keyCode == KeyEvent.KEYCODE_1)) {
            SoundPoolUtil.getSoundPoolUtil().loadVoice(this, 1);
            value = 1;
        } else if ((keyCode == KeyEvent.KEYCODE_2)) {
            SoundPoolUtil.getSoundPoolUtil().loadVoice(this, 2);
            value = 2;
        } else if ((keyCode == KeyEvent.KEYCODE_3)) {
            SoundPoolUtil.getSoundPoolUtil().loadVoice(this, 3);
            value = 3;
        } else if ((keyCode == KeyEvent.KEYCODE_4)) {
            SoundPoolUtil.getSoundPoolUtil().loadVoice(this, 4);
            value = 4;
        } else if ((keyCode == KeyEvent.KEYCODE_5)) {
            SoundPoolUtil.getSoundPoolUtil().loadVoice(this, 5);
            value = 5;
        } else if ((keyCode == KeyEvent.KEYCODE_6)) {
            SoundPoolUtil.getSoundPoolUtil().loadVoice(this, 6);
            value = 6;
        } else if ((keyCode == KeyEvent.KEYCODE_7)) {
            SoundPoolUtil.getSoundPoolUtil().loadVoice(this, 7);
            value = 7;
        } else if ((keyCode == KeyEvent.KEYCODE_8)) {
            SoundPoolUtil.getSoundPoolUtil().loadVoice(this, 8);
            value = 8;
        } else if ((keyCode == KeyEvent.KEYCODE_9)) {
            SoundPoolUtil.getSoundPoolUtil().loadVoice(this, 9);
            value = 9;
        }
        return value;
    }

    private void passwordInput(int key) {
        guestPassword = guestPassword + key;
        setTempkeyValue(guestPassword);
        if (guestPassword.length() == 6) {
            checkPassword();
        }
    }

    private void checkPassword() {
        setCurrentStatus(PASSWORD_CHECKING_MODE);
        String thisPassword = guestPassword;
        guestPassword = "";
        takePicture(thisPassword, false, this);
    }

    private void initPasswordStatus() {
        stopPasswordTimeoutChecking();
        setDialStatus("请输入访客密码");
        //callLayout.setVisibility(View.INVISIBLE);
        //guestLayout.setVisibility(View.VISIBLE);
        videoLayout.setVisibility(View.INVISIBLE);
        setCurrentStatus(PASSWORD_MODE);
        guestPassword = "";
        setTempkeyValue(guestPassword);
        startTimeoutChecking();
    }

    private void startTimeoutChecking() {
        passwordTimeoutThread = new Thread() {
            public void run() {
                try {
                    sleep(DeviceConfig.PASSWORD_WAIT_TIME); //等待指定的一个等待时间
                    if (!isInterrupted()) { //检查线程没有被停止
                        if (currentStatus == PASSWORD_MODE) { //如果现在是密码输入状态
                            if (guestPassword.equals("")) { //如果密码一直是空白的
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
                } catch (InterruptedException e) {
                }
                passwordTimeoutThread = null;
            }
        };
        passwordTimeoutThread.start();
    }

    protected void stopPasswordTimeoutChecking() {
        if (passwordTimeoutThread != null) {
            passwordTimeoutThread.interrupt();
            passwordTimeoutThread = null;
        }
    }

    private void passwordInput() {
        guestPassword = backKey(guestPassword);
        setTempkeyValue(guestPassword);
    }

    private void callInput() {
        if (DeviceConfig.DEVICE_TYPE.equals("C")) {
            if (blockId > 0) {
                if (callNumber.equals("")) {
                    blockId = 0;
                    blockNo = backKey(blockNo);
                    setDialStatus("请输入楼栋编号");
                    setDialValue(blockNo);
                } else {
                    callNumber = backKey(callNumber);
                    setDialValue(callNumber);
                }
            } else {
                blockNo = backKey(blockNo);
                setDialValue(blockNo);
            }
        } else {
            callNumber = backKey(callNumber);
            setDialValue(callNumber);
        }
    }

    private String backKey(String code) {
        if (code != null && code != "") {
            int length = code.length();
            if (length == 1) {
                code = "";
            } else {
                code = code.substring(0, (length - 1));
            }
        }
        return code;
    }

    /**
     * 录制卡片
     */
    private void receiveCard() {
        String bla = et_blackno.getText().toString();
        String uin = et_unitno.getText().toString();
        Log.d(TAG, "receiveCard: bla=" + bla);
        Log.d(TAG, "receiveCard: uni=" + uin);
        try {
            String url = DeviceConfig.SERVER_URL + "/app/rfid/bind?cardNo=" + this.cardId;
            url = url + "&communityId=" + communityId;
            url = url + "&blockNo=" + bla;
            url = url + "&unitNo=" + bla + uin;
            Log.d(TAG, "login: url=" + url);
            try {
                URL thisUrl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) thisUrl.openConnection();
                conn.setRequestMethod("GET");
                Log.d(TAG, "login: token=" + httpServerToken);
                if (httpServerToken != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + httpServerToken);
                }
                Log.d(TAG, "receiveCard: token=" + httpServerToken);
                conn.setConnectTimeout(5000);
                int code = conn.getResponseCode();
                Log.d(TAG, "login: code=" + code);
                if (code == 200) {
                    InputStream is = conn.getInputStream();
                    String result = HttpUtils.readMyInputStream(is);
                    Log.d(TAG, "login: result=" + result);
                    JSONObject resultObj = Ajax.getJSONObject(result);
                    int resultCode = resultObj.getInt("code");
                    Log.d(TAG, "login: code=" + resultCode);
                    Message message = Message.obtain();
                    if (resultCode == 0) {
                        //录入成功
                        message.what = 0x11;
                    } else {
                        //录入失败
                        message.what = 0x12;
                    }
                    handler.sendMessage(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
        }
    }

    private void callInput(int key, int id) {
        blockNo = blockNo + key;
        setTextValue(id, blockNo);
    }

    void setTextValue(final int id, String value) {
        final String thisValue = value;
        handler.post(new Runnable() {
            @Override
            public void run() {
                setTextView(id, thisValue);
            }
        });
    }

    public String cardId;

    private void onKeyDown(int keyCode) {
        if (nfcFlag) {//录入卡片信息
            String black = et_blackno.getText().toString();
            String unit = et_unitno.getText().toString();
            if (keyCode == KeyEvent.KEYCODE_STAR || keyCode == DeviceConfig.DEVICE_KEYCODE_STAR) {//取消
                if (!isFlag && (unit == null || unit.equals(""))) {//返回到房屋输入框
                    et_blackno.setFocusable(true);
                    et_blackno.setFocusableInTouchMode(true);
                    et_blackno.requestFocus();
                }
                if (isFlag && !(black == null || black.equals(""))) {//删除输入的房屋数字
                    blockNo = backKey(blockNo);
                    setTextValue(R.id.et_blockno, blockNo);
                }
                if (!isFlag && !(unit == null || unit.equals(""))) {//删除输入的门数字
                    blockNo = backKey(blockNo);
                    setTextValue(R.id.et_unitno, blockNo);
                }
                if (isFlag && (black == null || black.equals(""))) {
                    rl_nfc.setVisibility(View.GONE);
                    nfcFlag = false;
                }
            } else if (keyCode == KeyEvent.KEYCODE_POUND || keyCode == DeviceConfig.DEVICE_KEYCODE_POUND) {//录入卡片
                if (TextUtils.isEmpty(cardId)) {
                    Toast.makeText(this, "未检测到卡片信息", Toast.LENGTH_LONG).show();
                } else {
                    if (isFlag) {//切换edittext焦点到房屋编号输入框
                        if (!blockNo.equals("")) {
                            blockNo = "";
                        }
                        et_unitno.setFocusable(true);
                        et_unitno.setFocusableInTouchMode(true);
                        et_unitno.requestFocus();
                    } else {
                        if (TextUtils.isEmpty(black) || TextUtils.isEmpty(unit)) {
                            Toast.makeText(MainActivity.this, "楼栋编号或者房屋编号不能为空", Toast.LENGTH_LONG).show();
                            return;
                        }
                        new Thread() {
                            @Override
                            public void run() {
                                super.run();
                                receiveCard();
                            }
                        }.start();
                    }

                }
            } else {
                int key = convertKeyCode(keyCode);
                if (key >= 0) {
                    if (isFlag) {
                        callInput(key, R.id.et_blockno);
                    } else {
                        callInput(key, R.id.et_unitno);
                    }
                }
            }
        } else {
            if (currentStatus == CALL_MODE || currentStatus == PASSWORD_MODE) {
                int key = convertKeyCode(keyCode);
                if (key >= 0) {
                    if (currentStatus == CALL_MODE) {
                        callInput(key);
                    } else {
                        passwordInput(key);
                    }
                } else if (keyCode == KeyEvent.KEYCODE_POUND || keyCode == DeviceConfig.DEVICE_KEYCODE_POUND) {
                    if (currentStatus == CALL_MODE) {
                        initPasswordStatus();
                    } else {
                        initDialStatus();
                    }
                } else if (keyCode == KeyEvent.KEYCODE_STAR || keyCode == DeviceConfig.DEVICE_KEYCODE_STAR) {
                    if (currentStatus == CALL_MODE) {
                        callInput();
                    } else {
                        passwordInput();
                    }

                    TextView tv_input_text = (TextView) findViewById(R.id.tv_input_text);
                    String str = tv_input_text.getText().toString();
                    if (str == null || str.equals("")) {
                        //跳转到登录界面
                        Intent intent = new Intent(this, StartActivity.class);
                        startActivity(intent);
                    }
                }
            } else if (currentStatus == ERROR_MODE) {
                Utils.DisplayToast(MainActivity.this, "当前网络异常");
            } else if (currentStatus == CALLING_MODE) {
                if (keyCode == KeyEvent.KEYCODE_STAR || keyCode == DeviceConfig.DEVICE_KEYCODE_STAR) {

                    startCancelCall();
                }
            } else if (currentStatus == ONVIDEO_MODE) {
                if (keyCode == KeyEvent.KEYCODE_STAR || keyCode == DeviceConfig.DEVICE_KEYCODE_STAR) {
                    startDisconnectVideo();

                }
            } else if (currentStatus == DIRECT_CALLING_MODE) {
                if (keyCode == KeyEvent.KEYCODE_STAR || keyCode == DeviceConfig.DEVICE_KEYCODE_STAR) {
                    resetDial();
                    startCancelDirectCall();

                }
            } else if (currentStatus == DIRECT_CALLING_TRY_MODE) {
                if (keyCode == KeyEvent.KEYCODE_STAR || keyCode == DeviceConfig.DEVICE_KEYCODE_STAR) {
                    resetDial();
                    startCancelDirectCall();

                }
            } else if (currentStatus == DIRECT_MODE) {
                if (keyCode == KeyEvent.KEYCODE_STAR || keyCode == DeviceConfig.DEVICE_KEYCODE_STAR) {
                    resetDial();
                    startDisconnectDirectCall();

                }
            }
        }

    }

    private void startDisconnectDirectCall() {
        Message message = Message.obtain();
        message.what = MainService.MSG_DISCONNECT_DIRECT;
        try {
            serviceMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void startCancelDirectCall() {
        Message message = Message.obtain();
        message.what = MainService.MSG_CANCEL_DIRECT;
        try {
            serviceMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void startDisconnectVideo() {
        Message message = Message.obtain();
        message.what = MainService.MSG_DISCONNECT_VIEDO;
        try {
            serviceMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    protected void startCancelCall() {
        new Thread() {
            public void run() {
                stopCallCamera();
                try {
                    sleep(1000);
                } catch (Exception e) {
                }
                doCancelCall();
                try {
                    sleep(1000);
                } catch (Exception e) {
                }
                toast("您已经取消拨号");
                resetDial();
            }
        }.start();
    }

    protected void stopCallCamera() {
        setDialValue("正在取消拨号");
        setCurrentStatus(CALL_CANCEL_MODE);
        clearImageUuidAvaible(lastImageUuid);
        Log.v("MainService", "取消拍照" + lastImageUuid);
    }

    protected void doCancelCall() {
        Message message = Message.obtain();
        message.what = MainService.MSG_CANCEL_CALL;
        try {
            serviceMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    protected void resetDial() {
        callNumber = "";
        setDialValue(callNumber);
        setCurrentStatus(CALL_MODE);
    }

    private void setImageUuidAvaibale(String uuid) {
        Log.v("MainService", "加入UUID" + uuid);
        uuidMaps.put(uuid, "Y");
    }

    private void clearImageUuidAvaible(String uuid) {
        Log.v("MainService", "清除UUID" + uuid);
        uuidMaps.remove(uuid);
    }

    void setCommunityName(String value) {
        final String thisValue = value;
        handler.post(new Runnable() {
            @Override
            public void run() {
                setTextView(R.id.tv_community, thisValue);
            }
        });
    }

    void setLockName(String value) {
        final String thisValue = value;
        handler.post(new Runnable() {
            @Override
            public void run() {
                setTextView(R.id.tv_lock, thisValue);
            }
        });
    }

    void initVideoViews() {
        if (localView != null) return;
        if (MainService.callConnection != null)
            localView = (SurfaceView) MainService.callConnection.createVideoView(true, this, true);
        localView.setVisibility(View.INVISIBLE);
        videoLayout.addView(localView);
        localView.setKeepScreenOn(true);
        localView.setZOrderMediaOverlay(true);
        localView.setZOrderOnTop(true);

        if (MainService.callConnection != null)
            remoteView = (SurfaceView) MainService.callConnection.createVideoView(false, this, true);
        remoteView.setVisibility(View.INVISIBLE);
        remoteView.setKeepScreenOn(true);
        remoteView.setZOrderMediaOverlay(true);
        remoteView.setZOrderOnTop(true);
        //remoteLayout.addView(remoteView);
    }

    void setVideoSurfaceVisibility(int visible) {
        if (localView != null)
            localView.setVisibility(visible);
        if (remoteView != null)
            remoteView.setVisibility(visible);
    }

    synchronized void setCurrentStatus(int status) {
        currentStatus = status;
    }

    private void initDialStatus() {
        //callLayout.setVisibility(View.VISIBLE);
        //guestLayout.setVisibility(View.INVISIBLE);
        videoLayout.setVisibility(View.INVISIBLE);
        setCurrentStatus(CALL_MODE);
        callNumber = "";
        blockNo = "";
        blockId = 0;
        if (DeviceConfig.DEVICE_TYPE == "C") {
            setDialStatus("请输入楼栋编号");
        } else {
            setDialStatus("请输入房屋编号");
        }
        setDialValue(callNumber);
    }

    void setDialStatus(String value) {
        final String thisValue = value;
        handler.post(new Runnable() {
            @Override
            public void run() {
                setTextView(R.id.tv_input_label, thisValue);
            }
        });
    }

    void setDialValue(String value) {
        final String thisValue = value;
        handler.post(new Runnable() {
            @Override
            public void run() {
                setTextView(R.id.tv_input_text, thisValue);
            }
        });
    }

    void toast(final String message) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Utils.DisplayToast(MainActivity.this, message);
            }
        });
    }

    void setTempkeyValue(String value) {
        final String thisValue = value;
        handler.post(new Runnable() {
            @Override
            public void run() {
                setTextView(R.id.tv_input_text, thisValue);
            }
        });
    }

    void setTextView(int id, String txt) {
        ((TextView) findViewById(id)).setText(txt);
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

    protected void takePicture(final String thisValue, final boolean isCall, final TakePictureCallback callback) {
        if (currentStatus == CALLING_MODE || currentStatus == PASSWORD_CHECKING_MODE) {
            final String uuid = getUUID();
            lastImageUuid = uuid;
            setImageUuidAvaibale(uuid);
            callback.beforeTakePickture(thisValue, isCall, uuid);
            Log.v("MainService", "开始启动拍照");
            new Thread() {
                public void run() {
                    final String thisUuid = uuid;
                    if (checkTakePictureAvailable(thisUuid)) {
                        doTakePicture(thisValue, isCall, uuid, callback);
                    } else {
                        Log.v("MainService", "取消拍照");
                    }
                }
            }.start();
        }
    }

    private synchronized void doTakePicture(final String thisValue, final boolean isCall, final String uuid, final TakePictureCallback callback) {
        Camera camera = null;
        try {
            camera = Camera.open();

        } catch (Exception e) {
        }
        Log.v("MainService", "打开相机");
        if (camera == null) {
            try {
                camera = Camera.open(0);
            } catch (Exception e) {
            }
        }
        if (camera != null) {
            try {
                Camera.Parameters parameters = camera.getParameters();
                parameters.setPreviewSize(320, 240);
                try {
                    camera.setParameters(parameters);
                } catch (Exception err) {
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
                            if (checkTakePictureAvailable(uuid)) {
                                new Thread() {
                                    public void run() {
                                        String fileUrl = null;
                                        try {
                                            Log.v("MainService", "开始上传照片");
                                            fileUrl = UploadUtil.uploadFile(file, url);
                                            Log.v("MainService", "上传照片成功");
                                        } catch (Exception e) {
                                        }
                                        if (checkTakePictureAvailable(uuid)) {
                                            callback.afterTakePickture(thisValue, fileUrl, isCall, uuid);
                                        } else {
                                            Log.v("MainService", "上传照片成功,但已取消");
                                        }
                                        clearImageUuidAvaible(uuid);
                                        Log.v("MainService", "正常清除" + uuid);
                                        try {
                                            if (file != null) {
                                                file.deleteOnExit();
                                            }
                                        } catch (Exception e) {
                                        }
                                    }
                                }.start();
                            } else {
                                Log.v("MainService", "拍照成功，但已取消");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (Exception e) {
                try {
                    camera.stopPreview();
                } catch (Exception err) {
                }
                try {
                    camera.release();
                } catch (Exception err) {
                }
                callback.afterTakePickture(thisValue, null, isCall, uuid);
                Log.v("MainService", "照相出异常清除UUID");
                clearImageUuidAvaible(uuid);
            }
        }
    }

    private boolean checkTakePictureAvailable(String uuid) {
        String thisValue = uuidMaps.get(uuid);
        boolean result = false;
        if (thisValue != null && thisValue.equals("Y")) {
            result = true;
        }
        Log.v("MainService", "检查UUID" + uuid + result);
        return result;
    }

    private String getUUID() {
        UUID uuid = UUID.randomUUID();
        String result = UUID.randomUUID().toString();
        return result;
    }

    private StringBuilder doornum = new StringBuilder();
    TXBinderInfo[] arrayBinder1;
    List<TXBinderInfo> binderList1;
    AlertDialog dialog;

   /* @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_0:
                SoundPoolUtil.getSoundPoolUtil().loadVoice(this, 0);
                return true;
            case KeyEvent.KEYCODE_1:
                SoundPoolUtil.getSoundPoolUtil().loadVoice(this, 1);
                return true;
            case KeyEvent.KEYCODE_2:
                SoundPoolUtil.getSoundPoolUtil().loadVoice(this, 2);
                return true;
            case KeyEvent.KEYCODE_3:
                SoundPoolUtil.getSoundPoolUtil().loadVoice(this, 3);
                return true;
            case KeyEvent.KEYCODE_4:
                SoundPoolUtil.getSoundPoolUtil().loadVoice(this, 4);
                return true;
            case KeyEvent.KEYCODE_5:
                SoundPoolUtil.getSoundPoolUtil().loadVoice(this, 5);
                return true;
            case KeyEvent.KEYCODE_6:
                SoundPoolUtil.getSoundPoolUtil().loadVoice(this, 6);
                return true;
            case KeyEvent.KEYCODE_7:
                SoundPoolUtil.getSoundPoolUtil().loadVoice(this, 7);
                return true;
            case KeyEvent.KEYCODE_8:
                SoundPoolUtil.getSoundPoolUtil().loadVoice(this, 8);
                return true;
            case KeyEvent.KEYCODE_9:
                SoundPoolUtil.getSoundPoolUtil().loadVoice(this, 9);
                return true;
            case KeyEvent.KEYCODE_STAR:
                SoundPoolUtil.getSoundPoolUtil().loadVoice(this, 10);
                return true;
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_POUND:
                SoundPoolUtil.getSoundPoolUtil().loadVoice(this, 11);
                return true;
            case KeyEvent.KEYCODE_DEL:

                break;
        }
        Message message = Message.obtain();
        message.what = MainService.MSG_ASSEMBLE_KEY;
        message.obj = keyCode;
        handler.sendMessage(message);
        return super.onKeyUp(keyCode, event);
    }*/



   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuItem m_wifi = menu.add("设置wifi");
        MenuItem m_unbind = menu.add("解除绑定");
        MenuItem m_upload_log = menu.add("上传日志");
        MenuItem m_opendoor_log = menu.add("打开主门");
        MenuItem m_opendoor1_log = menu.add("打开副门");
        MenuItem m_setAlarm = menu.add("设置定时开机");
        MenuItem m_runReboot = menu.add("重启");
        MenuItem m_runShutdown = menu.add("关机");
        MenuItem m_setPlugedShutdown = menu.add("设置拔电关机");

        MenuItem m_exit_log = menu.add("退出");

        m_wifi.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                startActivity(new Intent(MainActivity.this,WifiConnActivity.class));
                return false;
            }
        });

        m_unbind.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {

                return true;
            }
        });

        m_upload_log.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                uploadDeviceLog(null);
                return true;
            }
        });

        m_opendoor_log.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                int status = 2;
                Intent ds_intent = new Intent();
                ds_intent.setAction(DoorLock.DoorLockOpenDoor);
                ds_intent.putExtra("index",0);
                ds_intent.putExtra("status",status);
                sendBroadcast(ds_intent);
                return true;
            }
        });

        m_opendoor1_log.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                int status = 2;
                Intent ds_intent = new Intent();
                ds_intent.setAction(DoorLock.DoorLockOpenDoor);
                ds_intent.putExtra("index",1);
                ds_intent.putExtra("status",status);
                sendBroadcast(ds_intent);
                return true;
            }
        });
        m_exit_log.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                setResult(RESULT_OK);
                finish();
                sendBroadcast(new Intent("com.android.action.display_navigationbar"));
                return true;
            }
        });

        m_setAlarm.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                long wakeupTime = SystemClock.elapsedRealtime() + 240000;       //唤醒时间,如果是关机唤醒时间不能低于3分钟,否则无法实现关机定时重启

                DoorLock.getInstance().runSetAlarm(wakeupTime);
                return true;
            }
        });

        m_runReboot.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                DoorLock.getInstance().runReboot();
                return true;
            }
        });

        m_runShutdown.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                DoorLock.getInstance().runShutdown();
                return true;
            }
        });

        m_setPlugedShutdown.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                DoorLock.getInstance().setPlugedShutdown();
                return true;
            }
        });

        return true;
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_OK);
                finish();
                return true;
        }
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            int keyCode = event.getKeyCode();
            onKeyDown(keyCode);
        }
        return false;
    }

    public void eraseAllBinders(View v) {
        AlertDialog dialog = null;
        Builder builder = new Builder(this).setTitle(R.string.unbind).setMessage(R.string.q_unbind_all).setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                dialog.dismiss();

            }
        }).setNegativeButton(R.string.unbind, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                dialog.dismiss();
                TXDeviceService.eraseAllBinders();
            }
        });
        dialog = builder.create();
        dialog.show();
    }

    public void uploadDeviceLog(View v) {
        TXDeviceService.getInstance().uploadSDKLog();
    }

    protected void onResume() {
        super.onResume();


        Intent intent = getIntent();
        String bindnum = intent.getStringExtra("bindnmu");
        if (!"".equals(bindnum) && "havenum".equals(bindnum)) {
            iv_bind.setImageDrawable(getResources().getDrawable(R.mipmap.binder_default_head));
        } else if (!"".equals(bindnum) && "nullnum".equals(bindnum)) {
            iv_bind.setImageDrawable(getResources().getDrawable(R.mipmap.bind_offline));
        }

        if (dialog != null && dialog.isShowing()) {/*去掉呼叫中弹出框*/
            dialog.dismiss();
        }
        iv_setting = (ImageView) findViewById(R.id.iv_setting);/*绑定状态显示*/
        TXBinderInfo[] arrayBinder = TXDeviceService.getBinderList();
        if (arrayBinder != null) {
            List<TXBinderInfo> binderList = new ArrayList<TXBinderInfo>();
            for (int i = 0; i < arrayBinder.length; ++i) {
                binderList.add(arrayBinder[i]);
            }
            if (mAdapter != null) {
                mAdapter.freshBinderList(binderList);
            }
            if (binderList.size() > 0) {
                iv_bind.setImageDrawable(getResources().getDrawable(R.mipmap.binder_default_head));
            } else {
                iv_bind.setImageDrawable(getResources().getDrawable(R.mipmap.bind_offline));
            }
        }
        initAexNfcReader();

        iv_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(MainActivity.this, iv_setting);
                popup.getMenuInflater()
                        .inflate(R.menu.poupup_menu_home, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_settings1:
                                // startActivity(new Intent(MainActivity.this, WifiConnActivity.class));
                                Intenet.system_set(MainActivity.this);
                                break;

                            case R.id.action_catIP:
                                Toast.makeText(MainActivity.this, "本机的IP：" + Intenet.getHostIP(), Toast.LENGTH_LONG).show();
                                break;

                            case R.id.action_catVersion:
                                Toast.makeText(MainActivity.this, "本机的固件版本：" + hwservice.getSdkVersion(), Toast.LENGTH_LONG).show();
                                break;

                            case R.id.action_settings2:
                                Toast.makeText(MainActivity.this, "该功能暂未开放", Toast.LENGTH_LONG).show();
                                break;

                            case R.id.action_settings3:
                                TXDeviceService.getInstance().uploadSDKLog();
                                break;

                            case R.id.action_settings4:
                                int status = 2;
                                Intent ds_intent = new Intent();
                                ds_intent.setAction(DoorLock.DoorLockOpenDoor);
                                ds_intent.putExtra("index", 0);
                                ds_intent.putExtra("status", status);
                                sendBroadcast(ds_intent);
                                break;

                            case R.id.action_settings5:
                                int status1 = 2;
                                Intent ds_intent1 = new Intent();
                                ds_intent1.setAction(DoorLock.DoorLockOpenDoor);
                                ds_intent1.putExtra("index", 1);
                                ds_intent1.putExtra("status", status1);
                                sendBroadcast(ds_intent1);
                                break;

                            case R.id.action_settings6:
                                long wakeupTime = SystemClock.elapsedRealtime() + 240000;       //唤醒时间,如果是关机唤醒时间不能低于3分钟,否则无法实现关机定时重启
                                DoorLock.getInstance().runSetAlarm(wakeupTime);
                                break;

                            case R.id.action_settings7:
                                DoorLock.getInstance().runReboot();
                                break;

                            case R.id.action_settings8:
                                DoorLock.getInstance().runShutdown();
                                break;
                            case R.id.action_settings9:
                                DoorLock.getInstance().setPlugedShutdown();
                                break;
                            case R.id.action_settings10:
                                setResult(RESULT_OK);
                                finish();
                                sendBroadcast(new Intent("com.android.action.display_navigationbar"));
                                break;
                            case R.id.action_settings11:
                                //initSpeech(MainActivity.this);
                                break;
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });
        //registering popup with OnMenuItemClickListener
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            handle.postDelayed(runnable, 1000);
            if (dialog != null && dialog.isShowing()) {
                dialogtime++;

                if (dialogtime >= 30) {
                    handle.removeCallbacks(runnable);
                    dialog.setMessage("呼叫失败");
                    dialog.dismiss();
                    dialogtime = 0;
                }
            }
        }
    };

    private int dialogtime = 0;

    private Handler handle = new Handler();

    protected void onPause() {
        super.onPause();

        //unbindService(mConn);

    }

    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
        disableReaderMode();
        unregisterReceiver(receive);
        unregisterReceiver(mNotifyReceiver);
        sendBroadcast(new Intent("com.android.action.display_navigationbar"));
    }

    private Receive receive;

    private void initAexNfcReader() {
        if (DeviceConfig.IS_NFC_AVAILABLE) {
            nfcReader = new NfcReader(this);
            enableReaderMode();
            receive = new Receive();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(NfcReader.ACTION_NFC_CARDINFO);
            registerReceiver(receive, intentFilter);
        }
    }

    private void enableReaderMode() {
        if (DeviceConfig.IS_NFC_AVAILABLE) {
            NfcAdapter nfc = NfcAdapter.getDefaultAdapter(this);
            if (nfc != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    if (this instanceof NfcAdapter.ReaderCallback) {
                        nfc.enableReaderMode(this, this, NfcReader.READER_FLAGS, null);
                    }
                }
            }
        }
    }

    private void disableReaderMode() {
        Log.i("", "禁用读卡模式");
        if (DeviceConfig.IS_NFC_AVAILABLE) {
            NfcAdapter nfc = NfcAdapter.getDefaultAdapter(this);
            if (nfc != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    nfc.disableReaderMode(this);
                }
            }
        }
    }

    @Override
    public void onTagDiscovered(Tag tag) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if ((nfcReader != null) && (nfcReader instanceof NfcAdapter.ReaderCallback)) {
                NfcAdapter.ReaderCallback nfcReader = (NfcAdapter.ReaderCallback) this.nfcReader;
                nfcReader.onTagDiscovered(tag);
            }
        }
    }

    @Override
    public void callBackList(Parcelable[] list) {
        listTemp1 = list;
    }


    class Receive extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String actionName = intent.getAction();
            if (NfcReader.ACTION_NFC_CARDINFO.equals(actionName)) {
                String cardInfo = intent.getStringExtra("cardinfo");
                Log.d("0000000", "onReceive: cardinfo=" + cardInfo);
            }
        }
    }

    @Override
    public void onAccountReceived(String account) {
        cardId = account;
        if (!nfcFlag) {
            Message message = Message.obtain();
            message.what = MainService.MSG_CARD_INCOME;
            message.obj = account;
            try {
                serviceMessenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            tv_message.setText(cardId);
        }
    }



    private void showAlert(String strTitle, String strMsg) {
        // TODO Auto-generated method stub
        AlertDialog dialogError;
        Builder builder = new AlertDialog.Builder(this).setTitle(strTitle).setMessage(strMsg).setPositiveButton("取消", null).setNegativeButton("确定", null);
        dialogError = builder.create();
        dialogError.show();
    }

    protected void startDialorPasswordDirectly(final String thisValue, final String fileUrl, final boolean isCall, String uuid) {
        if (currentStatus == CALLING_MODE || currentStatus == PASSWORD_CHECKING_MODE) {
            Message message = Message.obtain();
            if (isCall) {
                setDialValue("呼叫" + thisValue + "，取消请按删除号键");
                message.what = MainService.MSG_START_DIAL;
            } else {
                setTempkeyValue("准备验证密码" + thisValue + "...");
                message.what = MainService.MSG_CHECK_PASSWORD;
            }
            String[] parameters = new String[3];
            parameters[0] = thisValue;
            parameters[1] = fileUrl;
            parameters[2] = uuid;
            message.obj = parameters;
            try {
                serviceMessenger.send(message);
            } catch (RemoteException er) {
                er.printStackTrace();
            }
        }
    }

    protected void startSendPictureDirectly(final String thisValue, final String fileUrl, final boolean isCall, String uuid) {
        if (fileUrl == null || fileUrl.length() == 0) {
            return;
        }
        Message message = Message.obtain();
        if (isCall) {
            message.what = MainService.MSG_START_DIAL_PICTURE;
        } else {
            message.what = MainService.MSG_CHECK_PASSWORD_PICTURE;
        }
        String[] parameters = new String[3];
        parameters[0] = thisValue;
        parameters[1] = fileUrl;
        parameters[2] = uuid;
        message.obj = parameters;
        try {
            serviceMessenger.send(message);
        } catch (RemoteException er) {
            er.printStackTrace();
        }
    }

    @Override
    public void beforeTakePickture(String thisValue, boolean isCall, String uuid) {
        startDialorPasswordDirectly(thisValue, null, isCall, uuid);
    }

    @Override
    public void afterTakePickture(String thisValue, String fileUrl, boolean isCall, String uuid) {
        startSendPictureDirectly(thisValue, fileUrl, isCall, uuid);
    }

    // 使用Handler实现UI线程与Timer线程之间的信息传递,每5秒告诉UI线程获得wifiInto
    // 使用Handler实现UI线程与Timer线程之间的信息传递,每5秒告诉UI线程获得wifiInto
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            Log.e("=====listTemp1",listTemp1+"");
            switch (msg.what) {
                // 如果收到正确的消息就获取WifiInfo，改变图片并显示信号强度
                case 11:
                    wifi_image.setImageResource(R.mipmap.ethernet);
                    if (listTemp1 != null && listTemp1.length > 0) {
                        iv_bind.setImageDrawable(getResources().getDrawable(R.mipmap.binder_default_head));
                    } else {
                        iv_bind.setImageDrawable(getResources().getDrawable(R.mipmap.bind_offline));
                    }

                    break;
                case 1:
                    wifi_image.setImageResource(R.mipmap.wifi02);
                    if (listTemp1 != null && listTemp1.length > 0) {
                        iv_bind.setImageDrawable(getResources().getDrawable(R.mipmap.binder_default_head));
                    } else {
                        iv_bind.setImageDrawable(getResources().getDrawable(R.mipmap.bind_offline));
                    }

                    break;
                case 2:
                    wifi_image.setImageResource(R.mipmap.wifi02);
                    if (listTemp1 != null && listTemp1.length > 0) {
                        iv_bind.setImageDrawable(getResources().getDrawable(R.mipmap.binder_default_head));
                    } else {
                        iv_bind.setImageDrawable(getResources().getDrawable(R.mipmap.bind_offline));
                    }
                    break;
                case 3:
                    wifi_image.setImageResource(R.mipmap.wifi03);
                    if (listTemp1 != null && listTemp1.length > 0) {
                        iv_bind.setImageDrawable(getResources().getDrawable(R.mipmap.binder_default_head));
                    } else {
                        iv_bind.setImageDrawable(getResources().getDrawable(R.mipmap.bind_offline));
                    }
                    break;
                case 4:
                    wifi_image.setImageResource(R.mipmap.wifi04);
                    if (listTemp1 != null && listTemp1.length > 0) {
                        iv_bind.setImageDrawable(getResources().getDrawable(R.mipmap.binder_default_head));
                    } else {
                        iv_bind.setImageDrawable(getResources().getDrawable(R.mipmap.bind_offline));
                    }
                    break;
                case 5:
                    wifi_image.setImageResource(R.mipmap.wifi05);
                    iv_bind.setImageDrawable(getResources().getDrawable(R.mipmap.bind_offline));

                    break;
                case 6://无网络连接
                    rl.setVisibility(View.VISIBLE);
                    break;
                case 7:
                    rl.setVisibility(View.GONE);
                    break;
                default:
                    //以防万一
                    wifi_image.setImageResource(R.mipmap.wifi_05);
                    rl.setVisibility(View.VISIBLE);
                    iv_bind.setImageDrawable(getResources().getDrawable(R.mipmap.bind_offline));

            }
        }

    };
}


