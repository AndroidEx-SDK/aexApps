package com.androidex.apps.home;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
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
import com.androidex.apps.home.fragment.AboutLocalFragment;
import com.androidex.apps.home.fragment.AfterBankcardFragment;
import com.androidex.apps.home.fragment.DialogFragmentManger;
import com.androidex.apps.home.fragment.FrontBankcardFragment;
import com.androidex.apps.home.fragment.NetWorkSettingFragment;
import com.androidex.apps.home.fragment.OtherCardFragment;
import com.androidex.apps.home.fragment.SetPassWordFragment;
import com.androidex.apps.home.fragment.SetUUIDFragment;
import com.androidex.apps.home.fragment.StartSettingFragment;
import com.androidex.apps.home.fragment.SystemSettingFragment;
import com.androidex.apps.home.view.CircleTextProgressbar;
import com.androidex.common.AndroidExActivityBase;
import com.androidex.common.DummyContent;
import com.androidex.common.LogFragment;
import com.androidex.devices.aexddAndroidNfcReader;
import com.androidex.devices.aexddB58Printer;
import com.androidex.devices.aexddMT319Reader;
import com.androidex.devices.aexddNfcReader;
import com.androidex.devices.aexddZTC70;
import com.androidex.devices.appDeviceDriver;
import com.androidex.devices.appDevicesManager;
import com.androidex.logger.Log;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class FullscreenActivity extends AndroidExActivityBase implements NfcAdapter.ReaderCallback, View.OnClickListener {
    public static final String LOG = "Log";
    public static final int DLG_NETINFO = 1004;
    public static final String action_back = "com.androidex.back";
    public static final String action_next = "com.androidex.next";
    public static final String action_finish = "com.androidex.finish";
    public static final String action_cancle = "com.androidex.cancle";
    public static final String action_Viewpager_gone = "com.androidex.action.viewpager.gone";
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mContentView;
    private View mControlsView;
    private CircleTextProgressbar progressbar;
    private appDevicesManager mDevices;
    public static WebJavaBridge.OnJavaBridgeListener mJbListener;
    private static Fragment mMainFragment = new MainFragment();
    private static Fragment mAboutFragment = new AboutFragment();
    private static aexLogFragment mLogFragment = new aexLogFragment();
    private static Fragment mAdvertFragment = new AdvertFragment();
    private static Fragment mSetUUIDFragment = new SetUUIDFragment();
    private static Fragment mSetPassWordFragment = new SetPassWordFragment();
    private static Fragment mFrontBankcardFragment = new FrontBankcardFragment();
    private static Fragment mAfterBankcardFragment = new AfterBankcardFragment();
    private static Fragment mOtherCardFragment = new OtherCardFragment();
    private static Fragment mAboutLocalFragment = new AboutLocalFragment();
    private static Fragment mSystemSettingFragment = new SystemSettingFragment();
    private static Fragment mNetWorkSettingFragment = new NetWorkSettingFragment();
    private static Fragment mStartSettingFragment = new StartSettingFragment();
    private NextBrodcastResive nbr;

    public static String cardInfo;//读取卡信息
    private static final Integer[] tabIcs = {R.mipmap.emoji_11, R.mipmap.systemset, R.mipmap.wifiset, R.mipmap.sdartset};

    private static List<Fragment> fragments;
    public TabLayout tabLayout;
    private ViewPager viewPager;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aexhome_main);
        hwservice.EnterFullScreen();
        getWindow().getDecorView().setBackgroundResource(R.drawable.default_wallpaper);
        initView();
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mContentView.setAdapter(mSectionsPagerAdapter);

        setFullScreenView(mContentView);
        setFullScreen(true);

        initProgressBar();
        initTablayoutAndViewPager();
        initBroadCast(); //注册广播

    }

    public void initView() {
        mDevices = new appDevicesManager(this);
        initActionBar(R.id.toolbar);
        mContentView = (ViewPager) findViewById(R.id.fullscreen_content);

        LinearLayout system_set = (LinearLayout) findViewById(R.id.system_set);
        LinearLayout about_local = (LinearLayout) findViewById(R.id.about_local);
        LinearLayout intnet_set = (LinearLayout) findViewById(R.id.intnet_set);
        LinearLayout start_set = (LinearLayout) findViewById(R.id.start_set);
        mControlsView = findViewById(R.id.dummy_button);
        mControlsView.setOnTouchListener(mDelayHideTouchListener);
        mContentView.setBackgroundResource(R.drawable.default_wallpaper);
        // mContentView.setPageTransformer(true, MyAnimation.Instance().new MyPageTransformer());//给ViewPager添加动画
        system_set.setOnClickListener(this);
        about_local.setOnClickListener(this);
        intnet_set.setOnClickListener(this);
        start_set.setOnClickListener(this);

    }

    public void initProgressBar() {
        progressbar = (CircleTextProgressbar) findViewById(R.id.progressbar);
        progressbar.setCountdownProgressListener(2, progressListener);
        //progressbar.setTimeMillis(60 * 1000);

    }

    public void initBroadCast() {
        nbr = new NextBrodcastResive();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(action_next);
        intentFilter.addAction(action_finish);
        intentFilter.addAction(action_back);
        intentFilter.addAction(action_cancle);
        intentFilter.addAction(action_Viewpager_gone);
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
        // viewPager.setOffscreenPageLimit(1);//预加载的页数
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
    public void showDialog(List<Fragment> listFragment) {
        if (listFragment.size() < 0 || listFragment == null) return;
        DialogFragmentManger.Instance()
                .setListFragment(listFragment)
                .setWidthPerHeight(0.75f)
                .setPadding(150)
                .setIsCancelable(false)
                .show(getSupportFragmentManager(), "dialog");
    }

    public void dismissDialog() {
        DialogFragmentManger.Instance().dimissDialog();
    }

    /**
     * 第一次启动是需要设置UUID和密码的fragment的list
     *
     * @return
     */
    private List<Fragment> getFragments() {
        List<Fragment> list = new ArrayList();
        list.add(mSetUUIDFragment);
        list.add(mSetPassWordFragment);
        return list;
    }

    /**
     * 刷卡或者插卡的fragment的list
     *
     * @return
     */
    private List<Fragment> getCarFragments() {
        List<Fragment> list = new ArrayList();
        list.add(mFrontBankcardFragment);
        list.add(mAfterBankcardFragment);
        list.add(mOtherCardFragment);
        return list;
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableReaderMode();
        //if(verify_password == 0)
        //    CheckPassword();
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
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                ViewGroup v = (ViewGroup) mContentView.getChildAt(mContentView.getCurrentItem());
                Intent mIntent = new Intent();
                mIntent.setAction(Intent.ACTION_VIEW);
                mIntent.setClassName("com.android.settings", "com.android.settings.Settings");
                mIntent.putExtra("back", true);
                sendBroadcast(new Intent("com.android.action.display_navigationbar"));
                startActivityForResult(mIntent, DLG_NETINFO);
                return true;

            case R.id.action_print:
                Log.i(TAG, "打印测试程序...");
                if (mDevices.mPrinter.Open()) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            mDevices.mPrinter.selfTest();
                            String str = "安卓工控";
                            try {
                                mDevices.mPrinter.WriteData(str.getBytes("GBK"), str.getBytes().length);
                                aexddB58Printer printer = (aexddB58Printer) (mDevices.mPrinter);
                                printer.ln();
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            mDevices.mPrinter.WriteDataHex("1D564200");
                            mDevices.mPrinter.Close();
                            Log.i(TAG, "打印测试结束，关闭打印机设备。");
                        }
                    }).start();

                } else {
                    String s = String.format("Open printer fial:%s", mDevices.mPrinter.mParams.optString(appDeviceDriver.PORT_ADDRESS));
                    Log.i(TAG, s);
                    Toast.makeText(this, s, Toast.LENGTH_LONG).show();
                }

                return true;
            case R.id.action_reader:
                if (mDevices.mBankCardReader.Open()) {
                    aexddMT319Reader reader = (aexddMT319Reader) mDevices.mBankCardReader;
                    reader.selfTest();
                    mDevices.mBankCardReader.Close();
                } else {
                    String s = String.format("Open bank reader fial:%s", mDevices.mBankCardReader.mParams.optString(appDeviceDriver.PORT_ADDRESS));
                    Log.i(TAG, s);
                    Toast.makeText(this, s, Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.action_cas_reader:
                if (mDevices.mBankCardReader.Open()) {
                    aexddMT319Reader reader = (aexddMT319Reader) mDevices.mCasCardReader;
                    reader.selfTest();
                    mDevices.mCasCardReader.Close();
                } else {
                    String s = String.format("Open cas reader fial:%s", mDevices.mCasCardReader.mParams.optString(appDeviceDriver.PORT_ADDRESS));
                    Log.i(TAG, s);
                    Toast.makeText(this, s, Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.action_password_key:
                if (mDevices.mPasswordKeypad.Open()) {
                    aexddZTC70 passworkkeypad = (aexddZTC70) mDevices.mZTPasswordKeypad;
                    passworkkeypad.selfTest();
                    int i = mDevices.mPasswordKeypad.ReciveDataLoop();
                    Log.i("按键：", i + "");
                    mDevices.mPasswordKeypad.Close();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    static {
        DummyContent.addItem(new DummyContent.DummyItem("log", "日志", "", LogFragment.class, "url=log", true, 0));
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
                showDialog(getFragments());
                break;
            case R.id.progressbar:
                progressbar.setTimeMillis(30 * 1000);
                progressbar.reStart();
                break;
            case R.id.about_local:
                viewPager.setVisibility(View.VISIBLE);
                viewPager.setCurrentItem(0);
                Toast.makeText(this,"关于本机",Toast.LENGTH_SHORT).show();
                break;
            case R.id.system_set:
                viewPager.setVisibility(View.VISIBLE);
                viewPager.setCurrentItem(1);
                Toast.makeText(this,"关于本机",Toast.LENGTH_SHORT).show();
                break;
            case R.id.intnet_set:
                viewPager.setVisibility(View.VISIBLE);
                viewPager.setCurrentItem(2);
                Toast.makeText(this,"关于本机",Toast.LENGTH_SHORT).show();
                break;

            case R.id.start_set:
                viewPager.setVisibility(View.VISIBLE);
                viewPager.setCurrentItem(3);
                Toast.makeText(this,"关于本机",Toast.LENGTH_SHORT).show();
                break;

        }
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
                    DialogFragmentManger.Instance().viewPager.setCurrentItem(page);
                    break;
                case action_back:
                    page = intent.getIntExtra("page", 0);
                    DialogFragmentManger.Instance().viewPager.setCurrentItem(page);
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
                    android.util.Log.e("接收读取卡的信息====", cardInfo + "0000");
                    dismissDialog();
                    showDialog(getCarFragments());
                    break;
                case action_Viewpager_gone:
                    viewPager.setVisibility(View.GONE);
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
        Log.i(TAG, "启用读卡模式");
        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(this);
        if (nfc != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (this instanceof NfcAdapter.ReaderCallback) {
                    nfc.enableReaderMode(this, this, aexddAndroidNfcReader.READER_FLAGS, null);

                }
            }
        }
    }

    /**
     * 禁用NFC读卡
     */
    public void disableReaderMode() {
        Log.i(TAG, "禁用读卡模式");
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
}


