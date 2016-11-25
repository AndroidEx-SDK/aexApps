package com.androidex.apps.home.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.widget.Toolbar;
import android.view.Window;
import android.widget.TextView;

import com.androidex.apps.home.Fragment.FrontBankcard;
import com.androidex.apps.home.Fragment.OtherCard;
import com.androidex.apps.home.R;
import com.androidex.common.AndroidExActivityBase;

import java.util.ArrayList;
import java.util.List;

public class TransactionActivity extends AndroidExActivityBase {

    private static final String TAG = "TransactionActivity";
    public NoScrollViewPager mViewPager;

    public List<Fragment> mFragmentList;

    public TransactionPagerAdapter mAdapter;

    public TextView time_count;

    private NextBrodcastResive nbr;

    private int recyle = 10;
    final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what==1){
                recyle -- ;
                time_count.setText(""+recyle);
                if(recyle>0){
                    Message message = handler.obtainMessage(1);
                    handler.sendMessageDelayed(message,1000);
                }else{
                    finish(); //进入广告界面,暂时用finish代替
                }

            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_transaction);
        //setFullScreen(true);
        //getWindow().getDecorView().setBackgroundResource(R.drawable.defaultallpaper);
        initToolBar(R.id.toolbar);
        initViewPager();//初始化viewpager
        init();
        timeCount(time_count);//实现倒计时功能 并在textview上显示

        nbr = new NextBrodcastResive();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FrontBankcard.str);
        intentFilter.addAction(OtherCard.action);
        intentFilter.addAction(OtherCard.action_back);
        intentFilter.addAction(FrontBankcard.action_fb_back);
        registerReceiver(nbr,intentFilter);
    }

    /**
     * 倒计时
     * @param time_count
     */
    private void timeCount(TextView time_count) {
        Message message = handler.obtainMessage(1);
        handler.sendMessageDelayed(message,1000);
    }

    /**
     * 初始化组件
     */
    private void init() {
        time_count = (TextView) findViewById(R.id.tv_time);
    }

    /**
     * 初始化toolbar
     */
    private void initToolBar(int id) {
        Toolbar toolBar = (Toolbar) findViewById(id);
        if(toolBar!=null){
           // setSupportActionBar(toolBar);
            toolBar.setLogo(R.drawable.ic_launcher);
        }



    }

    /**
     * 配置viewpager
     */
    private void initViewPager() {
        mViewPager = (NoScrollViewPager) findViewById(R.id.vp_fragment);
        mFragmentList = initData();
        mAdapter = new TransactionPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);
    }

    /**
     * 初始化viewpager的数据
     * @return
     */
    private List<Fragment> initData(){
        mFragmentList = new ArrayList<Fragment>();
        mFragmentList.add(new FrontBankcard());
        mFragmentList.add(new OtherCard());
        return mFragmentList;
    }

    /**
     * 适配器
     */
    public class TransactionPagerAdapter extends FragmentPagerAdapter{

        public TransactionPagerAdapter(FragmentManager fm){
            super(fm);
        }
        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                {
                    return new FrontBankcard();
                }
                case 1:
                {
                    return new OtherCard();
                }
                default:return null;
            }
        }


        @Override
        public int getCount() {
            return mFragmentList.size();
        }
    }

    private  class NextBrodcastResive extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(FrontBankcard.str)){
                mViewPager.setCurrentItem(1);
            }else if(intent.getAction().equals(OtherCard.action)){
                finish();
            }else if(intent.getAction().equals(OtherCard.action_back)){
                mViewPager.setCurrentItem(0);
            }else if(intent.getAction().equals(FrontBankcard.action_fb_back)){
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(nbr);
    }
}
