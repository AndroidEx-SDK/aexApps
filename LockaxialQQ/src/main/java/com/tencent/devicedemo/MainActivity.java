package com.tencent.devicedemo;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidex.DoorLock;
import com.androidex.GetUserInfo;
import com.androidex.LoyaltyCardReader;
import com.androidex.SoundPoolUtil;
import com.androidex.Zxing;
import com.dialog.SpotsDialog;
import com.entity.Banner;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.tencent.device.TXBinderInfo;
import com.tencent.device.TXDataPoint;
import com.tencent.device.TXDeviceService;
import com.viewpager.AutoScrollViewPager;
import com.wificonnect.WifiConnActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends Activity implements LoyaltyCardReader.AccountCallback{
  
	private GridView mGridView; 
	private BinderListAdapter mAdapter;
	private NotifyReceiver  mNotifyReceiver;
    private Toast toast;
    // Recommend NfcAdapter flags for reading from other Android devices. Indicates that this
    // activity is interested in NFC-A devices (including other Android devices), and that the
    // system should not check for the presence of NDEF-formatted data (e.g. Android Beam).
    public static int READER_FLAGS =
            NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK;
    public LoyaltyCardReader mLoyaltyCardReader;

    private EditText tv_input;
    private AutoScrollViewPager viewPager;
    private Banner banner;
    private Bg_Adapter bgAdapter;
    private ImageView imageView;
    private DisplayImageOptions options;
    private WifiInfo wifiInfo = null;		//获得的Wifi信息
    private WifiManager wifiManager = null;	//Wifi管理器
    private Handler handler;
    private ImageView wifi_image;			//信号图片显示
    private int level;						//信号强度值
    private ImageView iv_setting;
    private RelativeLayout rl;
    private ImageView iv_bind;


    protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated constructor stub
		super.onCreate(savedInstanceState);
        toast = Toast.makeText(getApplicationContext(),"", Toast.LENGTH_SHORT);
        //全屏设置，隐藏窗口所有装饰
        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);//清除FLAG
        getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //标题是属于View的，所以窗口所有的修饰部分被隐藏后标题依然有效
        //requestWindowFeature(getWindow().FEATURE_NO_TITLE);
        sendBroadcast(new Intent("com.android.action.hide_navigationbar"));
        //if(intent.getBooleanExtra("back", false))
        {
            ActionBar ab = getActionBar();
            if(ab != null)
                ab.setDisplayHomeAsUpEnabled(true);
        }

		setContentView(R.layout.activity_main);

        IntentFilter filter = new IntentFilter();
        filter.addAction(TXDeviceService.BinderListChange);
        filter.addAction(TXDeviceService.OnEraseAllBinders);
        filter.addAction(TXDeviceService.wifisetting);
        filter.addAction(DoorLock.DoorLockStatusChange);
        filter.addAction(DoorLock.DoorLockOpenDoor);
        filter.addAction(TXDeviceService.voicereceive);
        filter.addAction(TXDeviceService.isconnected);
        filter.addAction(TXDeviceService.BinderListChange);
        filter.addAction(TXDeviceService.OnEraseAllBinders);
        filter.addAction(DoorLock.DoorLockStatusChange);
        mNotifyReceiver = new NotifyReceiver();
        registerReceiver(mNotifyReceiver, filter);

        viewPager=(AutoScrollViewPager)findViewById(R.id.vp_main);
        imageView=(ImageView)findViewById(R.id.iv_erweima);
       /* XUtilsNetwork.getInstance().getBgBanners(new NetworkCallBack() {//网络请求
           // *//**网络获得轮播背景图片数据*//*
            @Override
            public void onSuccess(Object o) {
               Gson gson = new Gson();
                banner = gson.fromJson(o.toString(), Banner.class);
                bgAdapter = new Bg_Adapter(MainActivity.this, banner.getData());
                viewPager.setAdapter(bgAdapter);
                viewPager.setCycle(true);
                //viewPager.setSwipeScrollDurationFactor(2000);//设置ViewPager滑动动画间隔时间的倍率，达到减慢动画或改变动画速度的效果
                viewPager.setInterval(50000);//设置自动滚动的间隔时间，单位为毫秒
                viewPager.startAutoScroll();
            }

            @Override
            public void onFailure(String error) {

            }
        });*/
        //生成一个可以绑定设备的二维码
        Log.d("mainactivity",GetUserInfo.getSn());
        Bitmap bitmap = Zxing.createQRImage("http://iot.qq.com/add?pid=1700003316&sn="+GetUserInfo.getSn(),200,200,null);
        if (bitmap==null){

            options = new DisplayImageOptions.Builder()
                    .showImageOnFail(R.mipmap.fail)
                    .showImageOnLoading(R.mipmap.loading)
                    .cacheOnDisk(true)
                    .bitmapConfig(Bitmap.Config.ARGB_8888)
                    .build();

            BaseApplication.getApplication().getImageLoader().displayImage("http://www.tyjdtzjc.cn/resource/kindeditor/attached/image/20150831/20150831021658_90595.png", imageView,options);
        }else{
            imageView.setImageBitmap(bitmap);
        }
         /**二维码图片 这我百度的图片,你把网址替换就可以了*/

        //图片控件初始化
        wifi_image = (ImageView) findViewById(R.id.wifi_image);
        // 获得WifiManager
        wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        rl=(RelativeLayout)findViewById(R.id.net_view_rl);
        // 使用定时器,每隔5秒获得一次信号强度值
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                wifiInfo = wifiManager.getConnectionInfo();
                //获得信号强度值
                level = wifiInfo.getRssi();
                //根据获得的信号强度发送信息
                if (level <= 0 && level >= -50) {
                    Message msg = new Message();
                    msg.what = 1;
                    handler.sendMessage(msg);
                } else if (level < -50 && level >= -70) {
                    Message msg = new Message();
                    msg.what = 2;
                    handler.sendMessage(msg);
                } else if (level < -70 && level >= -80) {
                    Message msg = new Message();
                    msg.what = 3;
                    handler.sendMessage(msg);
                } else if (level < -80 && level >= -100) {
                    Message msg = new Message();
                    msg.what = 4;
                    handler.sendMessage(msg);
                } else {
                    Message msg = new Message();
                    msg.what = 5;
                    handler.sendMessage(msg);
                }

            }

        }, 1000, 5000);
        // 使用Handler实现UI线程与Timer线程之间的信息传递,每5秒告诉UI线程获得wifiInto
        // 使用Handler实现UI线程与Timer线程之间的信息传递,每5秒告诉UI线程获得wifiInto
        handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    // 如果收到正确的消息就获取WifiInfo，改变图片并显示信号强度
                    case 1:
                        wifi_image.setImageResource(R.mipmap.wifi02);
                        rl.setVisibility(View.GONE);
                        if(listTemp1!=null&&listTemp1.length>0){
                            iv_bind.setImageDrawable(getResources().getDrawable(R.mipmap.binder_default_head));
                        }else{
                            iv_bind.setImageDrawable(getResources().getDrawable(R.mipmap.bind_offline));
                        }
                        break;
                    case 2:
                        wifi_image.setImageResource(R.mipmap.wifi02);
                        rl.setVisibility(View.GONE);
                        if(listTemp1!=null&&listTemp1.length>0){
                            iv_bind.setImageDrawable(getResources().getDrawable(R.mipmap.binder_default_head));
                        }else{
                            iv_bind.setImageDrawable(getResources().getDrawable(R.mipmap.bind_offline));
                        }
                        break;
                    case 3:
                        wifi_image.setImageResource(R.mipmap.wifi03);
                        rl.setVisibility(View.GONE);
                        if(listTemp1!=null&&listTemp1.length>0){
                            iv_bind.setImageDrawable(getResources().getDrawable(R.mipmap.binder_default_head));
                        }else{
                            iv_bind.setImageDrawable(getResources().getDrawable(R.mipmap.bind_offline));
                        }
                        break;
                    case 4:
                        wifi_image.setImageResource(R.mipmap.wifi04);
                        rl.setVisibility(View.GONE);
                        if(listTemp1!=null&&listTemp1.length>0){
                            iv_bind.setImageDrawable(getResources().getDrawable(R.mipmap.binder_default_head));
                        }else{
                            iv_bind.setImageDrawable(getResources().getDrawable(R.mipmap.bind_offline));
                        }
                        break;
                    case 5:
                        wifi_image.setImageResource(R.mipmap.wifi05);
                        rl.setVisibility(View.VISIBLE);
                            iv_bind.setImageDrawable(getResources().getDrawable(R.mipmap.bind_offline));
                        startActivity(new Intent(
                                MainActivity.this, WifiConnActivity.class));
                        break;
                    default:
                        //以防万一
                        wifi_image.setImageResource(R.mipmap.wifi_05);
                        rl.setVisibility(View.VISIBLE);
                            iv_bind.setImageDrawable(getResources().getDrawable(R.mipmap.bind_offline));

                }
            }

        };

        rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {/**跳转网络设置*/
                /*try {
                    if (Build.VERSION.SDK_INT > 10) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setClassName("com.android.settings", "com.android.settings.Settings");
                        intent.putExtra("back",true);
                        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        sendBroadcast(new Intent("com.android.action.display_navigationbar"));
                        startActivityForResult(intent,1004);
                       return;
                    }
                } catch (Exception localException) {
                    localException.printStackTrace();
                    return;
                }*/
                Intent intent = new Intent();
                intent.setAction("android.net.wifi.PICK_WIFI_NETWORK");
                startActivity(intent);

        }
        });

        iv_bind=(ImageView)findViewById(R.id.user_bind);


		Intent startIntent = new Intent(this, TXDeviceService.class); 
		startService(startIntent);

        /*Intent i = new Intent(this, SpeechService.class);
        startService(i);*/

        Intent dlIntent = new Intent(this, DoorLock.class);
        startService(dlIntent);
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/GBK.TTF");
        TextView com_tv=(TextView)findViewById(R.id.tv_companyname);/** 公司名*/
        TextView com_xq=(TextView)findViewById(R.id.tv_xiaoqu);
        //TextView com_log=(TextView)findViewById(R.id.tv_log);
        com_xq.setText("办公区门禁");
        tv_input=(EditText)findViewById(R.id.tv_input);
        tv_input.setTypeface(typeFace);
        com_tv.setTypeface(typeFace);
        com_xq.setTypeface(typeFace);
       // com_log.setTypeface(typeFace);

		mGridView = (GridView) findViewById(R.id.gridView_binderlist);
		mAdapter = new BinderListAdapter(this);
		mGridView.setAdapter(mAdapter);

        boolean bNetworkSetted = this.getSharedPreferences("TXDeviceSDK", 0).getBoolean("NetworkSetted", false);
        SharedPreferences sharedPreferences= getSharedPreferences("test",
                Activity.MODE_PRIVATE);
        String networkSettingMode =sharedPreferences.getString("NetworkSettingMode", "");
        if("".equals(networkSettingMode)&& bNetworkSetted == false){
            Intent intent = new Intent(MainActivity.this, WifiDecodeActivity.class);
            startActivity(intent);
            SharedPreferences mySharedPreferences= getSharedPreferences("test",
                    Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = mySharedPreferences.edit();
            editor.putString("NetworkSettingMode", "true");
            editor.commit();
        }else if("true".equals(networkSettingMode)){

        }
        if(Build.VERSION.SDK_INT >= 19) {
            mLoyaltyCardReader = new LoyaltyCardReader(this);
        }

        // Disable Android Beam and register our card reader callback
        enableReaderMode();

        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //通话音量
        int max = mAudioManager.getStreamMaxVolume( AudioManager.STREAM_VOICE_CALL );
        int current = mAudioManager.getStreamVolume( AudioManager.STREAM_VOICE_CALL );
        Log.d("VIOCE_CALL","max : " + max + " current : " + current);
        //系统音量
        max = mAudioManager.getStreamMaxVolume( AudioManager.STREAM_SYSTEM );
        current = mAudioManager.getStreamVolume( AudioManager.STREAM_SYSTEM );
        Log.d("SYSTEM", "max : " + max + " current : " + current);
        //铃声音量
        max = mAudioManager.getStreamMaxVolume( AudioManager.STREAM_RING );
        current = mAudioManager.getStreamVolume( AudioManager.STREAM_RING );
        Log.d("RING", "max : " + max + " current : " + current);
        //音乐音量
        max = mAudioManager.getStreamMaxVolume( AudioManager.STREAM_MUSIC );
        current = mAudioManager.getStreamVolume( AudioManager.STREAM_MUSIC );
        Log.d("MUSIC", "max : " + max + " current : " + current);
        //提示声音音量
        max = mAudioManager.getStreamMaxVolume( AudioManager.STREAM_ALARM );
        current = mAudioManager.getStreamVolume( AudioManager.STREAM_ALARM );
        Log.d("ALARM", "max : " + max + " current : " + current);

        if (!isNetworkAvailable(MainActivity.this))
        {
            Toast.makeText(getApplicationContext(), "当前没有可用网络！", Toast.LENGTH_LONG).show();
            startActivity(new Intent(
                    MainActivity.this, WifiConnActivity.class));
        }
        if(false) {

            AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            Intent intent;
            PendingIntent pendingIntent;


            intent = new Intent(getApplicationContext(), AlarmReciver.class);
            pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 10000, pendingIntent);
        }
    }

  private StringBuilder doornum=new StringBuilder();
    TXBinderInfo [] arrayBinder1 ;
    List<TXBinderInfo> binderList1;
    AlertDialog dialog;
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch(keyCode){
            case KeyEvent.KEYCODE_0:
                SoundPoolUtil.getSoundPoolUtil().loadVoice(this, 0);
                doornum.append("0");
                tv_input.setText(doornum.toString());
                return true;
            case KeyEvent.KEYCODE_1:
                    SoundPoolUtil.getSoundPoolUtil().loadVoice(this,1);
                    doornum.append("1");
                    tv_input.setText(doornum.toString());
                return true;
            case KeyEvent.KEYCODE_2:
                SoundPoolUtil.getSoundPoolUtil().loadVoice(this,2);
                doornum.append("2");
                tv_input.setText(doornum.toString());
                return true;
            case KeyEvent.KEYCODE_3:
                SoundPoolUtil.getSoundPoolUtil().loadVoice(this,3);
                doornum.append("3");
                tv_input.setText(doornum.toString());
                return true;
            case KeyEvent.KEYCODE_4:
                SoundPoolUtil.getSoundPoolUtil().loadVoice(this,4);
                doornum.append("4");
                tv_input.setText(doornum.toString());
                return true;
            case KeyEvent.KEYCODE_5:
                SoundPoolUtil.getSoundPoolUtil().loadVoice(this,5);
                doornum.append("5");
                tv_input.setText(doornum.toString());
                return true;
            case KeyEvent.KEYCODE_6:
                SoundPoolUtil.getSoundPoolUtil().loadVoice(this,6);
                doornum.append("6");
                tv_input.setText(doornum.toString());
                return true;
            case KeyEvent.KEYCODE_7:
                SoundPoolUtil.getSoundPoolUtil().loadVoice(this,7);
                doornum.append("7");
                tv_input.setText(doornum.toString());
                return true;
            case KeyEvent.KEYCODE_8:
                SoundPoolUtil.getSoundPoolUtil().loadVoice(this, 8);
                doornum.append("8");
                tv_input.setText(doornum.toString());
                return true;
            case KeyEvent.KEYCODE_9:
                SoundPoolUtil.getSoundPoolUtil().loadVoice(this,9);
                doornum.append("9");
                tv_input.setText(doornum.toString());
                return true;
            case KeyEvent.KEYCODE_STAR:
                SoundPoolUtil.getSoundPoolUtil().loadVoice(this,10);
                return true;
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_POUND:
               if (isNetworkAvailable(MainActivity.this))
                {
                SoundPoolUtil.getSoundPoolUtil().loadVoice(this,11);
                if("666".equals(tv_input.getText().toString())){/**666为房门号 查找用户*/

                    arrayBinder1 = TXDeviceService.getBinderList();
                    binderList1=null;
                    if (arrayBinder1 != null){
                       binderList1 = new ArrayList<TXBinderInfo>();
                        for (int i = 0; i < arrayBinder1.length; ++i){
                            binderList1.add(arrayBinder1[i]);
                        }
                    }
                    if(binderList1!=null&&binderList1.size()>0){
                        dialog = new SpotsDialog(MainActivity.this,"呼叫中(按(删除)键取消呼叫)");
                        dialog.show();
                        if(dialog.isShowing()){
                            long tinyid = binderList1.get(0).tinyid;
                            String nickname = binderList1.get(0).getNickName();
                            Intent  binder = new Intent(MainActivity.this, BinderActivity.class);
                            binder.putExtra("tinyid", tinyid);
                            binder.putExtra("nickname", nickname);
                            TXDeviceService.getInstance().sendNotifyMsg("提示", 1700003316, new long[]{tinyid});/*发送强提醒通知*/
                            TXDataPoint[] txDataPoints=new TXDataPoint[]{};
                            TXDeviceService.ackDataPoint(tinyid, txDataPoints);
                            handle.postDelayed(runnable, 1000);
                        }

                    }else{
                        DissmissDialog builder=new DissmissDialog(MainActivity.this,R.style.selectorDialog,"没有绑定用户!");
                        builder.show();
                    }
                    doornum=new StringBuilder("");
                    tv_input.setText("");
                }else{
                   DissmissDialog builder=new DissmissDialog(MainActivity.this,R.style.selectorDialog,"此用户没绑定!");
                    builder.show();
                    doornum=new StringBuilder("");
                    tv_input.setText("");
                }
                }else{
                   DissmissDialog builder=new DissmissDialog(MainActivity.this,R.style.selectorDialog,"网络问题,请检查网络!");
                   builder.show();
                   doornum=new StringBuilder("");
                   tv_input.setText("");
               }
                return true;
            case KeyEvent.KEYCODE_DEL:
                if(!"".equals(doornum.toString())){
                    String num=doornum.toString().substring(0, doornum.length()-1);
                    doornum=new StringBuilder(num);
                    tv_input.setText(num);
                }
                break;
        }

        return super.onKeyUp(keyCode, event);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        AudioManager audio = (AudioManager) getSystemService(Service.AUDIO_SERVICE);
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                audio .adjustStreamVolume(
                        AudioManager.STREAM_VOICE_CALL,
                        AudioManager.ADJUST_RAISE,
                        AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                audio.adjustStreamVolume(
                        AudioManager.STREAM_VOICE_CALL,
                        AudioManager.ADJUST_LOWER,
                        AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
                return true;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

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
        return super.dispatchKeyEvent(event);
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
	
	public void uploadDeviceLog(View v) 
	{
		TXDeviceService.getInstance().uploadSDKLog();
	}

	protected void onResume(){
		super.onResume();


        Intent  intent=getIntent();
        String bindnum=intent.getStringExtra("bindnmu");
        if(!"".equals(bindnum)&&"havenum".equals(bindnum)){
            iv_bind.setImageDrawable(getResources().getDrawable(R.mipmap.binder_default_head));
        }else if(!"".equals(bindnum)&&"nullnum".equals(bindnum)){
            iv_bind.setImageDrawable(getResources().getDrawable(R.mipmap.bind_offline));
        }

        if(dialog!=null&&dialog.isShowing()){/*去掉呼叫中弹出框*/
            dialog.dismiss();
        }
        iv_setting=(ImageView)findViewById(R.id.iv_setting);/*绑定状态显示*/
		TXBinderInfo [] arrayBinder = TXDeviceService.getBinderList();
		if (arrayBinder != null){
			List<TXBinderInfo> binderList = new ArrayList<TXBinderInfo>();
			for (int i = 0; i < arrayBinder.length; ++i){
				binderList.add(arrayBinder[i]);
			}
			if (mAdapter != null) {
				mAdapter.freshBinderList(binderList);
			}
            if(binderList.size()>0){
                iv_bind.setImageDrawable(getResources().getDrawable(R.mipmap.binder_default_head));
            }else{
                iv_bind.setImageDrawable(getResources().getDrawable(R.mipmap.bind_offline));
            }
		}
        enableReaderMode();

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
                                startActivity(new Intent(MainActivity.this, WifiConnActivity.class));
                                break;
                            case R.id.action_settings2:
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
    private Runnable runnable=new Runnable() {
        @Override
        public void run() {
            handle.postDelayed(runnable,1000);
            if(dialog!=null&&dialog.isShowing()){
                dialogtime++;

                if(dialogtime>=30){
                    handle.removeCallbacks(runnable);
                    dialog.setMessage("呼叫失败");
                    dialog.dismiss();
                    dialogtime=0;
                }
            }
        }
    };

    private int dialogtime=0;

    private Handler handle=new Handler();
	
	protected void onPause(){
		super.onPause();
        disableReaderMode();

        //unbindService(mConn);
    }
	
	protected void onDestroy(){
		super.onDestroy();
        //unbindService(mConn);
        unregisterReceiver(mNotifyReceiver);
        sendBroadcast(new Intent("com.android.action.display_navigationbar"));
	}

    private void enableReaderMode() {
        Log.i("", "启用读卡模式");
        if(Build.VERSION.SDK_INT >= 19)
        {
            Activity activity = this;
            NfcAdapter nfc = NfcAdapter.getDefaultAdapter(activity);
            if (nfc != null) {
                nfc.enableReaderMode(activity, mLoyaltyCardReader, READER_FLAGS, null);
            }
        }
    }

    private void disableReaderMode() {
        Log.i("", "禁用读卡模式");
        if(Build.VERSION.SDK_INT >= 19) {
            Activity activity = this;
            NfcAdapter nfc = NfcAdapter.getDefaultAdapter(activity);
            if (nfc != null) {
                nfc.disableReaderMode(activity);
            }
        }
    }

    @Override
    public void onAccountReceived(String account) {
        // This callback is run on a background thread, but updates to UI elements must be performed
        // on the UI thread.
        toast.setText(account);
        toast.show();
        /*getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAccountField.setText(account);
            }
        });*/
    }


    Parcelable[] listTemp1;
    public class NotifyReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction() == TXDeviceService.BinderListChange){
				Parcelable[] listTemp = intent.getExtras().getParcelableArray("binderlist");
                listTemp1=listTemp;
				List<TXBinderInfo> binderList = new ArrayList<TXBinderInfo>();
				for (int i = 0; i < listTemp.length; ++i){
					TXBinderInfo  binder = (TXBinderInfo)(listTemp[i]);
					binderList.add(binder); 
				}
				if (mAdapter != null) {
					mAdapter.freshBinderList(binderList);
				}

                if(binderList.size()>0){
                    iv_bind.setImageDrawable(getResources().getDrawable(R.mipmap.binder_default_head));
                   // startActivity(new Intent(MainActivity.this, WifiDecodeActivity.class));
                }else{
                    iv_bind.setImageDrawable(getResources().getDrawable(R.mipmap.bind_offline));
                    startActivity(new Intent(MainActivity.this, WifiDecodeActivity.class));
                }
			} else if (intent.getAction() == TXDeviceService.OnEraseAllBinders){
				int resultCode = intent.getExtras().getInt(TXDeviceService.OperationResult);
				if (0 != resultCode) {
					showAlert("解除绑定失败", "解除绑定失败，错误码:" + resultCode);
				} else {
					showAlert("解除绑定成功", "解除绑定成功!!!");
                    iv_bind.setImageDrawable(getResources().getDrawable(R.mipmap.bind_offline));
                    startActivity(new Intent(MainActivity.this, WifiDecodeActivity.class));
				}

			} else if(intent.getAction() == DoorLock.DoorLockStatusChange){
                //门禁状态改变事件
                //showAlert("门禁状态改变",intent.getStringExtra("doorsensor"));
                String doorsendor = String.format("doorsensor=%s",intent.getStringExtra("doorsensor"));
                Log.d("NotifyReceiver", doorsendor);
                toast.setText(doorsendor);
                toast.show();
            }else if(intent.getAction() == TXDeviceService.wifisetting){
                if(!isNetworkAvailable(MainActivity.this))
               startActivity(new Intent(MainActivity.this,WifiConnActivity.class));
            }else if(intent.getAction().equals(DoorLock.DoorLockOpenDoor)){
                if(dialog!=null&&dialog.isShowing()){
                    try {
                        Thread.sleep(3000);/*休眠三秒*/
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    dialog.dismiss();
                }
            }else if(intent.getAction().equals(TXDeviceService.voicereceive)){
                String filepath=intent.getStringExtra("filepath");
                if ("".equals(filepath)) return;
                Intent intent1=new Intent(MainActivity.this,AudioRecordActivity.class);
                intent1.putExtra("filepath",filepath);
                startActivity(intent1);

            }else if (intent.getAction().equals(TXDeviceService.isconnected)){
                String ishave=intent.getStringExtra("ishave");
                if(!"".equals(ishave)&&ishave.equals("yes")){
                    iv_bind.setImageDrawable(getResources().getDrawable(R.mipmap.binder_default_head));
                }else{
                    iv_bind.setImageDrawable(getResources().getDrawable(R.mipmap.bind_offline));
                }

            }
		}
	}
	
	private void showAlert(String strTitle, String strMsg) {
		// TODO Auto-generated method stub
		AlertDialog dialogError;
		Builder builder = new AlertDialog.Builder(this).setTitle(strTitle).setMessage(strMsg).setPositiveButton("取消", null).setNegativeButton("确定",null);
		dialogError = builder.create();
		dialogError.show();
	}


    /**
     * 检查当前网络是否可用
     *
     * @param
     * @return
     */

    public boolean isNetworkAvailable(Activity activity)
    {
        Context context = activity.getApplicationContext();
        // 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null)
        {
            return false;
        }
        else
        {
            // 获取NetworkInfo对象
            NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();

            if (networkInfo != null && networkInfo.length > 0)
            {
                for (int i = 0; i < networkInfo.length; i++)
                {
                    networkInfo[i].isAvailable();
                    // 判断当前网络状态是否为连接状态
                    if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
