package com.androidex.apps.home;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.androidex.aexlibs.WebJavaBridge;
import com.androidex.apps.home.activity.SystemMainActivity;
import com.androidex.apps.home.utils.MyAnimation;
import com.androidex.apps.home.view.CircleTextProgressbar;
import com.androidex.common.AndroidExActivityBase;
import com.androidex.common.DummyContent;
import com.androidex.common.LogFragment;
import com.androidex.common.OnMultClickListener;
import com.androidex.devices.aexddAndroidNfcReader;
import com.androidex.devices.aexddB58Printer;
import com.androidex.devices.aexddNfcReader;
import com.androidex.devices.appDeviceDriver;
import com.androidex.devices.appDevicesManager;
import com.androidex.logger.Log;

import java.io.UnsupportedEncodingException;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class FullscreenActivity extends AndroidExActivityBase implements OnMultClickListener, NfcAdapter.ReaderCallback {

      public static final String LOG = "Log";
      /**
       * The {@link PagerAdapter} that will provide
       * fragments for each of the sections. We use a
       * {@link FragmentPagerAdapter} derivative, which will keep every
       * loaded fragment in memory. If this becomes too memory intensive, it
       * may be best to switch to a
       * {@link FragmentStatePagerAdapter}.
       */
      private SectionsPagerAdapter mSectionsPagerAdapter;
      /**
       * The {@link ViewPager} that will host the section contents.
       */
      private ViewPager mContentView;
      private View mControlsView;
      private int recyle = 20;

      public static WebJavaBridge.OnJavaBridgeListener mJbListener;
      private static MainFragment mMainFragment = new MainFragment();
      private static AboutFragment mAboutFragment = new AboutFragment();
      private static aexLogFragment mLogFragment = new aexLogFragment();
      private static AdvertFragment mAdvertFragment = new AdvertFragment();
      private CircleTextProgressbar progressbar;
      private appDevicesManager mDevices;

      /**
       * Touch listener to use for in-layout UI controls to delay hiding the
       * system UI. This is to prevent the jarring behavior of controls going away
       * while interacting with activity UI.
       */
      private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                  if (AUTO_HIDE) {
                        delayedHide(1000);
                  }
                  return false;
            }
      };

      private CircleTextProgressbar.OnCountdownProgressListener progressListener = new CircleTextProgressbar.OnCountdownProgressListener() {
            @Override
            public void onProgress(int what, int progress) {
                  if (what == 1) {
                        progressbar.setText(progress + "s");
                  } else if (what == 2) {
                        progressbar.setText(progress + "s");
                  }
                  // 比如在首页，这里可以判断进度，进度到了100或者0的时候，你可以做跳过操作。
            }
      };


      @Override
      protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.aexhome_main);
            hwservice.EnterFullScreen();
            getWindow().getDecorView().setBackgroundResource(R.drawable.default_wallpaper);
            initView();
            // Create the adapter that will return a fragment for each of the three
            // primary sections of the activity.
            mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
            // Set up the ViewPager with the sections adapter.
            mContentView.setAdapter(mSectionsPagerAdapter);
            registerMultClickListener(mContentView, this);

            setFullScreenView(mContentView);
            setFullScreen(true);
            //timeCount(progressbar);//实现倒计时功能 并在textview上显示
            delayedHide(1000);
      }

      public void initView() {
            initActionBar(R.id.toolbar);
            mContentView = (ViewPager) findViewById(R.id.fullscreen_content);
            progressbar = (CircleTextProgressbar) findViewById(R.id.progressbar);
            mControlsView = findViewById(R.id.dummy_button);
            mControlsView.setOnTouchListener(mDelayHideTouchListener);
            mContentView.setBackgroundResource(R.drawable.default_wallpaper);
            //给ViewPager添加动画
            mContentView.setPageTransformer(true, MyAnimation.Instance().new MyPageTransformer());
            progressbar.setCountdownProgressListener(2, progressListener);
            mDevices = new appDevicesManager(this);
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

//      /**
//       * 倒计时
//       *
//       * @param time_count
//       */
//      private void timeCount(CountDownView time_count) {
//            Message message = handler.obtainMessage(1);
//            handler.sendMessageDelayed(message, 1000);
//      }

      @Override
      protected void onDestroy() {
            super.onDestroy();
            hwservice.ExitFullScreen();
            DisableFullScreen();
            mDevices.mPrinter.Close();
      }

      @Override
      protected void onPostCreate(Bundle savedInstanceState) {
            super.onPostCreate(savedInstanceState);
            // Trigger the initial hide() shortly after the activity has been
            // created, to briefly hint to the user that UI controls
            // are available.
            //在这里设置隐藏
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
            }
            toolbar.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                        android.util.Log.e("============", "运行了");
                        Intent intent = new Intent(FullscreenActivity.this, SystemMainActivity.class);
                        startActivity(intent);
                  }
            });
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
            // Inflate the menu; this adds items to the action bar if it is present.
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

      public static final int DLG_NETINFO = 1004;

      @Override
      public boolean onOptionsItemSelected(MenuItem item) {
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
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
                              try {
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
                              } catch (Exception e) {
                                    Log.i(TAG, e.getLocalizedMessage());
                                    e.printStackTrace();
                              }
                              Log.i(TAG, "打印测试结束，关闭打印机设备。");
                        } else {
                              String s = String.format("Open printer fial:%s", mDevices.mPrinter.mParams.optString(appDeviceDriver.PORT_ADDRESS));
                              Log.i(TAG, s);
                              Toast.makeText(this, s, Toast.LENGTH_LONG).show();
                        }

                        return true;
                  default:
                        return super.onOptionsItemSelected(item);
            }
      }

      @Override
      public void onStart() {
            super.onStart();
      }

      @Override
      public void onStop() {
            super.onStop();
      }

      @Override
      public boolean OnMultClick(View v, int times) {
            //mContentView表示在某个布局上点击才有效
            //if (times == 4 && v.equals(mContentView)) {     //连续4次点击执行事件
            //ToggleControlBar();
            Snackbar.make(v, "FAB", Snackbar.LENGTH_LONG).setAction("cancel", new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                        //这里的单击事件代表点击消除Action后的响应事件

                  }
            }).show();
            if (times == 1) {
                  Intent intent = new Intent(FullscreenActivity.ActionControlBar);
                  intent.putExtra("flag", "toggle");
                  intent.putExtra("bar", true);
                  mContentView.getContext().sendBroadcast(intent);
                  return true;
            }
            return false;
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
