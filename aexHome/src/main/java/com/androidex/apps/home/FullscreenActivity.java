package com.androidex.apps.home;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidex.aexlibs.WebJavaBridge;
import com.androidex.aexlibs.hwService;
import com.androidex.apps.home.fragment.AboutLocalFragment;
import com.androidex.apps.home.fragment.AfterBankcardFragment;
import com.androidex.apps.home.fragment.CameraFragment;
import com.androidex.apps.home.fragment.DialogFragmentManger;
import com.androidex.apps.home.fragment.FrontBankcardFragment;
import com.androidex.apps.home.fragment.NetWorkSettingFragment;
import com.androidex.apps.home.fragment.OtherCardFragment;
import com.androidex.apps.home.fragment.RecordVoiceFragment;
import com.androidex.apps.home.fragment.SetPassWordFragment;
import com.androidex.apps.home.fragment.SetUUIDFragment;
import com.androidex.apps.home.fragment.StartSettingFragment;
import com.androidex.apps.home.fragment.SystemSettingFragment;
import com.androidex.apps.home.fragment.VedioFragment;
import com.androidex.apps.home.utils.MacUtil;
import com.androidex.apps.home.utils.MyAnimation;
import com.androidex.apps.home.utils.NetWork;
import com.androidex.apps.home.utils.RebutSystem;
import com.androidex.apps.home.view.CircleTextProgressbar;
import com.androidex.common.AndroidExActivityBase;
import com.androidex.common.DummyContent;
import com.androidex.common.LogFragment;
import com.androidex.devices.aexddAndroidNfcReader;
import com.androidex.devices.aexddB58Printer;
import com.androidex.devices.aexddCRT310Reader;
import com.androidex.devices.aexddLCC1Reader;
import com.androidex.devices.aexddMT319Reader;
import com.androidex.devices.aexddNfcReader;
import com.androidex.devices.aexddX3Biovo;
import com.androidex.devices.aexddZTC70;
import com.androidex.devices.appDeviceDriver;
import com.androidex.devices.appDevicesManager;
import com.androidex.logger.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class FullscreenActivity extends AndroidExActivityBase implements NfcAdapter.ReaderCallback, View.OnClickListener, aexLogFragment.CallBackValue {
    public static final int DLG_NETINFO = 1004;
    public static final String action_back = "com.androidex.back";
    public static final String action_next = "com.androidex.next";
    public static final String action_finish = "com.androidex.finish";
    public static final String action_cancle = "com.androidex.cancle";
    public static final String action_Viewpager_gone = "com.androidex.action.viewpager.gone";
    public static final String action_start_text = "com.androidex.action.start.text";
    public static final String action_start_wifi_text = "com.androidex.action.start.wifi.text";
    public static final String action_start_print_text = "com.androidex.action.start.print.text";
    public static final String action_start_network_text = "com.androidex.action.start.network.text";
    public static String aexp_lan_mac = "/sys/class/androidex_parameters/androidex/lan_mac";
    public static String aexp_bt_mac = "/sys/class/androidex_parameters/androidex/bt_mac";
    public static String aexp_wlan_mac = "/sys/class/androidex_parameters/androidex/wlan_mac";
    private static Fragment mMainFragment = new MainFragment();
    private static Fragment mAboutFragment = new AboutFragment();
    private static aexLogFragment mLogFragment = new aexLogFragment();
    private static Fragment mAdvertFragment = new AdvertFragment();
    private static Fragment mFrontBankcardFragment = new FrontBankcardFragment();
    private static Fragment mAfterBankcardFragment = new AfterBankcardFragment();
    private static Fragment mOtherCardFragment = new OtherCardFragment();
    private static Fragment mAboutLocalFragment = new AboutLocalFragment();
    private static Fragment mSystemSettingFragment = new SystemSettingFragment();
    private static Fragment mNetWorkSettingFragment = new NetWorkSettingFragment();
    private static Fragment mStartSettingFragment = new StartSettingFragment();
    private SectionsPagerAdapter mSectionsPagerAdapter;
    public ViewPager mContentView;         //整体大布局的Viewpager
    private ViewPager viewPager;            //有关系统设置的Viewpager
    private View mControlsView;             //当前activity的布局
    private CircleTextProgressbar progressbar;
    private appDevicesManager mDevices;
    public static WebJavaBridge.OnJavaBridgeListener mJbListener;
    private NextBrodcastResive nbr;
    public static String cardInfo;          //读取卡信息
    private static List<Fragment> fragments;
    private List<Fragment> list;
    boolean isInitConfig = false;//控制是否进行初始化配置
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(1000);
            }
            return false;
        }
    };

    public CircleTextProgressbar.OnCountdownProgressListener progressListener = new CircleTextProgressbar.OnCountdownProgressListener() {
        @Override
        public void onProgress(int what, int progress) {
            if (what == 2) {
                progressbar.setText(progress + "s");
            }
            if (progress == 1) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
                viewPager.setVisibility(View.GONE);
            }
        }
    };

    public String runShellCommand(String cmd) {
        String ret = "";
        byte[] retBytes = new byte[2048];
        Log.d(TAG, String.format("runShellCommand(%s)", cmd));
        try {
            cmd += "\n";
            Process exeEcho1 = Runtime.getRuntime().exec("su");
            OutputStream ot = exeEcho1.getOutputStream();
            ot.write(cmd.getBytes());
            ot.flush();
            ot.close();
            InputStream in = exeEcho1.getInputStream();
            int r = in.read(retBytes);
            if (r > 0)
                ret = new String(retBytes, 0, r);
        } catch (IOException e) {
            Log.e("AexService", "shell cmd wrong:" + e.toString());
        }
        return ret;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aexhome_main);
        hwservice.EnterFullScreen();
        android.util.Log.d(TAG, "onCreate: " + hwservice.getSdkVersion());
        mControlsView = findViewById(R.id.dummy_button);
        initView();
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mContentView.setAdapter(mSectionsPagerAdapter);
        initProgressBar();
        initTablayoutAndViewPager();
        initBroadCast();
        //RebutSystem.reBut(this);  //60分钟重启动，用于老化测试
       /* //if (hwservice.get_uuid().equals("FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF")) {
            if (!isInitConfig) {
                initConfig();
                isInitConfig = true;
            }
            if (isInitConfig) {
                Toast.makeText(this, "请设置UUID", Toast.LENGTH_LONG).show();
                SetUUIDFragment.instance().show(getSupportFragmentManager(), "uuidfragment");
            }
        //}*/
    }

    public void initView() {
        mDevices = new appDevicesManager(this);
        initActionBar(R.id.toolbar);
        mContentView = (ViewPager) findViewById(R.id.fullscreen_content);
        LinearLayout system_set = (LinearLayout) findViewById(R.id.system_set);
        LinearLayout about_local = (LinearLayout) findViewById(R.id.about_local);
        LinearLayout intnet_set = (LinearLayout) findViewById(R.id.intnet_set);
        LinearLayout start_set = (LinearLayout) findViewById(R.id.start_set);
        setFullScreen(true);
        setFullScreenView(mContentView);
        getWindow().getDecorView().setBackgroundResource(R.drawable.default_wallpaper);
        mContentView.setBackgroundResource(R.drawable.default_wallpaper);
        mContentView.setPageTransformer(true, MyAnimation.Instance().new MyPageTransformer());//给ViewPager添加动画
        mControlsView.setOnTouchListener(mDelayHideTouchListener);
        system_set.setOnClickListener(this);
        about_local.setOnClickListener(this);
        intnet_set.setOnClickListener(this);
        start_set.setOnClickListener(this);
    }

    public void initConfig() {
        Log.d(TAG, hwservice.getSdkVersion());

        /**
         *针对22寸机配置
         */
        Log.d(TAG, runShellCommand(String.format("echo \"0x34\" > %s", hwService.aexp_flag0)));
        Log.d(TAG, runShellCommand(String.format("echo \"0x0C\" > %s", hwService.aexp_flag1)));
        Log.d(TAG, runShellCommand(String.format("echo \"\" > %s", hwservice.AEX_PARAMETERS_BDUART)));
        //hwservice.writeHex(aexp_lan_mac, MacUtil.getNETMacAddress());//此处调用会报错导致机器重启
        hwservice.writeHex(aexp_bt_mac, MacUtil.getBTMacAddress());
        hwservice.writeHex(aexp_wlan_mac, MacUtil.getWIFIMacAddress(this));

        Log.d(TAG, String.format("flag0:", hwservice.get_flag0() + ""));
        Log.d(TAG, String.format("flag1:", hwservice.getAndroidExParameter(hwService.aexp_flag1)));
        Log.d(TAG, String.format("lan_mac:", hwservice.getAndroidExParameter(aexp_lan_mac)));
        Log.d(TAG, String.format("bt_mac:", hwservice.getAndroidExParameter(aexp_bt_mac)));
        Log.d(TAG, String.format("wlan_mac:", hwservice.getAndroidExParameter(aexp_wlan_mac)));
    }

    public void initProgressBar() {
        progressbar = (CircleTextProgressbar) findViewById(R.id.progressbar);
        progressbar.setCountdownProgressListener(2, progressListener);
    }

    public void initBroadCast() {
        nbr = new NextBrodcastResive();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(action_next);
        intentFilter.addAction(action_finish);
        intentFilter.addAction(action_back);
        intentFilter.addAction(action_cancle);
        intentFilter.addAction(action_Viewpager_gone);
        intentFilter.addAction(action_start_text);//启动自动测试程序
        intentFilter.addAction(action_start_wifi_text);//启动wifi测试页面
        intentFilter.addAction(action_start_network_text);//启动以太网测试页面
        intentFilter.addAction(action_start_print_text);//启动打印机测试
        intentFilter.addAction(aexddAndroidNfcReader.START_ACTION);
        registerReceiver(nbr, intentFilter);
    }

    private void initTablayoutAndViewPager() {
        fragments = new ArrayList<>();
        fragments.add(mAboutLocalFragment);
        fragments.add(mSystemSettingFragment);
        fragments.add(mNetWorkSettingFragment);
        fragments.add(mStartSettingFragment);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
    }

    /**
     * 显示Dialog
     * setListFragment  此方法为必调方法
     * setWidthPerHeight设置宽高比（暂时无效，因此设置默认0.75f）
     * setPadding       距离屏幕左右宽度
     * setIsCancelable  外部是否可点
     *
     * @param listFragment
     */
    public void showDialog(List<Fragment> listFragment, boolean flag) {
        if (listFragment.size() < 0 || listFragment == null) return;
        DialogFragmentManger.instance()
                .setListFragment(listFragment)
                .setWidthPerHeight(0.75f)
                .setPadding(100)
                .setIsScrollViewPager(flag)
                .setIsCancelable(false)
                .show(getSupportFragmentManager(), "dialog");
    }

    public void dismissDialog() {
        DialogFragmentManger.instance().dissMissDialog();
    }

    /**
     * 刷卡或者插卡的fragment的list
     *
     * @return
     */
    private List<Fragment> getCarFragments() {
        list = new ArrayList();
        list.add(mFrontBankcardFragment);
        list.add(mAfterBankcardFragment);
        list.add(mOtherCardFragment);
        return list;
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableReaderMode();//启动NFC
        hwservice.ExitFullScreen();
        EnableFullScreen();
    }

    @Override
    protected void onPause() {
        super.onPause();
        disableReaderMode();
        hwservice.ExitFullScreen();
        DisableFullScreen();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showExitDialog();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hwservice.ExitFullScreen();
        DisableFullScreen();
        mDevices.mPrinter.Close();
        unregisterReceiver(nbr);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // are available.
        //在这里设置开启自动隐藏toolbar
        delayedHide(AUTO_HIDE_DELAY_MILLIS);
    }

    @Override
    public void initActionBar(int resId) {
        Toolbar toolbar = (Toolbar) findViewById(resId);
        super.initActionBar(resId);
        if (toolbar != null) {
            toolbar.setLogo(com.androidex.aexapplibs.R.drawable.androidex);      //设置logo图片
            toolbar.setNavigationIcon(com.androidex.aexapplibs.R.drawable.back);     //设置导航按钮
            toolbar.setTitle(R.string.app_name);          //设置标题
            toolbar.setSubtitle(R.string.app_subtitle);   //设置子标题
            toolbar.setTitleTextColor(Color.WHITE);
            toolbar.setSubtitleTextColor(Color.WHITE);
            setSupportActionBar(toolbar);
            toolbar.setOnClickListener(this);
        }
    }

    @Override
    public void ShowControlBar() {
        mControlsView.setVisibility(View.VISIBLE);
        super.ShowControlBar();
    }

    @Override
    public void HideControlBar() {
        mControlsView.setVisibility(View.GONE);
        super.HideControlBar();
    }

    /**
     * Create a chain of targets that will receive log data
     */
    @Override
    public void initializeLogging() {
        super.initializeLogging();
        mLogFragment.initializeLogging();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem m4 = menu.add(R.string.str_quit);
        m4.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                showExitDialog();
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mContentView.setCurrentItem(2);
        switch (item.getItemId()) {
            case R.id.action_settings:
                system_set();//系统设置
                return true;

            case R.id.action_setuuid:
                Toast.makeText(this, "请设置UUID", Toast.LENGTH_LONG).show();
                SetUUIDFragment.instance().isStartText(false).show(getSupportFragmentManager(), "uuidfragment");
                return true;
            case R.id.action_print:
                printText();//打印机测试
                return true;

            case R.id.action_reader:
                readerText(1);//读卡器测试
                return true;

            case R.id.action_cas_reader:
                //casReaderText(2);//燃气读卡器测试
                Crt310Text();
                return true;

            case R.id.action_password_key:
                ztPasswordKeypadText();//密码键盘测试
                return true;

            case R.id.action_x3biovo:
                x3BiovoText();//指纹仪测试

                return true;

            case R.id.action_camera://相机测试
                CameraFragment.instance().show(getSupportFragmentManager(), "camerafragment");
                return true;

            case R.id.action_video://视频播放测试程序
                VedioFragment.Instance().show(getSupportFragmentManager(), "vediofragment");
                return true;

            case R.id.action_onekey_text:
                startText();//启动自动测试
                return true;

            case R.id.action_record_voice://录音机测试
                RecordVoiceFragment.instance().show(getSupportFragmentManager(), "recordvoicefragment");
                return true;

            case R.id.action_stop_reboot://停止老化测试
                AlertDialog.Builder builder = new AlertDialog.Builder(FullscreenActivity.this);
                builder.setCancelable(false);
                builder.setMessage("请选择是否进行5分钟开关机老化测试")
                        .setPositiveButton("开始老化", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                RebutSystem.startRebut();
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("停止老化", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                RebutSystem.stopReBut();
                                dialog.dismiss();
                            }
                        }).show();
                return true;
            case R.id.action_exit_fullscreen:
                sendBroadcast(new Intent("com.android.action.display_navigationbar"));
                //hwservice.ExitFullScreen();
                return true;

            case R.id.action_start_otherapk:
                android.util.Log.e(TAG, "BroadcastReceive：UpdateService");
                hwservice.execRootCommand("pm -r install /sdcard/DCIM/TextAirkiss-sign.apk");
                hwservice.execRootCommand("pm -r install /sdcard/DCIM/ComAssistant.apk");
                android.util.Log.e(TAG, "BroadcastReceive：UpdateService");

                //启动锁相门禁
                Intent intent1 = new Intent();
                intent1.setComponent(new ComponentName("com.tencent.devicedemo",
                        "com.tencent.devicedemo.InitActivity"));
                startActivity(intent1);

                android.util.Log.e(TAG, "BroadcastReceive：UpdateService");
                return true;

            case R.id.action_unintall:
//                String  s= hwservice.execRootCommand("mount -o rw,remount /misc");
//                Log.d(TAG,"删除开机图片mount:"+s);
//                String s1 = hwservice.execRootCommand("rm -rf /misc/boot_logo.bmp.gz");
//                Log.d(TAG,"删除开机图片rm gz:"+s1);
//                String s2 = hwservice.execRootCommand("rm -rf /misc/boot_logo.bmp");
//                Log.d(TAG,"删除开机图片rm bmp:"+s2);
//                String s3 = hwservice.execRootCommand("sync");
//                Log.d(TAG,"删除开机图片sync:"+s3);
//                Log.d(TAG,"删除开机图片目录文件ls:"+hwservice.execRootCommand("ls /misc/boot_logo.bmp.gz"));
//                Log.d(TAG,"删除开机图片目录文件ls:"+hwservice.execRootCommand("ls /misc/boot_logo.bmp"));
//                //hwservice.execRootCommand("reboot");

//                Log.d(TAG,"runShellCommand:mount:"+runShellCommand("mount -o rw,remount /misc"));
//                Log.d(TAG,"runShellCommand:rm:"+runShellCommand("rm -rf /misc/boot_logo.bmp.gz;rm -rf /misc/boot_logo.bmp"));
//                Log.d(TAG,"runShellCommand:sync:"+runShellCommand("sync"));
//                Log.d(TAG,"runShellCommand:ls:"+runShellCommand("ls /misc/boot_logo.bmp.gz"));
//                Log.d(TAG,"runShellCommand:ls:"+runShellCommand("ls /misc/boot_logo.bmp"));
//                runShellCommand("reboot");

                Uri packageUri = Uri.parse("package:" + FullscreenActivity.this.getPackageName());
                Intent intent = new Intent(Intent.ACTION_DELETE, packageUri);
                startActivity(intent);
                return true;

            case R.id.action_startEthernet://打开以太网

                //lcc1ReaderText();
                hwservice.EthernetStart();
                return true;

            case R.id.action_closeEthernet://关闭以太网
                hwservice.EthernetStop();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * 启动自动测试
     */
    private void startText() {
        readerText(1);//银行卡读卡器测试mac
        casReaderText(2);//燃气读卡器测试
        ztPasswordKeypadText();//密码键盘测试
        CameraFragment.instance().show(getSupportFragmentManager(), "camerafragment");//相机测试
        // showDialog(getVedioFragments(), true);//视频播放测试程序
        // NetWork.wifiManger(this);
        //netWorkText();  //以太网测试
        //NetWork.netWorkManger(this);
        //VedioFragment.Instance().show(getSupportFragmentManager(),"recordvoicefragment");
        //printText();//打印机测试

    }

    public void netWorkText() {
        boolean isCon = NetWork.isConnect(this);
        if (isCon) {
            Log.d(TAG + "FullscreenActivity", "以太网测试成功");
            RecordVoiceFragment.instance().show(getSupportFragmentManager(), "recordvoicefragment");
        } else {
            //弹出对话框
            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(FullscreenActivity.this);
            builder.setCancelable(false);
            builder.setMessage("请确认关闭无线网并插入网线")
                    .setPositiveButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (NetWork.isConnect(FullscreenActivity.this)) {
                                Log.d(TAG + "FullscreenActivity", "以太网测试成功");
                            } else {
                                Log.e(TAG + "FullscreenActivity", "以太网测试失败");
                            }
                            RecordVoiceFragment.instance().show(getSupportFragmentManager(), "recordvoicefragment");
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("否", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            netWorkText();
                        }
                    }).show();
        }
    }

    /**
     * 读卡器测试程序
     */
    private void readerText(int i) {
        if (mDevices.mBankCardReader.Open()) {

            aexddMT319Reader mBankCardReader = (aexddMT319Reader) mDevices.mBankCardReader;
            mBankCardReader.selfTest(i);
            mBankCardReader.Close();
        } else {
            String s = String.format("Open bank reader fial:%s", mDevices.mBankCardReader.mParams.optString(appDeviceDriver.PORT_ADDRESS));
            Log.i(TAG, s);
            Toast.makeText(this, s, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * CRT310电动读卡器
     */
    private void Crt310Text() {
        if (mDevices.mCRT310CardReader.Open()) {
            //lcc1ReaderText();//莱卡读卡器测试程序
            aexddCRT310Reader mCRT310Reader = (aexddCRT310Reader) mDevices.mCRT310CardReader;
            mCRT310Reader.Close();
        } else {
            String s = String.format("Open cas reader fial:%s", mDevices.mCRT310CardReader.mParams.optString(appDeviceDriver.PORT_ADDRESS));
            Log.i(TAG, s);
            Toast.makeText(this, s, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 燃气读卡器测试
     */
    private void casReaderText(int i) {
        if (mDevices.mCasCardReader.Open()) {
            //lcc1ReaderText();//莱卡读卡器测试程序
            aexddMT319Reader mCasCardReader = (aexddMT319Reader) mDevices.mCasCardReader;
            mCasCardReader.selfTest(i);
            mCasCardReader.Close();
        } else {
            String s = String.format("Open cas reader fial:%s", mDevices.mCasCardReader.mParams.optString(appDeviceDriver.PORT_ADDRESS));
            Log.i(TAG, s);
            Toast.makeText(this, s, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 系统设置
     */
    private void system_set() {
        ViewGroup v = (ViewGroup) mContentView.getChildAt(mContentView.getCurrentItem());
        Intent mIntent = new Intent();
        mIntent.setAction(Intent.ACTION_VIEW);
        mIntent.setClassName("com.android.settings", "com.android.settings.Settings");
        mIntent.putExtra("back", true);
        sendBroadcast(new Intent("com.android.action.display_navigationbar"));
        startActivityForResult(mIntent, DLG_NETINFO);
    }

    /**
     * 政通密码键盘测试程序
     */
    private void ztPasswordKeypadText() {
        if (mDevices.mZTPasswordKeypad.Open()) {
            aexddZTC70 passworkkeypad = (aexddZTC70) mDevices.mZTPasswordKeypad;
            passworkkeypad.selfTest();
            //打开dialogfragment测试
            //PasswordPadFragment.instance(passworkkeypad).show(getSupportFragmentManager(),"passwordpadfragment");
            mDevices.mZTPasswordKeypad.Close();
        } else {
            String s = String.format("Open passkeypad reader fial:%s", mDevices.mPasswordKeypad.mParams.optString(appDeviceDriver.PORT_ADDRESS));
            Log.i(TAG, s);
            Toast.makeText(this, s, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 指纹仪测试
     */
    private void x3BiovoText() {

        if (mDevices.mX3Biovo.Open()) {
            aexddX3Biovo mX3Biovo = (aexddX3Biovo) mDevices.mX3Biovo;
            mX3Biovo.selfTest();
            mDevices.mX3Biovo.Close();
        } else {
            String s = String.format("Open passkeypad reader fial:%s", mDevices.mX3Biovo.mParams.optString(appDeviceDriver.PORT_ADDRESS));
            Log.i(TAG, s);
            Toast.makeText(this, s, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 莱卡读卡器测试程序
     */
    private void lcc1ReaderText() {
        final aexddLCC1Reader reader = (aexddLCC1Reader) mDevices.mCasCardReader;

        // int b = reader.WriteDataHex("AAB40007800000000000148D");//鸣响
        // int a =reader.WriteDataHex("AAB70007800000018500041A");//打开蜂鸣指令
        int cc = reader.WriteDataHex("AA20800520000000002F");//上电
        byte[] bytes = reader.ReciveData(120, 1000 * 1000 * 1000);

        printHexString(bytes);
        reader.selfTest();
    }

    /**
     * 打印机测试程序
     */
    public void printText() {
        if (mDevices.mPrinter.Open()) {
            //发送广播
            Intent intent = new Intent();
            intent.setAction(aexLogFragment.PRINT_ACTION);
            sendBroadcast(intent);
        } else {
            String s = String.format("Open printer fial:%s", mDevices.mPrinter.mParams.optString(appDeviceDriver.PORT_ADDRESS));
            Log.i(TAG, s);
            Toast.makeText(this, s, Toast.LENGTH_LONG).show();
        }
    }

    static {
        DummyContent.addItem(new DummyContent.DummyItem("log", "日志", "", LogFragment.class, "url=log", true, 0));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1001://系统wifi返回键
                AlertDialog.Builder builder = new AlertDialog.Builder(FullscreenActivity.this);
                builder.setCancelable(false);
                builder.setMessage("wifi网络是否正常")
                        .setPositiveButton("正常", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d("wifi网络", "wifi网络OK");
                                NetWork.netWorkManger(FullscreenActivity.this);
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("NG", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d("wifi网络", "wifi网络失败");
                                NetWork.netWorkManger(FullscreenActivity.this);
                                dialog.dismiss();
                            }
                        }).show();
                break;
            case 1002://以太网返回
                netWorkText();
                break;
        }
    }

    /**
     * 根据Fragment的字符串标识来启动显示。
     *
     * @param id
     */
    public void showFragment(String id) {
        DummyContent.DummyItem aItem = DummyContent.findItemByTag(id);
        if (aItem != null) {
            showFragment(mMainFragment.getView().getId(), aItem);
            mJbListener = (WebJavaBridge.OnJavaBridgeListener) aItem.getView();
            DummyContent.setActive(aItem);
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbar:
                SetPassWordFragment.instance().show(getSupportFragmentManager(), "passwordfragment");
                break;
            case R.id.progressbar:
                progressbar.setTimeMillis(30 * 1000);
                progressbar.reStart();
                break;
            case R.id.about_local:
                viewPager.setVisibility(View.VISIBLE);
                viewPager.setCurrentItem(0);
                break;
            case R.id.system_set:
                viewPager.setVisibility(View.VISIBLE);
                viewPager.setCurrentItem(1);
                break;
            case R.id.intnet_set:
                viewPager.setVisibility(View.VISIBLE);
                viewPager.setCurrentItem(2);
                break;

            case R.id.start_set:
                viewPager.setVisibility(View.VISIBLE);
                viewPager.setCurrentItem(3);
                break;

        }
    }

    //aexLogFragment回调
    @Override
    public void sendMessageValue(final String value, final int totalLength, final int length) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                aexddB58Printer printer = (aexddB58Printer) (mDevices.mPrinter);
                android.util.Log.d("fullscreenactivity", value);
                if (value != null) {
                    printer.selfTest(value, totalLength, length);
                } else {
                    printer.selfTest();
                }
                if ((length == totalLength)) {
                    mDevices.mPrinter.cutPaper(1);
                    mDevices.mPrinter.Close();
                    Log.i(TAG, "打印测试结束，关闭打印机设备。");
                }
            }
        }).start();
    }

    static class PagerAdapter extends FragmentPagerAdapter {

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments == null ? 0 : fragments.size();
        }
    }

    /**
     * 实现viewpager切换Fragment的广播
     */
    private class NextBrodcastResive extends BroadcastReceiver {

        private int page;

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case action_next:
                    page = intent.getIntExtra("page", 0);
                    DialogFragmentManger.instance().viewPager.setCurrentItem(page);
                    break;
                case action_back:
                    page = intent.getIntExtra("page", 0);
                    DialogFragmentManger.instance().viewPager.setCurrentItem(page);
                    break;
                case action_cancle:
                    dismissDialog();
                    break;
                case action_finish:
                    dismissDialog();
                    delayedHide(AUTO_HIDE_DELAY_MILLIS);
                    break;

                case aexddAndroidNfcReader.START_ACTION://接收读取卡的信息
                    cardInfo = intent.getStringExtra("cardinfo");
                    dismissDialog();
                    showDialog(getCarFragments(), false);
                    break;

                case action_Viewpager_gone:
                    viewPager.setVisibility(View.GONE);
                    break;

                case action_start_text://启动测试程序
                    AlertDialog.Builder builder = new AlertDialog.Builder(FullscreenActivity.this);
                    builder.setCancelable(false);
                    builder.setMessage("是否启动测试程序")
                            .setPositiveButton("是", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.d(TAG, "启动测试程序");
                                    dialog.dismiss();
                                    mContentView.setCurrentItem(2);
                                    startText();
                                }
                            })
                            .setNegativeButton("否", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).show();
                    break;

                case action_start_wifi_text://启动WIFI测试
                    NetWork.wifiManger(FullscreenActivity.this);
                    break;

                case action_start_network_text://启动以太网测试
                    NetWork.netWorkManger(FullscreenActivity.this);
                    break;

                case action_start_print_text://启动打印机测试
                    Toast.makeText(FullscreenActivity.this, "请在NFC处刷卡", Toast.LENGTH_LONG).show();
                    printText();
                    break;
            }
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("Fullscreen", "mContentView click");
                    Intent intent = new Intent(FullscreenActivity.ActionControlBar);
                    intent.putExtra("flag", "toggle");
                    intent.putExtra("bar", true);
                    rootView.getContext().sendBroadcast(intent);
                }
            });
            switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
                case 1:
                    rootView.setBackgroundResource(R.drawable.default_wallpaper);
                    break;
                case 2:
                    rootView.setBackgroundResource(R.drawable.wallpaper01);
                    break;
                case 3:
                default:
                    rootView.setBackgroundResource(R.drawable.wallpaper02);
                    break;
            }
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     * 主页面的ViewPagers的页面生成Adapter，主页面的数量和内容在这里产生。
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public static final int PAGER_COUNT = 3;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * 获得页面数量
         *
         * @return 返回实际的页面数量
         */
        @Override
        public int getCount() {
            // Show 3 total pages.
            return PAGER_COUNT;
        }

        /**
         * 获得指定序号的页面Fragment对象
         *
         * @param position
         * @return
         */
        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0: {
                    return mAdvertFragment;
                }
                case 1: {
                    return mAboutFragment;
                }
                case 2:     //日志Fragment
                {
                    return mLogFragment;
                }
                default:
                    return null;
            }
            //return PlaceholderFragment.newInstance(position + 1);
        }

        /**
         * 获得Pager的标题
         *
         * @param position Pager的序号
         * @return
         */
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Main";    //主页面
                case 1:
                    return "About";     //关于本机页面
                case 2:
                    return "Log";       //运行日志页面
                default:
                    return "Unknown";
            }
        }
    }

    /**
     * 此函数实现NfcAdapter.ReaderCallback接口，这里调用NFC Reader类的接口来实现该函数的功能。
     * 这里只是为了把这个调用映射到此Activity而已。
     *
     * @param tag
     */
    @Override
    public void onTagDiscovered(Tag tag) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            aexddNfcReader lNfcReader = appDevicesManager.getDevicesManager(this).mNfcReader;
            if ((lNfcReader != null) && (lNfcReader instanceof NfcAdapter.ReaderCallback)) {
                NfcAdapter.ReaderCallback nfcReader = (NfcAdapter.ReaderCallback) lNfcReader;
                nfcReader.onTagDiscovered(tag);
            }
        }
    }

    /**
     * 启用NFC读卡
     */
    public void enableReaderMode() {
        //Log.i(TAG, "启用NFC读卡模式");
        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(this);
        if (nfc != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (this instanceof NfcAdapter.ReaderCallback) {
                    nfc.enableReaderMode(this, (aexddAndroidNfcReader) mDevices.mNfcReader, aexddAndroidNfcReader.READER_FLAGS, null);
                }
            }
        }
    }

    /**
     * 禁用NFC读卡
     */
    public void disableReaderMode() {
        //Log.i(TAG, "禁用读卡模式");
        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(this);
        if (nfc != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                nfc.disableReaderMode(this);
            }
        }
    }

    /**
     * 当NFC读卡器读到AID后调用此函数事件通知此Activity。
     *
     * @param account
     */
    public void onAccountReceived(final String account) {
        // This callback is run on a background thread, but updates to UI elements must be performed
        // on the UI thread.
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //mAccountField.setText(account);
                Log.i(TAG, String.format("NFC:%s", account));
            }
        });
    }

    public static void printHexString(byte[] b) {
        if (b != null) {
            StringBuffer string = new StringBuffer();
            for (int i = 0; i < b.length; i++) {
                String hex = Integer.toHexString(b[i] & 0xFF);
                if (hex.length() == 1) {
                    hex = '0' + hex;
                }
                string.append(hex.toUpperCase() + " ");
                android.util.Log.d("111111", hex.toUpperCase() + " ");
            }
            android.util.Log.d("111111", string + " ");
            System.out.println("");
        } else {
            android.util.Log.d("111111", "w为空");
        }
    }

}


